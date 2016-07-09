package com.softdesign.devintensive.ui.view.behaviors;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Отвечает за обработку скроллинга хедера
 */
public class HeaderBehavior extends CoordinatorLayout.Behavior<LinearLayout> {
    private Context mContext;

    public HeaderBehavior() {
    }

    public HeaderBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, LinearLayout child, View dependency) {
        return dependency instanceof NestedScrollView;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, LinearLayout child, View dependency) {
        float startToolbarPosition = dependency.getY();
        int maxScrollDistance = (int) (startToolbarPosition - getStatusBarHeight());
        float percentComplete = startToolbarPosition / maxScrollDistance;
        int padding = (int) (getPaddingTop() * (0.9 - percentComplete));
        child.setPadding(0, padding, 0, padding);
        dependency.setPadding(0, child.getHeight(), 0, 0);
        child.setY(dependency.getY());
        return true;
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private int getPaddingTop() {
        int result = 0;
        int resourceId = mContext.getResources().getIdentifier("spacing_medial_28", "dimen", mContext.getPackageName());
        if (resourceId > 0) {
            result = mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}