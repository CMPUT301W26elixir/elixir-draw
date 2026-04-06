package com.example.allot.view.shared;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.example.allot.R;
/**
 * Renders the shared bottom navigation bar used across the app.
 */
public class BottomNavBarView extends LinearLayout {

    /**
     * Represents the available tabs in the bottom navigation bar.
     */
    public enum Tab {
        EXPLORE,
        SAVED,
        MY_EVENTS,
        SCAN,
        PROFILE
    }

    private LinearLayout exploreTab;
    private LinearLayout savedTab;
    private LinearLayout myEventsTab;
    private LinearLayout scanTab;
    private LinearLayout profileTab;

    private ImageView exploreIcon;
    private ImageView savedIcon;
    private ImageView myEventsIcon;
    private ImageView scanIcon;
    private ImageView profileIcon;

    private TextView exploreLabel;
    private TextView savedLabel;
    private TextView myEventsLabel;
    private TextView scanLabel;
    private TextView profileLabel;

    /**
     * Creates a new BottomNavBarView instance.
     *
     * @param context the context
     */
    public BottomNavBarView(Context context) {
        super(context);
        init(context);
    }

    /**
     * Creates a new BottomNavBarView instance.
     *
     * @param context the context
     * @param attrs the attrs
     */
    public BottomNavBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * Creates a new BottomNavBarView instance.
     *
     * @param context the context
     * @param attrs the attrs
     * @param defStyleAttr the def style attr
     */
    public BottomNavBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * Performs init.
     *
     * @param context the context
     */
    private void init(Context context) {
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_bottom_nav_bar, this, true);

        exploreTab = findViewById(R.id.exploreTab);
        savedTab = findViewById(R.id.savedTab);
        myEventsTab = findViewById(R.id.myEventsTab);
        scanTab = findViewById(R.id.scanTab);
        profileTab = findViewById(R.id.profileTab);

        exploreIcon = findViewById(R.id.exploreIcon);
        savedIcon = findViewById(R.id.savedIcon);
        myEventsIcon = findViewById(R.id.myEventsIcon);
        scanIcon = findViewById(R.id.scanIcon);
        profileIcon = findViewById(R.id.profileIcon);

        exploreLabel = findViewById(R.id.exploreLabel);
        savedLabel = findViewById(R.id.savedLabel);
        myEventsLabel = findViewById(R.id.myEventsLabel);
        scanLabel = findViewById(R.id.scanLabel);
        profileLabel = findViewById(R.id.profileLabel);

        setTabEnabled(exploreTab, false);
        setTabEnabled(savedTab, false);
        setTabEnabled(myEventsTab, false);
        setTabEnabled(scanTab, false);
        setTabEnabled(profileTab, false);
        setSelectedTab(Tab.EXPLORE);
    }

    /**
     * Updates the selected tab.
     *
     * @param selectedTab the selected tab
     */
    public void setSelectedTab(Tab selectedTab) {
        updateTabState(exploreTab, exploreIcon, exploreLabel, selectedTab == Tab.EXPLORE);
        updateTabState(savedTab, savedIcon, savedLabel, selectedTab == Tab.SAVED);
        updateTabState(myEventsTab, myEventsIcon, myEventsLabel, selectedTab == Tab.MY_EVENTS);
        updateTabState(scanTab, scanIcon, scanLabel, selectedTab == Tab.SCAN);
        updateTabState(profileTab, profileIcon, profileLabel, selectedTab == Tab.PROFILE);
    }

    /**
     * Updates the on tab click listener.
     *
     * @param tab the tab
     * @param listener the listener
     */
    public void setOnTabClickListener(Tab tab, @Nullable OnClickListener listener) {
        LinearLayout tabView = getTabView(tab);
        setTabEnabled(tabView, listener != null);
        tabView.setOnClickListener(listener);
    }

    /**
     * Updates the on tab selected listener.
     *
     * @param listener the listener
     */
    public void setOnTabSelectedListener(@Nullable OnTabSelectedListener listener) {
        setOnTabClickListener(Tab.EXPLORE,
                listener == null ? null : view -> listener.onTabSelected(Tab.EXPLORE));
        setOnTabClickListener(Tab.SAVED,
                listener == null ? null : view -> listener.onTabSelected(Tab.SAVED));
        setOnTabClickListener(Tab.MY_EVENTS,
                listener == null ? null : view -> listener.onTabSelected(Tab.MY_EVENTS));
        setOnTabClickListener(Tab.SCAN,
                listener == null ? null : view -> listener.onTabSelected(Tab.SCAN));
        setOnTabClickListener(Tab.PROFILE,
                listener == null ? null : view -> listener.onTabSelected(Tab.PROFILE));
    }

    /**
     * Updates the tab enabled.
     *
     * @param tabView the tab view
     * @param enabled the enabled
     */
    private void setTabEnabled(LinearLayout tabView, boolean enabled) {
        tabView.setEnabled(enabled);
        tabView.setClickable(enabled);
        tabView.setFocusable(enabled);
    }

    /**
     * Returns the tab view.
     *
     * @param tab the tab
     * @return the tab view
     */
    private LinearLayout getTabView(Tab tab) {
        switch (tab) {
            case EXPLORE:
                return exploreTab;
            case SAVED:
                return savedTab;
            case MY_EVENTS:
                return myEventsTab;
            case SCAN:
                return scanTab;
            case PROFILE:
            default:
                return profileTab;
        }
    }

    /**
     * Performs update tab state.
     *
     * @param tabView the tab view
     * @param iconView the icon view
     * @param labelView the label view
     * @param selected the selected
     */
    private void updateTabState(LinearLayout tabView, ImageView iconView, TextView labelView, boolean selected) {
        int color = ContextCompat.getColor(
                getContext(),
                selected ? R.color.bottom_nav_selected : R.color.bottom_nav_unselected
        );

        iconView.setImageTintList(ColorStateList.valueOf(color));
        labelView.setTextColor(color);
        tabView.setSelected(selected);
    }

    /**
     * Listener interface for receiving bottom navigation tab selection events.
     */
    public interface OnTabSelectedListener {

        /**
         * Handles the tab selected callback.
         *
         * @param tab the tab
         */
        void onTabSelected(Tab tab);
    }
}








