package com.almc.wwfsolver.trainer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;


public class WwfOcrModelTrainier
{
	public static void main(String[] args)
	{
		new WwfOcrModelTrainier(args[0], args[1], args[2]);		
	}

	public WwfOcrModelTrainier(String imgFilename, String annoFilename, String outModelsFilename)
	{
		WwfOcrModel models = new WwfOcrModel();
		
		TrainingImgParser img = new TrainingImgParser(imgFilename);
		FeatureVector[][] boardVectors = img.getBoardPieces();
		FeatureVector[] availVectors = img.getAvailableLetters();
		
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(new File(annoFilename)));
			String line;
			int i = 0; //line index
			while((line = br.readLine())!= null)
			{
				line = line.toUpperCase();
				
				if (i < TrainingImgParser.NUM_BOARD_TILES)
				{
					for (int j = 0; j < TrainingImgParser.NUM_BOARD_TILES; j++)
					{
						char c = line.charAt(j);
						if (c != ' ')
						{
							models.addFeatureVector(boardVectors[i][j], c);
						}
					}
				}
				else
				{
					//assume to be available letters
					char[] availLetters = line.toCharArray();
					for(int k = 0; k < availLetters.length; k++)
					{
						models.addFeatureVector(availVectors[k], availLetters[k]);
					}
				}
				
				i++;
			}
			
			br.close();
			
			String serializedModels = models.serialize();
			BufferedWriter bw = new BufferedWriter(new FileWriter(outModelsFilename, false));
			bw.write(serializedModels);
			bw.close();
		}
		catch (Exception e)
		{
			System.err.println("Error training");
			e.printStackTrace();
			return;
		}
		
		
	}
}
