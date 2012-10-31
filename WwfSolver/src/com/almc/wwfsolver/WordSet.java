package com.almc.wwfsolver;

import java.util.HashSet;

public class WordSet
{
    private WordLocation mPrimaryWord = null;
    private HashSet<WordLocation> mIncidentalWords;
    private WordOrientation mOrientation;

    public WordSet(WordOrientation orientation)
    {
        mIncidentalWords = new HashSet<WordLocation>();
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

    public HashSet<WordLocation> getIncidentalWords()
    {
        return mIncidentalWords;
    }

    public void AddIncidentalWord(WordLocation word)
    {
        mIncidentalWords.add(word);
    }

    public HashSet<WordLocation> GetFullHashSet()
    {
        HashSet<WordLocation> fullSet = new HashSet<WordLocation>();

        if (mPrimaryWord != null)
        {
            fullSet.add(mPrimaryWord);
        }
        fullSet.addAll(mIncidentalWords);
        return fullSet;
    }
}