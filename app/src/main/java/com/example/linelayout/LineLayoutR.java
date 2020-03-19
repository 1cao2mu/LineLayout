package com.example.linelayout;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义线路组件-环型
 */
public class LineLayoutR extends ViewGroup {
    // TODO: 2020/3/13 以后统一加注释
    //可设置参数
    private NinePatch lineDrawableNoPassNinePatch;
    private NinePatch lineDrawablePassedNinePatch;
    private NinePatch lineDrawablePassingNinePatch;
    private Drawable pointDrawableNoPass;
    private Drawable pointDrawablePassed;
    private Drawable pointDrawablePassing;

    private int lineLayoutHeight = 40;
    private int lineViewHeight = 22;

    private int pointViewHeight = 30;
    private int pointViewWidth = 30;
    private int tipsNameLayoutHeight = 100;
    private Drawable leftTopDrawableNoPassR;
    private Drawable leftTopDrawablePassedR;
    private Drawable leftTopDrawablePassingR;
    private Drawable rightTopDrawableNoPassR;
    private Drawable rightTopDrawablePassedR;
    private Drawable rightTopDrawablePassingR;
    private Drawable leftBottomDrawableNoPassR;
    private Drawable leftBottomDrawablePassedR;
    private Drawable leftBottomDrawablePassingR;
    private Drawable rightBottomDrawableNoPassR;
    private Drawable rightBottomDrawablePassedR;
    private Drawable rightBottomDrawablePassingR;

    private int stationNameNoPassColor = Color.BLUE;
    private int stationNamePassedColor = Color.RED;
    private int stationNamePassingColor = Color.YELLOW;
    private float stationNameSize = 25;
    private float stationNameSpeed = 0.5f;
    private boolean stationNameBold = true;
    private int stationNameMaxLine = 7;
    private int stopNumber = -1;
    private int stopType = -1;//0是进站 1是出站

    private AnimationDrawable pointAnim;
    private float pointAnimWidth = 30;
    private float pointAnimHeight = 30;

    private Drawable enterOutDrawable;
    private float enterOutWidth = 40;
    private float enterOutHeight = 40;

    //需要使用的局部变量
    private Context context;
    private int topPerWidth = 0;
    private int bottomPerWidth = 0;
    private List<String> listData = new ArrayList<>();
    private float[] topLinePoints;
    private float[] bottomLinePoints;
    private float[] topPointPoints;
    private float[] centerPointPoints;
    private float[] bottomPointPoints;
    private int topPointNum = 0;
    private int bottomPointNum = 0;
    private Rect rect;
    private int offsetLeft = 0;
    private int offsetRight = 0;
    private int pointAnimCurrentInt = -1;
    //子组件和相关
    private List<StationNameView> nameViewList = new ArrayList<>();
    private ImageView iv_enter_out;
    private ImageView iv_next_tip;
    private TextView tv_next_tip;
    private TipsNameView tv_change_message;
    private int enterOutIsCurrentStop = -1;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == 0) {
                pointAnimCurrentInt++;
                if (pointAnim == null) {
                    pointAnimCurrentInt = -1;
                }
                if (pointAnimCurrentInt > pointAnim.getNumberOfFrames() - 1) {
                    pointAnimCurrentInt = 0;
                }
                postInvalidate();
            }
            return true;
        }
    });

    public LineLayoutR(Context context) {
        super(context);
        this.context = context;
        init(null, 0);
    }

    public LineLayoutR(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs, 0);
    }

    public LineLayoutR(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.LineLayoutR, defStyle, 0);

        int lineDrawableNoPassId = a.getResourceId(
                R.styleable.LineLayoutR_lineDrawableNoPassR, R.drawable.default_r_line_view_img_no_pass);
        Bitmap lineDrawableNoPassBitmap = BitmapFactory.decodeResource(getResources(), lineDrawableNoPassId);
        lineDrawableNoPassNinePatch = new NinePatch(lineDrawableNoPassBitmap, lineDrawableNoPassBitmap.getNinePatchChunk(), null);
        int lineDrawablePassedId = a.getResourceId(
                R.styleable.LineLayoutR_lineDrawablePassedR, R.drawable.default_r_line_view_img_passed);
        Bitmap lineDrawablePassedBitmap = BitmapFactory.decodeResource(getResources(), lineDrawablePassedId);
        lineDrawablePassedNinePatch = new NinePatch(lineDrawablePassedBitmap, lineDrawablePassedBitmap.getNinePatchChunk(), null);
        int lineDrawablePassingId = a.getResourceId(
                R.styleable.LineLayoutR_lineDrawablePassingR, R.drawable.default_r_line_view_img_passing);
        Bitmap lineDrawablePassingBitmap = BitmapFactory.decodeResource(getResources(), lineDrawablePassingId);
        lineDrawablePassingNinePatch = new NinePatch(lineDrawablePassingBitmap, lineDrawablePassingBitmap.getNinePatchChunk(), null);


        if (a.hasValue(R.styleable.LineLayoutR_pointDrawableNoPassR)) {
            pointDrawableNoPass = a.getDrawable(
                    R.styleable.LineLayoutR_pointDrawableNoPassR);
        } else {
            pointDrawableNoPass = getResources().getDrawable(R.drawable.point_blue);
        }
        if (a.hasValue(R.styleable.LineLayoutR_lineDrawablePassedR)) {
            pointDrawablePassed = a.getDrawable(
                    R.styleable.LineLayoutR_lineDrawablePassedR);
        } else {
            pointDrawablePassed = getResources().getDrawable(R.drawable.point_red);
        }
        if (a.hasValue(R.styleable.LineLayoutR_pointDrawablePassingR)) {
            pointDrawablePassing = a.getDrawable(
                    R.styleable.LineLayoutR_pointDrawablePassingR);
        } else {
            pointDrawablePassing = getResources().getDrawable(R.drawable.point_yellow);
        }

        if (a.hasValue(R.styleable.LineLayoutR_leftTopDrawableNoPassR)) {
            leftTopDrawableNoPassR = a.getDrawable(
                    R.styleable.LineLayoutR_leftTopDrawableNoPassR);
        } else {
            leftTopDrawableNoPassR = getResources().getDrawable(R.drawable.default_r_left_top_no_pass);
        }
        if (a.hasValue(R.styleable.LineLayoutR_leftTopDrawablePassedR)) {
            leftTopDrawablePassedR = a.getDrawable(
                    R.styleable.LineLayoutR_leftTopDrawablePassedR);
        } else {
            leftTopDrawablePassedR = getResources().getDrawable(R.drawable.default_r_left_top_passed);
        }
        if (a.hasValue(R.styleable.LineLayoutR_leftTopDrawablePassingR)) {
            leftTopDrawablePassingR = a.getDrawable(
                    R.styleable.LineLayoutR_leftTopDrawablePassingR);
        } else {
            leftTopDrawablePassingR = getResources().getDrawable(R.drawable.default_r_left_top_passing);
        }

        if (a.hasValue(R.styleable.LineLayoutR_leftBottomDrawableNoPassR)) {
            leftBottomDrawableNoPassR = a.getDrawable(
                    R.styleable.LineLayoutR_leftBottomDrawableNoPassR);
        } else {
            leftBottomDrawableNoPassR = getResources().getDrawable(R.drawable.default_r_left_bottom_no_pass);
        }
        if (a.hasValue(R.styleable.LineLayoutR_leftBottomDrawablePassedR)) {
            leftBottomDrawablePassedR = a.getDrawable(
                    R.styleable.LineLayoutR_leftBottomDrawablePassedR);
        } else {
            leftBottomDrawablePassedR = getResources().getDrawable(R.drawable.default_r_left_bottom_passed);
        }
        if (a.hasValue(R.styleable.LineLayoutR_leftBottomDrawablePassingR)) {
            leftBottomDrawablePassingR = a.getDrawable(
                    R.styleable.LineLayoutR_leftBottomDrawablePassingR);
        } else {
            leftBottomDrawablePassingR = getResources().getDrawable(R.drawable.default_r_left_bottom_passing);
        }
        if (a.hasValue(R.styleable.LineLayoutR_rightTopDrawableNoPassR)) {
            rightTopDrawableNoPassR = a.getDrawable(
                    R.styleable.LineLayoutR_rightTopDrawableNoPassR);
        } else {
            rightTopDrawableNoPassR = getResources().getDrawable(R.drawable.default_r_right_top_no_pass);
        }
        if (a.hasValue(R.styleable.LineLayoutR_rightTopDrawablePassedR)) {
            rightTopDrawablePassedR = a.getDrawable(
                    R.styleable.LineLayoutR_rightTopDrawablePassedR);
        } else {
            rightTopDrawablePassedR = getResources().getDrawable(R.drawable.default_r_right_top_passed);
        }
        if (a.hasValue(R.styleable.LineLayoutR_rightTopDrawablePassingR)) {
            rightTopDrawablePassingR = a.getDrawable(
                    R.styleable.LineLayoutR_rightTopDrawablePassingR);
        } else {
            rightTopDrawablePassingR = getResources().getDrawable(R.drawable.default_r_right_top_passing);
        }

        if (a.hasValue(R.styleable.LineLayoutR_rightBottomDrawableNoPassR)) {
            rightBottomDrawableNoPassR = a.getDrawable(
                    R.styleable.LineLayoutR_rightBottomDrawableNoPassR);
        } else {
            rightBottomDrawableNoPassR = getResources().getDrawable(R.drawable.default_r_right_bottom_no_pass);
        }
        if (a.hasValue(R.styleable.LineLayoutR_rightBottomDrawablePassedR)) {
            rightBottomDrawablePassedR = a.getDrawable(
                    R.styleable.LineLayoutR_rightBottomDrawablePassedR);
        } else {
            rightBottomDrawablePassedR = getResources().getDrawable(R.drawable.default_r_right_bottom_passed);
        }
        if (a.hasValue(R.styleable.LineLayoutR_rightBottomDrawablePassingR)) {
            rightBottomDrawablePassingR = a.getDrawable(
                    R.styleable.LineLayoutR_rightBottomDrawablePassingR);
        } else {
            rightBottomDrawablePassingR = getResources().getDrawable(R.drawable.default_r_right_bottom_passing);
        }

        if (a.hasValue(R.styleable.LineLayoutR_pointAnimR)) {
            Drawable drawable = a.getDrawable(R.styleable.LineLayoutR_pointAnimR);
            if (drawable instanceof AnimationDrawable) {
                pointAnim = (AnimationDrawable) drawable;
                if (pointAnim.getNumberOfFrames() > 0) {
                    pointAnimCurrentInt = 0;
                }
            }
        } else {
            pointAnim = (AnimationDrawable) getResources().getDrawable(R.drawable.point_anim_r);
            if (pointAnim.getNumberOfFrames() > 0) {
                pointAnimCurrentInt = 0;
            }
        }
        pointAnimWidth = a.getDimension(R.styleable.LineLayoutR_pointAnimWidthR, pointAnimWidth);
        pointAnimHeight = a.getDimension(R.styleable.LineLayoutR_pointAnimHeightR, pointAnimHeight);
        rect = new Rect();
        if (a.hasValue(R.styleable.LineLayoutU_enterOutDrawableU)) {
            enterOutDrawable = a.getDrawable(
                    R.styleable.LineLayoutU_enterOutDrawableU);
        } else {
            enterOutDrawable = getResources().getDrawable(R.drawable.go);
        }
        a.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int totalWidth = getWidth();
        int totalHeight = getHeight();
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        int contentWidth = totalWidth - paddingLeft - paddingRight;
        int contentHeight = totalHeight - paddingTop - paddingBottom;
        int count = getChildCount();

        int left, top, right, bottom;
        int tipsNameTop = (int) (paddingTop + contentHeight / 2f - tipsNameLayoutHeight / 2f - lineLayoutHeight);
        int tipsNameBottom = (int) (paddingTop + contentHeight / 2f + tipsNameLayoutHeight / 2f + lineLayoutHeight);

        for (int i = 0, j = -1; i < count; i++) {
            View child = getChildAt(i);
            //注意此处不能使用getWidth和getHeight，这两个方法必须在onLayout执行完，才能正确获取宽高
            if (child instanceof StationNameView) {
                int childMeasureWidth = child.getMeasuredWidth();
                int childMeasureHeight = child.getMeasuredHeight();
                offsetLeft = tipsNameLayoutHeight / 2 + lineLayoutHeight;
                topPerWidth = (contentWidth - offsetLeft - offsetRight) / (topPointNum - 1);
                bottomPerWidth = (contentWidth - offsetLeft - offsetRight) / (bottomPointNum - 1);
                j++;
                if (j < topPointNum) {
                    //如果一行没有排满，继续往右排列
                    left = (int) (paddingLeft + offsetLeft + (j) * topPerWidth - childMeasureWidth / 2f);
                    right = left + childMeasureWidth;
                    top = tipsNameTop - childMeasureHeight;
                    bottom = tipsNameTop;
                    //确定子控件的位置，四个参数分别代表（左上右下）点的坐标值
                } else {
                    //如果一行没有排满，继续往右排列
                    int k = bottomPointNum - (j - topPointNum);
                    left = (int) (paddingLeft + offsetLeft + (k - 1f) * bottomPerWidth - childMeasureWidth / 2f);
                    right = left + childMeasureWidth;
                    top = tipsNameBottom;
                    bottom = top + childMeasureHeight;
                    //确定子控件的位置，四个参数分别代表（左上右下）点的坐标值
                }
                child.layout(left, top, right, bottom);
            } else if (child instanceof ImageView) {
                int childMeasureWidth = child.getMeasuredWidth();
                int childMeasureHeight = child.getMeasuredHeight();
                child.layout((int) (-enterOutWidth / 2), (int) (-enterOutHeight / 2), (int) (childMeasureWidth - enterOutWidth / 2), (int) (childMeasureHeight - enterOutHeight / 2));
            } else {
                child.layout(paddingLeft + offsetLeft, tipsNameTop, contentWidth + paddingLeft - offsetRight, tipsNameBottom);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 计算出所有的childView的宽和高
        final int size = getChildCount();
        offsetRight = tipsNameLayoutHeight / 2 + lineLayoutHeight;
        offsetLeft = tipsNameLayoutHeight / 2 + lineLayoutHeight;
        for (int i = 0; i < size; ++i) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                if (child instanceof StationNameView || child instanceof ImageView) {
                    measureChild(child, widthMeasureSpec, heightMeasureSpec);
                } else {
                    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                    int widthMeasureSpec_t = MeasureSpec.makeMeasureSpec(widthSize - offsetRight - offsetLeft, MeasureSpec.EXACTLY);
                    measureChild(child, widthMeasureSpec_t, heightMeasureSpec);
                }
            }
        }
        //测量并保存layout的宽高(使用getDefaultSize时，wrap_content和match_perent都是填充屏幕)
        //稍后会重新写这个方法，能达到wrap_content的效果
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int paddingLeft = getPaddingLeft() + offsetLeft;
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        int contentWidth = getWidth() - getPaddingLeft() - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        int lineTop = (int) (paddingTop + contentHeight / 2f - tipsNameLayoutHeight / 2f - lineLayoutHeight / 2f);
        int lineBottom = (int) (paddingTop + contentHeight / 2f + tipsNameLayoutHeight / 2f + lineLayoutHeight / 2f);

        for (int i = 0; i < topPointNum; i++) {
            if (i != topPointNum - 1) {
                topLinePoints[i * 4] = paddingLeft + ((i) * topPerWidth);
                topLinePoints[i * 4 + 1] = lineTop;
                topLinePoints[i * 4 + 2] = paddingLeft + ((i + 1.0f) * topPerWidth);
                topLinePoints[i * 4 + 3] = lineTop;
            }
            topPointPoints[i * 2] = paddingLeft + (i) * topPerWidth;
            topPointPoints[i * 2 + 1] = lineTop;
        }

        centerPointPoints[0] = getPaddingLeft() + (lineLayoutHeight-lineViewHeight) / 2f;
        centerPointPoints[1] = paddingTop + contentHeight / 2;
        centerPointPoints[2] = getWidth() - paddingRight - (lineLayoutHeight-lineViewHeight) / 2f;
        centerPointPoints[3] = paddingTop + contentHeight / 2;

        for (int i = 0; i < bottomPointNum; i++) {
            if (i != bottomPointNum - 1) {
                bottomLinePoints[i * 4] = paddingLeft + ((i) * bottomPerWidth);
                bottomLinePoints[i * 4 + 1] = lineBottom;
                bottomLinePoints[i * 4 + 2] = paddingLeft + ((i + 1.0f) * bottomPerWidth);
                bottomLinePoints[i * 4 + 3] = lineBottom;
            }
            bottomPointPoints[i * 2] = paddingLeft + (i) * bottomPerWidth;
            bottomPointPoints[i * 2 + 1] = lineBottom;
        }

        Drawable rightTopDrawable, rightBottomDrawable;
        Drawable leftTopDrawable, leftBottomDrawable;
        if (stopNumber > topPointNum - 1) {
            rightTopDrawable = rightTopDrawablePassedR;
            rightBottomDrawable = rightBottomDrawablePassedR;
        } else {
            rightTopDrawable = rightTopDrawableNoPassR;
            rightBottomDrawable = rightBottomDrawableNoPassR;
        }
        if (stopNumber == listData.size()) {
            leftTopDrawable = leftTopDrawablePassedR;
            leftBottomDrawable = leftBottomDrawablePassedR;
        } else {
            leftTopDrawable = leftTopDrawableNoPassR;
            leftBottomDrawable = leftBottomDrawableNoPassR;
        }
        for (int i = 0; i < listData.size(); i++) {
            NinePatch ninePatch;
            if (i < topPointNum) {
                if (i < stopNumber) {
                    ninePatch = lineDrawablePassedNinePatch;
                } else {
                    ninePatch = lineDrawableNoPassNinePatch;
                }
                if (i == 0) {
                    rect.left = (int) centerPointPoints[0];
                    rect.top = (int) (lineTop - lineViewHeight / 2f);
                    rect.right = (int) (topLinePoints[i * 4]);
                    rect.bottom = (int) centerPointPoints[1];
                    leftTopDrawable.setBounds(rect);
                    leftTopDrawable.draw(canvas);
                }
                if (i != topPointNum - 1) {
                    rect.left = (int) (topLinePoints[i * 4]);
                    rect.top = (int) (lineTop - lineViewHeight / 2f);
                    rect.right = (int) (topLinePoints[i * 4 + 2]);
                    rect.bottom = (int) (lineTop + lineViewHeight / 2f);
                    ninePatch.draw(canvas, rect);
                }
                if (i == topPointNum - 2) {
                    rect.left = (int) (topLinePoints[i * 4 + 2]);
                    rect.top = (int) (lineTop - lineViewHeight / 2f);
                    rect.right = (int) centerPointPoints[2];
                    rect.bottom = (int) centerPointPoints[3];
                    rightTopDrawable.setBounds(rect);
                    rightTopDrawable.draw(canvas);
                }
            } else {
                if (i < stopNumber + 1) {
                    ninePatch = lineDrawablePassedNinePatch;
                } else {
                    ninePatch = lineDrawableNoPassNinePatch;
                }
                int j = bottomPointNum - (i - topPointNum) - 1;
                if (j == 0) {
                    rect.left = (int) centerPointPoints[0];
                    rect.top = (int) centerPointPoints[1];
                    rect.right = (int) (bottomLinePoints[j * 4]);
                    rect.bottom = (int) (lineBottom + lineViewHeight / 2f);
                    leftBottomDrawable.setBounds(rect);
                    leftBottomDrawable.draw(canvas);
                }
                if (j != bottomPointNum - 1) {
                    rect.left = (int) (bottomLinePoints[j * 4]);
                    rect.top = (int) (lineBottom - lineViewHeight / 2f);
                    rect.right = (int) (bottomLinePoints[j * 4 + 2]);
                    rect.bottom = (int) (lineBottom + lineViewHeight / 2f);
                    ninePatch.draw(canvas, rect);
                }
                if (j == bottomPointNum - 2) {
                    rect.left = (int) (bottomLinePoints[j * 4 + 2]);
                    rect.top = (int) centerPointPoints[3];
                    rect.right = (int) centerPointPoints[2];
                    rect.bottom = (int) (lineBottom + lineViewHeight / 2f);
                    rightBottomDrawable.setBounds(rect);
                    rightBottomDrawable.draw(canvas);
                }
            }
        }

        if (stopNumber == listData.size()) {
            if (pointAnim != null && pointAnimCurrentInt != -1) {
                Drawable drawable = pointAnim.getFrame(pointAnimCurrentInt);
                drawable.setBounds((int) (topPointPoints[0] - pointAnimWidth / 2)
                        , (int) (lineTop - pointAnimHeight / 2),
                        (int) (topPointPoints[0] + pointAnimWidth / 2)
                        , (int) (lineTop + pointAnimHeight / 2));
                drawable.draw(canvas);
                if (!handler.hasMessages(0)) {
                    handler.sendEmptyMessageDelayed(0, pointAnim.getDuration(pointAnimCurrentInt));
                }
            }
        }


        for (int i = 0; i < listData.size(); i++) {
            Drawable pointDrawable;
            if (i < stopNumber) {
                pointDrawable = pointDrawablePassed;
            } else if (i == stopNumber) {
                pointDrawable = pointDrawablePassing;
            } else {
                pointDrawable = pointDrawableNoPass;
            }
            if (stopNumber == listData.size() && i == 0) {
                continue;
            }
            if (stopNumber == i) {
                if (pointAnim != null && pointAnimCurrentInt != -1) {
                    Drawable drawable = pointAnim.getFrame(pointAnimCurrentInt);
                    if (i < topPointNum) {
                        drawable.setBounds((int) (topPointPoints[i * 2] - pointAnimWidth / 2)
                                , (int) (lineTop - pointAnimHeight / 2),
                                (int) (topPointPoints[i * 2] + pointAnimWidth / 2)
                                , (int) (lineTop + pointAnimHeight / 2));
                    } else {
                        int j = bottomPointNum - (i - topPointNum) - 1;
                        drawable.setBounds((int) (bottomPointPoints[j * 2] - pointAnimWidth / 2f),
                                (int) (lineBottom - pointAnimHeight / 2f),
                                (int) (bottomPointPoints[j * 2] + pointAnimWidth / 2f),
                                (int) (lineBottom + pointAnimHeight / 2f));
                    }
                    drawable.draw(canvas);
                    if (!handler.hasMessages(0)) {
                        handler.sendEmptyMessageDelayed(0, pointAnim.getDuration(pointAnimCurrentInt));
                    }
                }
                continue;
            }

            if (i < topPointNum) {
                pointDrawable.setBounds((int) (topPointPoints[i * 2] - pointViewWidth / 2f), (int) (lineTop - pointViewHeight / 2f),
                        (int) (topPointPoints[i * 2] + pointViewWidth / 2f), (int) (lineTop + pointViewHeight / 2f));
                pointDrawable.draw(canvas);
            } else {
                int j = bottomPointNum - (i - topPointNum) - 1;
                pointDrawable.setBounds((int) (bottomPointPoints[j * 2] - pointViewWidth / 2f), (int) (lineBottom - pointViewHeight / 2f),
                        (int) (bottomPointPoints[j * 2] + pointViewWidth / 2f), (int) (lineBottom + pointViewHeight / 2f));
                pointDrawable.draw(canvas);
            }
        }

        if (stopNumber != -1 && stopType == 1) {
            startEndOutAnim();
        } else {
            stopEndOutAnim();
        }
    }

    private void startEndOutAnim() {
        if (iv_enter_out != null) {
            iv_enter_out.setVisibility(VISIBLE);
        } else {
            return;
        }
        if (enterOutIsCurrentStop != stopNumber) {
            AnimatorSet animatorSet = new AnimatorSet();
            ValueAnimator valueAnimatorP = new ValueAnimator();
            if (stopNumber == topPointNum) {
                valueAnimatorP.setObjectValues(new PointF(0, 0));
                valueAnimatorP.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        PointF point = (PointF) animation.getAnimatedValue();
                        iv_enter_out.setTranslationX(point.x);
                        iv_enter_out.setTranslationY(point.y);
                    }
                });
                final float d = tipsNameLayoutHeight + lineLayoutHeight;
                valueAnimatorP.setEvaluator(new TypeEvaluator() {
                    @Override
                    public Object evaluate(float fraction, Object startValue, Object endValue) {
                        return BezierUtil.calculateBezierPointForCubic(fraction,
                                new PointF(topPointPoints[(topPointNum - 1) * 2], topPointPoints[(topPointNum - 1) * 2 + 1]),
                                new PointF(topPointPoints[(topPointNum - 1) * 2] + d / 3 * 2, topPointPoints[(topPointNum - 1) * 2 + 1]),
                                new PointF(bottomPointPoints[(bottomPointNum - 1) * 2] + d / 3 * 2, bottomPointPoints[(bottomPointNum - 1) * 2 + 1]),
                                new PointF(bottomPointPoints[(bottomPointNum - 1) * 2], bottomPointPoints[(bottomPointNum - 1) * 2 + 1]));
                    }
                });
                ObjectAnimator mAnimatorR = ObjectAnimator.ofFloat(iv_enter_out, View.ROTATION, 0, 90, 180);
                valueAnimatorP.setRepeatCount(Animation.INFINITE);
                mAnimatorR.setRepeatCount(Animation.INFINITE);
                animatorSet.playTogether(mAnimatorR, valueAnimatorP);
            } else if (stopNumber == listData.size()) {
                valueAnimatorP.setObjectValues(new PointF(0, 0));
                valueAnimatorP.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        PointF point = (PointF) animation.getAnimatedValue();
                        iv_enter_out.setTranslationX(point.x);
                        iv_enter_out.setTranslationY(point.y);
                    }
                });
                final float d = tipsNameLayoutHeight + lineLayoutHeight;
                valueAnimatorP.setEvaluator(new TypeEvaluator() {
                    @Override
                    public Object evaluate(float fraction, Object startValue, Object endValue) {
                        return BezierUtil.calculateBezierPointForCubic(fraction,
                                new PointF(bottomPointPoints[0], bottomPointPoints[1]),
                                new PointF(bottomPointPoints[0] - d / 3 * 2, bottomPointPoints[1]),
                                new PointF(topPointPoints[0] - d / 3 * 2, topPointPoints[1]),
                                new PointF(topPointPoints[0], topPointPoints[1])
                        );
                    }
                });
                ObjectAnimator mAnimatorR = ObjectAnimator.ofFloat(iv_enter_out, View.ROTATION, 180, 270, 360);
                valueAnimatorP.setRepeatCount(Animation.INFINITE);
                mAnimatorR.setRepeatCount(Animation.INFINITE);
                animatorSet.playTogether(mAnimatorR, valueAnimatorP);

            } else if (stopNumber < topPointNum) {
                ObjectAnimator mAnimatorX = ObjectAnimator.ofFloat(iv_enter_out, View.TRANSLATION_X, topPointPoints[(stopNumber - 1) * 2], topPointPoints[(stopNumber) * 2]);
                ObjectAnimator mAnimatorY = ObjectAnimator.ofFloat(iv_enter_out, View.TRANSLATION_Y, topPointPoints[1], topPointPoints[1]);
                mAnimatorX.setRepeatCount(Animation.INFINITE);
                mAnimatorY.setRepeatCount(Animation.INFINITE);
                animatorSet.playTogether(mAnimatorX, mAnimatorY);
            } else {//stopNumber>topPointNum
                int j = bottomPointNum - (stopNumber - topPointNum);
                ObjectAnimator mAnimatorX = ObjectAnimator.ofFloat(iv_enter_out, View.TRANSLATION_X, bottomPointPoints[(j) * 2], bottomPointPoints[(j - 1) * 2]);
                ObjectAnimator mAnimatorY = ObjectAnimator.ofFloat(iv_enter_out, View.TRANSLATION_Y, bottomPointPoints[1], bottomPointPoints[1]);
                ObjectAnimator mAnimatorR = ObjectAnimator.ofFloat(iv_enter_out, View.ROTATION, 180, 180);
                mAnimatorX.setRepeatCount(Animation.INFINITE);
                mAnimatorY.setRepeatCount(Animation.INFINITE);
                mAnimatorR.setRepeatCount(Animation.INFINITE);
                animatorSet.playTogether(mAnimatorX, mAnimatorY, mAnimatorR);
            }
            animatorSet.setDuration(2000);
            animatorSet.start();

            enterOutIsCurrentStop = stopNumber;
        }
    }

    private void stopEndOutAnim() {
        if (iv_enter_out != null) {
            iv_enter_out.setVisibility(INVISIBLE);
        }
    }

    public void setListData(List<String> listData) {
        Log.e("setListData", getWidth() + " " + getHeight());
        this.listData = listData;
        handleListData();
    }

    @SuppressWarnings("PointlessArithmeticExpression")
    private void handleListData() {
        Log.e("handleListData", getWidth() + " " + getHeight());
        removeAllViews();
        nameViewList.clear();
        int listSize = listData.size();
        if (listSize % 2 == 0) {
            topPointNum = listSize / 2;
            bottomPointNum = listSize / 2;
        } else {
            topPointNum = listSize / 2;
            bottomPointNum = listSize / 2 + 1;
        }
        for (int i = 0; i < listData.size(); i++) {
            String content = listData.get(i);
            StationNameView stationNameView = new StationNameView(context);
            stationNameView.setStationNameSize(stationNameSize);
            stationNameView.setStationNameBold(stationNameBold);
            stationNameView.setStationNameSpeed(stationNameSpeed);
            int nameColor;
            if (i < stopNumber) {
                nameColor = stationNamePassedColor;
            } else if (i == stopNumber) {
                nameColor = stationNamePassingColor;
            } else {
                nameColor = stationNameNoPassColor;
            }
            stationNameView.setStationNameColor(nameColor);
            float oneTextWidth = stationNameView.getOneTextWidth();
            float oneTextHeight = stationNameView.getOneTextHeight();
            int lineNum = content.length() > stationNameMaxLine ? stationNameMaxLine : content.length();
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams((int) oneTextWidth, (int) (oneTextHeight * lineNum) + 1);
            stationNameView.setLayoutParams(layoutParams);
            stationNameView.setStationNameString(listData.get(i));
            addView(stationNameView);
            nameViewList.add(stationNameView);
        }
        topLinePoints = new float[(topPointNum - 1) * 4];
        bottomLinePoints = new float[(bottomPointNum - 1) * 4];
        topPointPoints = new float[topPointNum * 2];
        bottomPointPoints = new float[bottomPointNum * 2];
        centerPointPoints = new float[4];
        View view = View.inflate(context, R.layout.view_next_station_tips, null);
        iv_next_tip = view.findViewById(R.id.iv_next_tip);
        tv_next_tip = view.findViewById(R.id.tv_next_tip);
        tv_change_message = view.findViewById(R.id.tv_change_message);
        if (stopNumber != -1) {
            view.setVisibility(View.VISIBLE);
            int s = stopNumber;
            if (stopNumber == listSize) {
                s = 0;
            }
            tv_next_tip.setText(stopType == 0 ? "当前站:" + listData.get(s) : "下一站:" + listData.get(s));
        } else {
            view.setVisibility(View.INVISIBLE);
        }
        addView(view);
        iv_enter_out = new ImageView(context);
        iv_enter_out.setScaleType(ImageView.ScaleType.FIT_XY);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams((int) enterOutWidth, (int) enterOutHeight);
        iv_enter_out.setLayoutParams(layoutParams);
        if (stopType == 1 && stopNumber != -1) {
            iv_enter_out.setImageDrawable(enterOutDrawable);
            iv_enter_out.setVisibility(View.VISIBLE);
        } else {
            iv_enter_out.setImageDrawable(null);
            iv_enter_out.setVisibility(View.INVISIBLE);
        }
        addView(iv_enter_out);
    }


    public ImageView getIv_next_tip() {
        return iv_next_tip;
    }

    public TextView getTv_next_tip() {
        return tv_next_tip;
    }

    public TipsNameView getTv_change_message() {
        return tv_change_message;
    }

    public void setStopNumber(int stopNumber, int stopType) {
        this.enterOutIsCurrentStop = -1;
        this.stopType = stopType;
        if (stopType == 1) {
            this.stopNumber = stopNumber + 1;
        } else {
            this.stopNumber = stopNumber;
        }
        handleListData();
    }
}
