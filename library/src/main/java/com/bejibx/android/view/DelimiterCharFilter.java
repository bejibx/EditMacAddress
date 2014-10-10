package com.bejibx.android.view;

public final class DelimiterCharFilter extends CharFilter
{
    public DelimiterCharFilter(char filler)
    {
        super(filler);
    }

    @Override
    protected boolean isSelectable()
    {
        return false;
    }

    @Override
    protected boolean isValidChar(char c)
    {
        return false;
    }
}
