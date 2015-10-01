package com.rkitmedia.rkimage;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by keckardt on 01.10.2015.
 *
 * @author keckardt
 */
public class SquareImageView extends AspectRatioImageView {

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setAspectRatio(1);
        super.setAspectRatioEnabled(true);
    }

    @Override
    /** @hide this method is not used with square aspect ratio*/
    public void setAspectRatio(float aspectRatio) {
    }

    @Override
    /** @hide this method is not used with square aspect ratio*/
    public void setAspectRatioEnabled(boolean aspectRatioEnabled) {
    }
}
