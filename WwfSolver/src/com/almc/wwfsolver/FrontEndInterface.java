package com.almc.wwfsolver;

public interface FrontEndInterface
{
	void SetSearchStartLocation(int x, int y);

	void SetSearchRecurLocation(int x, int y);

	void SetLocationColorToDefault(int x, int y);

	void SetNumWordsEvaluated(int numWords);

	void SetNumSolutionsFound(int numSolutions);

	void SetHighestScoreFound(int maxScore);

}
