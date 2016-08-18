package com.tonyostudio.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ArrayRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by tonyofrancis on 8/12/16.
 */
public class FlyoutSpinner extends LinearLayout {

    private TextView mSelectedTextView;
    private ImageView mIndicatorImageView;
    private FlyoutMenu mFlyoutMenu;
    private boolean mMenuOpen;
    private boolean mItemSelected;
    private boolean mAnimateArrow;
    private int mMenuItemTextColor;
    private int mFlyoutMenuWidth;
    private int mFlyoutMenuBackgroundColor;
    private ArrayList<onSpinnerListener> mSpinnerListeners;
    private List<FlyoutSpinnerItem> mFlyoutSpinnerItems;

    public FlyoutSpinner(Context context) {
        this(context,null);
    }

    public FlyoutSpinner(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FlyoutSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs,defStyleAttr);
    }

    private void init(AttributeSet attrs,int defStyleAttr) {

        mItemSelected = false;
        mAnimateArrow = true;
        mMenuOpen = false;
        mSpinnerListeners = new ArrayList<>();
        mFlyoutSpinnerItems = new ArrayList<>();

        View view = LayoutInflater.from(getContext()).inflate(R.layout.flyout_spinner,this,false);
        mSelectedTextView = (TextView) view.findViewById(android.R.id.text1);
        mIndicatorImageView = (ImageView) view.findViewById(R.id.imageView);

        TypedArray styledAttributes = null;
        try {
            styledAttributes = getContext().obtainStyledAttributes(attrs, R.styleable.spinner);

            int selectedTextColor = styledAttributes.getColor(R.styleable.spinner_selectedTextColor,
                    ContextCompat.getColor(getContext(),android.R.color.black));

            mSelectedTextView.setTextColor(selectedTextColor);

            float titleTextSize = styledAttributes.getDimensionPixelSize(R.styleable.spinner_selectedTextSize,(int)mSelectedTextView.getTextSize());

            mSelectedTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,titleTextSize);

            int titleTextStyle = styledAttributes.getInt(R.styleable.spinner_selectedTextStyle,0);
            mSelectedTextView.setTypeface(mSelectedTextView.getTypeface(),titleTextStyle);

            boolean selectedTextAppCaps = styledAttributes.getBoolean(R.styleable.spinner_selectedTextAllCaps,false);
            mSelectedTextView.setAllCaps(selectedTextAppCaps);

            mAnimateArrow = styledAttributes.getBoolean(R.styleable.spinner_animateArrow,true);
            int arrowColor = styledAttributes.getColor(R.styleable.spinner_arrowColor,
                    ContextCompat.getColor(getContext(),android.R.color.black));

            DrawableCompat.setTint(DrawableCompat.wrap(mIndicatorImageView.getDrawable()),arrowColor);

            mFlyoutMenuWidth = styledAttributes.getInteger(R.styleable.spinner_menuWidth,-3);
            mFlyoutMenuBackgroundColor = styledAttributes.getColor(R.styleable.spinner_menuBackgroundColor,ContextCompat.getColor(getContext(),android.R.color.white));
            mMenuItemTextColor = styledAttributes.getColor(R.styleable.spinner_menuTextColor,ContextCompat.getColor(getContext(),android.R.color.black));

            int arrayRef = styledAttributes.getResourceId(R.styleable.spinner_menuArray,-1);

            if(arrayRef != -1) {
                setFlyoutMenuArray(arrayRef);
            }
        } finally {
            if(styledAttributes != null) {
                styledAttributes.recycle();
            }
        }

        super.setOnClickListener(mFlyoutClickListener);
        this.addView(view);
    }

    private void createFlyoutMenu() {
        int flyoutWidth = (mFlyoutMenuWidth == - 3) ? WRAP_CONTENT : mFlyoutMenuWidth;
        mFlyoutMenu = new FlyoutMenu(this,flyoutWidth, WRAP_CONTENT);
        mFlyoutMenu.setBackgroundColor(mFlyoutMenuBackgroundColor);
        mFlyoutMenu.addDismissListener(mFlyoutMenuDismissListener);
        mFlyoutMenu.setAdapter(new FlyoutSpinnerAdapter(mFlyoutSpinnerItems,mMenuItemTextColor,menuItemClickListener));
    }

    @Override
    public void setOnClickListener(OnClickListener clickListener) {}

    private OnClickListener mFlyoutClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {

            if (mMenuOpen) {
                mMenuOpen = false;
                mFlyoutMenu.dismiss();
            } else {
                createFlyoutMenu();

                if(mFlyoutMenu.getAdapter() != null && mFlyoutMenu.getAdapter().getItemCount() != 0) {
                    if(mAnimateArrow) {
                        animateArrow(true);
                    }
                    mMenuOpen = true;
                    mFlyoutMenu.show();
                }

                for (onSpinnerListener onSpinnerListener : mSpinnerListeners) {
                    onSpinnerListener.onSpinnerClick(FlyoutSpinner.this);
                }
            }
        }
    };

    private FlyoutMenu.DismissListener mFlyoutMenuDismissListener = new FlyoutMenu.DismissListener() {
        @Override
        public void onDismissFlyoutMenu(FlyoutMenu flyoutMenu) {
            if(mAnimateArrow) {
                animateArrow(false);
            }

            if(mItemSelected) {
                mItemSelected = false;
            } else {
                for (onSpinnerListener onSpinnerListener : mSpinnerListeners) {
                    onSpinnerListener.onNothingSelected(FlyoutSpinner.this);
                }
            }
        }
    };

    public void setSelectedMenuItem(@NonNull FlyoutSpinnerItem menuItem, boolean alertListeners) {
        mSelectedTextView.setText(menuItem.getTitle());

        if(alertListeners) {
            for (onSpinnerListener onSpinnerListener : mSpinnerListeners) {
                onSpinnerListener.onItemSelected(this,menuItem);
            }
        }
    }

    public void setFlyoutMenuWidth(int width) {
        mFlyoutMenuWidth = width;
    }

    public void addSpinnerListener(@NonNull onSpinnerListener spinnerListener) {
        this.mSpinnerListeners.add(spinnerListener);
    }

    public void removeSpinnerListener(@NonNull onSpinnerListener spinnerListener) {
        this.mSpinnerListeners.remove(spinnerListener);
    }

    public void setSelectedTextSize(@DimenRes int textSize) {
        mSelectedTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(textSize));
    }

    public void setSelectedTextColor(@ColorInt int textColor) {
        mSelectedTextView.setTextColor(textColor);
    }

    public void setArrowColor(@ColorInt int arrowColor) {
        DrawableCompat.setTint(DrawableCompat.wrap(mIndicatorImageView.getDrawable()),arrowColor);
    }

    public void setSelectedTextStyle(int textStyle) {
        mSelectedTextView.setTypeface(mSelectedTextView.getTypeface(),textStyle);
    }

    public void setSelectedTextAllCaps(boolean allCaps) {
        mSelectedTextView.setAllCaps(allCaps);
    }

    public void setAnimateArrow(boolean animate) {
        this.mAnimateArrow = animate;
    }

    public boolean isAnimateArrow() {
        return this.mAnimateArrow;
    }

    public List<FlyoutSpinnerItem> setFlyoutMenuArray(@ArrayRes int arrayRes) {
        String[] items = getResources().getStringArray(arrayRes);
        List<FlyoutSpinnerItem> menuItems = new ArrayList<>(items.length);

        for (int x = 0; x < items.length; x++) {
            FlyoutSpinnerItem menuItem = new FlyoutSpinnerItem(items[x]);
            menuItem.setData(x);
            menuItems.add(menuItem);
        }

        setMenuItemList(menuItems);
        return menuItems;
    }

    public void setMenuItemList(@NonNull List<FlyoutSpinnerItem> menuItems) {
        mFlyoutSpinnerItems = menuItems;
        mSelectedTextView.setText(menuItems.get(0).getTitle());
    }

    public List<FlyoutSpinnerItem> getMenuItems() {
        return mFlyoutSpinnerItems;
    }

    private void animateArrow(boolean shouldRotateUp) {
        Animation animation;

        if(shouldRotateUp) {
            animation = AnimationUtils.loadAnimation(getContext(),R.anim.rotate_up);

        } else {
            animation = AnimationUtils.loadAnimation(getContext(),R.anim.rotate_down);
        }

        mIndicatorImageView.startAnimation(animation);
    }

    private OnClickListener menuItemClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            mItemSelected = true;
            FlyoutSpinnerItem flyoutSpinnerItem = (FlyoutSpinnerItem) view.getTag();

            mSelectedTextView.setText(flyoutSpinnerItem.getTitle());

            for (onSpinnerListener onSpinnerListener : mSpinnerListeners) {
                onSpinnerListener.onItemSelected(FlyoutSpinner.this, flyoutSpinnerItem);
            }

            mFlyoutMenu.dismiss();

            if(mAnimateArrow) {
                animateArrow(false);
            }
        }
    };

     private static class FlyoutSpinnerAdapter extends RecyclerView.Adapter<ViewHolder>{

        private List<FlyoutSpinnerItem> mDataSet;
        private OnClickListener mClickListener;
        private int menuItemTextColor;

        public FlyoutSpinnerAdapter(@NonNull List<FlyoutSpinnerItem> dataSet, @ColorRes int textColor, OnClickListener clickListener) {
            this.mDataSet = dataSet;
            this.mClickListener = clickListener;
            this.menuItemTextColor = textColor;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1,parent,false);
            ((TextView)view.findViewById(android.R.id.text1)).setTextColor(menuItemTextColor);
            return new ViewHolder(view,mClickListener);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.itemView.setTag(mDataSet.get(position));
            ((TextView) holder.itemView).setText(mDataSet.get(position).getTitle());
        }

        @Override
        public int getItemCount() {
            return mDataSet.size();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(View itemView,OnClickListener clickListener) {
            super(itemView);
            itemView.setOnClickListener(clickListener);
        }
    }

    public interface onSpinnerListener {
        void onSpinnerClick(FlyoutSpinner flyoutSpinner);
        void onNothingSelected(FlyoutSpinner flyoutSpinner);
        void onItemSelected(FlyoutSpinner flyoutSpinner, FlyoutSpinnerItem flyoutSpinnerItem);
    }

    public static class FlyoutSpinnerItem {
        private String title;
        private Object data;

        public FlyoutSpinnerItem(@NonNull String title) {
            this.title = title;
        }

        @NonNull
        public String getTitle() {
            return title;
        }

        @Nullable
        public Object getData() {
            return data;
        }

        public void setData(@Nullable Object data) {
            this.data = data;
        }
    }
}
