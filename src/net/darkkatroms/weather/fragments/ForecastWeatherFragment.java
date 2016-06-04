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

import android.app.Activity;
import android.app.Fragment;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.util.darkkat.DetailedWeatherHelper;
import com.android.internal.util.darkkat.WeatherHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import net.darkkatroms.weather.Config;
import net.darkkatroms.weather.R;
import net.darkkatroms.weather.WeatherInfo;
import net.darkkatroms.weather.WeatherInfo.DayForecast;

public class ForecastWeatherFragment extends Fragment {

    private int mForecastDay = 0;
    private ImageView mImage;
    private TextView mDate;
    private TextView mProvider;
    private TextView mWind;
    private TextView mHumidity;
    private TextView mTempLowHight;
    private TextView mCondition;
    private TextView mPrecipitation;
    private TextView mPressure;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        return inflateAndSetupView(inflater, container, savedInstanceState);
    }

    private View inflateAndSetupView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.detailed_weather_forecast, container, false);

        mImage = (ImageView) layout.findViewById(R.id.forecast_weather_icon);
        mDate = (TextView) layout.findViewById(R.id.forecast_weather_date);
        mProvider = (TextView) layout.findViewById(R.id.forecast_weather_provider);
        mWind = (TextView) layout.findViewById(R.id.forecast_weather_wind);
        mHumidity = (TextView) layout.findViewById(R.id.forecast_weather_humidity);
        mTempLowHight = (TextView) layout.findViewById(R.id.forecast_weather_low_high);
        mCondition = (TextView) layout.findViewById(R.id.forecast_weather_condition);
        mPrecipitation = (TextView) layout.findViewById(R.id.forecast_weather_precipitation);
        mPressure = (TextView) layout.findViewById(R.id.forecast_weather_pressure);

        final boolean customizeColors = DetailedWeatherHelper.customizeColors(getActivity());
        final int iconColor = DetailedWeatherHelper.getConditionImageColor(getActivity());
        if (customizeColors) {
            final int backgroundColor = DetailedWeatherHelper.getContentBackgroundColor(getActivity());
            final int textColorPrimary = DetailedWeatherHelper.getContentTextColor(getActivity(), true);
            final int textColorSecondary = DetailedWeatherHelper.getContentTextColor(getActivity(), false);
            layout.setBackgroundColor(backgroundColor);
            if (iconColor != 0) {
                mImage.setImageTintList(ColorStateList.valueOf(iconColor));
            } else {
                mImage.setImageTintList(null);
            }
            mDate.setTextColor(textColorPrimary);
            mProvider.setTextColor(textColorSecondary);
            mWind.setTextColor(textColorPrimary);
            mHumidity.setTextColor(textColorPrimary);
            mTempLowHight.setTextColor(textColorSecondary);
            mCondition.setTextColor(textColorPrimary);
            mPrecipitation.setTextColor(textColorSecondary);
            mPressure.setTextColor(textColorSecondary);
        } else {
            if (iconColor == 0) {
                mImage.setImageTintList(null);
            }
        }

        updateWeather();
        return layout;
    }

    public void setForecastDay(int forecastDay) {
        mForecastDay = forecastDay;
    }

    public void updateWeather() {
        WeatherInfo weather = Config.getWeatherData(getActivity());
        if (weather != null) {
            ArrayList<DayForecast> forecasts = weather.getForecasts();
            if (forecasts != null) {
                DayForecast d = forecasts.get(mForecastDay);
                Drawable icon = weather.getConditionIcon(DetailedWeatherHelper.getConditionIconType(getActivity()), d.getConditionCode());

                TimeZone MyTimezone = TimeZone.getDefault();
                Calendar calendar = new GregorianCalendar(MyTimezone);
                StringBuilder sb = new StringBuilder();
                if (mForecastDay > 0) {
                    for (int i=0; i<mForecastDay; i++) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                    }
                }
                String day = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
                String dayNumber = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                String month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
                String year = String.valueOf(calendar.get(Calendar.YEAR));

                sb.append(day);
                sb.append(", ");
                sb.append(dayNumber);
                sb.append(". ");
                sb.append(month);
                sb.append(" ");
                sb.append(year);

                mImage.setImageDrawable(icon);
                mDate.setText(sb.toString());
                mProvider.setText(Config.getProviderId(getActivity()));
                String formattedWind = d.getFormattedWind();
                String formattedHumidity = d.getFormattedHumidity();
                mWind.setText(formattedWind);
                mHumidity.setText(formattedHumidity);
                if (formattedWind.equals(WeatherInfo.NO_VALUE)) {
                    mWind.setVisibility(View.INVISIBLE);
                }
                if (formattedHumidity.equals(WeatherInfo.NO_VALUE)) {
                    mHumidity.setVisibility(View.INVISIBLE);
                }
                mTempLowHight.setText(d.getFormattedLow() + " | " + d.getFormattedHigh());
                mCondition.setText(d.getCondition());
                setPrecipitation(d);
                String formattedPressure = d.getFormattedPressure();
                mPressure.setText(formattedPressure);
                if (formattedPressure.equals(WeatherInfo.NO_VALUE)) {
                    mPressure.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void setPrecipitation(DayForecast d) {
        final String rain = d.getFormattedRain();
        final String snow = d.getFormattedSnow();
        if (!snow.equals(WeatherInfo.NO_VALUE)) {
            mPrecipitation.setText(snow);
            mPrecipitation.setVisibility(View.VISIBLE);
        } else if (!rain.equals(WeatherInfo.NO_VALUE)) {
            mPrecipitation.setText(rain);
            mPrecipitation.setVisibility(View.VISIBLE);
        }
    }
}
