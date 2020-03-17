package com.example.linelayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义线路组件-U型
 */
public class LineLayoutU extends ViewGroup {
    // TODO: 2020/3/13 以后统一加注释
    //静态变量
    private static final int REMAINMODE_TOPMAX = 0;
    private static final int REMAINMODE_BOTTOMMAX = 1;
    private static final int REMAINMODE_CENTERMAX = 2;
    //可设置参数
    private Drawable lineDrawableNoPass;
    private Drawable lineDrawablePassed;
    private Drawable lineDrawablePassing;
    private int lineLayoutHeight = 40;
    private int lineViewHeight = 22;
    private Drawable pointDrawableNoPass;
    private Drawable pointDrawablePassed;
    private Drawable pointDrawablePassing;
    private int pointViewHeight = 30;
    private int pointViewWidth = 30;
    private int remainMode = REMAINMODE_CENTERMAX;
    private int tipsNameLayoutHeight = 100;
    private Drawable uTopDrawableNoPass;
    private Drawable uBottomDrawableNoPass;
    private Drawable uTopDrawablePassed;
    private Drawable uBottomDrawablePassed;
    private Drawable uTopDrawablePassing;
    private Drawable uBottomDrawablePassing;

    //需要使用的局部变量
    private Context context;
    private int topPerWidth = 0;
    private int bottomPerWidth = 0;
    private List<String> listData = new ArrayList<>();
    private float[] topLinePoints;
    private float[] centerLinePoints;
    private float[] bottomLinePoints;
    private float[] topPointPoints;
    private float[] centerPointPoints;
    private float[] bottomPointPoints;
    private int topPointNum = 0;
    private int bottomPointNum = 0;
    private NinePatch lineDrawableNoPassNinePatch;
    private NinePatch uTopDrawableNoPassNinePatch;
    private NinePatch uBottomDrawableNoPassNinePatch;
    private Rect rect;
    private int offsetLeft = 0;
    private int offsetRight = 0;

    //子组件和相关
    private ImageView iv_next_tip;
    private TextView tv_next_tip;
    private TipsNameView tv_change_message;

    public LineLayoutU(Context context) {
        super(context);
        this.context = context;
        init(null, 0);
    }

    public LineLayoutU(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs, 0);
    }

    public LineLayoutU(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.LineLayoutU, defStyle, 0);

        if (a.hasValue(R.styleable.LineLayoutU_lineDrawableNoPass)) {
            lineDrawableNoPass = a.getDrawable(
                    R.styleable.LineLayoutU_lineDrawableNoPass);
        }
        if (a.hasValue(R.styleable.LineLayoutU_lineDrawablePassed)) {
            lineDrawablePassed = a.getDrawable(
                    R.styleable.LineLayoutU_lineDrawablePassed);
        }
        if (a.hasValue(R.styleable.LineLayoutU_lineDrawablePassing)) {
            lineDrawablePassing = a.getDrawable(
                    R.styleable.LineLayoutU_lineDrawablePassing);
        }
        if (a.hasValue(R.styleable.LineLayoutU_pointDrawableNoPass)) {
            pointDrawableNoPass = a.getDrawable(
                    R.styleable.LineLayoutU_pointDrawableNoPass);
        } else {
            pointDrawableNoPass = getResources().getDrawable(R.drawable.point_blue);
        }
        if (a.hasValue(R.styleable.LineLayoutU_lineDrawablePassed)) {
            pointDrawablePassed = a.getDrawable(
                    R.styleable.LineLayoutU_lineDrawablePassed);
        } else {
            pointDrawablePassed = getResources().getDrawable(R.drawable.point_red);
        }
        if (a.hasValue(R.styleable.LineLayoutU_pointDrawablePassing)) {
            pointDrawablePassing = a.getDrawable(
                    R.styleable.LineLayoutU_pointDrawablePassing);
        } else {
            pointDrawablePassing = getResources().getDrawable(R.drawable.point_yellow);
        }


        int lineDrawableNoPassId = a.getResourceId(
                R.styleable.LineLayoutU_lineDrawableNoPass, R.drawable.default_u_line_view_img_no_pass);
        Bitmap lineDrawableNoPassBitmap = BitmapFactory.decodeResource(getResources(), lineDrawableNoPassId);
        lineDrawableNoPassNinePatch = new NinePatch(lineDrawableNoPassBitmap, lineDrawableNoPassBitmap.getNinePatchChunk(), null);

        int uTopDrawableNoPassId = a.getResourceId(
                R.styleable.LineLayoutU_uTopDrawableNoPass, R.drawable.default_u_mode_top_img);
        Bitmap uTopDrawableNoPassBitmap = BitmapFactory.decodeResource(getResources(), uTopDrawableNoPassId);
        uTopDrawableNoPassNinePatch = new NinePatch(uTopDrawableNoPassBitmap, uTopDrawableNoPassBitmap.getNinePatchChunk(), null);

        int uBottomDrawableNoPassId = a.getResourceId(
                R.styleable.LineLayoutU_uBottomDrawableNoPass, R.drawable.default_u_mode_bottom_img);
        Bitmap uBottomDrawableNoPassBitmap = BitmapFactory.decodeResource(getResources(), uBottomDrawableNoPassId);
        uBottomDrawableNoPassNinePatch = new NinePatch(uBottomDrawableNoPassBitmap, uBottomDrawableNoPassBitmap.getNinePatchChunk(), null);

        rect = new Rect();

        remainMode = a.getInt(R.styleable.LineLayoutU_remainMode, remainMode);
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
        topPerWidth = (contentWidth - offsetLeft) / topPointNum;
        bottomPerWidth = (contentWidth - offsetLeft) / bottomPointNum;
        int left, top, right, bottom;
        int tipsNameTop = (int) (paddingTop + contentHeight / 2f - tipsNameLayoutHeight / 2f - lineLayoutHeight);
        int tipsNameBottom = (int) (paddingTop + contentHeight / 2f + tipsNameLayoutHeight / 2f + lineLayoutHeight);

        for (int i = 0, j = -1; i < count; i++) {
            View child = getChildAt(i);
            //注意此处不能使用getWidth和getHeight，这两个方法必须在onLayout执行完，才能正确获取宽高
            if (child instanceof StationNameView) {
                int childMeasureWidth = child.getMeasuredWidth();
                int childMeasureHeight = child.getMeasuredHeight();
                offsetLeft = (childMeasureWidth > pointViewWidth ? childMeasureWidth : pointViewWidth) / 2;

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
            } else {
                child.layout(paddingLeft, tipsNameTop, contentWidth + paddingLeft - offsetRight, tipsNameBottom);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 计算出所有的childView的宽和高
        final int size = getChildCount();
        offsetRight = tipsNameLayoutHeight / 2 + lineLayoutHeight;
        for (int i = 0; i < size; ++i) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                if (child instanceof StationNameView) {
                    measureChild(child, widthMeasureSpec, heightMeasureSpec);
                } else {
                    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                    int widthMeasureSpec_t = MeasureSpec.makeMeasureSpec(widthSize - offsetRight, MeasureSpec.EXACTLY);
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
            if (i == 0) {
                topLinePoints[i * 4] = paddingLeft;
                topLinePoints[i * 4 + 1] = lineTop;
                topLinePoints[i * 4 + 2] = paddingLeft + (i) * topPerWidth;
                topLinePoints[i * 4 + 3] = lineTop;
            }
            int j = i + 1;
            topLinePoints[j * 4] = paddingLeft + ((i) * topPerWidth);
            topLinePoints[j * 4 + 1] = lineTop;
            if (i == topPointNum - 1) {
                topLinePoints[j * 4 + 2] = paddingLeft + ((i + 0.5f) * topPerWidth);
            } else {
                topLinePoints[j * 4 + 2] = paddingLeft + ((i + 1.0f) * topPerWidth);
            }
            topLinePoints[j * 4 + 3] = lineTop;

            topPointPoints[i * 2] = paddingLeft + (i) * topPerWidth;
            topPointPoints[i * 2 + 1] = lineTop;
        }

        centerPointPoints[0] = getWidth()-paddingRight;
        centerPointPoints[1] = paddingTop + contentHeight / 2;

        for (int i = 0; i < bottomPointNum; i++) {
            if (i == 0) {
                bottomLinePoints[i * 4] = paddingLeft;
                bottomLinePoints[i * 4 + 1] = lineBottom;
                bottomLinePoints[i * 4 + 2] = paddingLeft + (i) * bottomPerWidth;
                bottomLinePoints[i * 4 + 3] = lineBottom;
            }
            int j = i + 1;
            bottomLinePoints[j * 4] = paddingLeft + ((i) * bottomPerWidth);
            bottomLinePoints[j * 4 + 1] = lineBottom;
            if (i == bottomPointNum - 1) {
                bottomLinePoints[j * 4 + 2] = paddingLeft + ((i + 0.5f) * bottomPerWidth);
            } else {
                bottomLinePoints[j * 4 + 2] = paddingLeft + ((i + 1.0f) * bottomPerWidth);
            }
            bottomLinePoints[j * 4 + 3] = lineBottom;

            bottomPointPoints[i * 2] = paddingLeft + (i) * bottomPerWidth;
            bottomPointPoints[i * 2 + 1] = lineBottom;
        }

        for (int i = 1; i < topPointNum; i++) {
            rect.left = (int) (topLinePoints[i * 4]);
            rect.top = (int) (lineTop - lineViewHeight / 2f);
            rect.right = (int) (topLinePoints[i * 4 + 2]);
            rect.bottom = (int) (lineTop + lineViewHeight / 2f);
            lineDrawableNoPassNinePatch.draw(canvas, rect);
            if (i == topPointNum - 1) {
                rect.left = (int) (topLinePoints[i * 4 + 4]);
                rect.top = (int) (lineTop - lineViewHeight / 2f);
                rect.right = (int) centerPointPoints[0];
                rect.bottom = (int) centerPointPoints[1];
                uTopDrawableNoPassNinePatch.draw(canvas, rect);
            }
        }

        for (int i = 1; i < bottomPointNum; i++) {
            rect.left = (int) (bottomLinePoints[i * 4]);
            rect.top = (int) (lineBottom - lineViewHeight / 2f);
            rect.right = (int) (bottomLinePoints[i * 4 + 2]);
            rect.bottom = (int) (lineBottom + lineViewHeight / 2f);
            lineDrawableNoPassNinePatch.draw(canvas, rect);
            if (i == bottomPointNum - 1) {
                rect.left = (int) (bottomLinePoints[i * 4+2]);
                rect.top = (int) centerPointPoints[1];
                rect.right =(int) centerPointPoints[0];
                rect.bottom = (int) (lineBottom + lineViewHeight / 2f);
                uBottomDrawableNoPassNinePatch.draw(canvas, rect);
            }
        }


        for (int i = 0; i < topPointNum; i++) {
            pointDrawableNoPass.setBounds((int) (topPointPoints[i * 2] - pointViewWidth / 2f), (int) (lineTop - pointViewHeight / 2f),
                    (int) (topPointPoints[i * 2] + pointViewWidth / 2f), (int) (lineTop + pointViewHeight / 2f));
            pointDrawableNoPass.draw(canvas);
        }
        for (int i = 0; i < bottomPointNum; i++) {
            pointDrawableNoPass.setBounds((int) (bottomPointPoints[i * 2] - pointViewWidth / 2f), (int) (lineBottom - pointViewHeight / 2f),
                    (int) (bottomPointPoints[i * 2] + pointViewWidth / 2f), (int) (lineBottom + pointViewHeight / 2f));
            pointDrawableNoPass.draw(canvas);
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
        for (int i = 0; i < listData.size(); i++) {
            StationNameView stationNameView = new StationNameView(context);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(30, 60);
            stationNameView.setLayoutParams(layoutParams);
            stationNameView.setStationNameSize(30);
            stationNameView.setStationNameString(listData.get(i));
            addView(stationNameView);
        }
        int listSize = listData.size();
        if (listSize % 2 == 0) {
            topPointNum = listSize / 2;
            bottomPointNum = listSize / 2;
        } else {
            topPointNum = listSize / 2;
            bottomPointNum = listSize / 2 + 1;
        }
        topLinePoints = new float[(topPointNum + 1) * 4];
        bottomLinePoints = new float[(bottomPointNum + 1) * 4];
        topPointPoints = new float[(topPointNum + 1) * 2];
        centerPointPoints = new float[2];
        bottomPointPoints = new float[(bottomPointNum + 1) * 2];
        View view = View.inflate(context, R.layout.view_next_station_tips, null);
        iv_next_tip = view.findViewById(R.id.iv_next_tip);
        tv_next_tip = view.findViewById(R.id.tv_next_tip);
        tv_change_message = view.findViewById(R.id.tv_change_message);
        addView(view);
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
}
