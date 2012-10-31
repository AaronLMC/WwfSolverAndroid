package com.almc.wwfsolver.ocr;

import java.util.Arrays;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;

import com.almc.wwfsolver.Board;
import com.almc.wwfsolver.GameVals;

public class ImgParser
{
	//start of board in screenshot
	private static final int BOARD_X_START = 0; 
	private static final int BOARD_Y_START = 243;
	
	//start of available letters in screenshot
	private static final int[] AVAIL_X_START = new int[] {9, 111, 212, 313, 414, 516, 617};
	private static final int AVAIL_Y_START = 1085;
	
	private static final int LETTER_COLOR = 0xff4e1c00;
	private static final int LETTER_COLOR_2 = 0xffffffff;
	
	private static final int LETTER_BACKGROUND_COLOR = 0xfff4c13f;
	
	//dimensions of the letter location within a piece
	private static final int BRD_PIECE_SIZE = 48;
	private static final int BRD_LETTER_START_X = 1;
	private static final int BRD_LETTER_START_Y = 10;
	private static final int BRD_LETTER_SIZE = 35;
	
	//dimensions of the letter location within a piece
	
	private static final int AVL_PIECE_SIZE = 95;
	private static final double _BRD_TO_AVL_CONV = (double)AVL_PIECE_SIZE / BRD_PIECE_SIZE;
	private static final int AVL_LETTER_START_X = (int)(BRD_LETTER_START_X * _BRD_TO_AVL_CONV);
	private static final int AVL_LETTER_START_Y = (int)(BRD_LETTER_START_Y * _BRD_TO_AVL_CONV);
	private static final int AVL_LETTER_SIZE = (int)(BRD_LETTER_SIZE * _BRD_TO_AVL_CONV);
	
	private Board mBoard;
	
	public ImgParser(String imgFile)
	{
		Bitmap bmp = BitmapFactory.decodeFile(imgFile);
		parseBitmap(bmp);
	}
	
	private void parseBitmap(Bitmap bmp)
	{
		
		//get board pieces
		char[][] boardPieces = new char[GameVals.BOARD_SIZE][GameVals.BOARD_SIZE];		
		int[] pixels = new int[BRD_LETTER_SIZE * BRD_LETTER_SIZE];
		for(int i = 0; i < GameVals.BOARD_SIZE; i++)
		{
			for(int j = 0; j < GameVals.BOARD_SIZE; j++)
			{				
				int x = BOARD_X_START + BRD_PIECE_SIZE * j + BRD_LETTER_START_X;
				int y = BOARD_Y_START + BRD_PIECE_SIZE * i + BRD_LETTER_START_Y;				
				
				bmp.getPixels(pixels, 0, BRD_LETTER_SIZE, x, y, BRD_LETTER_SIZE, BRD_LETTER_SIZE);
				
				boardPieces[i][j] = getCharFromPieceImg(pixels, BRD_LETTER_SIZE);
				
			}
		}
		
		//get available letters
		String availLetters = "";
		pixels = new int[AVL_LETTER_SIZE * AVL_LETTER_SIZE];
		for(int k = 0; k < GameVals.AVAILABLE_LETTER_MAX; k++)
		{
			int x = AVAIL_X_START[k] + AVL_LETTER_START_X;
			int y = AVAIL_Y_START + AVL_LETTER_START_Y;
			
			bmp.getPixels(pixels, 0, AVL_LETTER_SIZE, x, y, AVL_LETTER_SIZE, AVL_LETTER_SIZE);
			
			char letter = getCharFromPieceImg(pixels, AVL_LETTER_SIZE);
			if (letter != ' ')
			{
				availLetters += letter;
			}
			
		}
		
		mBoard = new Board(boardPieces, availLetters.toCharArray());
	}
	
	private char getCharFromPieceImg(int[] pixels, int letterSize)
	{
		//count the number of letter-colored pixels in each row and column
		int[] rowBuckets = new int[letterSize];
		Arrays.fill(rowBuckets, 0);
		
		int[] colBuckets = new int[letterSize];
		Arrays.fill(colBuckets, 0);
		
		int numLetterPixels = 0;
		int numLetterBackgroundPixels = 0;
		
		for (int i = 0; i < letterSize; i++)
		{
			for (int j = 0; j < letterSize; j++)
			{
				int p = pixels[i*letterSize + j];
				if (p == LETTER_COLOR
					|| p == LETTER_COLOR_2)
				{
					rowBuckets[i]++;
					colBuckets[j]++;
					numLetterPixels++;
				}
				else if (p == LETTER_BACKGROUND_COLOR)
				{
					numLetterBackgroundPixels++;
				}
			}
		}
		
		//get ratio between width and height and sum them
		double ratioSum = 0;
		for(int k = 0; k < letterSize; k++)
		{
			double ratio;
			if (colBuckets[k] != 0)
			{
				ratio = (double)rowBuckets[k] / colBuckets[k];
			}
			else
			{
				ratio = -1;
			}
			
			ratioSum += ratio;
		}
		
		double ratioAvg = ratioSum / letterSize;
		
		//int letterKey = (int)(ratioSum * 10000);
		int letterKeyAvg = (int)(ratioAvg * 10000);
		
		
		Character c = CHAR_MAP_AVG.get(letterKeyAvg);
		if (c != null)
		{
			return c;
		}
		else if (numLetterPixels == 0 && numLetterBackgroundPixels > 10)
		{
			return '_';//blank letter
		}
		else
		{
			return ' ';
		}
	}
	
	private static final SparseArray<Character> CHAR_MAP_AVG = new SparseArray<Character>();
	static
	{
		CHAR_MAP_AVG.put(141726 / BRD_LETTER_SIZE, 'A');
		CHAR_MAP_AVG.put(42891 / BRD_LETTER_SIZE, 'B');
		CHAR_MAP_AVG.put(162832 / BRD_LETTER_SIZE, 'C');
		CHAR_MAP_AVG.put(95506 / BRD_LETTER_SIZE, 'D');
		CHAR_MAP_AVG.put(3474 / BRD_LETTER_SIZE, 'E');
		CHAR_MAP_AVG.put(-11428 / BRD_LETTER_SIZE, 'F');
		CHAR_MAP_AVG.put(175791 / BRD_LETTER_SIZE, 'G');
		CHAR_MAP_AVG.put(368214 / BRD_LETTER_SIZE, 'H');
		CHAR_MAP_AVG.put(-291071 / BRD_LETTER_SIZE, 'I');
		CHAR_MAP_AVG.put(-17843 / BRD_LETTER_SIZE, 'J');
		CHAR_MAP_AVG.put(203622 / BRD_LETTER_SIZE, 'K');
		CHAR_MAP_AVG.put(128928 / BRD_LETTER_SIZE, 'L');
		CHAR_MAP_AVG.put(351609 / BRD_LETTER_SIZE, 'M');
		CHAR_MAP_AVG.put(214603 / BRD_LETTER_SIZE, 'N');
		CHAR_MAP_AVG.put(174773 / BRD_LETTER_SIZE, 'O');
		CHAR_MAP_AVG.put(51166 / BRD_LETTER_SIZE, 'P');
		CHAR_MAP_AVG.put(175251 / BRD_LETTER_SIZE, 'Q');
		CHAR_MAP_AVG.put(106298 / BRD_LETTER_SIZE, 'R');
		CHAR_MAP_AVG.put(19849 / BRD_LETTER_SIZE, 'S');
		CHAR_MAP_AVG.put(68928 / BRD_LETTER_SIZE, 'T');
		CHAR_MAP_AVG.put(296753 / BRD_LETTER_SIZE, 'U');
		CHAR_MAP_AVG.put(106658 / BRD_LETTER_SIZE, 'V');
		CHAR_MAP_AVG.put(335116 / BRD_LETTER_SIZE, 'W');
		CHAR_MAP_AVG.put(247642 / BRD_LETTER_SIZE, 'X');
		CHAR_MAP_AVG.put(153008 / BRD_LETTER_SIZE, 'Y');
		CHAR_MAP_AVG.put(56334 / BRD_LETTER_SIZE, 'Z');
	}
	
	private static final SparseArray<Character> CHAR_MAP = new SparseArray<Character>();
	static
	{
		CHAR_MAP.put(141726, 'A');
		CHAR_MAP.put(42891, 'B');
		CHAR_MAP.put(162832, 'C');
		CHAR_MAP.put(95506, 'D');
		CHAR_MAP.put(3474, 'E');
		CHAR_MAP.put(-11428, 'F');
		CHAR_MAP.put(175791, 'G');
		CHAR_MAP.put(368214, 'H');
		CHAR_MAP.put(-291071, 'I');
		CHAR_MAP.put(-17843, 'J');
		CHAR_MAP.put(203622, 'K');
		CHAR_MAP.put(128928, 'L');
		CHAR_MAP.put(351609, 'M');
		CHAR_MAP.put(214603, 'N');
		CHAR_MAP.put(174773, 'O');
		CHAR_MAP.put(51166, 'P');
		CHAR_MAP.put(175251, 'Q');
		CHAR_MAP.put(106298, 'R');
		CHAR_MAP.put(19849, 'S');
		CHAR_MAP.put(68928, 'T');
		CHAR_MAP.put(296753, 'U');
		CHAR_MAP.put(106658, 'V');
		CHAR_MAP.put(335116, 'W');
		CHAR_MAP.put(247642, 'X');
		CHAR_MAP.put(153008, 'Y');
		CHAR_MAP.put(56334, 'Z');
	}
	
	
	/**
	 * Generate demo board
	 */
	public ImgParser()
	{		
		char[] availableLetters = _demoAvailableLetters.toCharArray();
		mBoard = new Board(_demoGameBoard, availableLetters);		
	}
	
	public Board getBoard()
	{
		return mBoard;
	}
	
    private String _demoAvailableLetters = "DGYODDT";
    private char[][] _demoGameBoard = {
        {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
        {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
        {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
        {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
        {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'T'},
        {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'S', 'I'},
        {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'N', ' ', ' ', ' ', 'U', 'P'},
        {' ', ' ', ' ', ' ', ' ', ' ', ' ', 'B', 'R', 'U', 'I', 'T', ' ', 'R', ' '},
        {' ', ' ', ' ', ' ', ' ', ' ', ' ', 'O', ' ', 'M', ' ', ' ', ' ', 'E', ' '},
        {' ', ' ', ' ', ' ', ' ', ' ', ' ', 'N', ' ', 'B', 'R', 'A', 'W', 'L', 'S'},
        {' ', ' ', ' ', ' ', 'L', ' ', 'H', 'E', 'H', 'S', ' ', ' ', ' ', 'Y', 'O'},
        {' ', ' ', ' ', 'Z', 'O', 'O', 'I', 'D', ' ', ' ', ' ', ' ', 'N', ' ', 'C'},
        {' ', ' ', ' ', ' ', 'P', ' ', 'D', ' ', ' ', 'R', ' ', 'L', 'I', 'N', 'K'},
        {' ', ' ', ' ', ' ', ' ', 'V', 'E', 'G', ' ', 'E', ' ', 'E', 'X', ' ', ' '},
        {' ', ' ', ' ', ' ', ' ', ' ', ' ', 'I', 'F', 'S', ' ', 'V', ' ', ' ', ' '},
    };
}
