package com.ehome.formview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.MeasureSpec.AT_MOST;

/**
 * @author: Routee
 * @date 2018/3/24
 * @mail wangc4@qianbaocard.com
 * ------------1.本类由Routee开发,阅读、修改时请勿随意修改代码排版格式后提交到git。
 * ------------2.阅读本类时，发现不合理请及时指正.
 * ------------3.如需在本类内部进行修改,请先联系Routee,若未经同意修改此类后造成损失本人概不负责。
 */

public class RouteeFormView extends View {

    private int     mHelpTextBgResId;
    private boolean mNeedDrawShader;

    public static class Units {
        public double y;
        String x;

        public Units(double y, String x) {
            this.x = x;
            this.y = y;
        }
    }

    public static class TextUnit {
        int    color;
        String text;

        public TextUnit(int color, String text) {
            this.color = color;
            this.text = text;
        }
    }

    public interface DataChangeListener {
        void dataChanged(int color, int position);
    }

    private int mMinSize;
    private Map<Integer, List<Units>> mDatas      = new LinkedHashMap<>();   //需要展示的数据
    private Map<Integer, List<Point>> mDataPoints = new LinkedHashMap<>();   //需要展示的点
    private Map<Integer, List<Rect>>  mDataRects  = new LinkedHashMap<>();   //需要展示的点的附近的坐标范围
    private List<List<TextUnit>>      mDataTexts  = new ArrayList<>();       //弹出提示框要展示的文字
    private int    mBaseColor;                                               //基础线条颜色
    private Paint  mPaint;                                                   //画笔
    private int    mBaseStrokeWidth;                                         //基础线条粗细
    private Double mUsefulY;                                                 //Y轴数值有效值
    private Double mMinUsefulY;                                              //Y轴数值最小值
    private Double mMaxUsefulY;                                              //Y轴数值最大值
    private int    mYDataSpacing;                                            //Y轴数值间隔大小
    private int    mBaseTextSize;                                            //文字大小
    private int    mHelpTextSize;                                            //弹出提示框文字大小
    private int    mMaxXTextWidth;                                           //X轴坐标值最大文字宽度
    private int    mMaxXTextHeight;                                          //X轴坐标值最大文字高度
    private int    mMaxYTextWidth;                                           //Y轴坐标值最大文字宽度
    private int    mMaxYTextHeight;                                          //Y轴坐标值最大文字高度
    private int    mMaxHelpTextWidth;                                        //弹出框提示文字宽度
    private int    mMaxHelpTextHeight;                                       //弹出框提示文字高度
    private int    mTextMarginX;                                             //X方向文字与表格间距
    private int    mTextMarginY;                                             //Y方向文字与表格间距
    private List<String> mYTexts = new ArrayList<>();                        //Y轴坐标值集合
    private List<String> mXTexts = new ArrayList<>();                        //X轴坐标值集合
    private float              mFormWidth;                                   //表格有效宽度（px值）
    private float              mFormHeight;                                  //表格有效高度（px值）
    private int                mXSpacingCount;                               //X轴坐标值元素的间隔个数
    private int                mLineSpacingCount;                            //Y轴坐标线的间隔个数
    private int                mLineSpacingCountRemainer;                    //Y轴坐标线的首个间隔个数
    private double             mMaxYValue;                                   //Y轴最大值
    private double             mMinYValue;                                   //Y轴最小值
    private float              mXPosition;                                   //event事件X轴位置
    private float              mYPosition;                                   //event事件Y轴位置
    private Rect               mPreRect;                                     //记录上一次选中点的范围
    private DataChangeListener mListener;                                    //数据变化监听listener
    private long               mDownEventMills;                              //手指按下时时间
    private long               mUpEventMills;                                //手指离开时间
    private int                mHelpTextMargin;                              //弹出提示框Margin
    private int                mHelpLineColor;                               //辅助线颜色
    private int                mPointWidth;                                  //小圆圈点的大小
    private int                mPointTouchWith;                              //小圆圈触摸范围
    private boolean isStartZero = false;                                     //Y轴是否从零开始

    public RouteeFormView(Context context) {
        this(context, null);
    }

    public RouteeFormView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RouteeFormView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RouteeFormView);
        mMinSize = a.getInteger(R.styleable.RouteeFormView_min_size, 0);
        mBaseColor = a.getColor(R.styleable.RouteeFormView_base_stroke_color, Color.parseColor("#d0d0d0"));
        mBaseStrokeWidth = a.getInteger(R.styleable.RouteeFormView_base_stroke_width, 1);
        mBaseTextSize = a.getInteger(R.styleable.RouteeFormView_base_text_size, 12);
        mHelpTextSize = a.getInteger(R.styleable.RouteeFormView_help_text_size, 14);
        mHelpTextMargin = a.getInteger(R.styleable.RouteeFormView_help_text_margin, 8);
        mTextMarginX = DisplayUtils.dp2px(getContext(), a.getInteger(R.styleable.RouteeFormView_text_margin_x, 4));
        mTextMarginY = DisplayUtils.dp2px(getContext(), a.getInteger(R.styleable.RouteeFormView_text_margin_y, 4));
        mHelpTextBgResId = a.getResourceId(R.styleable.RouteeFormView_helpTextBgRes, R.drawable.bg_routee_form_view_help_text);
        mNeedDrawShader = a.getBoolean(R.styleable.RouteeFormView_shader, false);
        mPointWidth = DisplayUtils.dp2px(getContext(), a.getInteger(R.styleable.RouteeFormView_point_size, 2));
        mPointTouchWith = DisplayUtils.dp2px(getContext(), a.getInteger(R.styleable.RouteeFormView_point_touch_size, 10));
        isStartZero = a.getBoolean(R.styleable.RouteeFormView_zero_start, false);
        a.recycle();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == AT_MOST && heightSpecMode == AT_MOST) {
            setMeasuredDimension(mMinSize, mMinSize);
        } else if (widthMeasureSpec == AT_MOST) {
            setMeasuredDimension(mMinSize, heightSpecSize);
        } else if (heightMeasureSpec == AT_MOST) {
            setMeasuredDimension(widthSpecSize, mMinSize);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDatas == null || mDatas.size() == 0) {
            return;
        }
        calc();
        drawText(canvas);
        drawLines(canvas);
        drawData(canvas);
        drawHelpLine(canvas);
        drawHelpText(canvas);
    }

    /**
     * 绘制坐标值
     *
     * @param canvas
     */
    private void drawText(Canvas canvas) {
        if (mPaint == null) {
            mPaint = new Paint();
        }
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mBaseColor);
        mPaint.setTextSize(DisplayUtils.dp2px(getContext(), mBaseTextSize));
        Rect bounds = new Rect();
        for (int i = 0; i < mYTexts.size(); i++) {
            mPaint.getTextBounds(mYTexts.get(i), 0, mYTexts.get(i).length(), bounds);
            canvas.drawText(mYTexts.get(i), mMaxYTextWidth - bounds.width(), i * mFormHeight / (mYTexts.size() - 1) + mMaxYTextHeight, mPaint);
        }
        for (int i = 0; i < (mXTexts.size() - 1) / mXSpacingCount + 1; i++) {
            mPaint.getTextBounds(mXTexts.get(i), 0, mXTexts.get(i).length(), bounds);
            float x = mMaxYTextWidth + mTextMarginY - bounds.width() / 2 + i * mXSpacingCount * mFormWidth / (mXTexts.size() - 1);
            float y = mFormHeight + mMaxXTextHeight + mTextMarginX + mMaxYTextHeight - 1;
            canvas.drawText(mXTexts.get(i * mXSpacingCount), x, y, mPaint);
        }
    }

    /**
     * 绘制坐标系及辅助坐标
     *
     * @param canvas
     */
    private void drawLines(Canvas canvas) {
        if (mPaint == null) {
            mPaint = new Paint();
        }
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mBaseColor);
        mPaint.setStrokeWidth(mBaseStrokeWidth);
        for (int i = 0; i < mYTexts.size(); i += mLineSpacingCount) {
            float yPosition = (i + mLineSpacingCountRemainer) * mFormHeight / (mYTexts.size() - 1) + mBaseStrokeWidth / 2 + mMaxYTextHeight;
            canvas.drawLine(mMaxYTextWidth + mTextMarginX, yPosition, getWidth() - mMaxXTextWidth / 2, yPosition, mPaint);
        }
    }

    /**
     * 绘制数据
     *
     * @param canvas
     */
    private void drawData(Canvas canvas) {
        Iterator<Integer> it = mDataPoints.keySet().iterator();
        while (it.hasNext()) {
            Path path = new Path();
            Integer color = it.next();
            List list = mDataPoints.get(color);
            for (int i = 0; i < list.size(); i++) {
                Point o = (Point) list.get(i);
                if (i == 0) {
                    path.moveTo(o.x, o.y);
                } else {
                    path.lineTo(o.x, o.y);
                }
            }

            mPaint.setColor(color);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(path, mPaint);
            if (mNeedDrawShader) {
                path.lineTo(((Point) list.get(list.size() - 1)).x, mFormHeight + mMaxYTextHeight);
                path.lineTo(mMaxYTextWidth + mTextMarginX, mFormHeight + mMaxYTextHeight);
                path.lineTo(((Point) list.get(0)).x, ((Point) list.get(0)).y);

                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(Color.WHITE);
                canvas.drawPath(path, mPaint);

                Shader shder = new LinearGradient(getWidth() / 2, 0, getWidth() / 2, getHeight()
                        , color & Color.parseColor("#44ffffff")
                        , color & Color.parseColor("#11ffffff"), Shader.TileMode.CLAMP);
                mPaint.setShader(shder);
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(color);
                canvas.drawPath(path, mPaint);
                mPaint.setShader(null);
            }
        }
    }

    /**
     * 绘制辅助坐标
     *
     * @param canvas
     */
    private void drawHelpLine(Canvas canvas) {
        Iterator<Integer> it = mDataRects.keySet().iterator();
        int color = mBaseColor;
        boolean find = false;
        while (it.hasNext() && !find) {
            color = it.next();
            List<Rect> rects = mDataRects.get(color);
            for (int i = 0; i < rects.size(); i++) {
                Rect rect = rects.get(i);
                if (rect.contains((int) mXPosition, (int) mYPosition)) {
                    mPreRect = new Rect(rect.centerX() - mPointWidth, rect.centerY() - mPointWidth, rect.centerX() + mPointWidth, rect.centerY() + mPointWidth);
                    mHelpLineColor = color;
                    find = true;
                    if (mListener != null) {
                        mListener.dataChanged(mHelpLineColor, i);
                    }
                    break;
                }
            }
        }
        if (mPreRect != null) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mBaseStrokeWidth / 2 == 0 ? 1 : mBaseStrokeWidth / 2);
            mPaint.setColor(mHelpLineColor);
            canvas.drawLine(mPreRect.centerX(), 0, mPreRect.centerX(), mFormHeight + mMaxYTextHeight, mPaint);
            canvas.drawLine(mMaxYTextWidth + mTextMarginY, mPreRect.centerY(), getWidth() - mMaxXTextWidth / 2 + 1, mPreRect.centerY(), mPaint);
            canvas.drawCircle(mPreRect.centerX(), mPreRect.centerY(), 4, mPaint);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.WHITE);
            canvas.drawCircle(mPreRect.centerX(), mPreRect.centerY(), 3, mPaint);
        }
    }

    private void drawHelpText(Canvas canvas) {
        if (!calcHelpTextSize()) {
            return;
        }
        Drawable drawable = ContextCompat.getDrawable(getContext(), mHelpTextBgResId);
        Rect rect = calcHelpRect();
        if (rect == null) {
            return;
        }
        drawable.setBounds(rect);
        drawable.draw(canvas);
        Rect bounds = new Rect();
        int margin = DisplayUtils.dp2px(getContext(), mHelpTextMargin);
        int height = (mMaxHelpTextHeight - margin * 2 - (mDataTexts.size() - 1) * DisplayUtils.dp2px(getContext(), 4)) / mDataTexts.size();
        mPaint.setTextSize(DisplayUtils.dp2px(getContext(), mHelpTextSize));
        for (int i = 0; i < mDataTexts.size(); i++) {
            int width = 0;
            for (TextUnit unit : mDataTexts.get(i)) {
                mPaint.setColor(unit.color);
                mPaint.getTextBounds(unit.text, 0, unit.text.length(), bounds);
                canvas.drawText(unit.text, rect.left + margin + width, rect.top + height + margin + i * (height + DisplayUtils.dp2px(getContext(), 4)), mPaint);
                width += bounds.width();
            }
        }
    }

    @Nullable
    private Rect calcHelpRect() {
        Rect rect = new Rect();
        int margin = DisplayUtils.dp2px(getContext(), mHelpTextMargin);
        if (mPreRect == null) {
            return null;
        }
        int x = mPreRect.centerX();
        int y = mPreRect.centerY();
        int l = 0;
        int t = 0;
        int r = 0;
        int b = 0;
        if (x >= mMaxHelpTextWidth + mMaxYTextWidth + mTextMarginY + margin) {
            l = (int) (x - mMaxHelpTextWidth) - margin;
            r = (int) x - margin;
        } else {
            l = (int) x + margin;
            r = (int) (x + mMaxHelpTextWidth) + margin;
        }
        if (y >= mMaxHelpTextHeight + margin) {
            t = (int) (y - mMaxHelpTextHeight - margin);
            b = (int) y - margin;
        } else if (getHeight() - y > mMaxHelpTextHeight + margin) {
            t = (int) y + margin;
            b = y + mMaxHelpTextHeight + margin;
        } else {
            t = getHeight() - mMaxHelpTextHeight;
            b = getHeight();
        }
        rect.set(l, t, r, b);
        return rect;
    }

    /**
     * 计算弹出框文字大小
     */
    private boolean calcHelpTextSize() {
        if (mDataTexts == null || mDataTexts.size() == 0) {
            return false;
        }
        mMaxHelpTextWidth = 0;
        mMaxHelpTextHeight = 0;
        mPaint.setTextSize(DisplayUtils.dp2px(getContext(), mHelpTextSize));
        Rect bounds = new Rect();
        for (List<TextUnit> list : mDataTexts) {
            int length = 0;
            int height = 0;
            for (TextUnit unit : list) {
                mPaint.getTextBounds(unit.text, 0, unit.text.length(), bounds);
                length += bounds.width();
                height = Math.max(height, bounds.height());
            }
            mMaxHelpTextWidth = Math.max(length, mMaxHelpTextWidth);
            mMaxHelpTextHeight += height;
        }
        mMaxHelpTextWidth = mMaxHelpTextWidth + DisplayUtils.dp2px(getContext(), 16);
        mMaxHelpTextHeight = mMaxHelpTextHeight + (mDataTexts.size() - 1) * DisplayUtils.dp2px(getContext(), 4) + DisplayUtils.dp2px(getContext(), 16);
        return true;
    }

    private void calc() {
        calcMaxYValue();
        calcMinYValue();
        calcYSpacing();
        calcYTextList();
        calcTextSize();
        calcFormSize();
        calcXTextList();
        calcBaseLines();
        calcData();
    }

    /**
     * 计算Y轴最大值
     *
     * @return Y轴最大值
     */
    private void calcMaxYValue() {
        if (mDatas == null || mDatas.size() == 0) {
            return;
        }
        double max = 0;
        for (Integer color : mDatas.keySet()) {
            for (Units units : mDatas.get(color)) {
                max = Math.max(max, units.y);
            }
        }
        mMaxUsefulY = max;
    }

    /**
     * 计算Y轴最小值
     *
     * @return Y轴最小值
     */
    private void calcMinYValue() {
        if (isStartZero) {
            mMinUsefulY = 0.0;
            return;
        }
        if (mDatas == null || mDatas.size() == 0) {
            return;
        }
        double min = 0;
        for (Integer color : mDatas.keySet()) {
            List<Units> list = mDatas.get(color);
            for (int i = 0; i < list.size(); i++) {
                if (i == 0) {
                    min = list.get(i).y;
                }
                min = Math.min(min, list.get(i).y);
            }
        }
        mMinUsefulY = min;
    }

    /**
     * 计算Y轴数值间隔大小
     */
    private void calcYSpacing() {
        mUsefulY = mMaxUsefulY - mMinUsefulY;
        if (mUsefulY == 0) {
            mMaxUsefulY = mMinUsefulY + 80;
            mUsefulY = 80.0;
        }
        int minSpacing = (int) (mUsefulY / 6);
        if (minSpacing == 0) {
            int w = (mMaxUsefulY + "").length();
            int spacing = w / 10;
            if (spacing != 0) {
                mYDataSpacing = spacing;
            } else if (mMaxUsefulY == 0) {
                mYDataSpacing = 20;
            } else if (mMaxUsefulY <= 1) {
                mYDataSpacing = 1;
            } else {
                mYDataSpacing = 2;
            }
            return;
        }
        String s = minSpacing + "";
        int length = s.length() - 1 > 0 ? s.length() - 1 : 0;
        int unit = (int) (1 * Math.pow(10, length));
        for (int i = 1; i <= 10; i += 1) {
            if (mUsefulY / (i * unit) < 6) {
                mYDataSpacing = i * unit;
                return;
            }
        }
    }

    /**
     * 计算Y坐标值数值集合
     */
    private void calcYTextList() {
        mYTexts = new ArrayList<>();
        if (mYDataSpacing == 1) {
            mMaxUsefulY = 1.0;
        }
        double remainder = mMaxUsefulY % mYDataSpacing;
        for (double i = mMaxUsefulY - remainder + mYDataSpacing; i >= mMinUsefulY - mYDataSpacing && i >= 0; i -= mYDataSpacing) {
            mYTexts.add((int) i + "");
        }
        if (mYTexts == null || mYTexts.size() == 0) {
            throw new RuntimeException("mMaxUsefulY = " + mMaxUsefulY + "; mMinUsefulY = " + mMinUsefulY + "; mYdataSpacing = " + mYDataSpacing + "; remainder = " + remainder + "");
        }
        String maxY = mYTexts.get(0);
        mMaxYValue = Double.parseDouble(maxY);
        String minY = mYTexts.get(mYTexts.size() - 1);
        mMinYValue = Double.parseDouble(minY);
    }

    /**
     * 计算所有文字大小
     */
    private void calcTextSize() {
        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setAntiAlias(true);
        }
        String xMax = "";
        for (Integer integer : mDatas.keySet()) {
            List<Units> units = mDatas.get(integer);
            for (Units unit : units) {
                xMax = unit.x.length() > xMax.length() ? unit.x : xMax;
            }
        }
        mPaint.setTextSize(DisplayUtils.dp2px(getContext(), mBaseTextSize));
        Rect bounds = new Rect();
        mPaint.getTextBounds(xMax, 0, xMax.length(), bounds);
        mMaxXTextHeight = bounds.height();
        mMaxXTextWidth = bounds.width();
        mPaint.getTextBounds(mYTexts.get(0), 0, mYTexts.get(0).length(), bounds);
        mMaxYTextHeight = bounds.height();
        mMaxYTextWidth = bounds.width();
        mMaxXTextHeight = Math.max(mMaxXTextHeight, mMaxYTextHeight);
        mMaxYTextHeight = Math.max(mMaxXTextHeight, mMaxYTextHeight);
        mMaxYTextWidth = Math.max(mMaxYTextWidth, mMaxXTextWidth / 2 - mTextMarginX);
    }

    /**
     * 计算表格大小
     */
    private void calcFormSize() {
        mFormWidth = getWidth() - mTextMarginX - mMaxYTextWidth - mMaxXTextWidth / 2 - 1;
        mFormHeight = getHeight() - mTextMarginY - mMaxXTextHeight - mMaxYTextHeight;
    }

    /**
     * 计算X轴所有数值
     */
    private void calcXTextList() {
        mXTexts.clear();
        mXSpacingCount = 1;
        Iterator<Integer> it = mDatas.keySet().iterator();
        if (it.hasNext()) {
            Integer next = it.next();
            List<Units> units = mDatas.get(next);
            while ((units.size() / mXSpacingCount + 1) * mMaxXTextWidth > mFormWidth * 2 / 3) {
                mXSpacingCount++;
            }
            for (int i = 0; i < units.size(); i++) {
                mXTexts.add(units.get(i).x + "");
            }
            return;
        }
    }

    /**
     * 计算基础线条
     */
    private void calcBaseLines() {
        mLineSpacingCount = (mYTexts.size() - 1) / 2;
        if (mLineSpacingCount == 0) {
            mLineSpacingCount = 1;
        }
        mLineSpacingCountRemainer = (mYTexts.size() - 1) % mLineSpacingCount;
    }

    private void calcData() {
        Iterator<Integer> it = mDatas.keySet().iterator();
        int size = mXTexts.size();
        while (it.hasNext()) {
            List<Point> listPoint = new ArrayList<>();
            List<Rect> listRect = new ArrayList<>();
            Integer color = it.next();
            List<Units> units = mDatas.get(color);
            for (int i = 0; i < units.size(); i++) {
                float x = i * mFormWidth / (size - 1) + mMaxYTextWidth + mTextMarginY;
                float y = (float) ((mMaxYValue - units.get(i).y) * mFormHeight / (mMaxYValue - mMinYValue) + mMaxYTextHeight);
                listPoint.add(new Point((int) x, (int) y));
                listRect.add(new Rect((int) (x - mPointTouchWith), (int) (y - mPointTouchWith), (int) (x + mPointTouchWith), (int) (y + mPointTouchWith)));
            }
            mDataPoints.put(color, listPoint);
            mDataRects.put(color, listRect);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        if (pointerCount > 1) {
            getParent().requestDisallowInterceptTouchEvent(false);
            return false;
        }
        mXPosition = event.getX();
        mYPosition = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownEventMills = Calendar.getInstance().getTimeInMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                mUpEventMills = Calendar.getInstance().getTimeInMillis();
                break;
            default:
                break;
        }
        if (mUpEventMills - mDownEventMills < 100 && mUpEventMills > mDownEventMills) {
            mPreRect = null;
        }
        getParent().requestDisallowInterceptTouchEvent(true);
        invalidate();
        return true;
    }

    /**
     * 设置选中数据改变的接口回调
     *
     * @param listener
     */
    public void setOnHelpDataChangedListener(DataChangeListener listener) {
        mListener = listener;
    }

    /**
     * 设置提示弹框内文字
     *
     * @param list
     */
    public void setHelpText(List<List<TextUnit>> list) {
        mDataTexts.clear();
        mDataTexts.addAll(list);
    }

    /**
     * 重设数据，并绘制界面
     *
     * @param map
     */
    public void resetData(Map<Integer, List<Units>> map) {
        this.mDatas.clear();
        mDataPoints.clear();
        mDataRects.clear();
        Iterator<Integer> it = map.keySet().iterator();
        while (it.hasNext()) {
            Integer color = it.next();
            mDatas.put(color, map.get(color));
        }
        mPreRect = null;
        invalidate();
    }

    public void setShaderable(boolean b) {
        mNeedDrawShader = b;
        invalidate();
    }
}
