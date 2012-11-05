package com.almc.wwfsolver.trainer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

public class WwfOcrModel
{
	private HashMap<FeatureVector, Character> m_modelMap = new HashMap<FeatureVector, Character>();
	
	
	public WwfOcrModel()
	{
	}
	
	public void addFeatureVector(FeatureVector vector, char c)
	{
		if (vector.isUnknownLetter() || vector.isBlankLetter())
		{
			System.err.println("Attempted to add error or blank char feature vector to model map");
			return;
		}
		
		m_modelMap.put(vector, c);
	}
	
	/**
	 * Returns space character if unknown or error vector
	 */
	public char getLetter(FeatureVector vector)
	{
		if (vector.isUnknownLetter())
		{
			return ' ';			
		}
		
		if (vector.isBlankLetter())
		{
			return '_';
		}
		
		if (m_modelMap.containsKey(vector))
		{
			return m_modelMap.get(vector);
		}
		else
		{
			return ' ';
		}
	}
	
	public String serialize()
	{
		StringBuilder serializedStr = new StringBuilder();
		
		for(FeatureVector vector : m_modelMap.keySet())
		{
			char c = m_modelMap.get(vector);
			String serializedVector = vector.serialize();
			
			serializedStr.append(String.format("%s\t%s\r\n", c, serializedVector));
		}
		
		return serializedStr.toString();
	}
	
	static public WwfOcrModel deserialize(String modelFile)
	{
		WwfOcrModel model = new WwfOcrModel();
		
		BufferedReader br = null;
		String line = null;
		try
		{
			br = new BufferedReader(new FileReader(new File(modelFile)));			
			
			while ((line = br.readLine()) != null)
			{
				String els[] = line.split("\t");
				if (els.length != 2)
				{
					System.out.println("Ignoring model line: " + line);
					continue;
				}
				
				char c = els[0].charAt(0);
				
				FeatureVector vector = FeatureVector.deserialize(els[1]);
				
				if (vector == null)
				{
					System.err.println("Unable to parse feature vector for char: " + c);
					continue;
				}
				else if (vector.isBlankLetter() || vector.isUnknownLetter())
				{
					System.err.println("Read error or blank letter vector from model file. Line: " + line);
					continue;
				}
				
				model.addFeatureVector(vector, c);
			}
		}
		catch (Exception e)
		{
			System.err.println("Error parsing model filename");
			if (line != null)
			{
				System.err.println("Last read line: " + line);
			}
			return null;
		}
		finally
		{
			if (br != null)
			{
				try
				{
					br.close();
				}
				catch (Exception e){}
			}
		}
		
		return model;
	}
}
