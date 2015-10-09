package com.rkitmedia.rkimage;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
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
    private static final int DEFAULT_PARALLAX_X = -1;
    private static final int DEFAULT_PARALLAX_Y = -1;

    private boolean parallaxEnabled = DEFAULT_PARALLAX_ENABLED;
    private int parallaxReverse = DEFAULT_PARALLAX_REVERSE;
    private boolean reverseX = false;
    private boolean reverseY = false;
    private boolean updateOnDraw = DEFAULT_UPDATE_ON_DRAW;
    private int interpolatorID = DEFAULT_INTERPOLATION;
    private View container = null;
    private int parallaxX = DEFAULT_PARALLAX_X;
    private int parallaxY = DEFAULT_PARALLAX_Y;

    private float scrollSpaceX = 0;
    private float scrollSpaceY = 0;

    private Interpolator interpolator;
    private ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener = null;
    private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = null;
    private ViewTreeObserver.OnDrawListener onDrawListener = null;
    private OnLayoutChangeListener mOnContainerLayoutListener;
    private final Rect containerBounds = new Rect();
    private final int[] parallaxPos = new int[2];
    private final Rect parallaxRect = new Rect();
    private ScaleType scaleType;
    private final Matrix matrix = new Matrix();
    private float scale;
    private float centerX;
    private float centerY;

    public ParallaxImageView(Context context) {
        super(context);
    }

    public ParallaxImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        checkAttributes(attrs);
    }

    public ParallaxImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        checkAttributes(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ParallaxImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        checkAttributes(attrs);
    }

    private void checkAttributes(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ParallaxImageView);

        setParallaxEnabled(a.getBoolean(R.styleable.ParallaxImageView_rk_parallaxEnabled, DEFAULT_PARALLAX_ENABLED));
        setParallaxX(a.getDimensionPixelSize(R.styleable.ParallaxImageView_rk_parallaxX, DEFAULT_PARALLAX_X));
        setParallaxY(a.getDimensionPixelSize(R.styleable.ParallaxImageView_rk_parallaxY, DEFAULT_PARALLAX_Y));
        setParallaxReverse(a.getInt(R.styleable.ParallaxImageView_rk_parallaxReverse, DEFAULT_PARALLAX_REVERSE));
        setParallaxUpdateOnDrawEnabled(a.getBoolean(R.styleable.ParallaxImageView_rk_parallaxUpdateOnDraw, DEFAULT_UPDATE_ON_DRAW));
        setParallaxInterpolator(a.getInt(R.styleable.ParallaxImageView_rk_parallaxInterpolation, DEFAULT_INTERPOLATION));
        setParallaxContainer(a.getResourceId(R.styleable.ParallaxImageView_rk_parallaxContainer, 0));
        setScaleType(ScaleType.MATRIX);

        a.recycle();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        setParallaxEnabled(isParallaxEnabled());
    }

    @Override
    protected void onDetachedFromWindow() {
        setParallaxEnabled(false);

        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureParallax();
    }

    private void measureParallax() {
        if (!isAttachedToWindow()) {
            return;
        }

        getContainerSize();

        if (getDrawable() != null) {
            float dNewHeight, dNewWidth;
            int dWidth = getDrawable().getIntrinsicWidth();
            int dHeight = getDrawable().getIntrinsicHeight();
            int vWidth = getMeasuredWidth();
            int vHeight = getMeasuredHeight();

            if (parallaxX >= 0) {
                dWidth = Math.max(dWidth, vWidth + parallaxX);
            }

            if (parallaxY >= 0) {
                dHeight = Math.max(dHeight, vHeight + parallaxY);
            }

            if (dWidth * vHeight > vWidth * dHeight) {
                scale = (float) vHeight / dHeight;
                dNewWidth = dWidth * scale;
                dNewHeight = vHeight;
            } else {
                scale = (float) vWidth / dWidth;
                dNewWidth = vWidth;
                dNewHeight = dHeight * scale;
            }

            scrollSpaceX = Math.max(0, dNewWidth - vWidth);
            scrollSpaceY = Math.max(0, dNewHeight - vHeight);

            if (parallaxX >= 0) {
                scrollSpaceX = parallaxX;
            }

            if (parallaxY >= 0) {
                scrollSpaceY = parallaxY;
            }

            centerX = Math.max(0, dNewWidth - vWidth) * 0.5F;
            centerY = Math.max(0, dNewHeight - vHeight) * 0.5F;
        }

        applyParallax();
    }

    private View findParentRecursively(View view, int targetId) {
        if (view.getId() == targetId) {
            return view;
        }
        View parent = (View) view.getParent();
        if (parent == null) {
            return null;
        }
        return findParentRecursively(parent, targetId);
    }

    private void getContainerSize() {
        if (container != null) {
            container.getGlobalVisibleRect(containerBounds);
        } else {
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                display.getRectSize(containerBounds);
            } else {
                //noinspection deprecation
                containerBounds.set(0, 0, display.getWidth(), display.getHeight());
            }
        }
    }

    private void applyParallax() {
        float translateX = -centerX;
        float translateY = -centerY;
        float scrollDelta, interpolatedScrollDelta;

        getLocationOnScreen(parallaxPos);
        getDrawingRect(parallaxRect);

        if (scrollSpaceY != 0) {
            scrollDelta = 1F - Math.max(0, Math.min((float) (parallaxPos[1] - containerBounds.top + parallaxRect.height()) / (containerBounds.height() + parallaxRect.height()), 1));
            interpolatedScrollDelta = interpolator.getInterpolation(scrollDelta);
            translateY += Math.min(Math.max((0.5f - interpolatedScrollDelta), -0.5f), 0.5f) * scrollSpaceY;

            if (reverseY) {
                translateY *= -1;
            }
        }

        if (scrollSpaceX != 0) {
            scrollDelta = 1F - Math.max(0, Math.min((float) (parallaxPos[0] - containerBounds.left + parallaxRect.width()) / (containerBounds.width() + parallaxRect.width()), 1));
            interpolatedScrollDelta = interpolator.getInterpolation(scrollDelta);
            translateX += Math.min(Math.max((0.5f - interpolatedScrollDelta), -0.5f), 0.5f) * scrollSpaceX;

            if (reverseX) {
                translateX *= -1;
            }
        }

        matrix.reset();
        matrix.setScale(scale, scale);
        matrix.postTranslate(translateX, translateY);
        setImageMatrix(matrix);
    }

    public void setParallaxContainer(final int viewID) {
        if (viewID == 0) {
            return;
        }

        post(new Runnable() {
            @Override
            public void run() {
                View view = findParentRecursively(ParallaxImageView.this, viewID);

                if (view != null) {
                    setParallaxContainer(view);
                } else {
                    throw new IllegalArgumentException("Unknown view with ID " + viewID);
                }
            }
        });
    }

    public void setParallaxContainer(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (container != null && mOnContainerLayoutListener != null) {
                container.removeOnLayoutChangeListener(mOnContainerLayoutListener);
            }

            if (view != null) {
                mOnContainerLayoutListener = new OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        measureParallax();
                    }
                };

                view.addOnLayoutChangeListener(mOnContainerLayoutListener);
            }
        }

        container = view;
    }

    /**
     * set whether or not parallax is enabled. This will re-layout the view.
     *
     * @param parallaxEnabled weather or not parallax should be used
     */

    public void setParallaxEnabled(boolean parallaxEnabled) {
        this.parallaxEnabled = parallaxEnabled;

        if (parallaxEnabled) {
            if (mOnScrollChangedListener == null) {
                mOnScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        applyParallax();
                    }
                };
            }

            if (mOnGlobalLayoutListener == null) {
                mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        applyParallax();
                    }
                };
            }

            if (onDrawListener == null) {
                ViewTreeObserver viewTreeObserver = getViewTreeObserver();
                viewTreeObserver.addOnScrollChangedListener(mOnScrollChangedListener);
                viewTreeObserver.addOnGlobalLayoutListener(mOnGlobalLayoutListener);

                if (updateOnDraw && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    onDrawListener = new ViewTreeObserver.OnDrawListener() {
                        @Override
                        public void onDraw() {
                            applyParallax();
                        }
                    };
                    viewTreeObserver.addOnDrawListener(onDrawListener);
                }
            }

            requestLayout();
        } else {
            ViewTreeObserver viewTreeObserver = getViewTreeObserver();

            if (mOnScrollChangedListener != null) {
                viewTreeObserver.removeOnScrollChangedListener(mOnScrollChangedListener);
                mOnScrollChangedListener = null;
            }

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                viewTreeObserver.removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
            } else {
                //noinspection deprecation
                viewTreeObserver.removeGlobalOnLayoutListener(mOnGlobalLayoutListener);
            }

            if (updateOnDraw && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                viewTreeObserver.removeOnDrawListener(onDrawListener);
            }
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

    public void setParallaxUpdateOnDrawEnabled(boolean updateOnDraw) {
        this.updateOnDraw = updateOnDraw;

        setParallaxEnabled(isParallaxEnabled());

        if (parallaxEnabled) {
            requestLayout();
        }
    }

    public boolean isUpdateOnDrawEnabled() {
        return updateOnDraw;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        super.setScaleType(ScaleType.MATRIX);
        this.scaleType = scaleType;
    }

    @Override
    public ScaleType getScaleType() {
        return scaleType;
    }

    public void setParallaxInterpolator(int interpolatorID) {
        this.interpolatorID = interpolatorID;
        this.interpolator = InterpolatorSelector.interpolatorId(interpolatorID);
    }

    public int getInterpolatorID() {
        return interpolatorID;
    }

    public Interpolator getInterpolator() {
        return interpolator;
    }

    public void setParallaxX(int parallaxX) {
        this.parallaxX = parallaxX;
        measureParallax();
    }

    public int getParallaxX() {
        return parallaxX;
    }

    public void setParallaxY(int parallaxY) {
        this.parallaxY = parallaxY;
        measureParallax();
    }

    public int getParallaxY() {
        return parallaxY;
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
