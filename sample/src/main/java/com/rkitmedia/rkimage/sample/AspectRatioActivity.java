package com.rkitmedia.rkimage.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.SeekBar;

/**
 * Created by keckardt on 03.10.2015.
 *
 * @author keckardt
 */
public class AspectRatioActivity extends AppCompatActivity {

    private SeekBar seekBar;
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ratio_activity);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.parallax:
                Intent intent = new Intent(this, ParallaxActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void resizeLayout(int progress) {
        layout.getLayoutParams().width = Math.round((float) progress / seekBar.getMax() * seekBar.getWidth());
        layout.requestLayout();
    }
}
