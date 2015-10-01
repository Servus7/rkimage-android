package com.rkitmedia.rkimage;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Created by keckardt on 01.10.2015.
 *
 * @author keckardt
 */
public class ParallaxImageView extends AspectRatioImageView {

    public static final int REVERSE_NONE = 1;
    public static final int REVERSE_X = 2;
    public static final int REVERSE_Y = 3;
    public static final int REVERSE_BOTH = 4;

    public boolean reverseX = false;
    public boolean reverseY = false;
    public boolean updateOnDraw = false;
    public boolean blockParallaxX = false;
    public boolean blockParallaxY = false;

    private int screenWidth;
    private int screenHeight;
    private float scrollSpaceX = 0;
    private float scrollSpaceY = 0;
    private float heightImageView;
    private float widthImageView;

    private Interpolator interpolator = new LinearInterpolator();

    private ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener = null;
    //    private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = null;
    private ViewTreeObserver.OnDrawListener onDrawListener = null;

    public ParallaxImageView(Context context) {
        super(context);
        checkScale();
    }

    public ParallaxImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        checkScale();
    }

    public ParallaxImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        checkScale();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ParallaxImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        checkScale();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mOnScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                applyParallax();
            }
        };

//        mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                heightImageView = (float) getHeight();
//                widthImageView = (float) getWidth();
//
//                applyParallax();
//            }
//        };

        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        viewTreeObserver.addOnScrollChangedListener(mOnScrollChangedListener);
//        viewTreeObserver.addOnGlobalLayoutListener(mOnGlobalLayoutListener);

        if (updateOnDraw
                && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            onDrawListener = new ViewTreeObserver.OnDrawListener() {
                @Override
                public void onDraw() {
                    applyParallax();
                }
            };
            viewTreeObserver.addOnDrawListener(onDrawListener);
        }

        parallaxAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        viewTreeObserver.removeOnScrollChangedListener(mOnScrollChangedListener);

//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            viewTreeObserver.removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
//        } else {
//            viewTreeObserver.removeGlobalOnLayoutListener(mOnGlobalLayoutListener);
//        }

        if (updateOnDraw && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            viewTreeObserver.removeOnDrawListener(onDrawListener);
        }

        super.onDetachedFromWindow();
    }

    private boolean checkScale() {
        switch (getScaleType()) {
            case CENTER:
            case CENTER_CROP:
            case CENTER_INSIDE:
                return true;
            case FIT_CENTER:
                Log.d("ParallaxEverywhere", "Scale type fitCenter unsupported");
                break;
            case FIT_END:
                Log.d("ParallaxEverywhere", "Scale type fitEnd unsupported");
                break;
            case FIT_START:
                Log.d("ParallaxEverywhere", "Scale type fitStart unsupported");
                break;
            case FIT_XY:
                Log.d("ParallaxEverywhere", "Scale type fitXY unsupported");
                break;
            case MATRIX:
                Log.d("ParallaxEverywhere", "Scale type matrix unsupported");
                break;
        }
        return false;
    }

    @Override
    protected void checkAttributes(AttributeSet attrs) {
        super.checkAttributes(attrs);

        TypedArray arr = getContext().obtainStyledAttributes(attrs, R.styleable.ParallaxImageView);
        int reverse = arr.getInt(R.styleable.ParallaxImageView_reverse, 1);

        updateOnDraw = arr.getBoolean(R.styleable.ParallaxImageView_update_onDraw, false);

        blockParallaxX = arr.getBoolean(R.styleable.ParallaxImageView_block_parallax_x, false);
        blockParallaxY = arr.getBoolean(R.styleable.ParallaxImageView_block_parallax_y, false);

        reverseX = false;
        reverseY = false;
        switch (reverse) {
            case REVERSE_NONE:
                break;
            case REVERSE_X:
                reverseX = true;
                break;
            case REVERSE_Y:
                reverseY = true;
                break;
            case REVERSE_BOTH:
                reverseX = true;
                reverseY = true;
                break;
        }

        checkScale();

        int interpolationId = arr.getInt(R.styleable.ParallaxImageView_interpolation, 0);

        interpolator = InterpolatorSelector.interpolatorId(interpolationId);

        arr.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getDrawable() != null) {
            int dheight = getDrawable().getIntrinsicHeight();
            int dwidth = getDrawable().getIntrinsicWidth();
            int vheight = getMeasuredHeight();
            int vwidth = getMeasuredWidth();

            heightImageView = vwidth;
            widthImageView = vheight;

            float scale;

            float dnewHeight = 0;
            float dnewWidth = 0;

            switch (getScaleType()) {
                case CENTER_CROP:
                case CENTER:
                case CENTER_INSIDE:
                    if (dwidth * vheight > vwidth * dheight) {
                        scale = (float) vheight / (float) dheight;
                        dnewWidth = dwidth * scale;
                        dnewHeight = vheight;
                    } else {
                        scale = (float) vwidth / (float) dwidth;
                        dnewWidth = vwidth;
                        dnewHeight = dheight * scale;
                    }
                    break;
                case FIT_CENTER:
                case FIT_END:
                case FIT_START:
                case FIT_XY:
                case MATRIX:
                    break;
            }

            scrollSpaceY = (dnewHeight > vheight) ? (dnewHeight - vheight) : 0;
            scrollSpaceX = (dnewWidth > vwidth) ? (dnewWidth - vwidth) : 0;
        }

        applyParallax();
    }

    private void parallaxAnimation() {
        initSizeScreen();

        applyParallax();

    }

    private void initSizeScreen() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            display.getSize(size);
            screenHeight = size.y;
            screenWidth = size.x;
        } else {
            screenHeight = display.getHeight();
            screenWidth = display.getWidth();
        }
    }

    private void applyParallax() {
        int[] location = new int[2];
        getLocationOnScreen(location);

        if (scrollSpaceY != 0) {
            float locationY = (float) location[1];
            float locationUsableY = locationY + heightImageView / 2;
            float scrollDeltaY = locationUsableY / screenHeight;

            float interpolatedScrollDeltaY = interpolator.getInterpolation(scrollDeltaY);

            if (reverseY)
                setMyScrollY((int) (Math.min(Math.max((0.5f - interpolatedScrollDeltaY), -0.5f), 0.5f) * -scrollSpaceY));
            else
                setMyScrollY((int) (Math.min(Math.max((0.5f - interpolatedScrollDeltaY), -0.5f), 0.5f) * scrollSpaceY));
        }

        if (scrollSpaceX != 0) {
            float locationX = (float) location[0];
            float locationUsableX = locationX + widthImageView / 2;
            float scrollDeltaX = locationUsableX / screenWidth;

            float interpolatedScrollDeltaX = interpolator.getInterpolation(scrollDeltaX);

            if (reverseX) {
                setMyScrollX((int) (Math.min(Math.max((0.5f - interpolatedScrollDeltaX), -0.5f), 0.5f) * -scrollSpaceX));
            } else {
                setMyScrollX((int) (Math.min(Math.max((0.5f - interpolatedScrollDeltaX), -0.5f), 0.5f) * scrollSpaceX));
            }
        }
    }

    private void setMyScrollX(int value) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setScrollX(value);
        } else {
            scrollTo(value, getScrollY());
        }
    }

    private void setMyScrollY(int value) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setScrollY(value);
        } else {
            scrollTo(getScrollX(), value);
        }
    }

    public void setInterpolator(Interpolator interpol) {
        interpolator = interpol;
    }

    public boolean isReverseX() {
        return reverseX;
    }

    public void setReverseX(boolean reverseX) {
        this.reverseX = reverseX;
    }

    public boolean isReverseY() {
        return reverseY;
    }

    public void setReverseY(boolean reverseY) {
        this.reverseY = reverseY;
    }

    public boolean isBlockParallaxX() {
        return blockParallaxX;
    }

    public void setBlockParallaxX(boolean blockParallaxX) {
        this.blockParallaxX = blockParallaxX;
    }

    public boolean isBlockParallaxY() {
        return blockParallaxY;
    }

    public void setBlockParallaxY(boolean blockParallaxY) {
        this.blockParallaxY = blockParallaxY;
    }

    private static class InterpolatorSelector {
        private static final int LINEAR = 0;
        private static final int ACCELERATE_DECELERATE = 1;
        private static final int ACCELERATE = 2;
        private static final int ANTICIPATE = 3;
        private static final int ANTICIPATE_OVERSHOOT = 4;
        private static final int BOUNCE = 5;
        private static final int DECELERATE = 6;
        private static final int OVERSHOOT = 7;

        public static Interpolator interpolatorId(int interpolationId) {
            switch (interpolationId) {
                case LINEAR:
                default:
                    return new LinearInterpolator();
                case ACCELERATE_DECELERATE:
                    return new AccelerateDecelerateInterpolator();
                case ACCELERATE:
                    return new AccelerateInterpolator();
                case ANTICIPATE:
                    return new AnticipateInterpolator();
                case ANTICIPATE_OVERSHOOT:
                    return new AnticipateOvershootInterpolator();
                case BOUNCE:
                    return new BounceInterpolator();
                case DECELERATE:
                    return new DecelerateInterpolator();
                case OVERSHOOT:
                    return new OvershootInterpolator();
                //TODO: this interpolations needs parameters
                //case CYCLE:
                //    return new CycleInterpolator();
                //case PATH:
                //    return new PathInterpolator();
            }
        }
    }
}
