package com.almc.wwfsolver;

import java.util.HashSet;
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
            mAvailableLetters = boardConfig.getAvailableLetters();
        }

        public Vector<WordSolution> GetSolutions()
        {
            mMaxScoreFound = 0;
            Vector<WordSolution> solutions = new Vector<WordSolution>();

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
                HashSet<WordSolution> words = SolutionSearch(GameVals.BOARD_CENTER_LOC, GameVals.BOARD_CENTER_LOC, 0);
                mFrontEnd.SetLocationColorToDefault(GameVals.BOARD_CENTER_LOC, GameVals.BOARD_CENTER_LOC);

                for (WordSolution w : words)
                {
                    if (!solutions.contains(w))
                    {
                        solutions.add(w);
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
                        HashSet<WordSolution> words = SolutionSearch(i, j, 0);
                        mFrontEnd.SetLocationColorToDefault(i, j);

                        for (WordSolution w : words)
                        {
                            if (!solutions.contains(w))
                            {
                                solutions.add(w);
                            }
                        }
                    }
                }
            }

            //TODO: report this value as solutions are found
            mFrontEnd.SetNumSolutionsFound(solutions.size());

            return solutions;
        }

        private HashSet<WordSolution> SolutionSearch(int x, int y, int depth)
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

            HashSet<WordSolution> solutions = new HashSet<WordSolution>();            
            
            //cycle through all starting idx so that we exercise all letter combinations
            for (int letterIdx = 0; letterIdx < mAvailableLetters.length; letterIdx++)
            {
                if (mUsedLetterIdxs.contains(letterIdx))
                {
                    continue;
                }

                //we have a letter and a location to try out
                char letter = mAvailableLetters[letterIdx];

                if (letter == GameVals.BLANK_TILE)
                {
                    //If tile is blank, cycle through all letters
                    for (int i = 0; i < GameVals.ALPHABET.length; i++)
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

        private void _evaluateLetter(char letter, boolean isBlankLetter, int letterIdx, int x, int y, HashSet<WordSolution> solutions, int depth)
        {
            mBoardLetters[x][y] = new CharInfo(letter, isBlankLetter);

            LetterLoc curLetterLoc = new LetterLoc(letter, x, y, isBlankLetter);
            mCurrentWord.add(curLetterLoc);
            mUsedLetterIdxs.add(letterIdx);

            //get score of current turn
            WordSet wordSet = GetWordHashSet();
            HashSet<WordLocation> wordHashSet = wordSet.GetFullHashSet();
            HashSet<WordLocation> out_illegalWords = new HashSet<WordLocation>();
            int score = GetWordScore(wordHashSet, mCurrentWord, out_illegalWords);
            if (score > 0)
            {
                //store this move as one that has scores
                LetterLoc[] letterArr = new LetterLoc[mCurrentWord.size()];
                mCurrentWord.copyInto(letterArr);
                WordSolution solution = new WordSolution(letterArr, wordHashSet, score);
                solutions.add(solution);
            }

            //see if we should continue down this path
            if (score < 0
                && AreIncidentalWordsIllegal(out_illegalWords, wordSet))
            {
                //an incidental word is illegal.  Adding new tiles won't help with this so we are done
                clearCandidateLetterFromBoard(x, y, letterIdx, curLetterLoc);
                return;
            }
            else if (wordSet.getPrimaryWord() != null
                && mWordDict.IsDeadWord(wordSet.getPrimaryWord().getWordText()))
            {
                //there are no words that contain our primary word, so stop
                clearCandidateLetterFromBoard(x, y, letterIdx, curLetterLoc);
                return;
            }

            //Look for next word
            if (wordSet.getOrientation() == WordOrientation.SINGLE_TILE)
            {
                //go in all 4 directions

                int y_next = NextTileUp(x, y);
                if (y_next >= 0)
                {
                    HashSet<WordSolution> words = SolutionSearch(x, y_next, depth + 1);
                    solutions.addAll(words);
                }

                y_next = NextTileDown(x, y);
                if (y_next >= 0)
                {
                    HashSet<WordSolution> words = SolutionSearch(x, y_next, depth + 1);
                    solutions.addAll(words);
                }

                int x_next = NextTileLeft(x, y);
                if (x_next >= 0)
                {
                    HashSet<WordSolution> words = SolutionSearch(x_next, y, depth + 1);
                    solutions.addAll(words);
                }

                x_next = NextTileRight(x, y);
                if (x_next >= 0)
                {
                    HashSet<WordSolution> words = SolutionSearch(x_next, y, depth + 1);
                    solutions.addAll(words);
                }
            }
            else if (wordSet.getOrientation() == WordOrientation.HORIZONTAL)
            {
                //go top and bottom
                int y_next = NextTileUp(x, y);
                if (y_next >= 0)
                {
                    HashSet<WordSolution> words = SolutionSearch(x, y_next, depth + 1);
                    solutions.addAll(words);
                }

                y_next = NextTileDown(x, y);
                if (y_next >= 0)
                {
                    HashSet<WordSolution> words = SolutionSearch(x, y_next, depth + 1);
                    solutions.addAll(words);
                }
            }
            else
            {
                //go left and right
                int x_next = NextTileLeft(x, y);
                if (x_next >= 0)
                {
                    HashSet<WordSolution> words = SolutionSearch(x_next, y, depth + 1);
                    solutions.addAll(words);
                }

                x_next = NextTileRight(x, y);
                if (x_next >= 0)
                {
                    HashSet<WordSolution> words = SolutionSearch(x_next, y, depth + 1);
                    solutions.addAll(words);
                }
            }

            //clear the location we are looking at
            clearCandidateLetterFromBoard(x, y, letterIdx, curLetterLoc);
        }

        private boolean AreIncidentalWordsIllegal(HashSet<WordLocation> illegalWords, WordSet wordSet)
        {
            for(WordLocation illegalWord : illegalWords)
            {
                if (wordSet.getIncidentalWords().contains(illegalWord))
                {
                    return true;
                }                
            }

            return false;
        }

        private void clearCandidateLetterFromBoard(int x, int y, int letterIdx, LetterLoc letterLoc)
        {
            mBoardLetters[x][y] = new CharInfo();
            mUsedLetterIdxs.removeElement(letterIdx);
            mCurrentWord.removeElement(letterLoc);
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

        private int GetWordScore(HashSet<WordLocation> wordHashSet, Vector<LetterLoc> playedTiles, HashSet<WordLocation> out_illegalWords)
        {
            int score = 0;

            //first check for illegal words
            for (WordLocation word : wordHashSet)
            {                
                if (!mWordDict.IsWordInList(word.getWordText()))
                {
                	out_illegalWords.add(word);
                }
            }
            if (out_illegalWords.size() > 0)
            {
                return -1;
            }

            for (WordLocation word : wordHashSet)
            {
                //score the word
                int wordScore = 0;
                int multipliers = 1;
                for (LetterLoc letter : word.getLetters())
                {
                    //blank letters get no points
                    if (letter.IsBlankLetter)
                    {
                        continue;
                    }

                    int letterScore = GameVals.LETTER_SCORE.get(letter.Letter);

                    //apply letter bonus if this is a played tile
                    if (playedTiles.contains(letter))
                    {
                        GameVals.Bonus letterBonus = GameVals.BONUS_TILES[letter.X][letter.Y];
                        switch (letterBonus)
                        {
                        case D_LT:
                            letterScore *= 2;
                            break;
                        case T_LT:
                            letterScore *= 3;
                            break;
                        case D_WD:
                            multipliers *= 2;
                            break;
                        case T_WD:
                            multipliers *= 3;
                            break;
						default:
							break;
                        }
                    }

                    wordScore += letterScore;
                }
                wordScore *= multipliers;

                score += wordScore;
            }

            if (playedTiles.size() == 7)
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

        private WordSet GetWordHashSet()
        { 
            //determine orientation of played tiles
            WordOrientation orientation;
            int lastWordIdx = mCurrentWord.size() - 1;
            int startX = mCurrentWord.get(0).X;
            //int startY = mCurrentWord.get(0).Y;
            int endX = mCurrentWord.get(lastWordIdx).X;
            //int endY = mCurrentWord.get(lastWordIdx).Y;

            if (mCurrentWord.size() == 1)
                orientation = WordOrientation.SINGLE_TILE;
            else if (startX != endX)
                orientation = WordOrientation.VERTICAL;
            else
                orientation = WordOrientation.HORIZONTAL;
            
            //Initialize word set
            WordSet wordSet = new WordSet(orientation);

            //for each letter in the proposed word, add the score of any new words generated by that letter
            for (LetterLoc letter : mCurrentWord)
            {
                //evaluate in X direction
                int initX = letter.X;
                int minX = initX;
                for (int i = initX; i >= 0; i--)
                {
                    if (mBoardLetters[i][letter.Y].IsEmptySpace())
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
                    if (mBoardLetters[i][letter.Y].IsEmptySpace())
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
//                    String word = "";
//                    for (int i = minX; i <= maxX; i++)
//                    {
//                        word += mBoardLetters[i][letter.Y];
//                    }

                    Vector<LetterLoc> letters = new Vector<LetterLoc>();
                    for (int i = minX; i <= maxX; i++)
                    {
                        letters.add(mBoardLetters[i][letter.Y].GenLetterLoc(i, letter.Y));
                    }

                    if (orientation == WordOrientation.VERTICAL)
                        wordSet.setPrimaryWord(new WordLocation(letters));
                    else
                        wordSet.AddIncidentalWord(new WordLocation(letters));
                }

                //evaluate in Y direction
                int initY = letter.Y;
                int minY = initY;
                for (int j = initY; j >= 0; j--)
                {
                    if (mBoardLetters[letter.X][j].IsEmptySpace())
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
                    if (mBoardLetters[letter.X][j].IsEmptySpace())
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
                	Vector<LetterLoc> letters = new Vector<LetterLoc>();
                    for (int j = minY; j <= maxY; j++)
                    {
                        letters.add(mBoardLetters[letter.X][j].GenLetterLoc(letter.X, j));
                    }

                    if (orientation == WordOrientation.HORIZONTAL)
                        wordSet.setPrimaryWord(new WordLocation(letters));
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
            if (mCurrentWord.size() > 0)
            {
                int initX = mCurrentWord.get(0).X;
                int initY = mCurrentWord.get(0).Y;

                //location is not in line with existing current word
                if (x != initX && y != initY)
                {
                    return false;
                }

                if (x == initX)
                {
                    //make sure there is a letter between InitX and X
                    int smallerX = Math.min(x, initX);
                    int largerX = Math.max(x, initX);
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
                    int smallerY = Math.min(y, initY);
                    int largerY = Math.max(y, initY);
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
            if (!mBoardLetters[x][y].IsEmptySpace())
            {
                return true;
            }

            return false;
        }
    }



    enum WordOrientation
    {
        SINGLE_TILE,
        VERTICAL,
        HORIZONTAL
    }

    class CharInfo
    {
        public final char EMPTY_SPACE = ' ';

        public final char Letter;
        public final boolean IsBlankLetter;

        public CharInfo(char letter, boolean isBlankLetter)
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

