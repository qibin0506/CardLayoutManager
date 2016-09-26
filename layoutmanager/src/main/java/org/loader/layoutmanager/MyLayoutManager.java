package org.loader.layoutmanager;

import android.graphics.Rect;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by qibin on 2016/9/23.
 */

public class MyLayoutManager extends RecyclerView.LayoutManager {

    private int mVerticalOffset;
    private int mTotalHeight;

    private SparseArrayCompat<Rect> mAllItemFrames = new SparseArrayCompat<>();

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {

        if (getItemCount() <= 0 || state.isPreLayout()) { return;}

        detachAndScrapAttachedViews(recycler);

        int offsetY = 0;
        int count = getItemCount();
        int totalHeight = 0;

        for (int i = 0; i < count; i++) {
            View scrap = recycler.getViewForPosition(i);
            measureChildWithMargins(scrap, 0, 0);

            int width = getDecoratedMeasuredWidth(scrap);
            int height = getDecoratedMeasuredHeight(scrap);

            Rect rect = mAllItemFrames.get(i);
            if (rect == null) { rect = new Rect();}
            rect.set(0, offsetY, width, offsetY + height);
            mAllItemFrames.put(i, rect);

            totalHeight += height;
            offsetY += height;
        }

        mTotalHeight = Math.max(totalHeight, getVerticalSpace());
        fill(recycler, state);
    }

    private void fill(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() <= 0 || state.isPreLayout()) { return;}
        Rect displayFrame = new Rect(0, mVerticalOffset,
                getHorizontalSpace(), mVerticalOffset + getVerticalSpace());

        Rect rect = new Rect();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            rect.left = getDecoratedLeft(child);
            rect.top = getDecoratedTop(child);
            rect.right = getDecoratedRight(child);
            rect.bottom = getDecoratedBottom(child);

            if (!Rect.intersects(displayFrame, rect)) {
                removeAndRecycleView(child, recycler);
            }
        }

        for (int i = 0; i < getItemCount(); i++) {
            Rect temp = mAllItemFrames.get(i);
            if (Rect.intersects(displayFrame, temp)) {
                View scrap = recycler.getViewForPosition(i);
                addView(scrap);
                measureChildWithMargins(scrap, 0, 0);
                layoutDecorated(scrap, temp.left, temp.top - mVerticalOffset, temp.right, temp.bottom - mVerticalOffset);
            }
        }
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);

        int travel = dy;

        if (mVerticalOffset + dy < 0) {
            travel = -mVerticalOffset;
        }else if (mVerticalOffset + dy > mTotalHeight - getVerticalSpace()) {
            travel = mTotalHeight - getVerticalSpace() - mVerticalOffset;
        }

        offsetChildrenVertical(-travel);
        mVerticalOffset += travel;
        fill(recycler, state);
        Log.d("manager", getChildCount() + "----");
        Log.d("dy", dy + "--");
        return travel;
    }

    private int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private int getVerticalSpace() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }
}
