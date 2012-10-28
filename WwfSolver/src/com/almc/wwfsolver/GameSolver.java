package com.almc.wwfsolver;

import java.util.Vector;


    public class GameSolver
    {
        private FrontEndInterface mFrontEnd;
        private WordDict mWordDict;
        private CharInfo[][] mBoardLetters;
        private char[] mAvailableLetters;
        private Vector<Integer> mUsedLetterIdxs = new Vector<Integer>();
        private Vector<LetterLoc> mCurrentWord = new Vector<LetterLoc>();
        private int mMaxScoreFound = 0;

        public GameSolver(FrontEndInterface frontEnd, WordDict wordDict, Board boardConfig)
        {
            mFrontEnd = frontEnd;
            mWordDict = wordDict;
            mBoardLetters = CharInfo.ConvertBoardToCharInfoArr(boardConfig.getBoardLetters());
            mAvailableLetters = boardConfig.AvailableLetters;
        }

        public List<WordSolution> GetSolutions()
        {
            mMaxScoreFound = 0;
            List<WordSolution> solutions = new List<WordSolution>();

            //if the board is empty, find best word to place at center tile
            boolean boardIsEmpty = true;
            for (int i = 0; i < GameVals.BOARD_SIZE; i++)
            {
                for (int j = 0; j < GameVals.BOARD_SIZE; j++)
                {
                    if (LocationContainsLetter(i, j))
                    {
                        boardIsEmpty = false;
                        break;
                    }
                }

                if (!boardIsEmpty) break;
            }

            
            if (boardIsEmpty)
            {
                //look for solutions starting at center
                mFrontEnd.SetSearchStartLocation(GameVals.BOARD_CENTER_LOC, GameVals.BOARD_CENTER_LOC);
                List<WordSolution> words = SolutionSearch(GameVals.BOARD_CENTER_LOC, GameVals.BOARD_CENTER_LOC, 0);
                mFrontEnd.SetLocationColorToDefault(GameVals.BOARD_CENTER_LOC, GameVals.BOARD_CENTER_LOC);

                foreach (WordSolution w in words)
                {
                    if (!solutions.Contains(w))
                    {
                        solutions.Add(w);
                    }
                }
            }
            else
            {
                //Look for solutions that build off existing tiles on board
                for (int i = 0; i < GameVals.BOARD_SIZE; i++)
                {
                    for (int j = 0; j < GameVals.BOARD_SIZE; j++)
                    {
                        if (LocationContainsLetter(i, j))
                        {
                            continue;
                        }

                        //evaluate legality of placing an initial letter here
                        if (!IsLegalMove(i, j))
                        {
                            continue;
                        }

                        mFrontEnd.SetSearchStartLocation(i, j);                        
                        List<WordSolution> words = SolutionSearch(i, j, 0);
                        mFrontEnd.SetLocationColorToDefault(i, j);

                        foreach (WordSolution w in words)
                        {
                            if (!solutions.Contains(w))
                            {
                                solutions.Add(w);
                            }
                        }
                    }
                }
            }

            //TODO: report this value as solutions are found
            mFrontEnd.SetNumSolutionsFound(solutions.Count);

            return solutions;
        }

        private List<WordSolution> SolutionSearch(int x, int y, int depth)
        {
            if (depth > 0 && depth <= 3)
            {
                mFrontEnd.SetSearchRecurLocation(x, y);

                if (depth <= 2)
                {
                    int numWordsEvaluated = mWordDict.GetNumWordsEvaluated();
                    mFrontEnd.SetNumWordsEvaluated(numWordsEvaluated);
                }
            }

            List<WordSolution> solutions = new List<WordSolution>();            
            
            //cycle through all starting idx so that we exercise all letter combinations
            for (int letterIdx = 0; letterIdx < mAvailableLetters.Length; letterIdx++)
            {
                if (mUsedLetterIdxs.Contains(letterIdx))
                {
                    continue;
                }

                //we have a letter and a location to try out
                char letter = mAvailableLetters.ElementAt(letterIdx);

                if (letter == GameVals.BLANK_TILE)
                {
                    //If tile is blank, cycle through all letters
                    for (int i = 0; i < GameVals.ALPHABET.Length; i++)
                    {
                        char blankLetter = GameVals.ALPHABET[i];
                        _evaluateLetter(blankLetter, true, letterIdx, x, y, solutions, depth);
                    }
                }
                else
                {
                    _evaluateLetter(letter, false, letterIdx, x, y, solutions, depth);
                }

            }

            mFrontEnd.SetLocationColorToDefault(x, y);

            return solutions;
        }

        private void _evaluateLetter(char letter, boolean isBlankLetter, int letterIdx, int x, int y, List<WordSolution> solutions, int depth)
        {
            mBoardLetters[x, y] = new CharInfo(letter, isBlankLetter);

            LetterLoc curLetterLoc = new LetterLoc(letter, x, y, isBlankLetter);
            mCurrentWord.Add(curLetterLoc);
            mUsedLetterIdxs.Add(letterIdx);

            //get score of current turn
            WordSet wordSet = GetWordList();
            List<WordLocation> wordList = wordSet.GetFullList();
            HashSet<WordLocation> illegalWords;
            int score = GetWordScore(wordList, mCurrentWord, out illegalWords);
            if (score > 0)
            {
                //store this move as one that has scores
                LetterLoc[] letterArr = new LetterLoc[mCurrentWord.Count];
                mCurrentWord.CopyTo(letterArr);
                WordSolution solution = new WordSolution(letterArr, wordList, score);
                solutions.Add(solution);
            }

            //see if we should continue down this path
            if (score < 0
                && AreIncidentalWordsIllegal(illegalWords, wordSet))
            {
                //an incidental word is illegal.  Adding new tiles won't help with this so we are done
                clearCandidateLetterFromBoard(x, y, letterIdx, curLetterLoc);
                return;
            }
            else if (wordSet.PrimaryWord != null
                && mWordDict.IsDeadWord(wordSet.PrimaryWord.WordText))
            {
                //there are no words that contain our primary word, so stop
                clearCandidateLetterFromBoard(x, y, letterIdx, curLetterLoc);
                return;
            }

            //Look for next word
            if (wordSet.Orientation == WordOrientation.SINGLE_TILE)
            {
                //go in all 4 directions

                int y_next = NextTileUp(x, y);
                if (y_next >= 0)
                {
                    List<WordSolution> words = SolutionSearch(x, y_next, depth + 1);
                    solutions.AddRange(words);
                }

                y_next = NextTileDown(x, y);
                if (y_next >= 0)
                {
                    List<WordSolution> words = SolutionSearch(x, y_next, depth + 1);
                    solutions.AddRange(words);
                }

                int x_next = NextTileLeft(x, y);
                if (x_next >= 0)
                {
                    List<WordSolution> words = SolutionSearch(x_next, y, depth + 1);
                    solutions.AddRange(words);
                }

                x_next = NextTileRight(x, y);
                if (x_next >= 0)
                {
                    List<WordSolution> words = SolutionSearch(x_next, y, depth + 1);
                    solutions.AddRange(words);
                }
            }
            else if (wordSet.Orientation == WordOrientation.HORIZONTAL)
            {
                //go top and bottom
                int y_next = NextTileUp(x, y);
                if (y_next >= 0)
                {
                    List<WordSolution> words = SolutionSearch(x, y_next, depth + 1);
                    solutions.AddRange(words);
                }

                y_next = NextTileDown(x, y);
                if (y_next >= 0)
                {
                    List<WordSolution> words = SolutionSearch(x, y_next, depth + 1);
                    solutions.AddRange(words);
                }
            }
            else
            {
                //go left and right
                int x_next = NextTileLeft(x, y);
                if (x_next >= 0)
                {
                    List<WordSolution> words = SolutionSearch(x_next, y, depth + 1);
                    solutions.AddRange(words);
                }

                x_next = NextTileRight(x, y);
                if (x_next >= 0)
                {
                    List<WordSolution> words = SolutionSearch(x_next, y, depth + 1);
                    solutions.AddRange(words);
                }
            }

            //clear the location we are looking at
            clearCandidateLetterFromBoard(x, y, letterIdx, curLetterLoc);
        }

        private boolean AreIncidentalWordsIllegal(HashSet<WordLocation> illegalWords, WordSet wordSet)
        {
            foreach(WordLocation illegalWord in illegalWords)
            {
                if (wordSet.IncidentalWords.Contains(illegalWord))
                {
                    return true;
                }                
            }

            return false;
        }

        private void clearCandidateLetterFromBoard(int x, int y, int letterIdx, LetterLoc letterLoc)
        {
            mBoardLetters[x, y] = new CharInfo();
            mUsedLetterIdxs.Remove(letterIdx);
            mCurrentWord.Remove(letterLoc);
        }

        private int NextTileUp(int x, int y)
        {
            int y_next = -1;
            for (int j = y; j >= 0; j--)
            {
                if (IsLegalMove(x, j))
                {
                    y_next = j;
                    break;
                }
            }
            return y_next;
        }

        private int NextTileDown(int x, int y)
        {
            int y_next = -1;
            for (int j = y; j < GameVals.BOARD_SIZE; j++)
            {
                if (IsLegalMove(x, j))
                {
                    y_next = j;
                    break;
                }
            }
            return y_next;
        }

        private int NextTileLeft(int x, int y)
        {
            int x_next = -1;
            for (int i = x; i >= 0; i--)
            {
                if (IsLegalMove(i, y))
                {
                    x_next = i;
                    break;
                }
            }
            return x_next;
        }

        private int NextTileRight(int x, int y)
        {
            int x_next = -1;
            for (int i = x; i < GameVals.BOARD_SIZE; i++)
            {
                if (IsLegalMove(i, y))
                {
                    x_next = i;
                    break;
                }
            }
            return x_next;
        }

        private int GetWordScore(List<WordLocation> wordList, List<LetterLoc> playedTiles, HashSet<WordLocation> out_illegalWords)
        {
            int score = 0;

            //first check for illegal words
            foreach (WordLocation word in wordList)
            {                
                if (!mWordDict.IsWordInList(word.WordText))
                {
                    illegalWords.Add(word);
                }
            }
            if (illegalWords.Count > 0)
            {
                return -1;
            }

            foreach (WordLocation word in wordList)
            {
                //score the word
                int wordScore = 0;
                int multipliers = 1;
                foreach (LetterLoc letter in word.Letters)
                {
                    //blank letters get no points
                    if (letter.IsBlankLetter)
                    {
                        continue;
                    }

                    int letterScore = GameVals.LETTER_SCORE[letter.Letter];

                    //apply letter bonus if this is a played tile
                    if (playedTiles.Contains(letter))
                    {
                        GameVals.Bonus letterBonus = GameVals.BONUS_TILES[letter.X, letter.Y];
                        switch (letterBonus)
                        {
                            case GameVals.Bonus.D_LT:
                                letterScore *= 2;
                                break;
                            case GameVals.Bonus.T_LT:
                                letterScore *= 3;
                                break;
                            case GameVals.Bonus.D_WD:
                                multipliers *= 2;
                                break;
                            case GameVals.Bonus.T_WD:
                                multipliers *= 3;
                                break;
                        }
                    }

                    wordScore += letterScore;
                }
                wordScore *= multipliers;

                score += wordScore;
            }

            if (playedTiles.Count == 7)
            {
                score += GameVals.BONUS_USED_ALL_TILES;
            }

            if (score > mMaxScoreFound)
            {
                mMaxScoreFound = score;
                mFrontEnd.SetHighestScoreFound(mMaxScoreFound);
            }

            return score;
        }

        private WordSet GetWordList()
        { 
            //determine orientation of played tiles
            WordOrientation orientation;
            int lastWordIdx = mCurrentWord.Count - 1;
            int startX = mCurrentWord[0].X;
            int startY = mCurrentWord[0].Y;
            int endX = mCurrentWord[lastWordIdx].X;
            int endY = mCurrentWord[lastWordIdx].Y;

            if (mCurrentWord.Count == 1)
                orientation = WordOrientation.SINGLE_TILE;
            else if (startX != endX)
                orientation = WordOrientation.VERTICAL;
            else
                orientation = WordOrientation.HORIZONTAL;
            
            //Initialize word set
            WordSet wordSet = new WordSet(orientation);

            //for each letter in the proposed word, add the score of any new words generated by that letter
            foreach (LetterLoc letter in mCurrentWord)
            {
                //evaluate in X direction
                int initX = letter.X;
                int minX = initX;
                for (int i = initX; i >= 0; i--)
                {
                    if (mBoardLetters[i, letter.Y].IsEmptySpace())
                    {
                        break;
                    }
                    else
                    {
                        minX = i;
                    }
                }

                int maxX = initX;
                for (int i = initX; i < GameVals.BOARD_SIZE; i++)
                {
                    if (mBoardLetters[i, letter.Y].IsEmptySpace())
                    {
                        break;
                    }
                    else
                    {
                        maxX = i;
                    }
                }

                int wordLenX = maxX - minX + 1;
                if (wordLenX > 1)
                {
                    //get word
                    String word = "";
                    for (int i = minX; i <= maxX; i++)
                    {
                        word += mBoardLetters[i, letter.Y];
                    }

                    List<LetterLoc> letters = new List<LetterLoc>();
                    for (int i = minX; i <= maxX; i++)
                    {
                        letters.Add(mBoardLetters[i, letter.Y].GenLetterLoc(i, letter.Y));
                    }

                    if (orientation == WordOrientation.VERTICAL)
                        wordSet.PrimaryWord = new WordLocation(letters);
                    else
                        wordSet.AddIncidentalWord(new WordLocation(letters));
                }

                //evaluate in Y direction
                int initY = letter.Y;
                int minY = initY;
                for (int j = initY; j >= 0; j--)
                {
                    if (mBoardLetters[letter.X, j].IsEmptySpace())
                    {
                        break;
                    }
                    else
                    {
                        minY = j;
                    }
                }

                int maxY = initY;
                for (int j = initY; j < GameVals.BOARD_SIZE; j++)
                {
                    if (mBoardLetters[letter.X, j].IsEmptySpace())
                    {
                        break;
                    }
                    else
                    {
                        maxY = j;
                    }
                }

                int wordLenY = maxY - minY + 1;
                if (wordLenY > 1)
                {
                    //get word
                    List<LetterLoc> letters = new List<LetterLoc>();
                    for (int j = minY; j <= maxY; j++)
                    {
                        letters.Add(mBoardLetters[letter.X, j].GenLetterLoc(letter.X, j));
                    }

                    if (orientation == WordOrientation.HORIZONTAL)
                        wordSet.PrimaryWord = new WordLocation(letters);
                    else
                        wordSet.AddIncidentalWord(new WordLocation(letters));
                }
            }

            return wordSet;
        }

        private boolean IsLegalMove(int x, int y)
        {
            if (LocationContainsLetter(x, y))
            {
                return false;
            }

            //look at whether location is in-line with current word
            if (mCurrentWord.Count > 0)
            {
                int initX = mCurrentWord[0].X;
                int initY = mCurrentWord[0].Y;

                //location is not in line with existing current word
                if (x != initX && y != initY)
                {
                    return false;
                }

                if (x == initX)
                {
                    //make sure there is a letter between InitX and X
                    int smallerX = Math.Min(x, initX);
                    int largerX = Math.Max(x, initX);
                    for (int i = smallerX + 1; i < largerX; i++)
                    {
                        if (!LocationContainsLetter(i, y))
                        {
                            return false;
                        }
                    }
                }
                else if (y == initY)
                {
                    //make sure there is a letter between InitY and Y
                    int smallerY = Math.Min(y, initY);
                    int largerY = Math.Max(y, initY);
                    for (int j = smallerY + 1; j < largerY; j++)
                    {
                        if (!LocationContainsLetter(x, j))
                        {
                            return false;
                        }
                    }
                }
            }
            else
            {
                //this is the first move; make sure it is adjacent to an existing letter
                if (x > 0 && LocationContainsLetter(x - 1, y))
                    return true;
                if (x < GameVals.BOARD_SIZE - 1 && LocationContainsLetter(x + 1, y))
                    return true;

                if (y > 0 && LocationContainsLetter(x, y - 1))
                    return true;
                if (y < GameVals.BOARD_SIZE - 1 && LocationContainsLetter(x, y + 1))
                    return true;

                return false; //not adjacent to any tiles already on board
            }

            return true;
        }


        private boolean LocationContainsLetter(int x, int y)
        {
            //look at board location
            if (!mBoardLetters[x, y].IsEmptySpace())
            {
                return true;
            }

            return false;
        }
    }

    [DebuggerDisplay("{ToString()}")]
    public class WordSolution : IComparable
    {
        private LetterLoc[] mLetters;
        private List<WordLocation> mLegalWords;
        private int mScore;

        public WordSolution(LetterLoc[] letterList, List<WordLocation> legalWords, int score)
        {
            mLetters = letterList;
            mLegalWords = legalWords;
            mScore = score;            
        }

        public LetterLoc[] Letters
        {
            get { return mLetters; }
        }

        public List<WordLocation> LegalWords
        {
            get { return mLegalWords; }
        }

        public int Score
        {
            get { return mScore; }
        }

        public String getWordList()
        {
            String wordList = "";
            for (int i = 0; i < mLegalWords.Count; i++)
            {
                wordList += mLegalWords[i];

                if (i < mLegalWords.Count - 1)
                {
                    wordList += ", ";
                }
            }
            return wordList;
        }

        public override String ToString()
        {
            String wordList = getWordList();
            return String.Format("Score = {0}, Words = {1}, NumTiles = {2}", mScore, wordList, mLetters.Length);
        }

        public override boolean Equals(object obj)
        {
            if (obj is WordSolution)
            {
                //solutions are equal if the letters used are the same
                WordSolution otherObj = obj as WordSolution;

                if (mLetters.Length != otherObj.mLetters.Length)
                {
                    return false;
                }
                
                //Letters might be in a different order, but should be considered the same solution
                List<LetterLoc> myLetters = new List<LetterLoc>(mLetters);
                List<LetterLoc> otherLetters = new List<LetterLoc>(otherObj.mLetters);
                foreach (LetterLoc letter in myLetters)
                {
                    if (!otherLetters.Contains(letter))
                    {
                        return false;
                    }
                }

                return true;
            }
            else
            {
                return base.Equals(obj);
            }
        }

        public override int GetHashCode()
        {
            return base.GetHashCode();
        }

        #region IComparable Members

        public int CompareTo(object obj)
        {
            if (obj is WordSolution)
            {
                WordSolution objWordSolution = obj as WordSolution;

                if (objWordSolution.mScore < mScore)
                    return -1;
                else if (objWordSolution.mScore == mScore)
                    return 0;
                else
                    return 1;
            }
            else
            {
                return 0;
            }
        }

        #endregion
    }

    public enum WordOrientation
    {
        SINGLE_TILE,
        VERTICAL,
        HORIZONTAL
    }

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

        public WordLocation PrimaryWord
        {
            get { return mPrimaryWord; }
            set { mPrimaryWord = value; }
        }

        public WordOrientation Orientation
        {
            get { return mOrientation; }
        }

        public HashSet<WordLocation> IncidentalWords
        {
            get { return mIncidentalWords; }
        }

        public void AddIncidentalWord(WordLocation word)
        {
            mIncidentalWords.Add(word);
        }

        public List<WordLocation> GetFullList()
        {
            List<WordLocation> fullSet = new List<WordLocation>();

            if (mPrimaryWord != null)
            {
                fullSet.Add(mPrimaryWord);
            }
            fullSet.AddRange(mIncidentalWords);
            return fullSet;
        }
    }

    [DebuggerDisplay("{ToString()}")]
    public class WordLocation
    {
        private LetterLoc[] mLetters;
        private String mWordText;

        public WordLocation(List<LetterLoc> letters)
        {
            mLetters = new LetterLoc[letters.Count];
            letters.CopyTo(mLetters);

            mWordText = "";
            foreach (LetterLoc letter in mLetters)
            {
                 mWordText += letter.Letter;
            }
        }

        public String WordText
        {
            get { return mWordText;}
        }

        public LetterLoc[] Letters
        {
            get {return mLetters; }
        }

        public override String ToString()
        {
            return mWordText;
        }
    }

    [DebuggerDisplay("{ToString()}")]
    public class LetterLoc
    {
        public final char Letter;
        public final int X;
        public final int Y;
        public final booleanean IsBlankLetter;

        public LetterLoc(char letter, int x, int y, boolean isBlankLetter)
        {
            Letter = letter;
            X = x;
            Y = y;
            IsBlankLetter = isBlankLetter;
        }

        @Override
        public booleanean equals(object obj)
        {
            if (obj is LetterLoc)
            {
                LetterLoc objLetterLoc = obj as LetterLoc;

                return (Letter == objLetterLoc.Letter
                        && X == objLetterLoc.X
                        && Y == objLetterLoc.Y
                        && IsBlankLetter == objLetterLoc.IsBlankLetter);
            }
            else
            {
                return base.Equals(obj);
            }
            
        }

        @Override
        public String toString()
        {            
            return String.format("{0}{1}[{2},{3}]", Letter, (IsBlankLetter ? "*" : ""), X, Y);
        }
    }

    class CharInfo
    {
        public final char EMPTY_SPACE = ' ';

        public final char Letter;
        public final booleanean IsBlankLetter;

        public CharInfo(char letter, booleanean isBlankLetter)
        {
            Letter = letter;
            IsBlankLetter = isBlankLetter;
        }

        public CharInfo()
        {
            Letter = EMPTY_SPACE;
            IsBlankLetter = false;
        }

        public boolean IsEmptySpace()
        {
            return (Letter == EMPTY_SPACE);
        }

        public LetterLoc GenLetterLoc(int x, int y)
        {
            return new LetterLoc(Letter, x, y, IsBlankLetter);
        }

        public static CharInfo[][] ConvertBoardToCharInfoArr(char[][] board)
        {
            CharInfo[][] infoBoard = new CharInfo[GameVals.BOARD_SIZE][GameVals.BOARD_SIZE];

            for (int i = 0; i < GameVals.BOARD_SIZE; i++)
            {
                for (int j = 0; j < GameVals.BOARD_SIZE; j++)
                {
                    infoBoard[i][j] = new CharInfo(board[i][j], false);
                }
            }

            return infoBoard;            
        }
    }

