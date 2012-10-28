package com.almc.wwfsolver;

import java.util.Vector;

public class WordSet
{
    private WordLocation mPrimaryWord = null;
    private Vector<WordLocation> mIncidentalWords;
    private WordOrientation mOrientation;

    public WordSet(WordOrientation orientation)
    {
        mIncidentalWords = new Vector<WordLocation>();
        mOrientation = orientation;
    }

    public WordLocation getPrimaryWord()
    {
    	return mPrimaryWord;
    }
    
    public void setPrimaryWord(WordLocation word)
    {
    	mPrimaryWord = word;
    }

    public WordOrientation getOrientation()
    {
        return mOrientation;
    }

    public Vector<WordLocation> getIncidentalWords()
    {
        return mIncidentalWords;
    }

    public void AddIncidentalWord(WordLocation word)
    {
        mIncidentalWords.add(word);
    }

    public Vector<WordLocation> GetFullVector()
    {
        Vector<WordLocation> fullSet = new Vector<WordLocation>();

        if (mPrimaryWord != null)
        {
            fullSet.add(mPrimaryWord);
        }
        fullSet.addAll(mIncidentalWords);
        return fullSet;
    }
}