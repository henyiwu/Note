package com.gzik.pandora.commonUi.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangzhiping on 2022/12/3.
 */
public class FlowLayout extends ViewGroup {

    private int mHorizontalSpacing = 16; // 假设是padding
    private int mVerticalSpacing = 16; // 假设是padding

    private List<List<View>> allLine; // 记录所有行，用于layout
    private List<Integer> lineHeights; // 每一行的高

    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet set) {
        super(context, set);
    }

    public FlowLayout(Context context, AttributeSet set, int defStyleAttr) {
        super(context, set, defStyleAttr);
    }

    private void initMeasureParams() {
        allLine = new ArrayList<>();
        lineHeights = new ArrayList<>();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        initMeasureParams();
        // 度量孩子
        int childCount = getChildCount();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        int selfWidth = MeasureSpec.getSize(widthMeasureSpec);
        int selfHeight = MeasureSpec.getSize(heightMeasureSpec);

        List<View> lineViews = new ArrayList<>(); // 一行放的view
        int lineWidthUsed = 0; // 一行的宽度
        int lineHeight = 0; // 一行的高度

        int parentNeededHeight = 0; // measure过程中，子view要求的父viewGroup的高
        int parentNeededWidth = 0; // measure过程中，子view要求的父viewGroup的宽

        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            // 拿到子view的layout_height和layout_width
            // 参数1：父亲的宽度和测量模式
            // 参数2：父亲的padding，子类不能使用的
            // 参数3：子view的宽
            LayoutParams childLp = childView.getLayoutParams();
            int childWidthMeasureSpec = getChildMeasureSpec(
                    widthMeasureSpec, paddingLeft + paddingRight, childLp.width);
            int childHeightMeasureSpec = getChildMeasureSpec(
                    widthMeasureSpec, paddingTop + paddingBottom, childLp.height);
            // 触发childView对它的孩子进行测量
            childView.measure(childWidthMeasureSpec, childHeightMeasureSpec);

            // 获得子view的宽高
            int childMeasureWidth = childView.getMeasuredWidth();
            int childMeasureHeight = childView.getMeasuredHeight();

            // 判断宽超出
            if (childMeasureWidth + lineWidthUsed > selfWidth) {

                allLine.add(lineViews);
                lineHeights.add(lineHeight);

                // 一旦换行，保留宽高
                parentNeededWidth = Math.max(parentNeededWidth, lineWidthUsed + mHorizontalSpacing);
                parentNeededHeight = parentNeededHeight + lineHeight + mVerticalSpacing;

                lineViews = new ArrayList<>();
                lineWidthUsed = 0;
                lineHeight = 0;
            }
            lineViews.add(childView);

            lineWidthUsed = lineWidthUsed + childMeasureWidth + mHorizontalSpacing;
            lineHeight = Math.max(lineHeight, childMeasureHeight);

            // 最后一个view
            if (i == childCount - 1) {
                allLine.add(lineViews);
                lineHeights.add(lineHeight);
                parentNeededWidth = Math.max(parentNeededWidth, lineWidthUsed + mHorizontalSpacing);
                parentNeededHeight = parentNeededHeight + lineHeight + mVerticalSpacing;
            }
        }

        // 如果测量模式是指定宽高，直接用开发写死的尺寸，上面白测量
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int realWidth = (widthMode == MeasureSpec.EXACTLY) ? selfWidth : parentNeededWidth;
        int realHeight = (heightMode == MeasureSpec.EXACTLY) ? selfHeight : parentNeededHeight;
        // 确定自己的大小
        setMeasuredDimension(realWidth, realHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int lineCount = allLine.size();
        int curL = getPaddingLeft();
        int curT = getPaddingTop();
        for (int i = 0; i < lineCount; i++) {
            List<View> lineViews = allLine.get(i);
            int lineHeight = lineHeights.get(i);
            for (int j = 0; j < lineViews.size(); j++) {
                View view = lineViews.get(j);

                int left = curL;
                int top = curT;
                // getMeasureWidth在setMeasureDimension之后就能获取
                // getWidth在layout之后才能获取
                int right = left + view.getMeasuredWidth();
                int bottom = top + view.getMeasuredHeight();

                view.layout(left, top, right, bottom);

                curL = right + mHorizontalSpacing;
            }
            curL = getPaddingLeft();
            curT = curT + lineHeight + mVerticalSpacing;
        }
    }
}
