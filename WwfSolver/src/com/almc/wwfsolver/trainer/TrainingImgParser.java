package com.almc.wwfsolver.trainer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;



public class TrainingImgParser
{
	
	public static final int NUM_BOARD_TILES = 15;
	public static final int AVAILABLE_LETTER_MAX = 7;
	
	//start of board in screenshot
	private static final int BOARD_X_START = 0; 
	private static final int BOARD_Y_START = 243;
	
	//start of available letters in screenshot
	private static final int[] AVAIL_X_START = new int[] {9, 111, 212, 313, 414, 516, 617};
	private static final int AVAIL_Y_START = 1085;
		
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
	
	
	private FeatureVector[][] mBoardPieces;
	private FeatureVector[] mAvailableLetters;
	
	public TrainingImgParser(String imgFile)
	{		
		Bitmap bmp = BitmapFactory.decodeFile(imgFile);
		parseBitmap(bmp);
	}
	
	public FeatureVector[][] getBoardPieces()
	{
		return mBoardPieces;
	}
	
	public FeatureVector[] getAvailableLetters()
	{
		return mAvailableLetters;
	}
	
	private void parseBitmap(Bitmap bmp)
	{
		
		//get board pieces
		mBoardPieces = new FeatureVector[NUM_BOARD_TILES][NUM_BOARD_TILES];		
		int[] pixels = new int[BRD_LETTER_SIZE * BRD_LETTER_SIZE];
		for(int i = 0; i < NUM_BOARD_TILES; i++)
		{
			for(int j = 0; j < NUM_BOARD_TILES; j++)
			{				
				int x = BOARD_X_START + BRD_PIECE_SIZE * j + BRD_LETTER_START_X;
				int y = BOARD_Y_START + BRD_PIECE_SIZE * i + BRD_LETTER_START_Y;				
				
				bmp.getPixels(pixels, 0, BRD_LETTER_SIZE, x, y, BRD_LETTER_SIZE, BRD_LETTER_SIZE);
				
				mBoardPieces[i][j] = new FeatureVector(pixels, BRD_LETTER_SIZE);
				
			}
		}
		
		//get available letters
		mAvailableLetters = new FeatureVector[AVAILABLE_LETTER_MAX];
		pixels = new int[AVL_LETTER_SIZE * AVL_LETTER_SIZE];
		for(int k = 0; k < AVAILABLE_LETTER_MAX; k++)
		{
			int x = AVAIL_X_START[k] + AVL_LETTER_START_X;
			int y = AVAIL_Y_START + AVL_LETTER_START_Y;
			
			bmp.getPixels(pixels, 0, AVL_LETTER_SIZE, x, y, AVL_LETTER_SIZE, AVL_LETTER_SIZE);
			
			mAvailableLetters[k] = new FeatureVector(pixels, AVL_LETTER_SIZE);
		}
	}

}
