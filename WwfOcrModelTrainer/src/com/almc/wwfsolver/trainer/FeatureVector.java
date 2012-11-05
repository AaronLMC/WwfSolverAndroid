package com.almc.wwfsolver.trainer;

import java.util.Arrays;


public class FeatureVector
{
	//Approach: reduce letter box to bins and calculate ratio of width to height pixels in each bin
	//Feature: y1/x1, y2/x1, y3/x1, y1/x2, y2/x2, y3/x2, y1/x3, y2/x3, y3/x3, w/h
	
	private static final int LETTER_COLOR = 0xff4e1c00;
	private static final int LETTER_COLOR_2 = 0xffffffff;
	
	private static final int LETTER_BACKGROUND_COLOR = 0xfff4c13f;
	
	private static final int DBL_TO_INT_MULTIPLIER = 1000;
	
	private static final int NUM_BINS = 3; //number of bins to divide letter into
	
	private int[][] m_binRatios = new int[NUM_BINS][NUM_BINS]; //ratio of row/col pixes in each bin
	private int m_letterRatio = -1; //overall ratio of width to height of letter
	
	private boolean m_letterNotFound = false;
	private boolean m_isBlankLetter = false;
	
	private FeatureVector(){} //for de-serialization
	
	public FeatureVector(int[] pixels, int imgSize)
	{
		//count the number of letter-colored pixels in each row and column
		int[] rowBuckets = new int[imgSize];
		Arrays.fill(rowBuckets, 0);
		
		int[] colBuckets = new int[imgSize];
		Arrays.fill(colBuckets, 0);
		
		int numLetterPixels = 0;
		int numLetterBackgroundPixels = 0;
		
		for (int i = 0; i < imgSize; i++)
		{
			for (int j = 0; j < imgSize; j++)
			{
				int p = pixels[i*imgSize + j];
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
		
		if (numLetterPixels == 0 && numLetterBackgroundPixels > 10)
		{
			//we saw letter background but no letter text. There is a background letter
			m_isBlankLetter = true;
			return;
		}
		
		//
		//get overall ratio between width and height of letter pixels
		//
		
		//get start/stop indices for row/col buckets
		int rowStart = -1;
		int rowEnd = -1;		
		for(int i = 0; i < rowBuckets.length; i++)
		{
			if (rowBuckets[i] > 0)
			{
				if (rowStart == -1) rowStart = i;
				
				rowEnd = i;
			}
		}
		
		int colStart = -1;
		int colEnd = -1;		
		for(int i = 0; i < colBuckets.length; i++)
		{
			if (colBuckets[i] > 0)
			{
				if (colStart == -1) colStart = i;
				
				colEnd = i;
			}
		}
		
		//Check for valid values
		if (rowStart == -1 || colStart == -1)
		{
			System.err.println("Column or row start not found");
			m_letterNotFound = true;
			return;
		}
		
		int rowSize = rowEnd - rowStart;
		int colSize = colEnd - colStart;		
		
		double letterRatioDbl = (double)rowSize/colSize;
		m_letterRatio = (int)(letterRatioDbl * DBL_TO_INT_MULTIPLIER);
		
		//
		// Get ratios for bins
		//
		int binSize = imgSize / NUM_BINS;
		if (binSize == 0)
		{
			System.err.println("Too many bins - size of each bin is 0");
			m_letterNotFound = true;
			return;
		}
		
		for (int iBin = 0; iBin < NUM_BINS; iBin++)
		{
			for (int jBin = 0; jBin < NUM_BINS; jBin++)
			{
				double binRatioSum = 0;
				
				for(int k = 0; k < binSize; k++)
				{
						int x = k + jBin * binSize;
						int y = k + iBin * binSize;
						
						double ratio;
						if (colBuckets[x] != 0)
						{
							ratio = (double)rowBuckets[y] / colBuckets[x];
						}
						else
						{
							ratio = -1;
						}
						
						binRatioSum += ratio;
						
				}
				
				double binRatioAvg = binRatioSum / binSize;
				
				m_binRatios[iBin][jBin] = (int)(binRatioAvg * DBL_TO_INT_MULTIPLIER);
			}
		}
		
		
	}
	
	public boolean isUnknownLetter()
	{
		return m_letterNotFound;
	}
	
	public boolean isBlankLetter()
	{
		return m_isBlankLetter;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && obj instanceof FeatureVector)
		{
			FeatureVector other = (FeatureVector)obj;
			
			if (m_letterNotFound) return false; //undefined state
			
			if (m_isBlankLetter && other.m_isBlankLetter) return true;
			
			if (m_letterRatio != other.m_letterRatio) return false;
			
			for(int i = 0; i < NUM_BINS; i++)
			{
				for (int j = 0; j < NUM_BINS; j++)
				{
					if (m_binRatios[i][j] != other.m_binRatios[i][j]) return false;
				}
			}
			
			return true;
		}
		else
		{
			return super.equals(obj);
		}
	}
	
	public String serialize()
	{
		StringBuilder serializeStr = new StringBuilder();
		
		serializeStr.append(m_letterNotFound + ",");
		serializeStr.append(m_isBlankLetter + ",");
		
		serializeStr.append(m_letterRatio + ",");
		
		for(int i = 0; i < NUM_BINS; i++)
		{
			for (int j = 0; j < NUM_BINS; j++)
			{
				serializeStr.append(m_binRatios[i][j] + ",");
			}
		}		
		
		return serializeStr.substring(0, serializeStr.length() - 1); //trim last comma
	}
	
	public static FeatureVector deserialize(String serializedStr)
	{
		final int TOTAL_ITEMS = NUM_BINS * NUM_BINS + 3; //bins, letter ratio, letterFound, blankLetter
		
		String[] els = serializedStr.split(",");
		if (els.length != TOTAL_ITEMS)
		{
			System.err.println("Invalid FeatureVector serialization from string:\n\t" + serializedStr);
			return null;
		}
		
		try
		{
			FeatureVector vector = new FeatureVector();
			vector.m_letterNotFound = Boolean.parseBoolean(els[0]);
			vector.m_isBlankLetter = Boolean.parseBoolean(els[1]);
			vector.m_letterRatio = Integer.parseInt(els[2]);
			
			int elCursor = 3;
			for(int i = 0; i < NUM_BINS; i++)
			{
				for (int j = 0; j < NUM_BINS; j++)
				{
					vector.m_binRatios[i][j] = Integer.parseInt(els[elCursor]);
					elCursor++;
				}
			}
			
			return vector;
		} 
		catch (Exception e)
		{
			System.err.println("Error parsing serialized string as nummber:\n\t" + serializedStr);
			return null;
		}
	}
	
}
