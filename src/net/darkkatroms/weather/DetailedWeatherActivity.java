/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Copyright (C) 2016 DarkKat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.darkkatroms.weather;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.database.ContentObserver;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.internal.util.darkkat.DetailedWeatherHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import net.darkkatroms.weather.actionBar.ViewPagerTabs;
import net.darkkatroms.weather.fragments.CurrentWeatherFragment;
import net.darkkatroms.weather.fragments.ForecastWeatherFragment;
import net.darkkatroms.weather.R;
import net.darkkatroms.weather.WeatherInfo.DayForecast;

public class DetailedWeatherActivity extends Activity implements OnClickListener,
        OnLongClickListener {
    private static final String TAG = "DetailedWeatherActivity";

    private static final Uri WEATHER_URI =
            Uri.parse("content://net.darkkatroms.weather.provider/weather");

    private static final String CURRENT_WEATHER_TAG = "current_weather";
    private static final String FORECAST_WEATHER_TAG = "forecast_weather_";

    private static final int TOAST_SPACE_TOP = 24;

    private Handler mHandler;
    private ContentResolver mResolver;
    private WeatherObserver mWeatherObserver;

    private WeatherInfo mWeatherInfo;

    private ArrayList<String> mToolbarSubTitles = new ArrayList<String>();
    private ViewPager mTabPager;
    private ViewPagerTabs mViewPagerTabs;
    private TabPagerAdapter mTabPagerAdapter;
    private String[] mTabTitles;
    private final TabPagerListener mTabPagerListener = new TabPagerListener();

    private Toolbar mToolbar;
    private ImageView mUpdateButton;
    private CurrentWeatherFragment mCurrentWeatherFragment;
    private ForecastWeatherFragment[] mForecastWeatherFragments;

    private boolean mUpdateRequested = false;

    class WeatherObserver extends ContentObserver {
        WeatherObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            mResolver.registerContentObserver(WEATHER_URI, false, this);
        }

        void unobserve() {
            mResolver.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange) {
            if (getWeather() == null) {
                Log.e(TAG, "Error retrieving forecast data");
                if (mUpdateRequested) {
                    mUpdateRequested = false;
                }
            } else {
                mWeatherInfo = getWeather();
                updateWeather();
                if (mUpdateRequested) {
                    showToast(R.string.weather_updated);
                    mUpdateRequested = false;
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        mHandler = new Handler();
        mResolver = getContentResolver();
        mWeatherObserver = new WeatherObserver(mHandler);

        updateWeatherView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_update, menu);
        MenuItem itemUpdate = menu.findItem(R.id.item_update);
        LinearLayout updateButtonLayout = (LinearLayout) itemUpdate.getActionView();
        mUpdateButton = (ImageView) updateButtonLayout.findViewById(R.id.update_button);
        final boolean customizeColors = DetailedWeatherHelper.customizeColors(this);

        updateButtonLayout.setOnClickListener(this);
        updateButtonLayout.setOnLongClickListener(this);
        if (customizeColors) {
            final int iconColor = DetailedWeatherHelper.getActionBarIconColor(this);
            final int rippleColor = DetailedWeatherHelper.getActionBarRippleColor(this);
            mUpdateButton.setImageTintList(ColorStateList.valueOf(iconColor));
            ((RippleDrawable) updateButtonLayout.getBackground())
                    .setColor(ColorStateList.valueOf(rippleColor));
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        mWeatherObserver.unobserve();
        if (mTabPager != null) {
            mTabPager.setOnPageChangeListener(null);
        }
        super.onDestroy();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        finish();
    }

    private WeatherInfo getWeather() {
        return Config.getWeatherData(this);
    }

    private void updateWeatherView() {
        if (getWeather() == null) {
            Log.e(TAG, "Error retrieving forecast data, exiting");
            finish();
            return;
        }
        mWeatherInfo = getWeather();
        setTheme(getCustomThemeResId());
        setContentView(R.layout.detailed_weather_main);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        final FragmentManager fragmentManager = getFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();

        TimeZone myTimezone = TimeZone.getDefault();
        Calendar calendar = new GregorianCalendar(myTimezone);
        mTabTitles = new String[5];
        for (int i = 0; i <mTabTitles.length; i++) {
            if (i == 0) {
                mToolbarSubTitles.add(WeatherInfo.getFormattedDate(calendar.getTime(), false));
                mTabTitles[i] = getResources().getString(R.string.today_title);
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            } else {
                mToolbarSubTitles.add(WeatherInfo.getFormattedDate(calendar.getTime(), false));
                mTabTitles[i] = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG,
                        Locale.getDefault());
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
        }

        mTabPager = (ViewPager) findViewById(R.id.tab_pager);
        mTabPagerAdapter = new TabPagerAdapter();
        mTabPager.setAdapter(mTabPagerAdapter);
        mTabPager.setOnPageChangeListener(mTabPagerListener);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(getResources().getString(R.string.action_bar_current_title) + ", " + mWeatherInfo.getCity());
        mToolbar.setSubtitle(mToolbarSubTitles.get(0));

        final boolean customizeColors = DetailedWeatherHelper.customizeColors(this);
        if (customizeColors) {
            final int statusBarBgColor = DetailedWeatherHelper.getStatusBarBgColor(this);
            final int actionBarBgColor = DetailedWeatherHelper.getActionBarBgColor(this);
            final int textColorPrimary = DetailedWeatherHelper.getActionBarTextColor(this, true);
            final int textColorSecondary = DetailedWeatherHelper.getActionBarTextColor(this, false);
            View toolbarFrame = findViewById(R.id.toolbar_frame);
            getWindow().setStatusBarColor(statusBarBgColor);
            toolbarFrame.setBackgroundColor(actionBarBgColor);
            mToolbar.setTitleTextColor(textColorPrimary);
            mToolbar.setSubtitleTextColor(textColorSecondary);
        }
        setActionBar(mToolbar);

        mViewPagerTabs = (ViewPagerTabs) findViewById(R.id.lists_pager_header);
        mViewPagerTabs.setViewPager(mTabPager);

        Bundle b = getIntent().getExtras();
        int day = 0;
        if (b != null) {
            day = getTabPositionForTextDirection(b.getInt(DetailedWeatherHelper.DAY_INDEX));
        }
        if (day != 0) {
            mTabPager.setCurrentItem(day, false);
        }

        mCurrentWeatherFragment = (CurrentWeatherFragment)
                fragmentManager.findFragmentByTag(CURRENT_WEATHER_TAG);
        if (mCurrentWeatherFragment == null) {
            mCurrentWeatherFragment = new CurrentWeatherFragment();
            transaction.add(R.id.tab_pager, mCurrentWeatherFragment, CURRENT_WEATHER_TAG);

            mForecastWeatherFragments = new ForecastWeatherFragment[4];
            for (int i = 0; i < mForecastWeatherFragments.length; i++) {
                mForecastWeatherFragments[i] = new ForecastWeatherFragment();
                transaction.add(R.id.tab_pager, mForecastWeatherFragments[i], FORECAST_WEATHER_TAG + String.valueOf(i));
            }
        }

        transaction.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();

        ArrayList<String> days = mWeatherInfo.getHourForecastDays();
        mCurrentWeatherFragment.setForecastDay(days.get(0));
        for (int i = 0; i < mForecastWeatherFragments.length; i++) {
            mForecastWeatherFragments[i].setForecastDay(days.get(i + 1));
            mForecastWeatherFragments[i].setDayForecastIndex(i + 1);
        }
        mWeatherObserver.observe();
    }

    private void updateWeather() {
        if (mViewPagerTabs != null) {
            TimeZone myTimezone = TimeZone.getDefault();
            Calendar calendar = new GregorianCalendar(myTimezone);
            for (int i = 0; i <mTabTitles.length; i++) {
                if (i == 0) {
                    mToolbarSubTitles.add(WeatherInfo.getFormattedDate(calendar.getTime(), false));
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                } else {
                    mToolbarSubTitles.add(WeatherInfo.getFormattedDate(calendar.getTime(), false));
                    mTabTitles[i] = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG,
                            Locale.getDefault());
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                }
            }
            mToolbar.setTitle(getResources().getString(R.string.action_bar_current_title) + ", " + mWeatherInfo.getCity());
            final int tabPosition = getTabPositionForTextDirection(mTabPager.getCurrentItem());
            mToolbar.setSubtitle(mToolbarSubTitles.get(tabPosition));
            mViewPagerTabs.setTabTitles(mTabTitles);
            ArrayList<String> days = mWeatherInfo.getHourForecastDays();
            if (mCurrentWeatherFragment != null) {
                mCurrentWeatherFragment.setForecastDay(days.get(0));
                mCurrentWeatherFragment.updateWeather(mWeatherInfo);
            }
            if (mForecastWeatherFragments != null) {
                for (int i = 0; i < mForecastWeatherFragments.length; i++) {
                    mForecastWeatherFragments[i].setForecastDay(days.get(i + 1));
                    mForecastWeatherFragments[i].setDayForecastIndex(i + 1);
                    mForecastWeatherFragments[i].updateWeather(mWeatherInfo);
                }
            }
        }
    }

    private int getCustomThemeResId() {
        int index = DetailedWeatherHelper.getTheme(this);
        int resId = R.style.Theme_Material_DetailedWeather;
        if (index == DetailedWeatherHelper.THEME_DARKKAT) {
            resId = R.style.Theme_Material_DarkKat_DetailedWeather;
        } else if (index == DetailedWeatherHelper.THEME_MATERIAL_LIGHT) {
            resId = R.style.Theme_Material_Light_DetailedWeather;
        }
        return resId;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.update_button_layout) {
            RotateAnimation anim = new RotateAnimation(0.0f, 360.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setInterpolator(new LinearInterpolator());
            anim.setDuration(700);
            anim.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mUpdateButton.setAnimation(null);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            mUpdateButton.startAnimation(anim);
            mUpdateRequested = true;
            WeatherService.startUpdate(this, true);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.update_button_layout) {
            showToast(R.string.update_weather);
            return true;
        }
        return false;
    }

    private void showToast(int resId) {
		float density = getResources().getDisplayMetrics().density;
        int actionbarHeight = findViewById(R.id.toolbar_parent).getHeight();
        int spaceTopDP = TOAST_SPACE_TOP * Math.round(density);

        Toast toast = Toast.makeText(this, resId, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, actionbarHeight + spaceTopDP);
        toast.show();
    }

    private class TabPagerAdapter extends PagerAdapter {
        private final FragmentManager mFragmentManager;
        private FragmentTransaction mCurTransaction = null;

        private Fragment mCurrentPrimaryItem;

        public TabPagerAdapter() {
            mFragmentManager = getFragmentManager();
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public int getItemPosition(Object object) {
            if (object == mCurrentWeatherFragment) {
                    return getTabPositionForTextDirection(0);
            }
            for (int i = 0; i < mForecastWeatherFragments.length; i++) {
                if (object == mForecastWeatherFragments[i]) {
                    return getTabPositionForTextDirection(i + 1);
                }
            }
            return POSITION_NONE;
        }

        @Override
        public void startUpdate(ViewGroup container) {
        }

        private Fragment getFragment(int position) {
            position = getTabPositionForTextDirection(position);
            if (position == 0) {
                return mCurrentWeatherFragment;
            } else if (position == 1) {
                return mForecastWeatherFragments[position - 1];
            } else if (position == 2) {
                return mForecastWeatherFragments[position - 1];
            } else if (position == 3) {
                return mForecastWeatherFragments[position - 1];
            } else if (position == 4) {
                return mForecastWeatherFragments[position - 1];
            }
            throw new IllegalArgumentException("position: " + position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (mCurTransaction == null) {
                mCurTransaction = mFragmentManager.beginTransaction();
            }
            Fragment f = getFragment(position);
            mCurTransaction.show(f);

            f.setUserVisibleHint(f == mCurrentPrimaryItem);
            return f;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (mCurTransaction == null) {
                mCurTransaction = mFragmentManager.beginTransaction();
            }
            mCurTransaction.hide((Fragment) object);
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            if (mCurTransaction != null) {
                mCurTransaction.commitAllowingStateLoss();
                mCurTransaction = null;
                mFragmentManager.executePendingTransactions();
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return ((Fragment) object).getView() == view;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            Fragment fragment = (Fragment) object;
            if (mCurrentPrimaryItem != fragment) {
                if (mCurrentPrimaryItem != null) {
                    mCurrentPrimaryItem.setUserVisibleHint(false);
                }
                if (fragment != null) {
                    fragment.setUserVisibleHint(true);
                }
                mCurrentPrimaryItem = fragment;
            }
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabTitles[position];
        }
    }

    private class TabPagerListener implements ViewPager.OnPageChangeListener {

        // This package-protected constructor is here because of a possible compiler bug.
        // PeopleActivity$1.class should be generated due to the private outer/inner class access
        // needed here.  But for some reason, PeopleActivity$1.class is missing.
        // Since $1 class is needed as a jvm work around to get access to the inner class,
        // changing the constructor to package-protected or public will solve the problem.
        // To verify whether $1 class is needed, javap PeopleActivity$TabPagerListener and look for
        // references to PeopleActivity$1.
        //
        // When the constructor is private and PeopleActivity$1.class is missing, proguard will
        // correctly catch this and throw warnings and error out the build on user/userdebug builds.
        //
        // All private inner classes below also need this fix.
        TabPagerListener() {}

        @Override
        public void onPageScrollStateChanged(int state) {
            mViewPagerTabs.onPageScrollStateChanged(state);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            mViewPagerTabs.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }

        @Override
        public void onPageSelected(int position) {
            mViewPagerTabs.onPageSelected(position);
            if (mToolbar != null) {
                final int tabPosition = getTabPositionForTextDirection(position);
                mToolbar.setSubtitle(mToolbarSubTitles.get(tabPosition));
            }
        }
    }

    private boolean isRTL() {
        final Locale locale = Locale.getDefault();
        return TextUtils.getLayoutDirectionFromLocale(locale) == View.LAYOUT_DIRECTION_RTL;
    }

    /**
     * Returns the tab position adjusted for the text direction.
     */
    private int getTabPositionForTextDirection(int position) {
        if (isRTL()) {
            return 5 - 1 - position;
        }
        return position;
    }
}
