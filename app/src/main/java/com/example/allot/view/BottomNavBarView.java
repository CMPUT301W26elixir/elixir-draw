package com.example.allot.view;

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
 * Custom bottom navigation bar view for the app.
 * Supports 5 tabs: Explore, Saved, My Events, Scan, and Profile.
 * Handles UI updates for selected/unselected state and allows
 * click listeners to be attached.
 */
public class BottomNavBarView extends LinearLayout {

    /** Enum representing available tabs in the navigation bar */
    public enum Tab {
        EXPLORE,
        SAVED,
        MY_EVENTS,
        SCAN,
        PROFILE
    }

    // LinearLayout containers for each tab
    private LinearLayout exploreTab;
    private LinearLayout savedTab;
    private LinearLayout myEventsTab;
    private LinearLayout scanTab;
    private LinearLayout profileTab;

    // ImageView icons for each tab
    private ImageView exploreIcon;
    private ImageView savedIcon;
    private ImageView myEventsIcon;
    private ImageView scanIcon;
    private ImageView profileIcon;

    // TextView labels for each tab
    private TextView exploreLabel;
    private TextView savedLabel;
    private TextView myEventsLabel;
    private TextView scanLabel;
    private TextView profileLabel;

    /**
     * Constructor used when creating view programmatically.
     *
     * @param context Context of the parent activity or fragment
     */
    public BottomNavBarView(Context context) {
        super(context);
        init(context);
    }

    /**
     * Constructor called when inflating from XML.
     *
     * @param context Context of the parent activity or fragment
     * @param attrs XML attributes for the view
     */
    public BottomNavBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * Constructor with style attributes.
     *
     * @param context Context of the parent activity or fragment
     * @param attrs XML attributes
     * @param defStyleAttr Default style
     */
    public BottomNavBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * Initializes the view by inflating the layout, assigning
     * member variables, and setting default tab states.
     *
     * @param context Context for inflating layout
     */
    private void init(Context context) {
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_bottom_nav_bar, this, true);

        // Assign all tab containers
        exploreTab = findViewById(R.id.exploreTab);
        savedTab = findViewById(R.id.savedTab);
        myEventsTab = findViewById(R.id.myEventsTab);
        scanTab = findViewById(R.id.scanTab);
        profileTab = findViewById(R.id.profileTab);

        // Assign tab icons
        exploreIcon = findViewById(R.id.exploreIcon);
        savedIcon = findViewById(R.id.savedIcon);
        myEventsIcon = findViewById(R.id.myEventsIcon);
        scanIcon = findViewById(R.id.scanIcon);
        profileIcon = findViewById(R.id.profileIcon);

        // Assign tab labels
        exploreLabel = findViewById(R.id.exploreLabel);
        savedLabel = findViewById(R.id.savedLabel);
        myEventsLabel = findViewById(R.id.myEventsLabel);
        scanLabel = findViewById(R.id.scanLabel);
        profileLabel = findViewById(R.id.profileLabel);

        // Disable all tabs initially
        setTabEnabled(exploreTab, false);
        setTabEnabled(savedTab, false);
        setTabEnabled(myEventsTab, false);
        setTabEnabled(scanTab, false);
        setTabEnabled(profileTab, false);

        // Set default selected tab to Explore
        setSelectedTab(Tab.EXPLORE);
    }

    /**
     * Updates the visual state of all tabs based on which tab is selected.
     *
     * @param selectedTab The tab to mark as selected
     */
    public void setSelectedTab(Tab selectedTab) {
        updateTabState(exploreTab, exploreIcon, exploreLabel, selectedTab == Tab.EXPLORE);
        updateTabState(savedTab, savedIcon, savedLabel, selectedTab == Tab.SAVED);
        updateTabState(myEventsTab, myEventsIcon, myEventsLabel, selectedTab == Tab.MY_EVENTS);
        updateTabState(scanTab, scanIcon, scanLabel, selectedTab == Tab.SCAN);
        updateTabState(profileTab, profileIcon, profileLabel, selectedTab == Tab.PROFILE);
    }

    /**
     * Attaches a click listener to a specific tab.
     *
     * @param tab The tab to attach the listener to
     * @param listener The OnClickListener to handle clicks (nullable)
     */
    public void setOnTabClickListener(Tab tab, @Nullable OnClickListener listener) {
        LinearLayout tabView = getTabView(tab);
        setTabEnabled(tabView, listener != null);
        tabView.setOnClickListener(listener);
    }

    /**
     * Attaches a single OnTabSelectedListener to handle clicks for all tabs.
     *
     * @param listener Listener invoked when a tab is selected (nullable)
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
     * Enables or disables a tab for interaction.
     *
     * @param tabView The tab's LinearLayout container
     * @param enabled True to enable, false to disable
     */
    private void setTabEnabled(LinearLayout tabView, boolean enabled) {
        tabView.setEnabled(enabled);
        tabView.setClickable(enabled);
        tabView.setFocusable(enabled);
    }

    /**
     * Returns the LinearLayout container for a given tab.
     *
     * @param tab The tab to retrieve
     * @return LinearLayout container corresponding to the tab
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
     * Updates the visual appearance of a tab based on selection state.
     *
     * @param tabView Tab container to update
     * @param iconView Icon ImageView of the tab
     * @param labelView Label TextView of the tab
     * @param selected True if tab is selected, false otherwise
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
     * Interface for receiving tab selection events.
     */
    public interface OnTabSelectedListener {
        void onTabSelected(Tab tab);
    }
}