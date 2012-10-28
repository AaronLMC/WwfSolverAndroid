package com.almc.wwfsolver;

import java.util.Vector;

public class WordLocation
{
    private LetterLoc[] mLetters;
    private String mWordText;

    public WordLocation(Vector<LetterLoc> letters)
    {
        mLetters = new LetterLoc[letters.size()];
        letters.copyInto(mLetters);

        mWordText = "";
        for (LetterLoc letter : mLetters)
        {
             mWordText += letter.Letter;
        }
    }

    public String getWordText()
    {
        return mWordText;
    }

    public LetterLoc[] getLetters()
    {
        return mLetters;
    }

    @Override
    public String toString()
    {
        return mWordText;
    }
}
