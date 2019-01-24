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

package eu.hansolo.tilesfx.tools;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;


public class SunMoonCalculator {

    /**
     * The set of twilights to calculate (types of rise/set events).
     */
    public enum TWILIGHT {
        /**
         * Event ID for calculation of rising and setting times for astronomical
         * twilight. In this case, the calculated time will be the time when the
         * center of the object is at -18 degrees of geometrical elevation below the
         * astronomical horizon. At this time astronomical observations are possible
         * because the sky is dark enough.
         */
        TWILIGHT_ASTRONOMICAL, /**
         * Event ID for calculation of rising and setting times for nautical
         * twilight. In this case, the calculated time will be the time when the
         * center of the object is at -12 degrees of geometric elevation below the
         * astronomical horizon.
         */
        TWILIGHT_NAUTICAL, /**
         * Event ID for calculation of rising and setting times for civil twilight.
         * In this case, the calculated time will be the time when the center of the
         * object is at -6 degrees of geometric elevation below the astronomical
         * horizon.
         */
        TWILIGHT_CIVIL, /**
         * The standard value of 34' for the refraction at the local horizon.
         */
        BLUE_HOUR,
        HORIZON_34arcmin,
        GOLDEN_HOUR
    }

    public static final double RAD_TO_DEG              = 180.0 / Math.PI;
    public static final double DEG_TO_RAD              = 1.0 / RAD_TO_DEG;
    public static final double RAD_TO_HOUR             = 180.0 / (15.0 * Math.PI);
    public static final double RAD_TO_DAY              = RAD_TO_HOUR / 24.0;
    public static final double AU                      = 149597870.691;
    public static final double EARTH_RADIUS            = 6378.1366;
    public static final double TWO_PI                  = 2.0 * Math.PI;
    public static final double TWO_PI_INVERSE          = 1.0 / (2.0 * Math.PI);
    public static final double FOUR_PI                 = 4.0 * Math.PI;
    public static final double PI_OVER_TWO             = Math.PI / 2.0;
    public static final double SIDEREAL_DAY_LENGTH     = 1.00273781191135448;
    public static final double JULIAN_DAYS_PER_CENTURY = 36525.0;
    public static final double SECONDS_PER_DAY         = 86400;
    public static final double J2000                   = 2451545.0;

    private             ZonedDateTime sunriseZDT;
    private             ZonedDateTime sunsetZDT;
    private             ZonedDateTime sunriseGoldenHourZDT;
    private             ZonedDateTime sunsetGoldenHourZDT;
    private             ZonedDateTime sunriseCivilZDT;
    private             ZonedDateTime sunsetCivilZDT;
    private             ZonedDateTime sunriseBlueHourZDT;
    private             ZonedDateTime sunsetBlueHourZDT;


    /**
     * Values for azimuth, elevation, rise, set, and transit for the Sun. Angles in radians, rise ... as Julian days in UT.
     * Distance in AU.
     */
    public double sunAz, sunEl, sunrise, sunset, sunTransit, sunTransitElev, sunDist;

    /**
     * Values for azimuth, elevation, rise, set, and transit for the Moon. Angles in radians, rise ... as Julian days in UT.
     * Moon age is the number of days since last new Moon, in days, from 0 to 29.5. Distance in AU.
     */
    public double moonAz, moonEl, moonRise, moonSet, moonTransit, moonAge, moonTransitElev, moonDist;

    /**
     * Input values.
     */
    private double   jd_UT      = 0;
    private double   t          = 0;
    private double   latitude   = 0;
    private double   longitude  = 0;
    private double   ttMinusUT  = 0;
    private TWILIGHT twilight   = TWILIGHT.HORIZON_34arcmin;
    private double   slongitude = 0, sanomaly = 0;


    /**
     * Main constructor for Sun/Moon calculations. Observer angles in degrees.
     *
     * @param YEAR   The year.
     * @param MONTH  The month.
     * @param DAY    The day.
     * @param LAT_DEG Latitude for the observer.
     * @param LNG_DEG Longitude for the observer.
     *
     * @throws Exception If the date does not exists.
     */
    public SunMoonCalculator(final int YEAR, final int MONTH, final int DAY, final double LAT_DEG, final double LNG_DEG) throws Exception{
        this(YEAR, MONTH, DAY, 0, 0, 0, LAT_DEG, LNG_DEG);
    }
    /**
     * Main constructor for Sun/Moon calculations. Time should be given in
     * Universal Time (UT), observer angles in degrees.
     *
     * @param year   The year.
     * @param month  The month.
     * @param day    The day.
     * @param h      The hour.
     * @param m      Minute.
     * @param s      Second.
     * @param latDeg Latitude for the observer.
     * @param lngDeg Longitude for the observer.
     *
     * @throws Exception If the date does not exists.
     */
    public SunMoonCalculator(int year, int month, int day, int h, int m, int s, double latDeg, double lngDeg) throws Exception {
        sunriseZDT           = ZonedDateTime.now();
        sunsetZDT            = ZonedDateTime.now();
        sunriseGoldenHourZDT = ZonedDateTime.now();
        sunsetGoldenHourZDT  = ZonedDateTime.now();
        sunriseCivilZDT      = ZonedDateTime.now();
        sunsetCivilZDT       = ZonedDateTime.now();
        sunriseBlueHourZDT   = ZonedDateTime.now();
        sunsetBlueHourZDT    = ZonedDateTime.now();

        latitude  = lngDeg * DEG_TO_RAD;
        longitude = latDeg * DEG_TO_RAD;
        setDate(year, month, day, h, m, s);
    }


    /**
     * Transforms a Julian day (rise/set/transit fields) to a common date.
     *
     * @param JULIAN_DAY The Julian day.
     *
     * @return A set of integers: year, month, day, hour, minute, second.
     *
     * @throws Exception If the input date does not exists.
     */
    public static int[] getDate(final double JULIAN_DAY) throws Exception {
        if (JULIAN_DAY < 2299160.0 && JULIAN_DAY >= 2299150.0) {
            throw new Exception("invalid julian day " + JULIAN_DAY + ". This date does not exist.");
        }

        // The conversion formulas are from Meeus,
        // Chapter 7.
        double Z = Math.floor(JULIAN_DAY + 0.5);
        double F = JULIAN_DAY + 0.5 - Z;
        double A = Z;
        if (Z >= 2299161D) {
            int a = (int) ((Z - 1867216.25) / 36524.25);
            A += 1 + a - a / 4;
        }
        double B = A + 1524;
        int    C = (int) ((B - 122.1) / 365.25);
        int    D = (int) (C * 365.25);
        int    E = (int) ((B - D) / 30.6001);

        double exactDay = F + B - D - (int) (30.6001 * E);
        int    day      = (int) exactDay;
        int    month    = (E < 14) ? E - 1 : E - 13;
        int    year     = C - 4715;
        if (month > 2) year--;
        double h = ((exactDay - day) * SECONDS_PER_DAY) / 3600.0;

        int    hour   = (int) h;
        double m      = (h - hour) * 60.0;
        int    minute = (int) m;
        int    second = (int) ((m - minute) * 60.0);

        return new int[] { year, month, day, hour, minute, second };
    }

    public static ZonedDateTime getZonedDateTime(final double JULIAN_DAY, final ZoneOffset ZONE_OFFSET) throws Exception {
        int[]  date           = getDate(JULIAN_DAY);
        int    timeZoneOffset = ZONE_OFFSET.getTotalSeconds() / 3600;
        ZoneId zoneId         = ZoneId.ofOffset("UTC", ZONE_OFFSET);
        return ZonedDateTime.of(date[0], date[1], date[2], (date[3] + timeZoneOffset) % 24, date[4], date[5], 0, zoneId);
    }

    public static String getDateAsString(final double JULIAN_DAY) throws Exception {
        return getDateAsString(JULIAN_DAY, 0);
    }
    /**
     * Returns a date as a string.
     *
     * @param JULIAN_DAY The Juliand day.
     *
     * @return The String.
     *
     * @throws Exception If the date does not exists.
     */
    public static String getDateAsString(final double JULIAN_DAY, final int TIMEZONE_OFFSET) throws Exception {
        if (JULIAN_DAY == -1) return "NO RISE/SET/TRANSIT FOR THIS OBSERVER/DATE";

        int date[] = SunMoonCalculator.getDate(JULIAN_DAY);
        return date[0] + "/" + date[1] + "/" + date[2] + " " + ((date[3] + TIMEZONE_OFFSET) % 24) + ":" + date[4] + ":" + date[5] + " UT";
    }


    /**
     * Reduce an angle in radians to the range (0 - 2 Pi).
     *
     * @param r Value in radians.
     *
     * @return The reduced radian value.
     */
    public static double normalizeRadians(double r) {
        if (r < 0 && r >= -TWO_PI) return r + TWO_PI;
        if (r >= TWO_PI && r < FOUR_PI) return r - TWO_PI;
        if (r >= 0 && r < TWO_PI) return r;

        r -= TWO_PI * Math.floor(r * TWO_PI_INVERSE);
        if (r < 0.) r += TWO_PI;

        return r;
    }

    /**
     * Sets the rise/set times to return. Default is
     * for the local horizon.
     *
     * @param T The Twilight.
     */
    public void setTwilight(final TWILIGHT T) {
        twilight = T;
    }

    private void setUTDate(final double JULIAN_DAY) {
        jd_UT = JULIAN_DAY;
        t = (JULIAN_DAY + ttMinusUT / SECONDS_PER_DAY - J2000) / JULIAN_DAYS_PER_CENTURY;
    }

    public void setDate(final LocalDate DATE) throws Exception {
        setDate(DATE.getYear(), DATE.getMonthValue(), DATE.getDayOfMonth());
    }
    public void setDate(final LocalDateTime DATE_TIME) throws Exception {
        setDate(DATE_TIME.getYear(), DATE_TIME.getMonthValue(), DATE_TIME.getDayOfMonth(),
                DATE_TIME.getHour(), DATE_TIME.getMinute(), DATE_TIME.getSecond());
    }
    public void setDate(int year, int month, int day) throws Exception {
        setDate(year, month, day, 0, 0, 0);
    }
    public void setDate(int year, int month, int day, int h, int m, int s) throws Exception {
        // The conversion formulas are from Meeus, chapter 7.
        boolean julian = false;
        if (year < 1582 || (year == 1582 && month <= 10) || (year == 1582 && month == 10 && day < 15)) julian = true;
        if (month < 3) {
            year--;
            month += 12;
        }
        int A = year / 100;
        int B = julian ? 0 : 2 - A + A / 4;

        double dayFraction = (h + (m + (s / 60.0)) / 60.0) / 24.0;

        double jd = dayFraction + (int) (365.25D * (year + 4716)) + (int) (30.6001 * (month + 1)) + day + B - 1524.5;

        if (jd < 2299160.0 && jd >= 2299150.0) {
            throw new Exception("invalid julian day " + jd + ". This date does not exist.");
        }

        ttMinusUT = 0;
        if (year > -600 && year < 2200) {
            double x  = year + (month - 1 + day / 30.0) / 12.0;
            double x2 = x * x, x3 = x2 * x, x4 = x3 * x;
            if (year < 1600) {
                ttMinusUT = 10535.328003326353 - 9.995238627481024 * x + 0.003067307630020489 * x2 - 7.76340698361363E-6 * x3 + 3.1331045394223196E-9 * x4 +
                            8.225530854405553E-12 * x2 * x3 - 7.486164715632051E-15 * x4 * x2 + 1.9362461549678834E-18 * x4 * x3 - 8.489224937827653E-23 * x4 * x4;
            } else {
                ttMinusUT = -1027175.3477559977 + 2523.256625418965 * x - 1.885686849058459 * x2 + 5.869246227888417E-5 * x3 + 3.3379295816475025E-7 * x4 +
                            1.7758961671447929E-10 * x2 * x3 - 2.7889902806153024E-13 * x2 * x4 + 1.0224295822336825E-16 * x3 * x4 - 1.2528102370680435E-20 * x4 * x4;
            }
        }
        setUTDate(jd);
    }


    /**
     * Calculates everything for the Sun and the Moon.
     */
    public void calcSunAndMoon() {
        double jd = this.jd_UT;

        // First the Sun
        double out[] = doCalc(getSun());
        sunAz = out[0];
        sunEl = out[1];
        sunrise = out[2];
        sunset = out[3];
        sunTransit = out[4];
        sunTransitElev = out[5];
        sunDist = out[8];
        double sa = sanomaly, sl = slongitude;

        int niter = 3; // Number of iterations to get accurate rise/set/transit times
        sunrise = obtainAccurateRiseSetTransit(sunrise, 2, niter, true);
        sunset = obtainAccurateRiseSetTransit(sunset, 3, niter, true);
        sunTransit = obtainAccurateRiseSetTransit(sunTransit, 4, niter, true);
        if (sunTransit == -1) {
            sunTransitElev = 0;
        } else {
            // Update Sun's maximum elevation
            setUTDate(sunTransit);
            out = doCalc(getSun());
            sunTransitElev = out[5];
        }

        // Now Moon
        setUTDate(jd);
        sanomaly = sa;
        slongitude = sl;
        out = doCalc(getMoon());
        moonAz = out[0];
        moonEl = out[1];
        moonRise = out[2];
        moonSet = out[3];
        moonTransit = out[4];
        moonTransitElev = out[5];
        moonDist = out[8];
        double ma = moonAge;

        niter = 5; // Number of iterations to get accurate rise/set/transit times
        moonRise = obtainAccurateRiseSetTransit(moonRise, 2, niter, false);
        moonSet = obtainAccurateRiseSetTransit(moonSet, 3, niter, false);
        moonTransit = obtainAccurateRiseSetTransit(moonTransit, 4, niter, false);
        if (moonTransit == -1) {
            moonTransitElev = 0;
        } else {
            // Update Moon's maximum elevation
            setUTDate(moonTransit);
            getSun();
            out = doCalc(getMoon());
            moonTransitElev = out[5];
        }
        setUTDate(jd);
        sanomaly = sa;
        slongitude = sl;
        moonAge = ma;
    }

    private double[] getSun() {
        // SUN PARAMETERS (Formulae from "Calendrical Calculations")
        double lon  = (280.46645 + 36000.76983 * t + .0003032 * t * t);
        double anom = (357.5291 + 35999.0503 * t - .0001559 * t * t - 4.8E-07 * t * t * t);
        sanomaly = anom * DEG_TO_RAD;
        double c = (1.9146 - .004817 * t - .000014 * t * t) * Math.sin(sanomaly);
        c = c + (.019993 - .000101 * t) * Math.sin(2 * sanomaly);
        c = c + .00029 * Math.sin(3.0 * sanomaly); // Correction to the mean ecliptic longitude

        // Now, let calculate nutation and aberration
        double M1 = (124.90 - 1934.134 * t + 0.002063 * t * t) * DEG_TO_RAD;
        double M2 = (201.11 + 72001.5377 * t + 0.00057 * t * t) * DEG_TO_RAD;
        double d  = -.00569 - .0047785 * Math.sin(M1) - .0003667 * Math.sin(M2);

        slongitude = lon + c + d; // apparent longitude (error<0.003 deg)
        double slatitude = 0; // Sun's ecliptic latitude is always negligible
        double ecc       = .016708617 - 4.2037E-05 * t - 1.236E-07 * t * t; // Eccentricity
        double v         = sanomaly + c * DEG_TO_RAD; // True anomaly
        double sdistance = 1.000001018 * (1.0 - ecc * ecc) / (1.0 + ecc * Math.cos(v)); // In UA

        return new double[]{slongitude, slatitude, sdistance, Math.atan(696000 / (AU * sdistance))};
    }

    private double[] getMoon() {
        // MOON PARAMETERS (Formulae from "Calendrical Calculations")
        double phase = normalizeRadians((297.8502042 + 445267.1115168 * t - 0.00163 * t * t + t * t * t / 538841 - t * t * t * t / 65194000) * DEG_TO_RAD);

        // Anomalistic phase
        double anomaly = (134.9634114 + 477198.8676313 * t + .008997 * t * t + t * t * t / 69699 - t * t * t * t / 14712000);
        anomaly = anomaly * DEG_TO_RAD;

        // Degrees from ascending node
        double node = (93.2720993 + 483202.0175273 * t - 0.0034029 * t * t - t * t * t / 3526000 + t * t * t * t / 863310000);
        node = node * DEG_TO_RAD;

        double E = 1.0 - (.002495 + 7.52E-06 * (t + 1.0)) * (t + 1.0);

        // Now longitude, with the three main correcting terms of evection,
        // variation, and equation of year, plus other terms (error<0.01 deg)
        // P. Duffet's MOON program taken as reference
        double l = (218.31664563 + 481267.8811958 * t - .00146639 * t * t + t * t * t / 540135.03 - t * t * t * t / 65193770.4);
        l += 6.28875 * Math.sin(anomaly) + 1.274018 * Math.sin(2 * phase - anomaly) + .658309 * Math.sin(2 * phase);
        l += 0.213616 * Math.sin(2 * anomaly) - E * .185596 * Math.sin(sanomaly) - 0.114336 * Math.sin(2 * node);
        l += .058793 * Math.sin(2 * phase - 2 * anomaly) + .057212 * E * Math.sin(2 * phase - anomaly - sanomaly) + .05332 * Math.sin(2 * phase + anomaly);
        l += .045874 * E * Math.sin(2 * phase - sanomaly) + .041024 * E * Math.sin(anomaly - sanomaly) - .034718 * Math.sin(phase) - E * .030465 * Math.sin(sanomaly + anomaly);
        l += .015326 * Math.sin(2 * (phase - node)) - .012528 * Math.sin(2 * node + anomaly) - .01098 * Math.sin(2 * node - anomaly) + .010674 * Math.sin(4 * phase - anomaly);
        l += .010034 * Math.sin(3 * anomaly) + .008548 * Math.sin(4 * phase - 2 * anomaly);
        l += -E * .00791 * Math.sin(sanomaly - anomaly + 2 * phase) - E * .006783 * Math.sin(2 * phase + sanomaly) + .005162 * Math.sin(anomaly - phase) +
             E * .005 * Math.sin(sanomaly + phase);
        l += .003862 * Math.sin(4 * phase) + E * .004049 * Math.sin(anomaly - sanomaly + 2 * phase) + .003996 * Math.sin(2 * (anomaly + phase)) +
             .003665 * Math.sin(2 * phase - 3 * anomaly);
        l += E * 2.695E-3 * Math.sin(2 * anomaly - sanomaly) + 2.602E-3 * Math.sin(anomaly - 2 * (node + phase));
        l += E * 2.396E-3 * Math.sin(2 * (phase - anomaly) - sanomaly) - 2.349E-3 * Math.sin(anomaly + phase);
        l += E * E * 2.249E-3 * Math.sin(2 * (phase - sanomaly)) - E * 2.125E-3 * Math.sin(2 * anomaly + sanomaly);
        l += -E * E * 2.079E-3 * Math.sin(2 * sanomaly) + E * E * 2.059E-3 * Math.sin(2 * (phase - sanomaly) - anomaly);
        l += -1.773E-3 * Math.sin(anomaly + 2 * (phase - node)) - 1.595E-3 * Math.sin(2 * (node + phase));
        l += E * 1.22E-3 * Math.sin(4 * phase - sanomaly - anomaly) - 1.11E-3 * Math.sin(2 * (anomaly + node));
        double longitude = l;

        // Let's add nutation here also
        double M1 = (124.90 - 1934.134 * t + 0.002063 * t * t) * DEG_TO_RAD;
        double M2 = (201.11 + 72001.5377 * t + 0.00057 * t * t) * DEG_TO_RAD;
        double d  = -.0047785 * Math.sin(M1) - .0003667 * Math.sin(M2);
        longitude += d;

        // Get accurate Moon age
        double Psin = 29.530588853;
        moonAge = normalizeRadians((longitude - slongitude) * DEG_TO_RAD) * Psin / TWO_PI;

        // Now Moon parallax
        double parallax = .950724 + .051818 * Math.cos(anomaly) + .009531 * Math.cos(2 * phase - anomaly);
        parallax += .007843 * Math.cos(2 * phase) + .002824 * Math.cos(2 * anomaly);
        parallax += 0.000857 * Math.cos(2 * phase + anomaly) + E * .000533 * Math.cos(2 * phase - sanomaly);
        parallax += E * .000401 * Math.cos(2 * phase - anomaly - sanomaly) + E * .00032 * Math.cos(anomaly - sanomaly) - .000271 * Math.cos(phase);
        parallax += -E * .000264 * Math.cos(sanomaly + anomaly) - .000198 * Math.cos(2 * node - anomaly);
        parallax += 1.73E-4 * Math.cos(3 * anomaly) + 1.67E-4 * Math.cos(4 * phase - anomaly);

        // So Moon distance in Earth radii is, more or less,
        double distance = 1.0 / Math.sin(parallax * DEG_TO_RAD);

        // Ecliptic latitude with nodal phase (error<0.01 deg)
        l = 5.128189 * Math.sin(node) + 0.280606 * Math.sin(node + anomaly) + 0.277693 * Math.sin(anomaly - node);
        l += .173238 * Math.sin(2 * phase - node) + .055413 * Math.sin(2 * phase + node - anomaly);
        l += .046272 * Math.sin(2 * phase - node - anomaly) + .032573 * Math.sin(2 * phase + node);
        l += .017198 * Math.sin(2 * anomaly + node) + .009267 * Math.sin(2 * phase + anomaly - node);
        l += .008823 * Math.sin(2 * anomaly - node) + E * .008247 * Math.sin(2 * phase - sanomaly - node) + .004323 * Math.sin(2 * (phase - anomaly) - node);
        l += .0042 * Math.sin(2 * phase + node + anomaly) + E * .003372 * Math.sin(node - sanomaly - 2 * phase);
        l += E * 2.472E-3 * Math.sin(2 * phase + node - sanomaly - anomaly);
        l += E * 2.222E-3 * Math.sin(2 * phase + node - sanomaly);
        l += E * 2.072E-3 * Math.sin(2 * phase - node - sanomaly - anomaly);
        double latitude = l;

        return new double[]{longitude, latitude, distance * EARTH_RADIUS / AU, Math.atan(1737.4 / (distance * EARTH_RADIUS))};
    }

    private double[] doCalc(final double[] POS) {
        // Ecliptic to equatorial coordinates
        double t2  = this.t / 100.0;
        double tmp = t2 * (27.87 + t2 * (5.79 + t2 * 2.45));
        tmp = t2 * (-249.67 + t2 * (-39.05 + t2 * (7.12 + tmp)));
        tmp = t2 * (-1.55 + t2 * (1999.25 + t2 * (-51.38 + tmp)));
        tmp = (t2 * (-4680.93 + tmp)) / 3600.0;
        double angle = (23.4392911111111 + tmp) * DEG_TO_RAD; // obliquity

        // Add nutation in obliquity
        double M1 = (124.90 - 1934.134 * t + 0.002063 * t * t) * DEG_TO_RAD;
        double M2 = (201.11 + 72001.5377 * t + 0.00057 * t * t) * DEG_TO_RAD;
        double d  = .002558 * Math.cos(M1) - .00015339 * Math.cos(M2);
        angle += d * DEG_TO_RAD;

        POS[0] *= DEG_TO_RAD;
        POS[1] *= DEG_TO_RAD;
        double cl = Math.cos(POS[1]);
        double x  = POS[2] * Math.cos(POS[0]) * cl;
        double y  = POS[2] * Math.sin(POS[0]) * cl;
        double z  = POS[2] * Math.sin(POS[1]);
        tmp = y * Math.cos(angle) - z * Math.sin(angle);
        z = y * Math.sin(angle) + z * Math.cos(angle);
        y = tmp;

        // Obtain local apparent sidereal time
        double jd0   = Math.floor(jd_UT - 0.5) + 0.5;
        double T0    = (jd0 - J2000) / JULIAN_DAYS_PER_CENTURY;
        double secs  = (jd_UT - jd0) * SECONDS_PER_DAY;
        double gmst  = (((((-6.2e-6 * T0) + 9.3104e-2) * T0) + 8640184.812866) * T0) + 24110.54841;
        double msday = 1.0 + (((((-1.86e-5 * T0) + 0.186208) * T0) + 8640184.812866) / (SECONDS_PER_DAY * JULIAN_DAYS_PER_CENTURY));
        gmst = (gmst + msday * secs) * (15.0 / 3600.0) * DEG_TO_RAD;
        double lst = gmst + latitude;

        // Obtain topocentric rectangular coordinates
        // Set radiusAU = 0 for geocentric calculations
        // (rise/set/transit will have no sense in this case)
        double radiusAU     = EARTH_RADIUS / AU;
        double correction[] = new double[]{radiusAU * Math.cos(longitude) * Math.cos(lst), radiusAU * Math.cos(longitude) * Math.sin(lst), radiusAU * Math.sin(longitude)};
        double xtopo        = x - correction[0];
        double ytopo        = y - correction[1];
        double ztopo        = z - correction[2];

        // Obtain topocentric equatorial coordinates
        double ra  = 0.0;
        double dec = PI_OVER_TWO;
        if (ztopo < 0.0) dec = -dec;
        if (ytopo != 0.0 || xtopo != 0.0) {
            ra = Math.atan2(ytopo, xtopo);
            dec = Math.atan2(ztopo / Math.sqrt(xtopo * xtopo + ytopo * ytopo), 1.0);
        }
        double dist = Math.sqrt(xtopo * xtopo + ytopo * ytopo + ztopo * ztopo);

        // Hour angle
        double angh = lst - ra;

        // Obtain azimuth and geometric alt
        double sinlat = Math.sin(longitude);
        double coslat = Math.cos(longitude);
        double sindec = Math.sin(dec), cosdec = Math.cos(dec);
        double h      = sinlat * sindec + coslat * cosdec * Math.cos(angh);
        double alt    = Math.asin(h);
        double azy    = Math.sin(angh);
        double azx    = Math.cos(angh) * sinlat - sindec * coslat / cosdec;
        double azi    = Math.PI + Math.atan2(azy, azx); // 0 = north

        // Get apparent elevation
        if (alt > -3 * DEG_TO_RAD) {
            double r    = 0.016667 * DEG_TO_RAD * Math.abs(Math.tan(PI_OVER_TWO - (alt * RAD_TO_DEG + 7.31 / (alt * RAD_TO_DEG + 4.4)) * DEG_TO_RAD));
            double refr = r * (0.28 * 1010 / (10 + 273.0)); // Assuming pressure of 1010 mb and T = 10 C
            alt = Math.min(alt + refr, PI_OVER_TWO); // This is not accurate, but acceptable
        }

        switch (twilight) {
            case GOLDEN_HOUR:
                tmp = 6 * DEG_TO_RAD;
                break;
            case HORIZON_34arcmin:
                // Rise, set, transit times, taking into account Sun/Moon angular radius (pos[3]).
                // The 34' factor is the standard refraction at horizon.
                // Removing angular radius will do calculations for the center of the disk instead
                // of the upper limb.
                tmp = -(34.0 / 60.0) * DEG_TO_RAD - POS[3];
                break;
            case BLUE_HOUR:
                tmp = -4 * DEG_TO_RAD;
                break;
            case TWILIGHT_CIVIL:
                tmp = -6 * DEG_TO_RAD;
                break;
            case TWILIGHT_NAUTICAL:
                tmp = -12 * DEG_TO_RAD;
                break;
            case TWILIGHT_ASTRONOMICAL:
                tmp = -18 * DEG_TO_RAD;
                break;
        }

        // Compute cosine of hour angle
        tmp = (Math.sin(tmp) - Math.sin(longitude) * Math.sin(dec)) / (Math.cos(longitude) * Math.cos(dec));
        double celestialHoursToEarthTime = RAD_TO_DAY / SIDEREAL_DAY_LENGTH;

        // Make calculations for the meridian
        double transit_time1 = celestialHoursToEarthTime * normalizeRadians(ra - lst);
        double transit_time2 = celestialHoursToEarthTime * (normalizeRadians(ra - lst) - TWO_PI);
        double transit_alt   = Math.asin(Math.sin(dec) * Math.sin(longitude) + Math.cos(dec) * Math.cos(longitude));
        if (transit_alt > -3 * DEG_TO_RAD) {
            double r    = 0.016667 * DEG_TO_RAD * Math.abs(Math.tan(PI_OVER_TWO - (transit_alt * RAD_TO_DEG + 7.31 / (transit_alt * RAD_TO_DEG + 4.4)) * DEG_TO_RAD));
            double refr = r * (0.28 * 1010 / (10 + 273.0)); // Assuming pressure of 1010 mb and T = 10 C
            transit_alt = Math.min(transit_alt + refr, PI_OVER_TWO); // This is not accurate, but acceptable
        }

        // Obtain the current event in time
        double transit_time  = transit_time1;
        double jdToday       = Math.floor(jd_UT - 0.5) + 0.5;
        double transitToday2 = Math.floor(jd_UT + transit_time2 - 0.5) + 0.5;
        // Obtain the transit time. Preference should be given to the closest event
        // in time to the current calculation time
        if (jdToday == transitToday2 && Math.abs(transit_time2) < Math.abs(transit_time1)) transit_time = transit_time2;
        double transit = jd_UT + transit_time;

        // Make calculations for rise and set
        double rise = -1, set = -1;
        if (Math.abs(tmp) <= 1.0) {
            double ang_hor    = Math.abs(Math.acos(tmp));
            double rise_time1 = celestialHoursToEarthTime * normalizeRadians(ra - ang_hor - lst);
            double set_time1  = celestialHoursToEarthTime * normalizeRadians(ra + ang_hor - lst);
            double rise_time2 = celestialHoursToEarthTime * (normalizeRadians(ra - ang_hor - lst) - TWO_PI);
            double set_time2  = celestialHoursToEarthTime * (normalizeRadians(ra + ang_hor - lst) - TWO_PI);

            // Obtain the current events in time. Preference should be given to the closest event
            // in time to the current calculation time (so that iteration in other method will converge)
            double rise_time  = rise_time1;
            double riseToday2 = Math.floor(jd_UT + rise_time2 - 0.5) + 0.5;
            if (jdToday == riseToday2 && Math.abs(rise_time2) < Math.abs(rise_time1)) rise_time = rise_time2;

            double set_time  = set_time1;
            double setToday2 = Math.floor(jd_UT + set_time2 - 0.5) + 0.5;
            if (jdToday == setToday2 && Math.abs(set_time2) < Math.abs(set_time1)) set_time = set_time2;
            rise = jd_UT + rise_time;
            set = jd_UT + set_time;
        }

        return new double[]{azi, alt, rise, set, transit, transit_alt, ra, dec, dist};
    }

    private double obtainAccurateRiseSetTransit(double riseSetJD, final int INDEX, final int N_ITER, final boolean IS_SUN) {
        double step = -1;
        for (int i = 0; i < N_ITER; i++) {
            if (riseSetJD == -1) return riseSetJD; // -1 means no rise/set from that location
            setUTDate(riseSetJD);
            double out[] = IS_SUN ? doCalc(getSun()) : doCalc(getMoon());
            step = Math.abs(riseSetJD - out[INDEX]);
            riseSetJD = out[INDEX];
        }
        if (step > 1.0 / SECONDS_PER_DAY) return -1; // did not converge => without rise/set/transit in this date
        return riseSetJD;
    }

    public double[] getMoonDiskOrientationAngles() {
        double outS[]    = doCalc(getSun());
        double moonPos[] = getMoon();
        double outM[]    = doCalc(moonPos);
        double moonLon   = moonPos[0], moonLat = moonPos[1], moonRA = outM[6], moonDEC = outM[7];
        double sunRA     = outS[6], sunDEC = outS[7];

        // Moon's argument of latitude
        double F = (93.2720993 + 483202.0175273 * t - 0.0034029 * t * t - t * t * t / 3526000.0 + t * t * t * t / 863310000.0) * DEG_TO_RAD;
        // Moon's inclination
        double I = 1.54242 * DEG_TO_RAD;
        // Moon's mean ascending node longitude
        double omega = (125.0445550 - 1934.1361849 * t + 0.0020762 * t * t + t * t * t / 467410.0 - t * t * t * t / 18999000.0) * DEG_TO_RAD;
        // Obliquity of ecliptic (approx, better formulae up)
        double eps = 23.43929 * DEG_TO_RAD;

        // Obtain optical librations lp and bp
        double W     = moonLon - omega;
        double sinA  = Math.sin(W) * Math.cos(moonLat) * Math.cos(I) - Math.sin(moonLat) * Math.sin(I);
        double cosA  = Math.cos(W) * Math.cos(moonLat);
        double A     = Math.atan2(sinA, cosA);
        double lp    = normalizeRadians(A - F);
        double sinbp = -Math.sin(W) * Math.cos(moonLat) * Math.sin(I) - Math.sin(moonLat) * Math.cos(I);
        double bp    = Math.asin(sinbp);

        // Obtain position angle of axis p
        double x    = Math.sin(I) * Math.sin(omega);
        double y    = Math.sin(I) * Math.cos(omega) * Math.cos(eps) - Math.cos(I) * Math.sin(eps);
        double w    = Math.atan2(x, y);
        double sinp = Math.sqrt(x * x + y * y) * Math.cos(moonRA - w) / Math.cos(bp);
        double p    = Math.asin(sinp);

        // Compute bright limb angle bl
        double bl = (Math.PI + Math.atan2(Math.cos(sunDEC) * Math.sin(moonRA - sunRA),
                                          Math.cos(sunDEC) * Math.sin(moonDEC) * Math.cos(moonRA - sunRA) - Math.sin(sunDEC) * Math.cos(moonDEC)));

        // Paralactic angle par (first obtain local apparent sidereal time)
        double jd0   = Math.floor(jd_UT - 0.5) + 0.5;
        double T0    = (jd0 - J2000) / JULIAN_DAYS_PER_CENTURY;
        double secs  = (jd_UT - jd0) * SECONDS_PER_DAY;
        double gmst  = (((((-6.2e-6 * T0) + 9.3104e-2) * T0) + 8640184.812866) * T0) + 24110.54841;
        double msday = 1.0 + (((((-1.86e-5 * T0) + 0.186208) * T0) + 8640184.812866) / (SECONDS_PER_DAY * JULIAN_DAYS_PER_CENTURY));
        gmst = (gmst + msday * secs) * (15.0 / 3600.0) * DEG_TO_RAD;
        double lst = gmst + latitude;

        y = Math.sin(lst - moonRA);
        x = Math.tan(longitude) * Math.cos(moonDEC) - Math.sin(moonDEC) * Math.cos(lst - moonRA);
        double par = x != 0 ? Math.atan2(y, x) : (y / Math.abs(y)) * PI_OVER_TWO;
        return new double[]{ lp, bp, p, bl, par };
    }


    public void calcEphemeris(final ZoneId ZONE_ID) {
        try {
            setTwilight(TWILIGHT.HORIZON_34arcmin);
            calcSunAndMoon();
            LocalDateTime now        = LocalDateTime.now();
            ZonedDateTime zdt        = now.atZone(ZONE_ID);
            ZoneOffset    zoneOffset = zdt.getOffset();

            sunriseZDT = getZonedDateTime(sunrise, zoneOffset);
            sunsetZDT = getZonedDateTime(sunset, zoneOffset);

            // Calculate golden hour
            setTwilight(TWILIGHT.GOLDEN_HOUR);
            calcSunAndMoon();
            sunriseGoldenHourZDT = getZonedDateTime(sunrise, zoneOffset);
            sunsetGoldenHourZDT = getZonedDateTime(sunset, zoneOffset);

            // Calculate civil twilight
            setTwilight(TWILIGHT.TWILIGHT_CIVIL);
            calcSunAndMoon();
            sunriseCivilZDT = getZonedDateTime(sunrise, zoneOffset);
            sunsetCivilZDT = getZonedDateTime(sunset, zoneOffset);

            // Calculate blue hour
            setTwilight(TWILIGHT.BLUE_HOUR);
            calcSunAndMoon();
            sunriseBlueHourZDT = getZonedDateTime(sunrise, zoneOffset);
            sunsetBlueHourZDT = getZonedDateTime(sunset, zoneOffset);
        } catch (Exception e) {
            sunriseZDT           = ZonedDateTime.now();
            sunsetZDT            = ZonedDateTime.now();
            sunriseGoldenHourZDT = ZonedDateTime.now();
            sunsetGoldenHourZDT  = ZonedDateTime.now();
            sunriseCivilZDT      = ZonedDateTime.now();
            sunsetCivilZDT       = ZonedDateTime.now();
            sunriseBlueHourZDT   = ZonedDateTime.now();
            sunsetBlueHourZDT    = ZonedDateTime.now();
        }
    }
    public ZonedDateTime getSunrise() { return sunriseZDT; }
    public ZonedDateTime getSunset() { return sunsetZDT; }
    public ZonedDateTime getSunriseGoldenHour() { return sunriseGoldenHourZDT; }
    public ZonedDateTime getSunsetGoldenHour() { return sunsetGoldenHourZDT; }
    public ZonedDateTime getSunriseCivil() { return sunriseCivilZDT; }
    public ZonedDateTime getSunsetCivil() { return sunsetCivilZDT; }
    public ZonedDateTime getSunriseBlueHour() { return sunriseBlueHourZDT; }
    public ZonedDateTime getSunsetBlueHour() { return sunsetBlueHourZDT; }
}