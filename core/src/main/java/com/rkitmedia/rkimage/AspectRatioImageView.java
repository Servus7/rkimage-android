package com.rkitmedia.rkimage;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by keckardt on 01.10.2015.
 *
 * @author keckardt
 *         Maintains an aspect ratio based on either width or height. Disabled by default.
 */
public class AspectRatioImageView extends ImageView {
    public static final int MEASUREMENT_WIDTH = 0;
    public static final int MEASUREMENT_HEIGHT = 1;

    private static final int DEFAULT_DOMINANT_MEASUREMENT = MEASUREMENT_WIDTH;
    private static final boolean DEFAULT_ASPECT_RATIO_ENABLED = true;
    private static final float DEFAULT_ASPECT_RATIO = 1f;

    private boolean aspectRatioEnabled;
    private int dominantMeasurement;
    private float aspectRatio;
    private boolean attachedToWindow;

    public AspectRatioImageView(Context context) {
        super(context);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        checkAttributes(attrs);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        checkAttributes(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AspectRatioImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        checkAttributes(attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        attachedToWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        attachedToWindow = false;
    }

    @Override
    @CallSuper
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (!aspectRatioEnabled) {
            return;
        }

        int newWidth;
        int newHeight;

        switch (dominantMeasurement) {
            case MEASUREMENT_WIDTH:
                newWidth = getMeasuredWidth();
                newHeight = Math.round((float) newWidth / aspectRatio);
                break;

            case MEASUREMENT_HEIGHT:
                newHeight = getMeasuredHeight();
                newWidth = Math.round((float) newHeight * aspectRatio);
                break;

            default:
                throw new IllegalStateException("Unknown measurement with ID " + dominantMeasurement);
        }

        setMeasuredDimension(newWidth, newHeight);
    }

    @CallSuper
    protected void checkAttributes(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.AspectRatioImageView);

        setAspectRatio(a.getFloat(R.styleable.AspectRatioImageView_rk_aspectRatio, DEFAULT_ASPECT_RATIO));
        setAspectRatioEnabled(a.getBoolean(R.styleable.AspectRatioImageView_rk_aspectRatioEnabled, DEFAULT_ASPECT_RATIO_ENABLED));
        setDominantMeasurement(a.getInt(R.styleable.AspectRatioImageView_rk_dominantMeasurement, DEFAULT_DOMINANT_MEASUREMENT));

        a.recycle();
    }

    /**
     * Set the aspect ratio for this image view. This will update the view instantly.
     *
     * @param aspectRatio set aspect ratio
     */
    public void setAspectRatio(float aspectRatio) {
        if (aspectRatio <= 0) {
            throw new IllegalArgumentException("Invalid aspectRatio.");
        }

        this.aspectRatio = aspectRatio;

        if (aspectRatioEnabled) {
            requestLayout();
        }
    }

    /**
     * Set the aspect ratio for this image view. This will update the view instantly.
     *
     * @param aspectRatio set aspect ratio
     */
    public void setAspectRatio(double aspectRatio) {
        if (aspectRatio <= 0) {
            throw new IllegalArgumentException("Invalid aspectRatio.");
        }

        this.aspectRatio = (float) aspectRatio;

        if (aspectRatioEnabled) {
            requestLayout();
        }
    }

    /**
     * Get the aspect ratio for this image view.
     */
    public float getAspectRatio() {
        return aspectRatio;
    }

    /**
     * set whether or not the aspect ratio is enabled. This will re-layout the view.
     *
     * @param aspectRatioEnabled weather or not aspect ratio should be used
     */
    public void setAspectRatioEnabled(boolean aspectRatioEnabled) {
        this.aspectRatioEnabled = aspectRatioEnabled;

        if (aspectRatioEnabled) {
            requestLayout();
        }
    }

    /**
     * Get whether or not the aspect ratio is enabled.
     *
     * @return value of current aspect ratio usage
     */
    public boolean isAspectRatioEnabled() {
        return aspectRatioEnabled;
    }

    /**
     * Set the dominant measurement for the aspect ratio.
     *
     * @see #MEASUREMENT_WIDTH
     * @see #MEASUREMENT_HEIGHT
     */
    public void setDominantMeasurement(int dominantMeasurement) {
        if (dominantMeasurement != MEASUREMENT_HEIGHT && dominantMeasurement != MEASUREMENT_WIDTH) {
            throw new IllegalArgumentException("Invalid measurement type.");
        }

        this.dominantMeasurement = dominantMeasurement;

        if (aspectRatioEnabled) {
            requestLayout();
        }
    }

    /**
     * Get the dominant measurement for the aspect ratio.
     *
     * @return weather width or height is used as dominant measurement.
     */
    public int getDominantMeasurement() {
        return dominantMeasurement;
    }

    @Override
    public boolean isAttachedToWindow() {
        return attachedToWindow;
    }
}
