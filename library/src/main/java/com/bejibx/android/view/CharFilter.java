package com.bejibx.android.view;

public abstract class CharFilter
{
    protected final char mFiller;

    public CharFilter(char filler)
    {
        mFiller = filler;
    }

    protected abstract boolean isSelectable();
    protected abstract boolean isValidChar(char c);

    public char getFiller()
    {
        return mFiller;
    }
}
