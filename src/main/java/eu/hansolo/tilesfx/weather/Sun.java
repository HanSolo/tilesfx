/*
 * Copyright (c) 2016 by Gerrit Grunwald
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

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * We use the free REST API from Sunrise Sunset that you can find at
 * 
 *                 http://sunrise-sunset.org/api
 * 
 * It can be used free of charge. But please do not use that API in 
 * a manner that exceeds reasonable request volume, constitutes or
 * abusive usage.
 */

/**
 * Created by hansolo on 28.12.16.
 */
public class Sun {
    private static final String SUNRISE_SUNSET_URL = "http://api.sunrise-sunset.org/json?";


    // ******************** Methods *******************************************
    /**
     * Returns an array of ZonedDateTime objects for sunrise and sunset in UTC. Means there
     * are no daylight saving time adjustments.
     * @param LATITUDE
     * @param LONGITUDE
     * @return an array of ZonedDateTime objects for sunrise and sunset in UTC.
     */
    public static ZonedDateTime[] getSunriseSunsetAt(final double LATITUDE, final double LONGITUDE) {
        return getSunriseSunsetAt(LATITUDE, LONGITUDE, ZoneId.systemDefault());
    }
    public static ZonedDateTime[] getSunriseSunsetAt(final double LATITUDE, final double LONGITUDE, final ZoneId ZONE_ID) {
        StringBuilder response = new StringBuilder();
        try {
            final String URL_STRING = new StringBuilder(SUNRISE_SUNSET_URL).append("lat=").append(LATITUDE)
                                                                           .append("&lng=").append(LONGITUDE)
                                                                           .append("&date=today&formatted=0")
                                                                           .toString();
            final HttpURLConnection CONNECTION = (HttpURLConnection) new URL(URL_STRING).openConnection();
            final BufferedReader    IN         = new BufferedReader(new InputStreamReader(CONNECTION.getInputStream()));
            String                  inputLine;
            while ((inputLine = IN.readLine()) != null) {
                response.append(inputLine).append("\n");
            }
            IN.close();
            return parseJsonData(response.toString(), ZONE_ID);
        } catch (IOException ex) {
            return null;
        }
    }

    private static ZonedDateTime[] parseJsonData(final String JSON_DATA, final ZoneId ZONE_ID) {
        Object     obj     = JSONValue.parse(JSON_DATA);
        JSONObject jsonObj = (JSONObject) obj;

        // Results
        JSONObject results = (JSONObject) jsonObj.get("results");

        LocalDateTime sunrise                   = LocalDateTime.parse(results.get("sunrise").toString(), DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime sunset                    = LocalDateTime.parse(results.get("sunset").toString(), DateTimeFormatter.ISO_DATE_TIME);
        /*
        LocalDateTime solarNoon                 = LocalDateTime.parse(results.get("solar_noon").toString(), DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime dayLength                 = LocalDateTime.parse(results.get("day_length").toString(), DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime civilTwilightBegin        = LocalDateTime.parse(results.get("civil_twilight_begin").toString(), DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime nauticalTwilightBegin     = LocalDateTime.parse(results.get("nautical_twilight_begin").toString(), DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime nauticalTwilightEnd       = LocalDateTime.parse(results.get("nautical_twilight_end").toString(), DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime astronomicalTwilightBegin = LocalDateTime.parse(results.get("astronomical_twilight_begin").toString(), DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime astronomicalTwilightEnd   = LocalDateTime.parse(results.get("astronomical_twilight_end").toString(), DateTimeFormatter.ISO_DATE_TIME);
        */

        return new ZonedDateTime[] {ZonedDateTime.of(sunrise, ZONE_ID), ZonedDateTime.of(sunset, ZONE_ID)};
    }
}
