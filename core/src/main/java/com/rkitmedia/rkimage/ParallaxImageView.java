package com.rkitmedia.rkimage;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.util.AttributeSet;
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

    public static final int REVERSE_NONE = 0;
    public static final int REVERSE_X = 1;
    public static final int REVERSE_Y = 2;
    public static final int REVERSE_BOTH = 3;

    private static final boolean DEFAULT_PARALLAX_ENABLED = true;
    private static final int DEFAULT_PARALLAX_REVERSE = REVERSE_NONE;
    private static final boolean DEFAULT_UPDATE_ON_DRAW = false;
    private static final int DEFAULT_INTERPOLATION = InterpolatorSelector.LINEAR;

    private boolean parallaxEnabled = DEFAULT_PARALLAX_ENABLED;
    private int parallaxReverse = DEFAULT_PARALLAX_REVERSE;
    private boolean reverseX = false;
    private boolean reverseY = false;
    private boolean updateOnDraw = DEFAULT_UPDATE_ON_DRAW;
    private int interpolatorID = DEFAULT_INTERPOLATION;

    private int screenWidth;
    private int screenHeight;
    private float scrollSpaceX = 0;
    private float scrollSpaceY = 0;
    private float heightImageView;
    private float widthImageView;

    private Interpolator interpolator = new LinearInterpolator();

    private ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener = null;
    private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = null;
    private ViewTreeObserver.OnDrawListener onDrawListener = null;

    public ParallaxImageView(Context context) {
        super(context);
    }

    public ParallaxImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ParallaxImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ParallaxImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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

        mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                heightImageView = (float) getHeight();
                widthImageView = (float) getWidth();

                applyParallax();
            }
        };

        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        viewTreeObserver.addOnScrollChangedListener(mOnScrollChangedListener);
        viewTreeObserver.addOnGlobalLayoutListener(mOnGlobalLayoutListener);

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

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            viewTreeObserver.removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
        } else {
            //noinspection deprecation
            viewTreeObserver.removeGlobalOnLayoutListener(mOnGlobalLayoutListener);
        }

        if (updateOnDraw && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            viewTreeObserver.removeOnDrawListener(onDrawListener);
        }

        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getDrawable() != null) {
            int dheight = getDrawable().getIntrinsicHeight();
            int dwidth = getDrawable().getIntrinsicWidth();
            int vheight = getMeasuredHeight();
            int vwidth = getMeasuredWidth();

//            heightImageView = vwidth;
//            widthImageView = vheight;

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

    @Override
    @CallSuper
    protected void checkAttributes(AttributeSet attrs) {
        super.checkAttributes(attrs);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ParallaxImageView);

        setParallaxEnabled(a.getBoolean(R.styleable.ParallaxImageView_rk_parallaxEnabled, DEFAULT_PARALLAX_ENABLED));
        setParallaxReverse(a.getInt(R.styleable.ParallaxImageView_rk_reverse, DEFAULT_PARALLAX_REVERSE));
        setUpdateOnDrawEnabled(a.getBoolean(R.styleable.ParallaxImageView_rk_updateOnDraw, DEFAULT_UPDATE_ON_DRAW));
        setInterpolator(a.getInt(R.styleable.ParallaxImageView_rk_interpolation, DEFAULT_INTERPOLATION));

        a.recycle();
    }

    /**
     * set whether or not parallax is enabled. This will re-layout the view.
     *
     * @param parallaxEnabled weather or not parallax should be used
     */

    public void setParallaxEnabled(boolean parallaxEnabled) {
        this.parallaxEnabled = parallaxEnabled;

        if (parallaxEnabled) {
            requestLayout();
        }
    }

    /**
     * Get whether or not parallax is enabled.
     *
     * @return value of current parallax usage
     */
    public boolean isParallaxEnabled() {
        return parallaxEnabled;
    }

    public void setParallaxReverse(int parallaxReverse) {
        switch (parallaxReverse) {
            case REVERSE_NONE:
                reverseX = false;
                reverseY = false;
                break;
            case REVERSE_X:
                reverseX = true;
                reverseY = false;
                break;
            case REVERSE_Y:
                reverseX = false;
                reverseY = true;
                break;
            case REVERSE_BOTH:
                reverseX = true;
                reverseY = true;
                break;
            default:
                throw new IllegalArgumentException("Unknown parallax reverse with ID " + parallaxReverse);
        }

        this.parallaxReverse = parallaxReverse;

        if (parallaxEnabled) {
            requestLayout();
        }
    }

    public int getParallaxReverse() {
        return parallaxReverse;
    }

    public void setUpdateOnDrawEnabled(boolean updateOnDraw) {
        this.updateOnDraw = updateOnDraw;

        if (parallaxEnabled) {
            requestLayout();
        }
    }

    public boolean isUpdateOnDrawEnabled() {
        return updateOnDraw;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        super.setScaleType(scaleType);

        if (isParallaxEnabled()) {
            switch (scaleType) {
                case CENTER:
                case CENTER_CROP:
                case CENTER_INSIDE:
                    break;
                case FIT_CENTER:
                    throw new IllegalArgumentException("Scale type fitCenter unsupported");
                case FIT_END:
                    throw new IllegalArgumentException("Scale type fitEnd unsupported");
                case FIT_START:
                    throw new IllegalArgumentException("Scale type fitStart unsupported");
                case FIT_XY:
                    throw new IllegalArgumentException("Scale type fitXY unsupported");
                case MATRIX:
                    throw new IllegalArgumentException("Scale type matrix unsupported");
            }
        }
    }

    public void setInterpolator(int interpolatorID) {
        this.interpolatorID = interpolatorID;
        this.interpolator = InterpolatorSelector.interpolatorId(interpolatorID);
    }

    public int getInterpolatorID() {
        return interpolatorID;
    }

    public Interpolator getInterpolator() {
        return interpolator;
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

    private static class InterpolatorSelector {
        public static final int LINEAR = 0;
        public static final int ACCELERATE_DECELERATE = 1;
        public static final int ACCELERATE = 2;
        public static final int ANTICIPATE = 3;
        public static final int ANTICIPATE_OVERSHOOT = 4;
        public static final int BOUNCE = 5;
        public static final int DECELERATE = 6;
        public static final int OVERSHOOT = 7;

        public static Interpolator interpolatorId(int interpolationId) {
            switch (interpolationId) {
                case LINEAR:
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
                default:
                    throw new IllegalArgumentException("Unknown interpolation with ID " + interpolationId);
            }
        }
    }
}
