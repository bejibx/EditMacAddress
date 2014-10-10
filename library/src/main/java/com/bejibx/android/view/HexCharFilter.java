package com.bejibx.android.view;

import java.util.Arrays;
import java.util.HashSet;

public final class HexCharFilter extends CharFilter
{
    final HashSet<Character> PERMITTED_SYMBOLS = new HashSet<Character>(Arrays.asList(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F',
            'a', 'b', 'c', 'd', 'e', 'f'
    ));

    public HexCharFilter(char filler)
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
        return PERMITTED_SYMBOLS.contains(c);
    }
}
