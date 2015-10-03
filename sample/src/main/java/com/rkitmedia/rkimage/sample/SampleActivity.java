package com.rkitmedia.rkimage.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.SeekBar;

public class SampleActivity extends AppCompatActivity {

    private SeekBar seekBar;
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_activity);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        layout = (LinearLayout) findViewById(R.id.linear);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                resizeLayout(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar.post(new Runnable() {

            @Override
            public void run() {
                resizeLayout(seekBar.getProgress());
            }
        });
    }

    private void resizeLayout(int progress) {
        layout.getLayoutParams().width = Math.round((float) progress / seekBar.getMax() * seekBar.getWidth());
        layout.requestLayout();
    }
}
