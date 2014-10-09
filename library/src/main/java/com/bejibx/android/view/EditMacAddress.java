package com.bejibx.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Layout;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * EditMacAddress is a small class which extended EditText to provide simpler way to input MAC addresses.
 * Key differences from EditText:
 * 1. There is mask in text field. Looks like this: [ __:__:__:__:__:__ ].
 * 2. Input length is constant and you can input symbols only into allowed positions.
 *    If you do not fill field completely, fillers will be returned on unfilled positions.
 * 3. There is no cursor, instead there is selection with length 1.
 *    Looks like this: [ DE:AD:▌:  :  :   ]
 * 4. You could only input numbers from 0 to 9 and also hex-letters A, B, C, D, E, F in any case.
 *
 * Features:
 * 1. You can specify delimiter character from XML using attribute "delimiter".
 * 2. You can specify filler character from XML using attribute "filler".
 */
public class EditMacAddress extends EditText
{
    private static final boolean DEBUG = false;
    private static final String TAG = "EditMacAddress";
    private static final String DEFAULT_FILLER = " ";
    private static final String DEFAULT_DELIMITER = ":";

    private String mDelimiter;
    private String mFiller;

    private int mCursorPosition = 0;
    private int mTextLength = 0;

    static final HashSet<Character> PERMITTED_SYMBOLS = new HashSet<Character>(Arrays.asList(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
            'a', 'b', 'c', 'd', 'e', 'f'
    ));

    private boolean mBackspacePressed = false;
    private boolean mInputRejected = false;

    public EditMacAddress(Context context)
    {
        super(context);
        initializeView();
    }

    public EditMacAddress(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        obtainAttributes(context, attrs);
        initializeView();
    }

    public EditMacAddress(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        obtainAttributes(context, attrs);
        initializeView();
    }

    private void initializeView()
    {
        setCursorVisible(false);
        setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        setImeOptions(getImeOptions() | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        setText("  :  :  :  :  :  ".replace(" ", mFiller).replace(":", mDelimiter));
        mTextLength = getText().length();
        setFilters(new InputFilter[]{new MacAddressInputValidator()});
    }

    private void obtainAttributes(Context context, AttributeSet attrs)
    {
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EditMacAddress, 0, 0);
        String fillerStr = DEFAULT_FILLER;
        String delimiterStr = DEFAULT_DELIMITER;
        try
        {
            fillerStr = attributes.getString(R.styleable.EditMacAddress_filler);
            delimiterStr = attributes.getString(R.styleable.EditMacAddress_delimiter);
        } finally
        {
            attributes.recycle();
        }

        if (fillerStr == null || fillerStr.isEmpty())
        {
            setFiller(DEFAULT_FILLER.charAt(0));
        }
        else
        {
            setFiller(fillerStr.charAt(0));
        }

        if (delimiterStr == null || delimiterStr.isEmpty())
        {
            setDelimiter(DEFAULT_DELIMITER.charAt(0));
        }
        else
        {
            setDelimiter(delimiterStr.charAt(0));
        }
    }

    private boolean isPositionSelectable(int position)
    {
        int first = getFirstSelectablePosition();
        int last = getLastSelectablePosition();
        return position >= first &&
                position <= last &&
                (position + 1) % 3 != 0;
    }

    private boolean isValidSymbolForPosition(char symbol, int position)
    {
        if (isPositionSelectable(position))
        {
            return PERMITTED_SYMBOLS.contains(symbol);
        }
        else
            return String.valueOf(symbol).equals(mDelimiter);
    }

    private int checkPosition(int pos)
    {
        int first = getFirstSelectablePosition();
        int last = getLastSelectablePosition();
        if (pos > last)
        {
            pos = last;
        }
        if (pos < first)
        {
            pos = first;
        }
        return pos;
    }

    private int getNextSelectablePosition(int pos)
    {
        if (isPositionSelectable(pos + 1))
        {
            pos++;
        }
        else
        {
            pos += 2;
        }
        return checkPosition(pos);
    }

    private int getPreviousSelectablePosition(int pos)
    {
        if (isPositionSelectable(pos - 1))
        {
            pos--;
        }
        else
        {
            pos -= 2;
        }
        return checkPosition(pos);
    }

    private int getFirstSelectablePosition()
    {
        return 0;
    }

    private int getLastSelectablePosition()
    {
        return mTextLength - 1;
    }

    public void selectAtPosition(int position)
    {
        if (DEBUG) Log.v(TAG, String.format("selectAtPosition(position: %d)", position));
        if (getText().length() > 0)
        {
            if (!isPositionSelectable(position))
            {
                position = getNextSelectablePosition(position);
            }
            mCursorPosition = position;
            setSelection(mCursorPosition, mCursorPosition + 1);
        }
    }

    public void moveSelectionUp()
    {
        if (DEBUG) Log.v(TAG, "moveSelectionUp");
        selectAtPosition(getPreviousSelectablePosition(mCursorPosition));
    }

    public void moveSelectionDown()
    {
        if (DEBUG) Log.v(TAG, "moveSelectionDown");
        selectAtPosition(getNextSelectablePosition(mCursorPosition));
    }

    public void setDelimiter(char delimiter)
    {
        if (PERMITTED_SYMBOLS.contains(delimiter)
                || (mFiller.equals(DEFAULT_FILLER) && delimiter == DEFAULT_FILLER.charAt(0)))
        {
            mDelimiter = ":";
        }
        else
        {
            mDelimiter = String.valueOf(delimiter);
        }
    }

    public void setFiller(char filler)
    {
        if (PERMITTED_SYMBOLS.contains(filler))
        {
            mFiller = DEFAULT_FILLER;
        }
        else
        {
            mFiller = String.valueOf(filler);
        }
    }

    public String getUnformattedText()
    {
        return getText().toString().replace("[" + Pattern.quote(mDelimiter + mFiller) + "]", "");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        super.onTouchEvent(event);
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                Layout layout = getLayout();
                float x = event.getX() + getScrollX();
                int offset = layout.getOffsetForHorizontal(0, x);
                selectAtPosition(offset - 1);
                break;
        }
        return true;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd)
    {
        if (DEBUG) Log.v(TAG, String.format("onSelectionChanged(selStart: %d, selEnd: %d)", selStart, selEnd));
        super.onSelectionChanged(selStart, selEnd);
        if (selStart == selEnd)
        {
            selectAtPosition(mCursorPosition);
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int before, int after)
    {
        if (DEBUG) Log.v(TAG, String.format("onTextChanged(text: \"%s\", start: %d, before: %d, after: %d)", text, start, before, after));
        super.onTextChanged(text, start, before, after);
        if (!mInputRejected)
        {
            if (!mBackspacePressed)
            {
                moveSelectionDown();
            }
            else
            {
                mBackspacePressed = false;
                moveSelectionUp();
            }
        }
        else
        {
            mInputRejected = false;
        }
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs)
    {
        return new NoSelectionInputConnection(this, false);
    }

    /**
     * This class is absolutely needed because it seems that some soft input methods incorrectly
     * changing selection. Because of that often we get incorrect dstart/dend values in our input
     * filter. For example we have [11:22:33:44:55:6|6|], so when user press "delete" button we
     * are expecting to get dstart == 16 and dend == 17, but instead we get dstart == 15,
     * dend == 16. When rejecting selection changes in input connection everything works as
     * expected.
     */
    private class NoSelectionInputConnection extends BaseInputConnection
    {
        public NoSelectionInputConnection(View targetView, boolean fullEditor)
        {
            super(targetView, fullEditor);
        }

        @Override
        public boolean setSelection(int start, int end)
        {
            //do nothing
            return true;
        }
    }

    private class MacAddressInputValidator implements InputFilter
    {
        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned destination, int dstart, int dend)
        {
            if (DEBUG) Log.v(TAG, String.format(
                    "filter(source: \"%s\", start: %d, end: %d, dest: \"%s\", dstart: %d, dend: %d)",
                    source, start, end, destination, dstart, dend)
            );

            /* Insertion is not allowed */
            if (dend - dstart == 0 && destination.length() > 0)
            {
                return "";
            }

            /* One-char rules. Should work most of the time */
            if (dend - dstart == 1)
            {
                /* One char is going to be deleted */
                if (end - start == 0)
                {
                    if (isPositionSelectable(dstart))
                    {
                        mBackspacePressed = true;
                        return mFiller;
                    }
                    else
                    {
                        return mDelimiter;
                    }
                }
                else if (!isValidSymbolForPosition(source.charAt(start), dstart))
                {
                    mInputRejected = true;
                    return destination.subSequence(dstart, dend);
                }
                else return null;
            }
            else
            {
                StringBuilder builder = new StringBuilder();
                builder.append(source.subSequence(start, end).toString().replaceAll("[^0-9A-Fa-f" + Pattern.quote(mFiller) + "]", ""));
                int replacementLength = dend - dstart + (mTextLength - destination.length());
                while (builder.length() < replacementLength)
                {
                    builder.append(mFiller);
                }
                int i = 0;
                while (i < replacementLength)
                {
                    if (!isPositionSelectable(i + dstart))
                    {
                        builder.insert(i, mDelimiter);
                    }
                    i++;
                }

                return builder.substring(0, replacementLength);
            }
        }
    }
}
