package com.bejibx.android.view;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

/**
 * This class is absolutely needed because it seems that some soft input methods incorrectly
 * changing selection. Because of that often we get incorrect dstart/dend values in our input
 * filter. For example we have [11:22:33:44:55:6|6|], so when user press "delete" button we
 * are expecting to get dstart == 16 and dend == 17, but instead we get dstart == 15,
 * dend == 16. When rejecting selection changes in input connection everything works as
 * expected so I should override setSelection() method. The problem is class returned from default
 * implementation of EditText.onCreateInputConnection(...) - EditableInputConnection is internal so
 * I can't just grab it and override one method. Thus I need to use wrapper for this.
 */
public class InputConnectionWrapper implements InputConnection
{
    private InputConnection mWrappedInputConnection;

    public InputConnectionWrapper(InputConnection wrappedConnection)
    {
        mWrappedInputConnection = wrappedConnection;
    }

    @Override
    public boolean beginBatchEdit()
    {
        return mWrappedInputConnection.beginBatchEdit();
    }

    @Override
    public boolean endBatchEdit()
    {
        return mWrappedInputConnection.endBatchEdit();
    }

    @Override
    public boolean clearMetaKeyStates(int states)
    {
        return mWrappedInputConnection.clearMetaKeyStates(states);
    }

    @Override
    public boolean commitCompletion(CompletionInfo text)
    {
        return mWrappedInputConnection.commitCompletion(text);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean commitCorrection(CorrectionInfo correctionInfo)
    {
        return mWrappedInputConnection.commitCorrection(correctionInfo);
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition)
    {
        return mWrappedInputConnection.commitText(text, newCursorPosition);
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength)
    {
        return mWrappedInputConnection.deleteSurroundingText(beforeLength, afterLength);
    }

    @Override
    public boolean finishComposingText()
    {
        return mWrappedInputConnection.finishComposingText();
    }

    @Override
    public int getCursorCapsMode(int reqModes)
    {
        return mWrappedInputConnection.getCursorCapsMode(reqModes);
    }

    @Override
    public ExtractedText getExtractedText(ExtractedTextRequest request, int flags)
    {
        return mWrappedInputConnection.getExtractedText(request, flags);
    }

    @Override
    public CharSequence getTextBeforeCursor(int length, int flags)
    {
        return mWrappedInputConnection.getTextBeforeCursor(length, flags);
    }

    @Override
    public CharSequence getSelectedText(int flags)
    {
        return mWrappedInputConnection.getSelectedText(flags);
    }

    @Override
    public CharSequence getTextAfterCursor(int length, int flags)
    {
        return mWrappedInputConnection.getTextAfterCursor(length, flags);
    }

    @Override
    public boolean performEditorAction(int actionCode)
    {
        return mWrappedInputConnection.performEditorAction(actionCode);
    }

    @Override
    public boolean performContextMenuAction(int id)
    {
        return mWrappedInputConnection.performContextMenuAction(id);
    }

    @Override
    public boolean performPrivateCommand(String action, Bundle data)
    {
        return mWrappedInputConnection.performPrivateCommand(action, data);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean requestCursorUpdates(int cursorUpdateMode)
    {
        return mWrappedInputConnection.requestCursorUpdates(cursorUpdateMode);
    }

    @Override
    public boolean setComposingText(CharSequence text, int newCursorPosition)
    {
        return mWrappedInputConnection.setComposingText(text, newCursorPosition);
    }

    @Override
    public boolean setComposingRegion(int start, int end)
    {
        return mWrappedInputConnection.setComposingRegion(start, end);
    }

    @Override
    public boolean setSelection(int start, int end)
    {
        //do nothing
        return true;
    }

    @Override
    public boolean sendKeyEvent(KeyEvent event)
    {
        return mWrappedInputConnection.sendKeyEvent(event);
    }

    @Override
    public boolean reportFullscreenMode(boolean enabled)
    {
        return mWrappedInputConnection.reportFullscreenMode(enabled);
    }
}
