package com.bejibx.android.view;

public class AnyCharFilter extends CharFilter
{
    public AnyCharFilter(char filler)
    {
        super(filler);
    }

    @Override
    protected boolean isSelectable()
    {
        return true;
    }

    @Override
    protected boolean isValidChar(char c)
    {
        return false;
    }
}
