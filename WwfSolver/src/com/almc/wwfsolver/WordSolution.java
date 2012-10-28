package com.almc.wwfsolver;

import java.util.Vector;

public class WordSolution implements Comparable<WordSolution>
{
    private LetterLoc[] mLetters;
    private Vector<WordLocation> mLegalWords;
    private int mScore;

    public WordSolution(LetterLoc[] letterVector, Vector<WordLocation> legalWords, int score)
    {
        mLetters = letterVector;
        mLegalWords = legalWords;
        mScore = score;            
    }

    public LetterLoc[] getLetters()
    {
        return mLetters;
    }

    public Vector<WordLocation> getLegalWords()
    {
        return mLegalWords;
    }

    public int getScore()
    {
        return mScore;
    }

    public String getWordVector()
    {
        String wordVector = "";
        for (int i = 0; i < mLegalWords.size(); i++)
        {
            wordVector += mLegalWords.get(i);

            if (i < mLegalWords.size() - 1)
            {
                wordVector += ", ";
            }
        }
        return wordVector;
    }

    @Override
    public String toString()
    {
        String wordVector = getWordVector();
        return String.format("Score = {0}, Words = {1}, NumTiles = {2}", mScore, wordVector, mLetters.length);
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
            Vector<LetterLoc> otherLetters = new Vector<LetterLoc>();
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