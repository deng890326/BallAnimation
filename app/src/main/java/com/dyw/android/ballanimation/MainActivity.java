package com.dyw.android.ballanimation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    private BallView mBallView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBallView = (BallView) findViewById(R.id.ballView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBallView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBallView.startAnimation();
            }
        }, 500);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBallView.stopAnimation();
    }
}
