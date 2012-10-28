package com.almc.wwfsolver;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class WwfSolverActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wwf_solver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_wwf_solver, menu);
        return true;
    }
}
