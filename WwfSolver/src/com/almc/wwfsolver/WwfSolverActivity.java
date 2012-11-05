package com.almc.wwfsolver;

import java.io.InputStream;
import java.util.Vector;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.almc.wwfsolver.ocr.ImgParser;
import com.almc.wwfsolver.trainer.WwfOcrModel;

public class WwfSolverActivity extends Activity
{

	private GameSolver mGameSolver;
	private WordDict mWordDict;
	private WwfOcrModel mModels;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wwf_solver);
		
		Button goBtn = (Button)findViewById(R.id.btn_Go);
		
		goBtn.setOnClickListener(new Button.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				try
				{
					Log.v(WwfConstants.LOG_TAG, "Loading dictionary...");
					InputStream dictInputStream  = getAssets().open("wwfDict.txt");
					//mWordDict = new WordDict(dictInputStream);
					Log.v(WwfConstants.LOG_TAG, "Done loading dictionary");
				}
				catch (Exception e)
				{
					Log.e(WwfConstants.LOG_TAG, "Exception while trying to open dict file", e);
				}
				
				mModels = WwfOcrModel.deserialize("models.txt");
				
				ImgParser imgParser = new ImgParser("/sdcard/wwf_sample_image.bmp", mModels);//get demo board
				Board board = imgParser.getBoard();
				
				mGameSolver = new GameSolver(mSolverInferface, mWordDict, board);
				Vector<WordSolution> solutions = mGameSolver.GetSolutions();
				//TODO: sort solutions
				
				Log.v(WwfConstants.LOG_TAG, "Solutions:");
				for(WordSolution s : solutions)
				{
					Log.v(WwfConstants.LOG_TAG, "   " + s.toString());
				}
				
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_wwf_solver, menu);
		return true;
	}
	
	private FrontEndInterface mSolverInferface = new FrontEndInterface()
	{
		
		@Override
		public void SetSearchStartLocation(int x, int y)
		{
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void SetSearchRecurLocation(int x, int y)
		{
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void SetNumWordsEvaluated(int numWords)
		{
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void SetNumSolutionsFound(int numSolutions)
		{
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void SetLocationColorToDefault(int x, int y)
		{
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void SetHighestScoreFound(int maxScore)
		{
			// TODO Auto-generated method stub
			
		}
	};
	


}
