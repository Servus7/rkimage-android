package com.rkitmedia.rkimage;

import android.app.Application;
import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class AspectRatioImageViewTest extends ApplicationTestCase<Application> {
    public AspectRatioImageViewTest() {
        super(Application.class);
    }

    public void testGetSet() throws Exception {
        AspectRatioImageView imageView = new AspectRatioImageView(getContext());
        Throwable e;

        // set double value as aspect ratio
        imageView.setAspectRatio(1.3);
        assertEquals(imageView.getAspectRatio(), 1.3F);

        // set float value as aspect ratio
        imageView.setAspectRatio(1.3F);
        assertEquals(imageView.getAspectRatio(), 1.3F);

        // set int value as aspect ratio
        imageView.setAspectRatio(1);
        assertEquals(imageView.getAspectRatio(), 1.0F);

        // set invalid aspect ratio
        e = null;
        try {
            imageView.setAspectRatio(0);
        } catch (Exception ex) {
            e = ex;
        }
        assertTrue(e instanceof IllegalArgumentException);

        // set width as dominant measurement
        imageView.setDominantMeasurement(AspectRatioImageView.MEASUREMENT_WIDTH);
        assertEquals(imageView.getDominantMeasurement(), AspectRatioImageView.MEASUREMENT_WIDTH);

        // set height as dominant measurement
        imageView.setDominantMeasurement(AspectRatioImageView.MEASUREMENT_HEIGHT);
        assertEquals(imageView.getDominantMeasurement(), AspectRatioImageView.MEASUREMENT_HEIGHT);

        // set invalid dominant measurement
        e = null;
        try {
            imageView.setDominantMeasurement(-1);
        } catch (Exception ex) {
            e = ex;
        }
        assertTrue(e instanceof IllegalArgumentException);

        // enable aspect ratio
        imageView.setAspectRatioEnabled(true);
        assertEquals(imageView.isAspectRatioEnabled(), true);

        // disable aspect ratio
        imageView.setAspectRatioEnabled(false);
        assertEquals(imageView.isAspectRatioEnabled(), false);
    }
}