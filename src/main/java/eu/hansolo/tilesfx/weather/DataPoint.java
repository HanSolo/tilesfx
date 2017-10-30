/*
 * Copyright (c) 2017 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.tilesfx.weather;

import eu.hansolo.tilesfx.weather.DarkSky.ConditionAndIcon;
import eu.hansolo.tilesfx.weather.DarkSky.PrecipType;
import org.json.simple.JSONObject;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;


/**
 * Created by hansolo on 27.10.16.
 */
public class DataPoint {
    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private LocalDateTime    time;
    private TimeZone         timeZone;
    private String           summary;
    private ConditionAndIcon condition;
    private LocalDateTime    sunriseTime;
    private LocalDateTime    sunsetTime;
    private double           moonPhase;
    private double           precipIntensity;
    private double           precipIntensityMax;
    private LocalDateTime    precipIntensityMaxTime;
    private double           precipProbability;
    private PrecipType       precipType;
    private double           temperature;
    private double           temperatureMin;
    private LocalDateTime    temperatureMinTime;
    private double           temperatureMax;
    private LocalDateTime    temperatureMaxTime;
    private double           apparentTemperatureMin;
    private LocalDateTime    apparentTemperatureMinTime;
    private double           apparentTemperatureMax;
    private LocalDateTime    apparentTemperatureMaxTime;
    private double           dewPoint;
    private double           humidity;
    private double           windSpeed;
    private double           windBearing;
    private double           cloudCover;
    private double           pressure;
    private double           ozone;
    private double           nearestStormBearing;
    private double           nearestStormDistance;
    private double           precipAccumlation;
    private double           visibility;


    // ******************** Constructors **************************************
    public DataPoint() {
        time                       = LocalDateTime.now();
        timeZone                   = TimeZone.getDefault();
        summary                    = "";
        condition                  = ConditionAndIcon.NONE;
        sunriseTime                = LocalDateTime.now();
        sunsetTime                 = LocalDateTime.now();
        moonPhase                  = -1;
        precipIntensity            = 0;
        precipIntensityMax         = 0;
        precipIntensityMaxTime     = LocalDateTime.now();
        precipProbability          = -1;
        precipType                 = PrecipType.NONE;
        temperature                = 0;
        temperatureMin             = -1;
        temperatureMinTime         = LocalDateTime.now();
        temperatureMax             = 0;
        temperatureMaxTime         = LocalDateTime.now();
        apparentTemperatureMin     = 0;
        apparentTemperatureMinTime = LocalDateTime.now();
        apparentTemperatureMax     = -1;
        apparentTemperatureMaxTime = LocalDateTime.now();
        dewPoint                   = -1;
        humidity                   = -1;
        windSpeed                  = -1;
        windBearing                = -1;
        cloudCover                 = -1;
        pressure                   = -1;
        ozone                      = -1;
        nearestStormBearing        = -1;
        nearestStormDistance       = -1;
        precipAccumlation          = -1;
        visibility                 = -1;
    }


    // ******************** Methods *******************************************
    public LocalDateTime getTime() { return time; }
    public void setTime(final LocalDateTime TIME) { time = TIME; }

    public TimeZone getTimeZone() { return timeZone; }
    public void setTimeZone(final TimeZone TIME_ZONE) { timeZone = TIME_ZONE; }

    public String getSummary() { return summary; }
    public void setSummary(final String SUMMARY) { summary = SUMMARY; }

    public ConditionAndIcon getCondition() { return condition; }
    public void setCondition(final ConditionAndIcon Condition) { condition = Condition; }

    public LocalDateTime getSunriseTime() { return sunriseTime; }
    public void setSunriseTime(final LocalDateTime SUNRISE_TIME) { sunriseTime = SUNRISE_TIME; }

    public LocalDateTime getSunsetTime() { return sunsetTime; }
    public void setSunsetTime(final LocalDateTime SUNSET_TIME) { sunsetTime = SUNSET_TIME; }

    public double getMoonPhase() { return moonPhase; }
    public void setMoonPhase(final double MOON_PHASE) { moonPhase = MOON_PHASE; }

    public double getPrecipIntensity() { return precipIntensity; }
    public void setPrecipIntensity(final double PRECIP_INTENSITY) { precipIntensity = PRECIP_INTENSITY; }

    public double getPrecipIntensityMax() { return precipIntensityMax; }
    public void setPrecipIntensityMax(final double PRECIP_INTENSITY_MAX) { precipIntensityMax = PRECIP_INTENSITY_MAX; }

    public LocalDateTime getPrecipIntensityMaxTime() { return precipIntensityMaxTime; }
    public void setPrecipIntensityMaxTime(final LocalDateTime PRECIP_INTENSITY_MAX_TIME) { precipIntensityMaxTime = PRECIP_INTENSITY_MAX_TIME; }

    public double getPrecipProbability() { return precipProbability; }
    public void setPrecipProbability(final double PRECIP_PROBABILITY) { precipProbability = PRECIP_PROBABILITY; }

    public PrecipType getPrecipType() { return precipType; }
    public void setPrecipType(final PrecipType PRECIP_TYPE) { precipType = PRECIP_TYPE; }

    public double getTemperature() { return temperature; }
    public void setTemperature(final double TEMPERATURE) { temperature = TEMPERATURE; }

    public double getTemperatureMin() { return temperatureMin; }
    public void setTemperatureMin(final double TEMPERATURE_MIN) { temperatureMin = TEMPERATURE_MIN; }

    public LocalDateTime getTemperatureMinTime() { return temperatureMinTime; }
    public void setTemperatureMinTime(final LocalDateTime TEMPERATURE_MIN_TIME) { temperatureMinTime = TEMPERATURE_MIN_TIME; }

    public double getTemperatureMax() { return temperatureMax; }
    public void setTemperatureMax(final double TEMPERATURE_MAX) { temperatureMax = TEMPERATURE_MAX; }

    public LocalDateTime getTemperatureMaxTime() { return temperatureMaxTime; }
    public void setTemperatureMaxTime(final LocalDateTime TEMPERATURE_MAX_TIME) { temperatureMaxTime = TEMPERATURE_MAX_TIME; }

    public double getApparentTemperatureMin() { return apparentTemperatureMin; }
    public void setApparentTemperatureMin(final double APPARENT_TEMPERATURE_MIN) { apparentTemperatureMin = APPARENT_TEMPERATURE_MIN; }

    public LocalDateTime getApparentTemperatureMinTime() { return apparentTemperatureMinTime; }
    public void setApparentTemperatureMinTime(final LocalDateTime APPARENT_TEMPERATURE_MIN_TIME) { apparentTemperatureMinTime = APPARENT_TEMPERATURE_MIN_TIME; }

    public double getApparentTemperatureMax() { return apparentTemperatureMax; }
    public void setApparentTemperatureMax(final double APPARENT_TEMPERATURE_MAX) { apparentTemperatureMax = APPARENT_TEMPERATURE_MAX; }

    public LocalDateTime getApparentTemperatureMaxTime() { return apparentTemperatureMaxTime; }
    public void setApparentTemperatureMaxTime(final LocalDateTime APPARENT_TEMPERATURE_MAX_TIME) { apparentTemperatureMaxTime = APPARENT_TEMPERATURE_MAX_TIME; }

    public double getDewPoint() { return dewPoint; }
    public void setDewPoint(final double DEW_POINT) { dewPoint = DEW_POINT; }

    public double getHumidity() { return humidity; }
    public void setHumidity(final double HUMIDITY) { humidity = HUMIDITY; }

    public double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(final double WIND_SPEED) { windSpeed = WIND_SPEED; }

    public double getWindBearing() { return windBearing; }
    public void setWindBearing(final double WIND_BEARING) { windBearing = WIND_BEARING; }

    public double getCloudCover() { return cloudCover; }
    public void setCloudCover(final double CLOUD_COVER) { cloudCover = CLOUD_COVER; }

    public double getPressure() { return pressure; }
    public void setPressure(final double PRESSURE) { pressure = PRESSURE; }

    public double getOzone() { return ozone; }
    public void setOzone(final double OZONE) { ozone = OZONE; }

    public double getNearestStormBearing() { return nearestStormBearing; }
    public void setNearestStormBearing(final double NEAREST_STORM_BEARING) { nearestStormBearing = NEAREST_STORM_BEARING; }

    public double getNearestStormDistance() { return nearestStormDistance; }
    public void setNearestStormDistance(final double NEAREST_STORM_DISTANCE) { nearestStormDistance = NEAREST_STORM_DISTANCE; }

    public double getPrecipAccumlation() { return precipAccumlation; }
    public void setPrecipAccumlation(final double PRECIP_ACCUMLATION) { precipAccumlation = PRECIP_ACCUMLATION; }

    public double getVisibility() { return visibility; }
    public void setVisibility(final double VISIBILITY) { visibility = VISIBILITY; }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("date", DTF.format(ZonedDateTime.of(time, timeZone.toZoneId())));
        json.put("summary", summary);
        json.put("condition", condition.name());
        json.put("temperature", temperature);
        json.put("pressure", pressure);
        json.put("humidity", humidity);
        json.put("windSpeed", windSpeed);
        json.put("tempMin", temperatureMin);
        json.put("tempMax", temperatureMax);
        return json;
    }

    public String toJsonString() { return toJson().toJSONString().replace("\\",""); }
}
