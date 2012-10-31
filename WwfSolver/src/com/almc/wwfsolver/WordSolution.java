package com.almc.wwfsolver;

import java.util.HashSet;

public class WordSolution implements Comparable<WordSolution>
{
    private LetterLoc[] mLetters;
    private HashSet<WordLocation> mLegalWords;
    private int mScore;

    public WordSolution(LetterLoc[] letterHashSet, HashSet<WordLocation> legalWords, int score)
    {
        mLetters = letterHashSet;
        mLegalWords = legalWords;
        mScore = score;            
    }

    public LetterLoc[] getLetters()
    {
        return mLetters;
    }

    public HashSet<WordLocation> getLegalWords()
    {
        return mLegalWords;
    }

    public int getScore()
    {
        return mScore;
    }

    public String getWordHashSet()
    {
        String wordHashSet = "";
        int count = 0;
        for (WordLocation word : mLegalWords)
        {
            wordHashSet += word;
            count++;

            if (count < mLegalWords.size() - 1)
            {
                wordHashSet += ", ";
            }
        }
        return wordHashSet;
    }

    @Override
    public String toString()
    {
        String wordHashSet = getWordHashSet();
        return String.format("Score = %d, Words = %s, NumTiles = %d", mScore, wordHashSet, mLetters.length);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof WordSolution)
        {
            //solutions are equal if the letters used are the same
            WordSolution otherObj = (WordSolution)obj;

            if (mLetters.length != otherObj.mLetters.length)
            {
                return false;
            }
            
            //Letters might be in a different order, but should be considered the same solution
            HashSet<LetterLoc> otherLetters = new HashSet<LetterLoc>();
            for (LetterLoc otherLetter : otherObj.mLetters) otherLetters.add(otherLetter);
            for (LetterLoc letter : mLetters)
            {
                if (!otherLetters.contains(letter))
                {
                    return false;
                }
            }

            return true;
        }
        else
        {
            return super.equals(obj);
        }
    }

	@Override
	public int compareTo(WordSolution otherSolution)
	{
         if (otherSolution.mScore < mScore)
             return -1;
         else if (otherSolution.mScore == mScore)
             return 0;
         else
             return 1;
	}

}