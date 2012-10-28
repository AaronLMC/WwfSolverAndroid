package com.almc.wwfsolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import android.util.Log;
import android.util.SparseArray;


    public class WordDict
    {
        private Vector<String> mLiveWords = new Vector<String>();
        private Vector<String> mDeadWords = new Vector<String>();
     
        //{word length, word list}
        private SparseArray<Vector<String>> mWordList = new SparseArray<Vector<String>>();
        private int mMaxWordLength = 0;


        public WordDict(String dictFile)
        {
            try
            {
            	BufferedReader br = new BufferedReader(new FileReader(new File(dictFile)));
                String line;
                while ((line = br.readLine()) != null)
                {
                    String word = line.trim().toUpperCase();
                    int length = word.length();
                    if (length == 0)
                    {
                        //ignore empty words
                        continue;
                    }

                    Vector<String> set;
                    if (mWordList.get(length) == null)
                    {
                        set = new Vector<String>();
                        mWordList.put(length, set);
                    }
                    else
                    {
                        set = mWordList.get(length);
                    }

                    if (set.contains(word))
                    {
                        Log.e(WwfConstants.LOG_TAG, "Duplicate word in dictionary: " + word);
                    }

                    set.add(word);

                    if (length > mMaxWordLength)
                    {
                        mMaxWordLength = length;
                    }
                }
                br.close();
            }
            catch (Exception e)
            {
            	Log.e(WwfConstants.LOG_TAG, "Error opening dictionary file", e);
            }
        }

        public boolean IsWordInList(String word)
        {
            if (mWordList.get(word.length()) == null)
            {
                return false;
            }

            return mWordList.get(word.length()).contains(word);
        }

        /// <summary>
        /// Returns true if given word cannot be built into a longer word in the dictionary
        /// </summary>
        public boolean IsDeadWord(String testWord)
        {
            //Vector<String> candidates = new Vector<String>();

            if (mDeadWords.contains(testWord))
            {
                return true;
            }
            else if (mLiveWords.contains(testWord))
            {
                return false;
            }

            for (int i = testWord.length() + 1; i <= mMaxWordLength; i++)
            {
                if (mWordList.get(i) != null)
                {
                    Vector<String> list = mWordList.get(i);
                    for (String word : list)
                    {
                        if (word.contains(testWord))
                        {
                            mLiveWords.add(testWord);
                            return false;
                        }
                    }
                }
            }

            //Keep record of words that have dead ends
            mDeadWords.add(testWord);
            return true;
        }

        public int GetNumWordsEvaluated()
        {
            return mLiveWords.size() + mDeadWords.size();
        }
    }


