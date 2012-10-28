package com.almc.wwfsolver;

public class LetterLoc
{
    public final char Letter;
    public final int X;
    public final int Y;
    public final boolean IsBlankLetter;

    public LetterLoc(char letter, int x, int y, boolean isBlankLetter)
    {
        Letter = letter;
        X = x;
        Y = y;
        IsBlankLetter = isBlankLetter;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof LetterLoc)
        {
            LetterLoc objLetterLoc = (LetterLoc)obj;

            return (Letter == objLetterLoc.Letter
                    && X == objLetterLoc.X
                    && Y == objLetterLoc.Y
                    && IsBlankLetter == objLetterLoc.IsBlankLetter);
        }
        else
        {
            return super.equals(obj);
        }
        
    }

    @Override
    public String toString()
    {            
        return String.format("{0}{1}[{2},{3}]", Letter, (IsBlankLetter ? "*" : ""), X, Y);
    }
}