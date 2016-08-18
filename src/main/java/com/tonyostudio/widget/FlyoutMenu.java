package com.tonyostudio.widget;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import java.util.ArrayList;

/**
 * Created by tonyofrancis on 8/16/16.
 */

public class FlyoutMenu {

    private PopupWindow mPopupWindow;
    private RecyclerView mRecyclerView;
    private View mAnchorView;
    private ArrayList<DismissListener> mDismissListeners;

    public FlyoutMenu(@NonNull View anchorView) {
        this(anchorView, ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public FlyoutMenu(@NonNull View anchorView, int width, int height) {
        super();
        mAnchorView = anchorView;
        mDismissListeners = new ArrayList<>();

        mRecyclerView = new RecyclerView(anchorView.getContext());
        mRecyclerView.setBackgroundColor(ContextCompat.getColor(anchorView.getContext(),android.R.color.white));
        mRecyclerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(anchorView.getContext()));

        mPopupWindow = new PopupWindow(mRecyclerView,width, height);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setOnDismissListener(mDismissListener);
    }

    private PopupWindow.OnDismissListener mDismissListener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            for (DismissListener dismissListener : mDismissListeners) {
                dismissListener.onDismissFlyoutMenu(FlyoutMenu.this);
            }
        }
    };

    public void setWidth(int width) {
        mPopupWindow.setWidth(width);
    }

    public void setAdapter(@Nullable RecyclerView.Adapter adapter) {
        mRecyclerView.setAdapter(adapter);
    }

    public RecyclerView.Adapter getAdapter() {
        return mRecyclerView.getAdapter();
    }

    public void show() {
        mPopupWindow.showAsDropDown(mAnchorView);
    }

    public void dismiss() {
        mPopupWindow.dismiss();
    }

    public void addDismissListener(@NonNull DismissListener dismissListener) {
        mDismissListeners.add(dismissListener);
    }

    public void removeDismissListener(@NonNull DismissListener dismissListener) {
        mDismissListeners.remove(dismissListener);
    }

    public interface DismissListener {
        void onDismissFlyoutMenu(FlyoutMenu flyoutMenu);
    }

    public void setBackgroundColor(@ColorInt int color) {
        mRecyclerView.setBackgroundColor(color);
    }
}