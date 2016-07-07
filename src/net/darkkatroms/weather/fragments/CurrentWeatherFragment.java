/*
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

package net.darkkatroms.weather.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.res.ColorStateList;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.util.darkkat.DetailedWeatherHelper;
import com.android.internal.util.darkkat.WeatherHelper;

import net.darkkatroms.weather.Config;
import net.darkkatroms.weather.R;
import net.darkkatroms.weather.WeatherInfo;
import net.darkkatroms.weather.WeatherInfo.HourForecast;

import java.util.ArrayList;

public class CurrentWeatherFragment extends Fragment {
    private WeatherInfo mWeatherInfo;
    private LayoutInflater mInflater;

    private LinearLayout mCardsLayout;
    private View mCard;

    private TextView mCurrent;
    private TextView mTime;
    private ImageView mImage;
    private View mImageDivider;
    private TextView mTemp;
    private TextView mTempLowHight;
    private View mTempDivider;
    private TextView mCondition;
    private TextView[] mDayTempsValues;
    private View mExpandedContent;
    private TextView mPrecipitationTitle;
    private TextView mPrecipitationValue;
    private TextView mWindTitle;
    private TextView mWindValue;
    private TextView mSunriseTitle;
    private TextView mSunriseValue;
    private TextView mHumidityTitle;
    private TextView mHumidityValue;
    private TextView mPressureTitle;
    private TextView mPressureValue;
    private TextView mSunsetTitle;
    private TextView mSunsetValue;

    private View mExpandCollapseButtonDivider;
    private TextView mProviderLink;
    private LinearLayout mExpandCollapseButton;
    private TextView mExpandCollapseButtonText;
    private ImageView mExpandCollapseButtonIcon;

    private String mForecastDay;
    private ArrayList<ViewHolder> mHolders = new ArrayList<ViewHolder>();

    private ValueAnimator mAnimator;
    private int mExpandedContentHeight = 0;
    private boolean mAnimateExpansion = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        return inflateAndSetupView(inflater, container, savedInstanceState);
    }

    private View inflateAndSetupView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mWeatherInfo = Config.getWeatherData(getActivity());
        mInflater = inflater;

        View layout = mInflater.inflate(R.layout.detailed_weather_current, container, false);
        mCardsLayout = (LinearLayout) layout.findViewById(R.id.current_cards_layout);
        mCard = layout.findViewById(R.id.current_card);

        mCurrent = (TextView) layout.findViewById(R.id.current_weather);
        mTime = (TextView) layout.findViewById(R.id.current_time);
        mImage = (ImageView) layout.findViewById(R.id.current_condition_image);
        mImageDivider = layout.findViewById(R.id.current_image_divider);
        mTemp = (TextView) layout.findViewById(R.id.current_temp);
        mTempDivider = layout.findViewById(R.id.current_temp_divider);
        mTempLowHight = (TextView) layout.findViewById(R.id.current_low_high);
        mCondition = (TextView) layout.findViewById(R.id.current_condition);
        TextView[] dayTempsTitles = {
            (TextView) layout.findViewById(R.id.current_temp_morning_title),
            (TextView) layout.findViewById(R.id.current_temp_day_title),
            (TextView) layout.findViewById(R.id.current_temp_evening_title),
            (TextView) layout.findViewById(R.id.current_temp_night_title)
        };
        mDayTempsValues = new TextView[] {
                (TextView) layout.findViewById(R.id.current_temp_morning_value),
                (TextView) layout.findViewById(R.id.current_temp_day_value),
                (TextView) layout.findViewById(R.id.current_temp_evening_value),
                (TextView) layout.findViewById(R.id.current_temp_night_value)
        };
        mExpandedContent = layout.findViewById(R.id.current_expanded_content_layout);
        mPrecipitationTitle = (TextView) layout.findViewById(R.id.current_precipitation_title);
        mPrecipitationValue = (TextView) layout.findViewById(R.id.current_precipitation_value);
        mWindTitle = (TextView) layout.findViewById(R.id.current_wind_title);
        mWindValue = (TextView) layout.findViewById(R.id.current_wind_value);
        mSunriseTitle = (TextView) layout.findViewById(R.id.current_sunrise_title);
        mSunriseValue = (TextView) layout.findViewById(R.id.current_sunrise_value);
        mHumidityTitle = (TextView) layout.findViewById(R.id.current_humidity_title);
        mHumidityValue = (TextView) layout.findViewById(R.id.current_humidity_value);
        mPressureTitle = (TextView) layout.findViewById(R.id.current_pressure_title);
        mPressureValue = (TextView) layout.findViewById(R.id.current_pressure_value);
        mSunsetTitle = (TextView) layout.findViewById(R.id.current_sunset_title);
        mSunsetValue = (TextView) layout.findViewById(R.id.current_sunset_value);
        mExpandCollapseButtonDivider = 
                layout.findViewById(R.id.current_expand_collapse_button_divider);
        mProviderLink = 
                (TextView) layout.findViewById(R.id.current_provider_link);
        mExpandCollapseButton = 
                (LinearLayout) layout.findViewById(R.id.current_expand_collapse_button);
        mExpandCollapseButtonText = 
                (TextView) layout.findViewById(R.id.current_expand_collapse_button_text);
        mExpandCollapseButtonIcon = 
                (ImageView) layout.findViewById(R.id.current_expand_collapse_button_icon);

        mExpandedContent.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mExpandedContentHeight = mExpandedContent.getHeight();
                mExpandedContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mExpandedContent.setVisibility(View.GONE);
            }
        });

        mProviderLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWeatherInfo != null) {
                    String cityId = mWeatherInfo.getId();
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("http://openweathermap.org/city/" + cityId));
                    startActivity(intent);
                }
            }
        });
        mExpandCollapseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAnimator != null) {
                    mAnimator.start();
                }
            }
        });

        final boolean customizeColors = DetailedWeatherHelper.customizeColors(getActivity());
        final int conditionImageColor = DetailedWeatherHelper.getConditionImageColor(getActivity());
        if (customizeColors) {
            final int backgroundColor = DetailedWeatherHelper.getContentBackgroundColor(getActivity());
            final int cardBackground = DetailedWeatherHelper.getCardsBackgroundColor(getActivity());
            final int textColorPrimary = DetailedWeatherHelper.getCardsTextColor(getActivity(), true);
            final int textColorSecondary = DetailedWeatherHelper.getCardsTextColor(getActivity(), false);
            final int iconColor = DetailedWeatherHelper.getCardsIconColor(getActivity());
            final int dividerAlpha = DetailedWeatherHelper.getDividerAlpha(getActivity());
            final int dividerColor = (dividerAlpha << 24) | (textColorPrimary & 0x00ffffff);
            final int rippleColor = DetailedWeatherHelper.getCardsRippleColor(getActivity());

            layout.setBackgroundColor(backgroundColor);
            mCard.setBackgroundTintList(ColorStateList.valueOf(cardBackground));

            mCurrent.setTextColor(textColorPrimary);
            mTime.setTextColor(textColorSecondary);
            if (conditionImageColor != 0) {
                mImage.setImageTintList(ColorStateList.valueOf(conditionImageColor));
            } else {
                mImage.setImageTintList(null);
            }

            mImageDivider.setBackgroundColor(dividerColor);
            mTemp.setTextColor(textColorPrimary);
            mTempDivider.setBackgroundColor(dividerColor);
            mTempLowHight.setTextColor(textColorSecondary);
            mCondition.setTextColor(textColorPrimary);

            for (int i = 0; i < mDayTempsValues.length; i++) {
                dayTempsTitles[i].setTextColor(textColorPrimary);
                mDayTempsValues[i].setTextColor(textColorSecondary);
            }

            mPrecipitationTitle.setTextColor(textColorPrimary);
            mPrecipitationValue.setTextColor(textColorSecondary);
            mWindTitle.setTextColor(textColorPrimary);
            mWindValue.setTextColor(textColorSecondary);
            mSunriseTitle.setTextColor(textColorPrimary);
            mSunriseValue.setTextColor(textColorSecondary);
            mHumidityTitle.setTextColor(textColorPrimary);
            mHumidityValue.setTextColor(textColorSecondary);
            mPressureTitle.setTextColor(textColorPrimary);
            mPressureValue.setTextColor(textColorSecondary);
            mSunsetTitle.setTextColor(textColorPrimary);
            mSunsetValue.setTextColor(textColorSecondary);
            mExpandCollapseButtonDivider.setBackgroundColor(dividerColor);
            ((RippleDrawable) mProviderLink.getBackground())
                    .setColor(ColorStateList.valueOf(rippleColor));
            ((RippleDrawable) mExpandCollapseButton.getBackground())
                    .setColor(ColorStateList.valueOf(rippleColor));
            mExpandCollapseButtonText.setTextColor(textColorPrimary);
            mExpandCollapseButtonIcon.setImageTintList(ColorStateList.valueOf(iconColor));
        } else {
            if (conditionImageColor == 0) {
                mImage.setImageTintList(null);
            }
        }

        if (mWeatherInfo != null) {
            Drawable icon = mWeatherInfo.getConditionIcon(DetailedWeatherHelper.getConditionIconType(
                    getActivity()), mWeatherInfo.getConditionCode());
            final String[] tempValues = {
                mWeatherInfo.getForecasts().get(0).getFormattedMorning(),
                mWeatherInfo.getForecasts().get(0).getFormattedDay(),
                mWeatherInfo.getForecasts().get(0).getFormattedEvening(),
                mWeatherInfo.getForecasts().get(0).getFormattedNight()
            };

            mTime.setText(mWeatherInfo.getTime());
            mImage.setImageDrawable(icon);
            mTemp.setText(mWeatherInfo.getFormattedTemperature());
            mTempLowHight.setText(mWeatherInfo.getFormattedLow() + " | " + mWeatherInfo.getFormattedHigh());
            mCondition.setText(mWeatherInfo.getCondition());
            for (int i = 0; i < mDayTempsValues.length; i++) {
                mDayTempsValues[i].setText(tempValues[i]);
            }
            setPrecipitation(mWeatherInfo);
            mWindValue.setText(mWeatherInfo.getFormattedWind());
            mSunriseValue.setText(mWeatherInfo.getSunrise());
            mHumidityValue.setText(mWeatherInfo.getFormattedHumidity());
            mPressureValue.setText(mWeatherInfo.getFormattedPressure());
            mSunsetValue.setText(mWeatherInfo.getSunset());

            if (mCardsLayout != null && mForecastDay != null) {
                ArrayList<HourForecast> hourForecasts = mWeatherInfo.getHourForecastsDay(mForecastDay);
                if (hourForecasts.size() != 0) {
                    for (int i = 0; i < hourForecasts.size(); i++) {
                        HourForecast h = hourForecasts.get(i);
                        ViewHolder holder = new ViewHolder(mInflater);
                        holder.setColors(customizeColors);
                        holder.updateWeather(h);
                        mHolders.add(holder);
                        mCardsLayout.addView(holder.getForecastCard());
                    }
                }
            }
        }
        mAnimator = createAnimator();

        return layout;
    }

    public void setForecastDay(String forecastDay) {
        mForecastDay = forecastDay;
    }

    public void updateWeather(WeatherInfo weather) {
        if (weather == null) {
            return;
        }
        mWeatherInfo = weather;

        Drawable icon = mWeatherInfo.getConditionIcon(DetailedWeatherHelper.getConditionIconType(
                getActivity()), mWeatherInfo.getConditionCode());
        final String[] tempValues = {
            mWeatherInfo.getForecasts().get(0).getFormattedMorning(),
            mWeatherInfo.getForecasts().get(0).getFormattedDay(),
            mWeatherInfo.getForecasts().get(0).getFormattedEvening(),
            mWeatherInfo.getForecasts().get(0).getFormattedNight()
        };

        mTime.setText(mWeatherInfo.getTime());
        mImage.setImageDrawable(icon);
        mTemp.setText(mWeatherInfo.getFormattedTemperature());
        mTempLowHight.setText(mWeatherInfo.getFormattedLow() + " | " + mWeatherInfo.getFormattedHigh());
        mCondition.setText(mWeatherInfo.getCondition());
        for (int i = 0; i < mDayTempsValues.length; i++) {
            mDayTempsValues[i].setText(tempValues[i]);
        }
        setPrecipitation(mWeatherInfo);
        mWindValue.setText(mWeatherInfo.getFormattedWind());
        mSunriseValue.setText(mWeatherInfo.getSunrise());
        mHumidityValue.setText(mWeatherInfo.getFormattedHumidity());
        mPressureValue.setText(mWeatherInfo.getFormattedPressure());
        mSunsetValue.setText(mWeatherInfo.getSunset());

        if (mCardsLayout != null && mForecastDay != null) {
            ArrayList<HourForecast> hourForecasts = mWeatherInfo.getHourForecastsDay(mForecastDay);
            if (hourForecasts.size() != 0) {
                if (mHolders.size() != hourForecasts.size()) {
                    final boolean customizeColors =
                            DetailedWeatherHelper.customizeColors(getActivity());
                    if (mCardsLayout.getChildCount() > 1) {
                        mCardsLayout.removeViews(1, mCardsLayout.getChildCount() - 1);
                    }
                    for (int i = 0; i < hourForecasts.size(); i++) {
                        HourForecast h = hourForecasts.get(i);
                        ViewHolder holder = new ViewHolder(mInflater);
                        holder.setColors(customizeColors);
                        holder.updateWeather(h);
                        mHolders.add(holder);
                        mCardsLayout.addView(holder.getForecastCard());
                    }
                } else {
                    for (int i = 0; i < hourForecasts.size(); i++) {
                        HourForecast h = hourForecasts.get(i);
                        mHolders.get(i).updateWeather(h);
                    }
                }
            }
        }
    }

    private  ValueAnimator createAnimator() {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = animation.getAnimatedFraction();
                float height;
                float alpha = value;
                if (mAnimateExpansion) {
                    height = mExpandedContentHeight * value;
                    
                } else {
                    height = mExpandedContentHeight * (1 - value);
                    alpha = 1 - value;
                }
                mExpandedContent.getLayoutParams().height = Math.round(height);
                mExpandCollapseButtonDivider.setAlpha(alpha);
                mExpandedContent.requestLayout();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (mAnimateExpansion) {
                    mExpandedContent.setVisibility(View.VISIBLE);
                    mExpandCollapseButtonDivider.setVisibility(View.VISIBLE);
                }
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mAnimateExpansion) {
                    mExpandCollapseButtonIcon.setImageResource(R.drawable.ic_expand_less);
                    mExpandCollapseButtonText.setText(R.string.collapse_card);
                } else {
                    mExpandCollapseButtonIcon.setImageResource(R.drawable.ic_expand_more);
                    mExpandCollapseButtonText.setText(R.string.expand_card);
                    mExpandedContent.setVisibility(View.GONE);
                    mExpandCollapseButtonDivider.setVisibility(View.GONE);
                }
                mAnimateExpansion = !mAnimateExpansion;
            }
        });
        return animator;
    }

    private void setPrecipitation(WeatherInfo w) {
        final String rain1H = w.getFormattedRain1H();
        final String rain3H = w.getFormattedRain3H();
        final String snow1H = w.getFormattedSnow1H();
        final String snow3H = w.getFormattedSnow3H();
        final String noValue = getActivity().getResources().getString(R.string.no_precipitation_value);
        if (!snow1H.equals(WeatherInfo.NO_VALUE)) {
            mPrecipitationValue.setText(snow1H);
        } else if (!snow3H.equals(WeatherInfo.NO_VALUE)) {
            mPrecipitationValue.setText(snow3H);
        } else if (!rain1H.equals(WeatherInfo.NO_VALUE)) {
            mPrecipitationValue.setText(rain1H);
        } else if (!rain3H.equals(WeatherInfo.NO_VALUE)) {
            mPrecipitationValue.setText(rain3H);
        } else {
            mPrecipitationValue.setText(noValue);
        }
    }

    private class ViewHolder {
        public View cardLayout;
        public View card;
        public View expandedContent;
        public TextView timeValue;
        public ImageView image;
        public View imageDivider;
        public TextView tempValue;
        public View tempConditionDivider;
        public TextView conditionValue;
        public TextView precipitationTitle;
        public TextView precipitationValue;
        public TextView windTitle;
        public TextView windValue;
        public TextView humidityTitle;
        public TextView humidityValue;
        public TextView pressureTitle;
        public TextView pressureValue;
        public View expandCollapseButtonDivider;
        public LinearLayout expandCollapseButton;
        public TextView expandCollapseButtonText;
        public ImageView expandCollapseButtonIcon;
        public ValueAnimator animator;
        public int expandedContentHeight = 0;
        public boolean animateExpansion = true;

        public ViewHolder(LayoutInflater inflater) {
            cardLayout = inflater.inflate(R.layout.forecast_weather_card, null);
            card = cardLayout.findViewById(R.id.forecast_card);
            expandedContent = cardLayout.findViewById(R.id.forecast_expanded_content_layout);
            timeValue = (TextView) cardLayout.findViewById(R.id.forecast_time);
            image = (ImageView) cardLayout.findViewById(R.id.forecast_condition_image);
            imageDivider = cardLayout.findViewById(R.id.forecast_image_divider);
            tempValue = (TextView) cardLayout.findViewById(R.id.forecast_temp_value);
            tempConditionDivider = cardLayout.findViewById(R.id.forecast_temp_condition_divider);
            conditionValue = (TextView) cardLayout.findViewById(R.id.forecast_condition_value);
            precipitationTitle = 
                    (TextView) cardLayout.findViewById(R.id.forecast_precipitation_title);
            precipitationValue = 
                    (TextView) cardLayout.findViewById(R.id.forecast_precipitation_value);
            windTitle = (TextView) cardLayout.findViewById(R.id.forecast_wind_title);
            windValue = (TextView) cardLayout.findViewById(R.id.forecast_wind_value);
            humidityTitle = (TextView) cardLayout.findViewById(R.id.forecast_humidity_title);
            humidityValue = (TextView) cardLayout.findViewById(R.id.forecast_humidity_value);
            pressureTitle = (TextView) cardLayout.findViewById(R.id.forecast_pressure_title);
            pressureValue = (TextView) cardLayout.findViewById(R.id.forecast_pressure_value);
            expandCollapseButtonDivider = 
                    cardLayout.findViewById(R.id.forecast_expand_collapse_button_divider);
            expandCollapseButton = 
                    (LinearLayout) cardLayout.findViewById(R.id.forecast_expand_collapse_button);
            expandCollapseButtonText = 
                    (TextView) cardLayout.findViewById(R.id.forecast_expand_collapse_button_text);
            expandCollapseButtonIcon = 
                    (ImageView) cardLayout.findViewById(R.id.forecast_expand_collapse_button_icon);

            expandedContent.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    expandedContentHeight = expandedContent.getHeight();
                    expandedContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    expandedContent.setVisibility(View.GONE);
                }
            });

            expandCollapseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (animator != null) {
                        animator.start();
                    }
                }
            });

            animator = createForecastAnimator();
        }

        public void setColors(boolean customizeColors) {
            final int conditionImageColor = 
                    DetailedWeatherHelper.getConditionImageColor(getActivity());
            if (customizeColors) {
                final int cardBackground = DetailedWeatherHelper.getCardsBackgroundColor(getActivity());
                final int textColorPrimary = 
                        DetailedWeatherHelper.getCardsTextColor(getActivity(), true);
                final int textColorSecondary = 
                        DetailedWeatherHelper.getCardsTextColor(getActivity(), false);
                final int iconColor = DetailedWeatherHelper.getCardsIconColor(getActivity());
                final int dividerAlpha = DetailedWeatherHelper.getDividerAlpha(getActivity());
                final int dividerColor = (dividerAlpha << 24) | (textColorPrimary & 0x00ffffff);
                final int rippleColor = DetailedWeatherHelper.getCardsRippleColor(getActivity());

                card.setBackgroundTintList(ColorStateList.valueOf(cardBackground));
                timeValue.setTextColor(textColorPrimary);

                if (conditionImageColor != 0) {
                    image.setImageTintList(ColorStateList.valueOf(conditionImageColor));
                } else {
                    image.setImageTintList(null);
                }

                imageDivider.setBackgroundColor(dividerColor);
                tempValue.setTextColor(textColorPrimary);
                tempConditionDivider.setBackgroundColor(dividerColor);
                conditionValue.setTextColor(textColorPrimary);
                precipitationTitle.setTextColor(textColorPrimary);
                precipitationValue.setTextColor(textColorSecondary);
                windTitle.setTextColor(textColorPrimary);
                windValue.setTextColor(textColorSecondary);
                humidityTitle.setTextColor(textColorPrimary);
                humidityValue.setTextColor(textColorSecondary);
                pressureTitle.setTextColor(textColorPrimary);
                pressureValue.setTextColor(textColorSecondary);
                expandCollapseButtonDivider.setBackgroundColor(dividerColor);
                ((RippleDrawable) expandCollapseButton.getBackground())
                        .setColor(ColorStateList.valueOf(rippleColor));
                expandCollapseButtonText.setTextColor(textColorPrimary);
                expandCollapseButtonIcon.setImageTintList(ColorStateList.valueOf(iconColor));
            } else if (conditionImageColor == 0) {
                image.setImageTintList(null);
            }
        }

        public View getForecastCard() {
            return cardLayout;
        }

        private  ValueAnimator createForecastAnimator() {
            ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
            animator.setInterpolator(new FastOutSlowInInterpolator());
            animator.setDuration(300);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = animation.getAnimatedFraction();
                    float height;
                    float alpha = value;
                    if (animateExpansion) {
                        height = expandedContentHeight * value;
                    } else {
                        height = expandedContentHeight * (1 - value);
                        alpha = 1 - value;
                    }
                    expandedContent.getLayoutParams().height = Math.round(height);
                    expandCollapseButtonDivider.setAlpha(alpha);
                    expandedContent.requestLayout();
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (animateExpansion) {
                        expandedContent.setVisibility(View.VISIBLE);
                        expandCollapseButtonDivider.setVisibility(View.VISIBLE);
                    }
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (animateExpansion) {
                        expandCollapseButtonIcon.setImageResource(R.drawable.ic_expand_less);
                        expandCollapseButtonText.setText(R.string.collapse_card);
                    } else {
                        expandCollapseButtonIcon.setImageResource(R.drawable.ic_expand_more);
                        expandCollapseButtonText.setText(R.string.expand_card);
                        expandedContent.setVisibility(View.GONE);
                        expandCollapseButtonDivider.setVisibility(View.GONE);
                    }
                    animateExpansion = !animateExpansion;
                }
            });
            return animator;
        }

        public void updateWeather(HourForecast h) {
            final Drawable icon = mWeatherInfo.getConditionIcon(
                    DetailedWeatherHelper.getConditionIconType(getActivity()), h.getConditionCode());
            final String rain = h.getFormattedRain();
            final String snow = h.getFormattedSnow();
            final String noPrecipitationValue = getActivity().getResources().getString(
                    R.string.no_precipitation_value);

            timeValue.setText(h.getTime());
            image.setImageDrawable(icon);
            tempValue.setText(h.getFormattedTemperature());
            conditionValue.setText(h.getCondition());
            if (!snow.equals(WeatherInfo.NO_VALUE)) {
                precipitationValue.setText(snow);
            } else if (!rain.equals(WeatherInfo.NO_VALUE)) {
                precipitationValue.setText(rain);
            } else {
                precipitationValue.setText(noPrecipitationValue);
            }
            windValue.setText(h.getFormattedWind());
            humidityValue.setText(h.getFormattedHumidity());
            pressureValue.setText(h.getFormattedPressure());
        }
    }
}
