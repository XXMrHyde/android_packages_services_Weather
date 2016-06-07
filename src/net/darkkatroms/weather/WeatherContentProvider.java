/*
 * Copyright (C) 2013 The CyanogenMod Project
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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import net.darkkatroms.weather.WeatherInfo.DayForecast;

public class WeatherContentProvider extends ContentProvider {
    private static final String TAG = "WeatherService:WeatherContentProvider";
    private static final boolean DEBUG = false;

    static WeatherInfo sCachedWeatherInfo;

    private static final int URI_TYPE_WEATHER = 1;
    private static final int URI_TYPE_SETTINGS = 2;

    private static final String COLUMN_CURRENT_CITY_ID =
            "city_id";
    private static final String COLUMN_CURRENT_CITY =
            "city";
    private static final String COLUMN_CURRENT_CONDITION =
            "condition";
    private static final String COLUMN_CURRENT_CONDITION_CODE =
            "condition_code";
    private static final String COLUMN_CURRENT_FORMATTED_TEMPERATURE =
            "formatted_temperature";
    private static final String COLUMN_CURRENT_TEMPERATURE_LOW =
            "temperature_low";
    private static final String COLUMN_CURRENT_TEMPERATURE_HIGHT =
            "temperature_hight";
    private static final String COLUMN_CURRENT_FORMATTED_TEMPERATURE_LOW =
            "formatted_temperature_low";
    private static final String COLUMN_CURRENT_FORMATTED_TEMPERATURE_HIGHT =
            "formatted_temperature_hight";
    private static final String COLUMN_CURRENT_FORMATTED_HUMIDITY =
            "formatted_humidity";
    private static final String COLUMN_CURRENT_FORMATTED_WIND =
            "formatted_wind";
    private static final String COLUMN_CURRENT_FORMATTED_PRESSURE =
            "formatted_pressure";
    private static final String COLUMN_CURRENT_FORMATTED_RAIN1H =
            "formatted_rain1h";
    private static final String COLUMN_CURRENT_FORMATTED_RAIN3H =
            "formatted_rain3h";
    private static final String COLUMN_CURRENT_FORMATTED_SNOW1H =
            "formatted_snow1h";
    private static final String COLUMN_CURRENT_FORMATTED_SNOW3H =
            "formatted_snow3h";
    private static final String COLUMN_CURRENT_TIME_STAMP =
            "time_stamp";

    private static final String COLUMN_FORECAST_CONDITION =
            "forecast_condition";
    private static final String COLUMN_FORECAST_CONDITION_CODE =
            "forecast_condition_code";
    private static final String COLUMN_FORECAST_TEMPERATURE_LOW =
            "forecast_temperature_low";
    private static final String COLUMN_FORECAST_TEMPERATURE_HIGH =
            "forecast_temperature_high";
    private static final String COLUMN_FORECAST_FORMATTED_TEMPERATURE_LOW =
            "forecast_formatted_temperature_low";
    private static final String COLUMN_FORECAST_FORMATTED_TEMPERATURE_HIGH =
            "forecast_formatted_temperature_high";
    private static final String COLUMN_FORECAST_FORMATTED_HUMIDITY =
            "forecast_formatted_humidity";
    private static final String COLUMN_FORECAST_FORMATTED_WIND =
            "forecast_formatted_wind";
    private static final String COLUMN_FORECAST_FORMATTED_PRESSURE =
            "forecast_formatted_pressure";
    private static final String COLUMN_FORECAST_FORMATTED_RAIN =
            "forecast_formatted_rain";
    private static final String COLUMN_FORECAST_FORMATTED_SNOW =
            "forecast_formatted_snow";

    private static final String COLUMN_ENABLED = "enabled";
    private static final String COLUMN_PROVIDER = "provider";
    private static final String COLUMN_INTERVAL = "interval";
    private static final String COLUMN_UNITS = "units";
    private static final String COLUMN_LOCATION = "location";

    private static final String[] PROJECTION_DEFAULT_WEATHER = new String[] {
            COLUMN_CURRENT_CITY_ID,
            COLUMN_CURRENT_CITY,
            COLUMN_CURRENT_CONDITION,
            COLUMN_CURRENT_CONDITION_CODE,
            COLUMN_CURRENT_FORMATTED_TEMPERATURE,
            COLUMN_CURRENT_TEMPERATURE_LOW,
            COLUMN_CURRENT_TEMPERATURE_HIGHT,
            COLUMN_CURRENT_FORMATTED_TEMPERATURE_LOW,
            COLUMN_CURRENT_FORMATTED_TEMPERATURE_HIGHT,
            COLUMN_CURRENT_FORMATTED_HUMIDITY,
            COLUMN_CURRENT_FORMATTED_WIND,
            COLUMN_CURRENT_FORMATTED_PRESSURE,
            COLUMN_CURRENT_FORMATTED_RAIN1H,
            COLUMN_CURRENT_FORMATTED_RAIN3H,
            COLUMN_CURRENT_FORMATTED_SNOW1H,
            COLUMN_CURRENT_FORMATTED_SNOW3H,
            COLUMN_CURRENT_TIME_STAMP,
            COLUMN_FORECAST_CONDITION,
            COLUMN_FORECAST_CONDITION_CODE,
            COLUMN_FORECAST_TEMPERATURE_LOW,
            COLUMN_FORECAST_TEMPERATURE_HIGH,
            COLUMN_FORECAST_FORMATTED_TEMPERATURE_LOW,
            COLUMN_FORECAST_FORMATTED_TEMPERATURE_HIGH,
            COLUMN_FORECAST_FORMATTED_HUMIDITY,
            COLUMN_FORECAST_FORMATTED_WIND,
            COLUMN_FORECAST_FORMATTED_PRESSURE,
            COLUMN_FORECAST_FORMATTED_RAIN,
            COLUMN_FORECAST_FORMATTED_SNOW
    };

    private static final String[] PROJECTION_DEFAULT_SETTINGS = new String[] {
            COLUMN_ENABLED,
            COLUMN_PROVIDER,
            COLUMN_INTERVAL,
            COLUMN_UNITS,
            COLUMN_LOCATION
    };

    public static final String AUTHORITY = "net.darkkatroms.weather.provider";

    private static final UriMatcher sUriMatcher;
    static {
        sUriMatcher = new UriMatcher(URI_TYPE_WEATHER);
        sUriMatcher.addURI(AUTHORITY, "weather", URI_TYPE_WEATHER);
        sUriMatcher.addURI(AUTHORITY, "settings", URI_TYPE_SETTINGS);
    }

    private Context mContext;

    @Override
    public boolean onCreate() {
        mContext = getContext();
        sCachedWeatherInfo = Config.getWeatherData(mContext);
        return true;
    }

    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {

        final int projectionType = sUriMatcher.match(uri);
        final MatrixCursor result = new MatrixCursor(resolveProjection(projection, projectionType));

        if (DEBUG) Log.i(TAG, "query: " + uri.toString());

        if (projectionType == URI_TYPE_SETTINGS) {
            result.newRow()
                    .add(COLUMN_ENABLED, Config.isEnabled(mContext) ? 1 : 0)
                    .add(COLUMN_PROVIDER, Config.getProviderId(mContext))
                    .add(COLUMN_INTERVAL, Config.getUpdateInterval(mContext))
                    .add(COLUMN_UNITS, Config.isMetric(mContext) ? 0 : 1)
                    .add(COLUMN_LOCATION, Config.isCustomLocation(mContext) ? Config.getLocationName(mContext) : "");

            return result;
        } else if (projectionType == URI_TYPE_WEATHER) {
            WeatherInfo weather = sCachedWeatherInfo;
            if (weather != null) {
                // current
                result.newRow()
                        .add(COLUMN_CURRENT_CITY_ID, weather.getId())
                        .add(COLUMN_CURRENT_CITY, weather.getCity())
                        .add(COLUMN_CURRENT_CONDITION, weather.getCondition())
                        .add(COLUMN_CURRENT_CONDITION_CODE, weather.getConditionCode())
                        .add(COLUMN_CURRENT_FORMATTED_TEMPERATURE, weather.getFormattedTemperature())
                        .add(COLUMN_CURRENT_TEMPERATURE_LOW, weather.getLow())
                        .add(COLUMN_CURRENT_TEMPERATURE_HIGHT, weather.getHigh())
                        .add(COLUMN_CURRENT_FORMATTED_TEMPERATURE_LOW, weather.getFormattedLow())
                        .add(COLUMN_CURRENT_FORMATTED_TEMPERATURE_HIGHT, weather.getFormattedHigh())
                        .add(COLUMN_CURRENT_FORMATTED_HUMIDITY, weather.getFormattedHumidity())
                        .add(COLUMN_CURRENT_FORMATTED_WIND, weather.getFormattedWind())
                        .add(COLUMN_CURRENT_FORMATTED_PRESSURE, weather.getFormattedPressure())
                        .add(COLUMN_CURRENT_FORMATTED_RAIN1H, weather.getFormattedRain1H())
                        .add(COLUMN_CURRENT_FORMATTED_RAIN3H, weather.getFormattedRain3H())
                        .add(COLUMN_CURRENT_FORMATTED_SNOW1H, weather.getFormattedSnow1H())
                        .add(COLUMN_CURRENT_FORMATTED_SNOW3H, weather.getFormattedSnow3H())
                        .add(COLUMN_CURRENT_TIME_STAMP, weather.getTimestamp().toString());


                // forecast
                for (DayForecast day : weather.getForecasts()) {
                    result.newRow()
                            .add(COLUMN_FORECAST_CONDITION, day.getCondition())
                            .add(COLUMN_FORECAST_CONDITION_CODE, day.getConditionCode())
                            .add(COLUMN_FORECAST_TEMPERATURE_LOW, day.getLow())
                            .add(COLUMN_FORECAST_TEMPERATURE_HIGH, day.getHigh())
                            .add(COLUMN_FORECAST_FORMATTED_TEMPERATURE_LOW, day.getFormattedLow())
                            .add(COLUMN_FORECAST_FORMATTED_TEMPERATURE_HIGH, day.getFormattedHigh())
                            .add(COLUMN_FORECAST_FORMATTED_HUMIDITY, day.getFormattedHumidity())
                            .add(COLUMN_FORECAST_FORMATTED_WIND, day.getFormattedWind())
                            .add(COLUMN_FORECAST_FORMATTED_PRESSURE, day.getFormattedPressure())
                            .add(COLUMN_FORECAST_FORMATTED_RAIN, day.getFormattedRain())
                            .add(COLUMN_FORECAST_FORMATTED_SNOW, day.getFormattedSnow());
                }
                return result;
            }
        }
        return null;
    }

    private String[] resolveProjection(String[] projection, int uriType) {
        if (projection != null)
            return projection;
        switch (uriType) {
            default:
            case URI_TYPE_WEATHER:
                return PROJECTION_DEFAULT_WEATHER;

            case URI_TYPE_SETTINGS:
                return PROJECTION_DEFAULT_SETTINGS;
        }
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    public static void updateCachedWeatherInfo(Context context) {
        if (DEBUG) Log.d(TAG, "updateCachedWeatherInfo()");
        sCachedWeatherInfo = Config.getWeatherData(context);
        context.getContentResolver().notifyChange(
                Uri.parse("content://" + WeatherContentProvider.AUTHORITY + "/weather"), null);
    }
}
