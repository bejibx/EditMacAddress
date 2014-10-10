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

import java.util.HashMap;
import java.util.LinkedList;

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
    private static final char DEFAULT_FILLER = ' ';

    private char mFiller;

    private int mCursorPosition = 0;
    private int mFirstSelectablePosition = 0;
    private int mLastSelectablePosition = 0;

    private HashMap<Character, CharFilter> mFiltersCache = new HashMap<Character, CharFilter>();
    private CharFilter[] mMask;

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

        parseMask("HH:HH:HH:HH:HH:HH");

        setFilters(new InputFilter[]{new MacAddressInputValidator()});
    }

    private void obtainAttributes(Context context, AttributeSet attrs)
    {
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EditMacAddress, 0, 0);
        String fillerStr = String.valueOf(DEFAULT_FILLER);
        try
        {
            fillerStr = attributes.getString(R.styleable.EditMacAddress_filler);
        } finally
        {
            attributes.recycle();
        }

        if (fillerStr == null || fillerStr.isEmpty())
        {
            setFiller(DEFAULT_FILLER);
        }
        else
        {
            setFiller(fillerStr.charAt(0));
        }
    }

    private void parseMask(String mask)
    {
        LinkedList<CharFilter> positions = new LinkedList<CharFilter>();
        char[] chars = mask.toCharArray();

        int i = 0;
        StringBuilder maskString = new StringBuilder();
        while (i < chars.length)
        {
            char maskChar = chars[i];
            CharFilter filter;

            if (mFiltersCache.containsKey(maskChar))
            {
                filter = mFiltersCache.get(maskChar);
            }
            else
            {
                filter = getFilterForCharacter(maskChar);
                if (filter == null)
                {
                    filter = new AnyCharFilter(' ');
                }
                mFiltersCache.put(maskChar, filter);
            }

            positions.add(filter);
            maskString.append(filter.getFiller());
            if (filter.isSelectable())
            {
                if (i < mFirstSelectablePosition)
                {
                    mFirstSelectablePosition = i;
                }
                else if (i > mLastSelectablePosition)
                {
                    mLastSelectablePosition = i;
                }
            }
            i++;
        }

        mMask = positions.toArray(new CharFilter[positions.size()]);
        setText(maskString.toString());
    }

    private boolean isPositionSelectable(int position)
    {
        return position >= 0 && position < getMaskLength() && mMask[position].isSelectable();
    }

    private boolean isValidCharForPosition(char c, int pos)
    {
        return pos >= 0 && pos < getMaskLength() && mMask[pos].isValidChar(c);
    }

    private char getFillerForPosition(int position)
    {
        if (position >= 0 && position < getMaskLength())
        {
            return mMask[position].getFiller();
        }
        else
        {
            return DEFAULT_FILLER;
        }
    }

    private int getMaskLength()
    {
        return mMask != null ? mMask.length : -1;
    }

    private int getNextSelectablePosition(int pos)
    {
        if (pos >= 0)
            for (int i = pos + 1; i < getMaskLength(); i++)
                if (mMask[i].isSelectable())
                    return i;

        return getLastSelectablePosition();
    }

    private int getPreviousSelectablePosition(int pos)
    {
        if (pos < getMaskLength())
            for (int i = pos - 1; i >= 0; i--)
                if (mMask[i].isSelectable())
                    return i;

        return getFirstSelectablePosition();
    }

    private int getFirstSelectablePosition()
    {
        return mFirstSelectablePosition;
    }

    private int getLastSelectablePosition()
    {
        return mLastSelectablePosition;
    }

    protected CharFilter getFilterForCharacter(char maskCharacter)
    {
        switch (maskCharacter)
        {
            case 'H':
                return new HexCharFilter(mFiller);

            default:
                return new DelimiterCharFilter(maskCharacter);
        }
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

    public void setFiller(char filler)
    {
        mFiller = filler;
    }

    public String getUnformattedText()
    {
        StringBuilder unformatted = new StringBuilder(getText());
        int i = 0;
        while (i < unformatted.length())
        {
            if (!isPositionSelectable(i))
            {
                unformatted.delete(i, i + 1);
            }
            i++;
        }
        return unformatted.toString();
    }

    @SuppressWarnings("NullableProblems")
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

    @SuppressWarnings("NullableProblems")
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
                        return String.valueOf(mFiller);
                    }
                    else
                    {
                        // should never be here
                        return String.valueOf(mMask[dstart].getFiller());
                    }
                }
                else if (!isValidCharForPosition(source.charAt(start), dstart))
                {
                    mInputRejected = true;
                    return destination.subSequence(dstart, dend);
                }
                else return null;
            }
            else
            {
                StringBuilder builder = new StringBuilder();
                builder.append(source.subSequence(start, end).toString());

                int i = 0;
                while (i < builder.length())
                {
                    if (!isPositionSelectable(dstart + 1))
                    {
                        builder.insert(i, getFillerForPosition(dstart + i));
                        i++;
                    }
                    else if (!isValidCharForPosition(builder.charAt(i), dstart + i))
                    {
                        builder.delete(i, i + 1);
                    }
                    else
                    {
                        i++;
                    }
                }

                int replacementLength = dend - dstart + (getMaskLength() - destination.length());
                while ((i = builder.length()) < replacementLength)
                {
                    builder.append(getFillerForPosition(i));
                }

                return builder.substring(0, replacementLength);
            }
        }
    }
}
