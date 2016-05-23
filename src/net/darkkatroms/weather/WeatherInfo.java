/*
 * Copyright (C) 2012 The AOKP Project
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

public class WeatherInfo {
    private static final DecimalFormat sNoDigitsFormat = new DecimalFormat("0");
    private static final DecimalFormat sPrecipitationDigitsFormat = new DecimalFormat("#0.##");

    private static final int ICON_MONOCHROME = 0;
    private static final int ICON_COLORED    = 1;

    private static final float PRECIPITATION_ITEM_THRESHOLD = 0.005f;
    public static final String PRECIPITATION_ITEM_IS_NAN    = "PrecipitationIsNaN";
    public static final String PRECIPITATION_ITEM_IS_ZERO   = "PrecipitationIsZero";

    private Context mContext;

    private String id;
    private String city;
    private String condition;
    private int conditionCode;
    private float temperature;
    private String tempUnit;
    private float humidity;
    private float wind;
    private int windDirection;
    private String speedUnit;
    private float rain1H;
    private float rain3H;
    private float snow1H;
    private float snow3H;
    private long timestamp;
    private ArrayList<DayForecast> forecasts;

    private static String sPrecipitationUnit1h;
    private static String sPrecipitationUnit3h;

    public WeatherInfo(Context context, String id,
            String city, String condition, int conditionCode, float temp,
            String tempUnit, float humidity, float wind, int windDir,
            String speedUnit, float rain1H, float rain3H, float snow1H, float snow3H,
            ArrayList<DayForecast> forecasts, long timestamp) {
        this.mContext = context.getApplicationContext();
        this.id = id;
        this.city = city;
        this.condition = condition;
        this.conditionCode = conditionCode;
        this.temperature = temp;
        this.tempUnit = tempUnit;
        this.humidity = humidity;
        this.wind = wind;
        this.windDirection = windDir;
        this.rain1H = rain1H;
        this.rain3H = rain3H;
        this.snow1H = snow1H;
        this.snow3H = snow3H;
        this.speedUnit = speedUnit;
        this.timestamp = timestamp;
        this.forecasts = forecasts;

        this.sPrecipitationUnit1h = mContext.getResources().getString(R.string.precipitation_unit_1h_title);
        this.sPrecipitationUnit3h = mContext.getResources().getString(R.string.precipitation_unit_3h_title);
    }

    public static class WeatherLocation {
        public String id;
        public String city;
        public String postal;
        public String countryId;
        public String country;
    }
    
    public static class DayForecast {
        public final float low, high;
        public final int conditionCode;
        public final String condition;
        public final float rain, snow;

        public DayForecast(float low, float high, String condition, int conditionCode,
                float rain, float snow) {
            this.low = low;
            this.high = high;
            this.condition = condition;
            this.conditionCode = conditionCode;
            this.rain = rain;
            this.snow = snow;
        }

        public String getFormattedLow() {
            return getFormattedValue(low, "\u00b0");
        }

        public String getFormattedHigh() {
            return getFormattedValue(high, "\u00b0");
        }

        public String getCondition(Context context) {
            return WeatherInfo.getCondition(context, conditionCode, condition);
        }

        public int getConditionCode() {
            return conditionCode;
        }

        public String getFormattedRain() {
            if (Float.isNaN(rain)) {
                return PRECIPITATION_ITEM_IS_NAN;
            }
            if (rain >= PRECIPITATION_ITEM_THRESHOLD) {
                return sPrecipitationDigitsFormat.format(rain) + sPrecipitationUnit1h;
            } else {
                return PRECIPITATION_ITEM_IS_ZERO;
            }
        }

        public String getFormattedSnow() {
            if (Float.isNaN(snow)) {
                return PRECIPITATION_ITEM_IS_NAN;
            }
            if (snow >= PRECIPITATION_ITEM_THRESHOLD) {
                return sPrecipitationDigitsFormat.format(snow) + sPrecipitationUnit1h;
            } else {
                return PRECIPITATION_ITEM_IS_ZERO;
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getCity() {
        return city;
    }

    public String getCondition() {
        return getCondition(mContext, conditionCode, condition);
    }

    public int getConditionCode() {
        return conditionCode;
    }

    public String getFormattedTemperature() {
        return getFormattedValue(temperature, "\u00b0" + tempUnit);
    }

    public String getFormattedLow() {
        return forecasts.get(0).getFormattedLow();
    }

    public String getFormattedHigh() {
        return forecasts.get(0).getFormattedHigh();
    }

    public String getFormattedHumidity() {
        return getFormattedValue(humidity, "%");
    }

    public String getFormattedWindSpeed() {
        if (wind < 0) {
            return getFormattedValue(0, speedUnit);
        }
        return getFormattedValue(wind, speedUnit);
    }

    public String getWindDirection() {
        int resId;

        if (windDirection < 0) resId = R.string.unknown;
        else if (windDirection < 23) resId = R.string.weather_N;
        else if (windDirection < 68) resId = R.string.weather_NE;
        else if (windDirection < 113) resId = R.string.weather_E;
        else if (windDirection < 158) resId = R.string.weather_SE;
        else if (windDirection < 203) resId = R.string.weather_S;
        else if (windDirection < 248) resId = R.string.weather_SW;
        else if (windDirection < 293) resId = R.string.weather_W;
        else if (windDirection < 338) resId = R.string.weather_NW;
        else resId = R.string.weather_N;

        return mContext.getString(resId);
    }

    public String getFormattedRain1H() {
        if (Float.isNaN(rain1H)) {
            return PRECIPITATION_ITEM_IS_NAN;
        }
        if (rain1H >= PRECIPITATION_ITEM_THRESHOLD) {
            return sPrecipitationDigitsFormat.format(rain1H) + sPrecipitationUnit1h;
        } else {
            return PRECIPITATION_ITEM_IS_ZERO;
        }
    }

    public String getFormattedRain3H() {
        if (Float.isNaN(rain3H)) {
            return PRECIPITATION_ITEM_IS_NAN;
        }
        if (rain3H >= PRECIPITATION_ITEM_THRESHOLD) {
            return sPrecipitationDigitsFormat.format(rain3H) + sPrecipitationUnit3h;
        } else {
            return PRECIPITATION_ITEM_IS_ZERO;
        }
    }

    public String getFormattedSnow1H() {
        if (Float.isNaN(snow1H)) {
            return PRECIPITATION_ITEM_IS_NAN;
        }
        if (snow1H >= PRECIPITATION_ITEM_THRESHOLD) {
            return sPrecipitationDigitsFormat.format(snow1H) + sPrecipitationUnit1h;
        } else {
            return PRECIPITATION_ITEM_IS_ZERO;
        }
    }

    public String getFormattedSnow3H() {
        if (Float.isNaN(snow3H)) {
            return PRECIPITATION_ITEM_IS_NAN;
        }
        if (snow3H >= PRECIPITATION_ITEM_THRESHOLD) {
            return sPrecipitationDigitsFormat.format(snow3H) + sPrecipitationUnit3h;
        } else {
            return PRECIPITATION_ITEM_IS_ZERO;
        }
    }

    public Date getTimestamp() {
        return new Date(timestamp);
    }

    public ArrayList<DayForecast> getForecasts() {
        return forecasts;
    }

    private static String getCondition(Context context, int conditionCode, String condition) {
        final Resources res = context.getResources();
        final int resId = res.getIdentifier("weather_" + conditionCode, "string", context.getPackageName());
        if (resId != 0) {
            return res.getString(resId);
        }
        return condition;
    }

    private static String getFormattedValue(float value, String unit) {
        if (Float.isNaN(value)) {
            return "-";
        }
        String formatted = sNoDigitsFormat.format(value);
        if (formatted.equals("-0")) {
            formatted = "0";
        }
        return formatted + unit;
    }

    public Drawable getConditionIcon(int iconNameValue, int conditionCode) {
        String iconName;

        if (iconNameValue == ICON_MONOCHROME) {
            iconName = "weather_";
        } else if (iconNameValue == ICON_COLORED) {
            iconName = "weather_color_";
        } else {
            iconName = "weather_vclouds_";
        }

        final int resId = mContext.getResources().getIdentifier(
                iconName + conditionCode, "drawable", mContext.getPackageName());
        if (resId != 0) {
            return mContext.getResources().getDrawable(resId);
        }

        // Use the default color set unknown icon
        return mContext.getResources().getDrawable(R.drawable.weather_color_na);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("WeatherInfo for ");
        builder.append(city);
        builder.append(" (");
        builder.append(id);
        builder.append(") @ ");
        builder.append(getTimestamp());
        builder.append(": ");
        builder.append(getCondition());
        builder.append("(");
        builder.append(conditionCode);
        builder.append("), temperature ");
        builder.append(getFormattedTemperature());
        builder.append(", low ");
        builder.append(getFormattedLow());
        builder.append(", high ");
        builder.append(getFormattedHigh());
        builder.append(", humidity ");
        builder.append(getFormattedHumidity());
        builder.append(", wind ");
        builder.append(getFormattedWindSpeed());
        builder.append(" at ");
        builder.append(getWindDirection());
        builder.append(", rain1H ");
        builder.append(getFormattedRain1H());
        builder.append(", rain3H ");
        builder.append(getFormattedRain3H());
        builder.append(", snow1H ");
        builder.append(getFormattedSnow1H());
        builder.append(", snow3H ");
        builder.append(getFormattedSnow3H());
        if (forecasts.size() > 0) {
            builder.append(", forecasts:");
        }
        for (int i = 0; i < forecasts.size(); i++) {
            DayForecast d = forecasts.get(i);
            if (i != 0) {
                builder.append(";");
            }
            builder.append(" day ").append(i + 1).append(": ");
            builder.append("high ").append(d.getFormattedHigh());
            builder.append(", low ").append(d.getFormattedLow());
            builder.append(", ").append(d.condition);
            builder.append("(").append(d.conditionCode).append(")");
            builder.append(", rain ").append(d.getFormattedRain());
            builder.append(", snow ").append(d.getFormattedSnow());
        }
        return builder.toString();
    }

    public String toSerializedString() {
        StringBuilder builder = new StringBuilder();
        builder.append(id).append('|');
        builder.append(city).append('|');
        builder.append(condition).append('|');
        builder.append(conditionCode).append('|');
        builder.append(temperature).append('|');
        builder.append(tempUnit).append('|');
        builder.append(humidity).append('|');
        builder.append(wind).append('|');
        builder.append(windDirection).append('|');
        builder.append(speedUnit).append('|');
        builder.append(rain1H).append('|');
        builder.append(rain3H).append('|');
        builder.append(snow1H).append('|');
        builder.append(snow3H).append('|');
        builder.append(timestamp).append('|');
        serializeForecasts(builder);
        return builder.toString();
    }

    private void serializeForecasts(StringBuilder builder) {
        builder.append(forecasts.size());
        for (DayForecast d : forecasts) {
            builder.append(';');
            builder.append(d.high).append(';');
            builder.append(d.low).append(';');
            builder.append(d.condition).append(';');
            builder.append(d.conditionCode).append(';');
            builder.append(d.rain).append(';');
            builder.append(d.snow);
        }
    }

    public static WeatherInfo fromSerializedString(Context context, String input) {
        if (input == null) {
            return null;
        }

        String[] parts = input.split("\\|");
        if (parts == null || parts.length != 16) {
            return null;
        }

        int conditionCode, windDirection;
        long timestamp;
        float temperature, humidity, wind, rain1H, rain3H, snow1H, snow3H;
        String[] forecastParts = parts[15].split(";");
        int forecastItems;
        ArrayList<DayForecast> forecasts = new ArrayList<DayForecast>();

        // Parse the core data
        try {
            conditionCode = Integer.parseInt(parts[3]);
            temperature = Float.parseFloat(parts[4]);
            humidity = Float.parseFloat(parts[6]);
            wind = Float.parseFloat(parts[7]);
            windDirection = Integer.parseInt(parts[8]);
            rain1H = Float.parseFloat(parts[10]);
            rain3H = Float.parseFloat(parts[11]);
            snow1H = Float.parseFloat(parts[12]);
            snow3H = Float.parseFloat(parts[13]);
            timestamp = Long.parseLong(parts[14]);
            forecastItems = forecastParts == null ? 0 : Integer.parseInt(forecastParts[0]);
        } catch (NumberFormatException e) {
            return null;
        }

        if (forecastItems == 0 || forecastParts.length != 6 * forecastItems + 1) {
            return null;
        }

        // Parse the forecast data
        try {
            for (int item = 0; item < forecastItems; item ++) {
                int offset = item * 6 + 1;
                DayForecast day = new DayForecast(
                        /* low */ Float.parseFloat(forecastParts[offset + 1]),
                        /* high */ Float.parseFloat(forecastParts[offset]),
                        /* condition */ forecastParts[offset + 2],
                        /* conditionCode */ Integer.parseInt(forecastParts[offset + 3]),
                        /* rain */ Float.parseFloat(forecastParts[offset + 4]),
                        /* snow */ Float.parseFloat(forecastParts[offset + 5]));

                if (!Float.isNaN(day.low) && !Float.isNaN(day.high) && day.conditionCode >= 0) {
                    forecasts.add(day);
                }
            }
        } catch (NumberFormatException ignored) {
        }

        if (forecasts.isEmpty()) {
            return null;
        }

        return new WeatherInfo(context,
                /* id */ parts[0], /* city */ parts[1], /* condition */ parts[2],
                conditionCode, temperature, /* tempUnit */ parts[5],
                humidity, wind, windDirection, /* speedUnit */ parts[9],
                rain1H, rain3H, snow1H, snow3H, /* forecasts */ forecasts, timestamp);
    }
}
