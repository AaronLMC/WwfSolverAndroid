package com.almc.wwfsolver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

import android.util.Log;
import android.util.SparseArray;


    public class WordDict
    {
        private HashSet<String> mLiveWords = new HashSet<String>();
        private HashSet<String> mDeadWords = new HashSet<String>();
     
        //{word length, word list}
        private SparseArray<HashSet<String>> mWordList = new SparseArray<HashSet<String>>();
        private int mMaxWordLength = 0;


        public WordDict(InputStream dictFileInputStream)
        {
            try
            {
            	BufferedReader br = new BufferedReader(new InputStreamReader(dictFileInputStream));
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

                    HashSet<String> set;
                    if (mWordList.get(length) == null)
                    {
                        set = new HashSet<String>();
                        mWordList.put(length, set);
                    }
                    else
                    {
                        set = mWordList.get(length);
                    }

//                    if (set.contains(word))
//                    {
//                        Log.e(WwfConstants.LOG_TAG, "Duplicate word in dictionary: " + word);
//                    }

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
            //HashSet<String> candidates = new HashSet<String>();

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
                    HashSet<String> list = mWordList.get(i);
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


