package com.example.linelayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义线路组件-U型
 */
public class LineLayoutU extends ViewGroup {
    private static final int STARTENDMOD_LEFT = 0;
    private static final int STARTENDMOD_CENTER = 1;
    // TODO: 2020/3/13 以后统一加注释
    //静态变量
    //可设置参数
    private int lineLayoutHeight = 40;
    private int lineViewHeight = 22;
    private Drawable pointDrawableNoPass;
    private Drawable pointDrawablePassed;
    private Drawable pointDrawablePassing;
    private int pointViewHeight = 30;
    private int pointViewWidth = 30;
    private int tipsNameLayoutHeight = 100;

    private NinePatch lineDrawableNoPassNinePatch;
    private NinePatch lineDrawablePassedNinePatch;
    private NinePatch lineDrawablePassingNinePatch;
    private Drawable topDrawableNoPass;
    private Drawable topDrawablePassed;
    private Drawable topDrawablePassing;
    private Drawable bottomDrawableNoPass;
    private Drawable bottomDrawablePassed;
    private Drawable bottomDrawablePassing;

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
    private float startEndWidth = 40;
    private float startEndHeight = 40;
    private int startEndMod = STARTENDMOD_LEFT;
    private Drawable startDrawable;
    private Drawable endDrawable;

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
    private Rect rect;
    private int offsetLeft = 0;
    private int offsetRight = 0;
    private int pointAnimCurrentInt = -1;

    //子组件和相关
    private ImageView iv_next_tip;
    private TextView tv_next_tip;
    private TipsNameView tv_change_message;

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
        if (a.hasValue(R.styleable.LineLayoutU_pointDrawableNoPassU)) {
            pointDrawableNoPass = a.getDrawable(
                    R.styleable.LineLayoutU_pointDrawableNoPassU);
        } else {
            pointDrawableNoPass = getResources().getDrawable(R.drawable.point_blue);
        }
        if (a.hasValue(R.styleable.LineLayoutU_lineDrawablePassedU)) {
            pointDrawablePassed = a.getDrawable(
                    R.styleable.LineLayoutU_lineDrawablePassedU);
        } else {
            pointDrawablePassed = getResources().getDrawable(R.drawable.point_red);
        }
        if (a.hasValue(R.styleable.LineLayoutU_pointDrawablePassingU)) {
            pointDrawablePassing = a.getDrawable(
                    R.styleable.LineLayoutU_pointDrawablePassingU);
        } else {
            pointDrawablePassing = getResources().getDrawable(R.drawable.point_yellow);
        }


        int lineDrawableNoPassId = a.getResourceId(
                R.styleable.LineLayoutU_lineDrawableNoPassU, R.drawable.default_u_line_view_img_no_pass);
        Bitmap lineDrawableNoPassBitmap = BitmapFactory.decodeResource(getResources(), lineDrawableNoPassId);
        lineDrawableNoPassNinePatch = new NinePatch(lineDrawableNoPassBitmap, lineDrawableNoPassBitmap.getNinePatchChunk(), null);
        int lineDrawablePassedId = a.getResourceId(
                R.styleable.LineLayoutU_lineDrawablePassedU, R.drawable.default_r_line_view_img_passed);
        Bitmap lineDrawablePassedBitmap = BitmapFactory.decodeResource(getResources(), lineDrawablePassedId);
        lineDrawablePassedNinePatch = new NinePatch(lineDrawablePassedBitmap, lineDrawablePassedBitmap.getNinePatchChunk(), null);
        int lineDrawablePassingId = a.getResourceId(
                R.styleable.LineLayoutU_lineDrawablePassingU, R.drawable.default_r_line_view_img_passing);
        Bitmap lineDrawablePassingBitmap = BitmapFactory.decodeResource(getResources(), lineDrawablePassingId);
        lineDrawablePassingNinePatch = new NinePatch(lineDrawablePassingBitmap, lineDrawablePassingBitmap.getNinePatchChunk(), null);


        if (a.hasValue(R.styleable.LineLayoutU_topDrawableNoPassU)) {
            topDrawableNoPass = a.getDrawable(
                    R.styleable.LineLayoutU_topDrawableNoPassU);
        } else {
            topDrawableNoPass = getResources().getDrawable(R.drawable.default_r_right_top_no_pass);
        }
        if (a.hasValue(R.styleable.LineLayoutU_topDrawablePassedU)) {
            topDrawablePassed = a.getDrawable(
                    R.styleable.LineLayoutU_topDrawablePassedU);
        } else {
            topDrawablePassed = getResources().getDrawable(R.drawable.default_r_right_top_passed);
        }
        if (a.hasValue(R.styleable.LineLayoutU_topDrawablePassingU)) {
            topDrawablePassing = a.getDrawable(
                    R.styleable.LineLayoutU_topDrawablePassingU);
        } else {
            topDrawablePassing = getResources().getDrawable(R.drawable.default_r_right_top_passing);
        }

        if (a.hasValue(R.styleable.LineLayoutU_bottomDrawableNoPassU)) {
            bottomDrawableNoPass = a.getDrawable(
                    R.styleable.LineLayoutU_bottomDrawableNoPassU);
        } else {
            bottomDrawableNoPass = getResources().getDrawable(R.drawable.default_r_right_bottom_no_pass);
        }
        if (a.hasValue(R.styleable.LineLayoutU_bottomDrawablePassedU)) {
            bottomDrawablePassed = a.getDrawable(
                    R.styleable.LineLayoutU_bottomDrawablePassedU);
        } else {
            bottomDrawablePassed = getResources().getDrawable(R.drawable.default_r_right_bottom_passed);
        }
        if (a.hasValue(R.styleable.LineLayoutU_bottomDrawablePassingU)) {
            bottomDrawablePassing = a.getDrawable(
                    R.styleable.LineLayoutU_bottomDrawablePassingU);
        } else {
            bottomDrawablePassing = getResources().getDrawable(R.drawable.default_r_right_bottom_passing);
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

        if (a.hasValue(R.styleable.LineLayoutU_startDrawableU)) {
            startDrawable = a.getDrawable(
                    R.styleable.LineLayoutU_startDrawableU);
        } else {
            if (startEndMod == STARTENDMOD_LEFT) {
                startDrawable = getResources().getDrawable(R.mipmap.start_left);
            } else {
                startDrawable = getResources().getDrawable(R.mipmap.start_center);
            }
        }

        if (a.hasValue(R.styleable.LineLayoutU_endDrawableU)) {
            endDrawable = a.getDrawable(
                    R.styleable.LineLayoutU_endDrawableU);
        } else {
            if (startEndMod == STARTENDMOD_LEFT) {
                endDrawable = getResources().getDrawable(R.mipmap.end_left);
            } else {
                endDrawable = getResources().getDrawable(R.mipmap.end_center);
            }
        }

        rect = new Rect();

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
                offsetLeft = (childMeasureWidth > pointViewWidth ? childMeasureWidth : pointViewWidth) / 2;
                if (startEndMod == STARTENDMOD_LEFT) {
                    offsetLeft += startEndWidth;
                }
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
            } else {
                child.layout(paddingLeft, tipsNameTop, contentWidth + paddingLeft - offsetRight, tipsNameBottom);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 计算出所有的childView的宽和高
        final int size = getChildCount();
        offsetRight = tipsNameLayoutHeight / 2 + lineLayoutHeight / 2 + lineViewHeight / 2;
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
            if (i != topPointNum - 1) {
                topLinePoints[i * 4] = paddingLeft + ((i) * topPerWidth);
                topLinePoints[i * 4 + 1] = lineTop;
                topLinePoints[i * 4 + 2] = paddingLeft + ((i + 1.0f) * topPerWidth);
                topLinePoints[i * 4 + 3] = lineTop;
            }
            topPointPoints[i * 2] = paddingLeft + (i) * topPerWidth;
            topPointPoints[i * 2 + 1] = lineTop;
        }

        centerPointPoints[0] = getWidth() - paddingRight;
        centerPointPoints[1] = paddingTop + contentHeight / 2;

        for (int i = 0; i < bottomPointNum; i++) {
            if (i != bottomPointNum - 1) {
                bottomLinePoints[i * 4] = paddingLeft + ((i) * bottomPerWidth);
                bottomLinePoints[i * 4 + 1] = lineTop;
                bottomLinePoints[i * 4 + 2] = paddingLeft + ((i + 1.0f) * bottomPerWidth);
                bottomLinePoints[i * 4 + 3] = lineTop;
            }
            bottomPointPoints[i * 2] = paddingLeft + (i) * bottomPerWidth;
            bottomPointPoints[i * 2 + 1] = lineTop;
        }

        Drawable rightTopDrawable, rightBottomDrawable;
        if (stopNumber > topPointNum - 1) {
            rightTopDrawable = topDrawablePassed;
            rightBottomDrawable = bottomDrawablePassed;
        } else {
            rightTopDrawable = topDrawableNoPass;
            rightBottomDrawable = bottomDrawableNoPass;
        }

        for (int i = 0; i < listData.size(); i++) {
            NinePatch ninePatch;
            if (i < topPointNum) {
                if (i < stopNumber) {
                    ninePatch = lineDrawablePassedNinePatch;
                } else {
                    ninePatch = lineDrawableNoPassNinePatch;
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
                    rect.right = (int) centerPointPoints[0];
                    rect.bottom = (int) centerPointPoints[1];
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
                if (j != bottomPointNum - 1) {
                    rect.left = (int) (bottomLinePoints[j * 4]);
                    rect.top = (int) (lineBottom - lineViewHeight / 2f);
                    rect.right = (int) (bottomLinePoints[j * 4 + 2]);
                    rect.bottom = (int) (lineBottom + lineViewHeight / 2f);
                    ninePatch.draw(canvas, rect);
                }
                if (j == bottomPointNum - 2) {
                    rect.left = (int) (bottomLinePoints[j * 4 + 2]);
                    rect.top = (int) centerPointPoints[1];
                    rect.right = (int) centerPointPoints[0];
                    rect.bottom = (int) (lineBottom + lineViewHeight / 2f);
                    rightBottomDrawable.setBounds(rect);
                    rightBottomDrawable.draw(canvas);
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
            if (i == 0 || i == listData.size() - 1) {
                Drawable drawable = startDrawable;
                if (startEndMod == STARTENDMOD_CENTER) {
                    if (i == 0) {
                        drawable = startDrawable;
                        drawable.setBounds((int) (topPointPoints[i * 2] - startEndWidth / 2), (int) (lineTop - startEndHeight / 2),
                                (int) (topPointPoints[i * 2] + startEndWidth / 2), (int) (lineTop + startEndHeight / 2));
                    } else if (i == listData.size() - 1) {
                        drawable = endDrawable;
                        int j = bottomPointNum - (i - topPointNum) - 1;
                        drawable.setBounds((int) (bottomPointPoints[j * 2] - startEndWidth / 2), (int) (lineBottom - startEndHeight / 2),
                                (int) (bottomPointPoints[j * 2] + startEndWidth / 2), (int) (lineBottom + startEndHeight / 2));
                    }
                    drawable.draw(canvas);
                    continue;
                } else if (startEndMod == STARTENDMOD_LEFT) {
                    if (i == 0) {
                        drawable = startDrawable;
                        drawable.setBounds((int) (topPointPoints[i * 2] - pointViewWidth / 2f - startEndWidth), (int) (lineTop - startEndHeight / 2),
                                (int) (topPointPoints[i * 2] - pointViewWidth / 2f), (int) (lineTop + startEndHeight / 2));
                    } else if (i == listData.size() - 1) {
                        int j = bottomPointNum - (i - topPointNum) - 1;
                        drawable = endDrawable;
                        drawable.setBounds((int) (bottomPointPoints[j * 2] - pointViewWidth / 2f - startEndWidth), (int) (lineBottom - startEndHeight / 2),
                                (int) (bottomPointPoints[j * 2] - pointViewWidth / 2f), (int) (lineBottom + startEndHeight / 2));
                    }
                    drawable.draw(canvas);
                }
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
        }
        topLinePoints = new float[(topPointNum - 1) * 4];
        bottomLinePoints = new float[(bottomPointNum - 1) * 4];
        topPointPoints = new float[topPointNum * 2];
        bottomPointPoints = new float[bottomPointNum * 2];
        centerPointPoints = new float[2];
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
        iv_next_tip.setVisibility(View.GONE);
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

    public void setStopNumber(int stopNumber, int stopType) {
        this.stopType = stopType;
        if (stopType == 1) {
            this.stopNumber = stopNumber + 1;
        } else {
            this.stopNumber = stopNumber;
        }
        handleListData();
    }
}
