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

import eu.hansolo.tilesfx.Section;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.function.Predicate;

import static eu.hansolo.tilesfx.tools.Country.AD;
import static eu.hansolo.tilesfx.tools.Country.AE;
import static eu.hansolo.tilesfx.tools.Country.AF;
import static eu.hansolo.tilesfx.tools.Country.AG;
import static eu.hansolo.tilesfx.tools.Country.AI;
import static eu.hansolo.tilesfx.tools.Country.AL;
import static eu.hansolo.tilesfx.tools.Country.AM;
import static eu.hansolo.tilesfx.tools.Country.AO;
import static eu.hansolo.tilesfx.tools.Country.AR;
import static eu.hansolo.tilesfx.tools.Country.AS;
import static eu.hansolo.tilesfx.tools.Country.AT;
import static eu.hansolo.tilesfx.tools.Country.AU;
import static eu.hansolo.tilesfx.tools.Country.AW;
import static eu.hansolo.tilesfx.tools.Country.AX;
import static eu.hansolo.tilesfx.tools.Country.AZ;
import static eu.hansolo.tilesfx.tools.Country.BA;
import static eu.hansolo.tilesfx.tools.Country.BB;
import static eu.hansolo.tilesfx.tools.Country.BD;
import static eu.hansolo.tilesfx.tools.Country.BE;
import static eu.hansolo.tilesfx.tools.Country.BF;
import static eu.hansolo.tilesfx.tools.Country.BG;
import static eu.hansolo.tilesfx.tools.Country.BH;
import static eu.hansolo.tilesfx.tools.Country.BI;
import static eu.hansolo.tilesfx.tools.Country.BJ;
import static eu.hansolo.tilesfx.tools.Country.BL;
import static eu.hansolo.tilesfx.tools.Country.BM;
import static eu.hansolo.tilesfx.tools.Country.BN;
import static eu.hansolo.tilesfx.tools.Country.BO;
import static eu.hansolo.tilesfx.tools.Country.BR;
import static eu.hansolo.tilesfx.tools.Country.BS;
import static eu.hansolo.tilesfx.tools.Country.BT;
import static eu.hansolo.tilesfx.tools.Country.BV;
import static eu.hansolo.tilesfx.tools.Country.BW;
import static eu.hansolo.tilesfx.tools.Country.BY;
import static eu.hansolo.tilesfx.tools.Country.BZ;
import static eu.hansolo.tilesfx.tools.Country.CA;
import static eu.hansolo.tilesfx.tools.Country.CC;
import static eu.hansolo.tilesfx.tools.Country.CD;
import static eu.hansolo.tilesfx.tools.Country.CF;
import static eu.hansolo.tilesfx.tools.Country.CG;
import static eu.hansolo.tilesfx.tools.Country.CH;
import static eu.hansolo.tilesfx.tools.Country.CI;
import static eu.hansolo.tilesfx.tools.Country.CK;
import static eu.hansolo.tilesfx.tools.Country.CL;
import static eu.hansolo.tilesfx.tools.Country.CM;
import static eu.hansolo.tilesfx.tools.Country.CN;
import static eu.hansolo.tilesfx.tools.Country.CO;
import static eu.hansolo.tilesfx.tools.Country.CR;
import static eu.hansolo.tilesfx.tools.Country.CU;
import static eu.hansolo.tilesfx.tools.Country.CV;
import static eu.hansolo.tilesfx.tools.Country.CX;
import static eu.hansolo.tilesfx.tools.Country.CY;
import static eu.hansolo.tilesfx.tools.Country.CZ;
import static eu.hansolo.tilesfx.tools.Country.DE;
import static eu.hansolo.tilesfx.tools.Country.DJ;
import static eu.hansolo.tilesfx.tools.Country.DK;
import static eu.hansolo.tilesfx.tools.Country.DM;
import static eu.hansolo.tilesfx.tools.Country.DO;
import static eu.hansolo.tilesfx.tools.Country.DZ;
import static eu.hansolo.tilesfx.tools.Country.EC;
import static eu.hansolo.tilesfx.tools.Country.EE;
import static eu.hansolo.tilesfx.tools.Country.EG;
import static eu.hansolo.tilesfx.tools.Country.EH;
import static eu.hansolo.tilesfx.tools.Country.ER;
import static eu.hansolo.tilesfx.tools.Country.ES;
import static eu.hansolo.tilesfx.tools.Country.ET;
import static eu.hansolo.tilesfx.tools.Country.FI;
import static eu.hansolo.tilesfx.tools.Country.FJ;
import static eu.hansolo.tilesfx.tools.Country.FK;
import static eu.hansolo.tilesfx.tools.Country.FM;
import static eu.hansolo.tilesfx.tools.Country.FO;
import static eu.hansolo.tilesfx.tools.Country.FR;
import static eu.hansolo.tilesfx.tools.Country.GA;
import static eu.hansolo.tilesfx.tools.Country.GB;
import static eu.hansolo.tilesfx.tools.Country.GD;
import static eu.hansolo.tilesfx.tools.Country.GE;
import static eu.hansolo.tilesfx.tools.Country.GF;
import static eu.hansolo.tilesfx.tools.Country.GG;
import static eu.hansolo.tilesfx.tools.Country.GH;
import static eu.hansolo.tilesfx.tools.Country.GI;
import static eu.hansolo.tilesfx.tools.Country.GL;
import static eu.hansolo.tilesfx.tools.Country.GM;
import static eu.hansolo.tilesfx.tools.Country.GP;
import static eu.hansolo.tilesfx.tools.Country.GQ;
import static eu.hansolo.tilesfx.tools.Country.GR;
import static eu.hansolo.tilesfx.tools.Country.GS;
import static eu.hansolo.tilesfx.tools.Country.GT;
import static eu.hansolo.tilesfx.tools.Country.GU;
import static eu.hansolo.tilesfx.tools.Country.GW;
import static eu.hansolo.tilesfx.tools.Country.GY;
import static eu.hansolo.tilesfx.tools.Country.HK;
import static eu.hansolo.tilesfx.tools.Country.HM;
import static eu.hansolo.tilesfx.tools.Country.HN;
import static eu.hansolo.tilesfx.tools.Country.HR;
import static eu.hansolo.tilesfx.tools.Country.HT;
import static eu.hansolo.tilesfx.tools.Country.HU;
import static eu.hansolo.tilesfx.tools.Country.ID;
import static eu.hansolo.tilesfx.tools.Country.IE;
import static eu.hansolo.tilesfx.tools.Country.IL;
import static eu.hansolo.tilesfx.tools.Country.IM;
import static eu.hansolo.tilesfx.tools.Country.IN;
import static eu.hansolo.tilesfx.tools.Country.IO;
import static eu.hansolo.tilesfx.tools.Country.IQ;
import static eu.hansolo.tilesfx.tools.Country.IR;
import static eu.hansolo.tilesfx.tools.Country.IS;
import static eu.hansolo.tilesfx.tools.Country.IT;
import static eu.hansolo.tilesfx.tools.Country.JE;
import static eu.hansolo.tilesfx.tools.Country.JM;
import static eu.hansolo.tilesfx.tools.Country.JO;
import static eu.hansolo.tilesfx.tools.Country.JP;
import static eu.hansolo.tilesfx.tools.Country.KE;
import static eu.hansolo.tilesfx.tools.Country.KG;
import static eu.hansolo.tilesfx.tools.Country.KH;
import static eu.hansolo.tilesfx.tools.Country.KI;
import static eu.hansolo.tilesfx.tools.Country.KM;
import static eu.hansolo.tilesfx.tools.Country.KN;
import static eu.hansolo.tilesfx.tools.Country.KP;
import static eu.hansolo.tilesfx.tools.Country.KR;
import static eu.hansolo.tilesfx.tools.Country.KW;
import static eu.hansolo.tilesfx.tools.Country.KY;
import static eu.hansolo.tilesfx.tools.Country.KZ;
import static eu.hansolo.tilesfx.tools.Country.LA;
import static eu.hansolo.tilesfx.tools.Country.LB;
import static eu.hansolo.tilesfx.tools.Country.LC;
import static eu.hansolo.tilesfx.tools.Country.LI;
import static eu.hansolo.tilesfx.tools.Country.LK;
import static eu.hansolo.tilesfx.tools.Country.LR;
import static eu.hansolo.tilesfx.tools.Country.LS;
import static eu.hansolo.tilesfx.tools.Country.LT;
import static eu.hansolo.tilesfx.tools.Country.LU;
import static eu.hansolo.tilesfx.tools.Country.LV;
import static eu.hansolo.tilesfx.tools.Country.LY;
import static eu.hansolo.tilesfx.tools.Country.MA;
import static eu.hansolo.tilesfx.tools.Country.MC;
import static eu.hansolo.tilesfx.tools.Country.MD;
import static eu.hansolo.tilesfx.tools.Country.ME;
import static eu.hansolo.tilesfx.tools.Country.MF;
import static eu.hansolo.tilesfx.tools.Country.MG;
import static eu.hansolo.tilesfx.tools.Country.MH;
import static eu.hansolo.tilesfx.tools.Country.MK;
import static eu.hansolo.tilesfx.tools.Country.ML;
import static eu.hansolo.tilesfx.tools.Country.MM;
import static eu.hansolo.tilesfx.tools.Country.MN;
import static eu.hansolo.tilesfx.tools.Country.MO;
import static eu.hansolo.tilesfx.tools.Country.MP;
import static eu.hansolo.tilesfx.tools.Country.MQ;
import static eu.hansolo.tilesfx.tools.Country.MR;
import static eu.hansolo.tilesfx.tools.Country.MS;
import static eu.hansolo.tilesfx.tools.Country.MT;
import static eu.hansolo.tilesfx.tools.Country.MU;
import static eu.hansolo.tilesfx.tools.Country.MV;
import static eu.hansolo.tilesfx.tools.Country.MW;
import static eu.hansolo.tilesfx.tools.Country.MX;
import static eu.hansolo.tilesfx.tools.Country.MY;
import static eu.hansolo.tilesfx.tools.Country.MZ;
import static eu.hansolo.tilesfx.tools.Country.NA;
import static eu.hansolo.tilesfx.tools.Country.NC;
import static eu.hansolo.tilesfx.tools.Country.NE;
import static eu.hansolo.tilesfx.tools.Country.NF;
import static eu.hansolo.tilesfx.tools.Country.NG;
import static eu.hansolo.tilesfx.tools.Country.NI;
import static eu.hansolo.tilesfx.tools.Country.NL;
import static eu.hansolo.tilesfx.tools.Country.NO;
import static eu.hansolo.tilesfx.tools.Country.NP;
import static eu.hansolo.tilesfx.tools.Country.NR;
import static eu.hansolo.tilesfx.tools.Country.NU;
import static eu.hansolo.tilesfx.tools.Country.NZ;
import static eu.hansolo.tilesfx.tools.Country.OM;
import static eu.hansolo.tilesfx.tools.Country.PA;
import static eu.hansolo.tilesfx.tools.Country.PE;
import static eu.hansolo.tilesfx.tools.Country.PF;
import static eu.hansolo.tilesfx.tools.Country.PG;
import static eu.hansolo.tilesfx.tools.Country.PH;
import static eu.hansolo.tilesfx.tools.Country.PK;
import static eu.hansolo.tilesfx.tools.Country.PL;
import static eu.hansolo.tilesfx.tools.Country.PM;
import static eu.hansolo.tilesfx.tools.Country.PN;
import static eu.hansolo.tilesfx.tools.Country.PR;
import static eu.hansolo.tilesfx.tools.Country.PS;
import static eu.hansolo.tilesfx.tools.Country.PT;
import static eu.hansolo.tilesfx.tools.Country.PW;
import static eu.hansolo.tilesfx.tools.Country.PY;
import static eu.hansolo.tilesfx.tools.Country.QA;
import static eu.hansolo.tilesfx.tools.Country.RE;
import static eu.hansolo.tilesfx.tools.Country.RO;
import static eu.hansolo.tilesfx.tools.Country.RS;
import static eu.hansolo.tilesfx.tools.Country.RU;
import static eu.hansolo.tilesfx.tools.Country.RW;
import static eu.hansolo.tilesfx.tools.Country.SA;
import static eu.hansolo.tilesfx.tools.Country.SB;
import static eu.hansolo.tilesfx.tools.Country.SC;
import static eu.hansolo.tilesfx.tools.Country.SD;
import static eu.hansolo.tilesfx.tools.Country.SE;
import static eu.hansolo.tilesfx.tools.Country.SG;
import static eu.hansolo.tilesfx.tools.Country.SH;
import static eu.hansolo.tilesfx.tools.Country.SI;
import static eu.hansolo.tilesfx.tools.Country.SJ;
import static eu.hansolo.tilesfx.tools.Country.SK;
import static eu.hansolo.tilesfx.tools.Country.SL;
import static eu.hansolo.tilesfx.tools.Country.SM;
import static eu.hansolo.tilesfx.tools.Country.SN;
import static eu.hansolo.tilesfx.tools.Country.SO;
import static eu.hansolo.tilesfx.tools.Country.SR;
import static eu.hansolo.tilesfx.tools.Country.ST;
import static eu.hansolo.tilesfx.tools.Country.SV;
import static eu.hansolo.tilesfx.tools.Country.SY;
import static eu.hansolo.tilesfx.tools.Country.SZ;
import static eu.hansolo.tilesfx.tools.Country.TC;
import static eu.hansolo.tilesfx.tools.Country.TD;
import static eu.hansolo.tilesfx.tools.Country.TG;
import static eu.hansolo.tilesfx.tools.Country.TH;
import static eu.hansolo.tilesfx.tools.Country.TJ;
import static eu.hansolo.tilesfx.tools.Country.TK;
import static eu.hansolo.tilesfx.tools.Country.TL;
import static eu.hansolo.tilesfx.tools.Country.TM;
import static eu.hansolo.tilesfx.tools.Country.TN;
import static eu.hansolo.tilesfx.tools.Country.TO;
import static eu.hansolo.tilesfx.tools.Country.TR;
import static eu.hansolo.tilesfx.tools.Country.TT;
import static eu.hansolo.tilesfx.tools.Country.TV;
import static eu.hansolo.tilesfx.tools.Country.TW;
import static eu.hansolo.tilesfx.tools.Country.TZ;
import static eu.hansolo.tilesfx.tools.Country.UA;
import static eu.hansolo.tilesfx.tools.Country.UG;
import static eu.hansolo.tilesfx.tools.Country.US;
import static eu.hansolo.tilesfx.tools.Country.UY;
import static eu.hansolo.tilesfx.tools.Country.UZ;
import static eu.hansolo.tilesfx.tools.Country.VA;
import static eu.hansolo.tilesfx.tools.Country.VC;
import static eu.hansolo.tilesfx.tools.Country.VE;
import static eu.hansolo.tilesfx.tools.Country.VG;
import static eu.hansolo.tilesfx.tools.Country.VI;
import static eu.hansolo.tilesfx.tools.Country.VN;
import static eu.hansolo.tilesfx.tools.Country.VU;
import static eu.hansolo.tilesfx.tools.Country.WF;
import static eu.hansolo.tilesfx.tools.Country.WS;
import static eu.hansolo.tilesfx.tools.Country.XK;
import static eu.hansolo.tilesfx.tools.Country.YE;
import static eu.hansolo.tilesfx.tools.Country.YT;
import static eu.hansolo.tilesfx.tools.Country.ZA;
import static eu.hansolo.tilesfx.tools.Country.ZM;
import static eu.hansolo.tilesfx.tools.Country.ZW;


/**
 * Created by hansolo on 11.12.15.
 */
public class Helper {
    private static final double                         EPSILON                  = 1E-6;
    private static final String                         HIRES_COUNTRY_PROPERTIES = "eu/hansolo/tilesfx/highres.properties";
    private static final String                         LORES_COUNTRY_PROPERTIES = "eu/hansolo/tilesfx/lowres.properties";
    private static       Properties                     hiresCountryProperties;
    private static       Properties                     loresCountryProperties;

    public static final double   MAP_WIDTH         = 1009.1149817705154 - 1.154000163078308;
    public static final double   MAP_HEIGHT        = 665.2420043945312;
    public static final double   MAP_OFFSET_X      = -MAP_WIDTH * 0.0285;
    public static final double   MAP_OFFSET_Y      = MAP_HEIGHT * 0.195;

    public static final double   MIN_FONT_SIZE     = 5;

    public static final CountryGroup AMERICAS = new CountryGroup("AMERICAS", AI, AG, AR, AW, BS, BB, BZ, BM, BO, BR, CA, KY, CL, CO, CR, CU, DM, DO, EC, SV, GF, GD, GP, GT, GY, HT, HN, JM, MQ, MX, MS, NI, PA, PY, PE, PR, BL, KN, LC, MF, PM, VC, SR, TT, TC, US, UY, VE, VG, VI);
    public static final CountryGroup APAC     = new CountryGroup("APAC", AS, AU, BD, BN, BT, CC, CK, CN, CX, FJ, FM, GU, HK, ID, IN, IO, JP, KH, KI, KP, KR, LA, LK, MH, MM, MN, MO, MP, MV, MY, NC, NF, NP, NR, NU, NZ, PF, PG, PH, PK, PN, PW, SB, SG, TH, TK, TL, TO, TV, TW, VN, VU, WF, WS);
    public static final CountryGroup APJC     = new CountryGroup("APJC", AS, AU, BD, BN, BT, CC, CK, CN, CX, FJ, FM, GU, HK, HM, ID, IN, IO, JP, KH, KI, KP, KR, LA, LK, MH, MM, MN, MO, MP, MV, MY, NC, NF, NP, NR, NU, NZ, PF, PG, PH, PN, PW, SB, SG, TH, TK, TL, TO, TV, TW, VN, VU, WS);
    public static final CountryGroup ANZ      = new CountryGroup("ANZ", AU, NZ);
    public static final CountryGroup BENELUX  = new CountryGroup("BENELUX", BE, NL, LU);
    public static final CountryGroup BRICS    = new CountryGroup("BRICS", RU, BR, CN, IN, ZA);
    public static final CountryGroup DACH     = new CountryGroup("DACH", DE, AT, CH);
    public static final CountryGroup EMEA     = new CountryGroup("EMEA", AF, AX, AL, DZ, AD, AO, AM, AT, AZ, BH, BY, BE, BJ, BA, BW, BV, BG, BF, BI, CM, CV, CF, TD, KM, CD, CG, HR, CY, CZ, DK, DJ, EG, GQ, ER, EE, ET, FK, FO, FI, FR, GA, GM, GE, DE, GH, GI, GR, GL, GG, GW, HU, IS, IR, IQ, IE, IM, IL, IT, CI, JE, JO, KZ, KE, XK, KW, KG, LV, LB, LS, LR, LY, LI, LT, LU, MK, MG, MW, ML, MT, MR, MU, YT, MD, MC, ME, MA, MZ, NA, NL, NE, NG, NO, OM, PK, PS, PL, PT, QA, RE, RO, RU, RW, SH, SM, ST, SA, SN, RS, SC, SL, SK, SI, SO, ZA, GS, ES, SD, SJ, SZ, SE, CH, SY, TJ, TZ, TG, TN, TR, TM, UG, UA, AE, GB, UZ, VA, EH, YE, ZM, ZW);
    public static final CountryGroup EU       = new CountryGroup("EU", BE, GR, LT, PT, BG, ES, LU, RO, CZ, FR, HU, SI, DK, HR, MT, SK, DE, IT, NL, FI, EE, CY, AT, SE, IE, LV, PL, GB);
    public static final CountryGroup NORAM    = new CountryGroup("NORAM", US, CA, MX, GT, BZ, CU, DO, HT, HN, SV, NI, CR, PA);

    public static final String[] TIME_0_TO_5       = {"1", "2", "3", "4", "5", "0"};
    public static final String[] TIME_5_TO_0       = {"5", "4", "3", "2", "1", "0"};
    public static final String[] TIME_0_TO_9       = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
    public static final String[] TIME_9_TO_0       = {"9", "8", "7", "6", "5", "4", "3", "2", "1", "0"};
    public static final String[] TIME_0_TO_12      = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
    public static final String[] TIME_0_TO_24      = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
                                                      "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "00"};
    public static final String[] TIME_24_TO_0      = {"00", "23", "22", "21", "20", "19", "18", "17", "16", "15", "14", "13",
                                                      "12", "11", "10", "09", "08", "07", "06", "05", "04", "03", "02", "01"};
    public static final String[] TIME_00_TO_59     = {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
                                                      "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
                                                      "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
                                                      "30", "31", "32", "33", "34", "35", "36", "37", "38", "39",
                                                      "40", "41", "42", "43", "44", "45", "46", "47", "48", "49",
                                                      "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"};
    public static final String[] TIME_59_TO_00     = {"59", "58", "57", "56", "55", "54", "53", "52", "51", "50",
                                                      "49", "48", "47", "46", "45", "44", "43", "42", "41", "40",
                                                      "39", "38", "37", "36", "35", "34", "33", "32", "31", "30",
                                                      "29", "28", "27", "26", "25", "24", "23", "22", "21", "20",
                                                      "19", "18", "17", "16", "15", "14", "13", "12", "11", "10",
                                                      "09", "08", "07", "06", "05", "04", "03", "02", "01", "00"};
    public static final String[] NUMERIC           = {" ", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
    public static final String[] ALPHANUMERIC      = {" ", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
                                                      "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
                                                      "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
                                                      "W", "X", "Y", "Z"};
    public static final String[] ALPHA             = {" ", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                                                      "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
                                                      "V", "W", "X", "Y", "Z"};
    public static final String[] EXTENDED          = {" ", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
                                                      "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
                                                      "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
                                                      "W", "X", "Y", "Z", "-", "/", ":", ",", "", ";", "@",
                                                      "#", "+", "?", "!", "%", "$", "=", "<", ">"};
    public static final String[] EXTENDED_UMLAUTE  = {" ", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
                                                      "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
                                                      "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
                                                      "W", "X", "Y", "Z", "-", "/", ":", ",", "", ";", "@",
                                                      "#", "+", "?", "!", "%", "$", "=", "<", ">", "Ä", "Ö", "Ü", "ß"};

    public static final <T extends Number> T clamp(final T MIN, final T MAX, final T VALUE) {
        if (VALUE.doubleValue() < MIN.doubleValue()) return MIN;
        if (VALUE.doubleValue() > MAX.doubleValue()) return MAX;
        return VALUE;
    }

    public static final int clamp(final int MIN, final int MAX, final int VALUE) {
        if (VALUE < MIN) return MIN;
        if (VALUE > MAX) return MAX;
        return VALUE;
    }
    public static final long clamp(final long MIN, final long MAX, final long VALUE) {
        if (VALUE < MIN) return MIN;
        if (VALUE > MAX) return MAX;
        return VALUE;
    }
    public static final double clamp(final double MIN, final double MAX, final double VALUE) {
        if (Double.compare(VALUE, MIN) < 0) return MIN;
        if (Double.compare(VALUE, MAX) > 0) return MAX;
        return VALUE;
    }

    public static final double clampMin(final double MIN, final double VALUE) {
        if (VALUE < MIN) return MIN;
        return VALUE;
    }
    public static final double clampMax(final double MAX, final double VALUE) {
        if (VALUE > MAX) return MAX;
        return VALUE;
    }

    public static final double round(final double VALUE, final int PRECISION) {
        final int SCALE = (int) Math.pow(10, PRECISION);
        return (double) Math.round(VALUE * SCALE) / SCALE;
    }

    public static final double roundTo(final double VALUE, final double TARGET) { return TARGET * (Math.round(VALUE / TARGET)); }

    public static final double roundToHalf(final double VALUE) { return Math.round(VALUE * 2) / 2.0; }


    public static final double nearest(final double SMALLER, final double VALUE, final double LARGER) {
        return (VALUE - SMALLER) < (LARGER - VALUE) ? SMALLER : LARGER;
    }

    public static int roundDoubleToInt(final double VALUE){
        double dAbs = Math.abs(VALUE);
        int    i      = (int) dAbs;
        double result = dAbs - (double) i;
        if (result < 0.5) {
            return VALUE < 0 ? -i : i;
        } else {
            return VALUE < 0 ? -(i + 1) : i + 1;
        }
    }

    public static final double[] calcAutoScale(final double MIN_VALUE, final double MAX_VALUE) {
        double maxNoOfMajorTicks = 10;
        double maxNoOfMinorTicks = 10;
        double niceMinValue;
        double niceMaxValue;
        double niceRange;
        double majorTickSpace;
        double minorTickSpace;
        niceRange      = (calcNiceNumber((MAX_VALUE - MIN_VALUE), false));
        majorTickSpace = calcNiceNumber(niceRange / (maxNoOfMajorTicks - 1), true);
        niceMinValue   = (Math.floor(MIN_VALUE / majorTickSpace) * majorTickSpace);
        niceMaxValue   = (Math.ceil(MAX_VALUE / majorTickSpace) * majorTickSpace);
        minorTickSpace = calcNiceNumber(majorTickSpace / (maxNoOfMinorTicks - 1), true);
        return new double[]{ niceMinValue, niceMaxValue, majorTickSpace, minorTickSpace };
    }

    /**
     * Calculates nice minValue, maxValue and stepSize for given MIN and MAX values
     * @param MIN
     * @param MAX
     * @return array of doubles with [niceMin, niceMax, niceRange, niceStep]
     */
    public static final double[] getNiceScale(final double MIN, final double MAX) {
        return getNiceScale(MIN, MAX, 20);
    }
    /**
     * Calculates nice minValue, maxValue and stepSize for given MIN and MAX values
     * @param MIN
     * @param MAX
     * @param MAX_NO_OF_TICKS
     * @return array of doubles with [niceMin, niceMax, niceRange, niceStep]
     */
    public static final double[] getNiceScale(final double MIN, final double MAX, final int MAX_NO_OF_TICKS) {
        // Minimal increment to avoid round extreme values to be on the edge of the chart
        double minimum = MIN;
        double maximum = MAX;
        double epsilon = (MAX - MIN) / 1e6;
        maximum += epsilon;
        minimum -= epsilon;
        double range = maximum - minimum;

        // Target number of values to be displayed on the Y axis (it may be less)
        int stepCount = MAX_NO_OF_TICKS;
        // First approximation
        double roughStep = range / (stepCount - 1);

        // Set best niceStep for the range
        //double[] goodNormalizedSteps = { 1, 1.5, 2, 2.5, 5, 7.5, 10 }; // keep the 10 at the end
        double[] goodNormalizedSteps = { 1, 2, 5, 10 };

        // Normalize rough niceStep to find the normalized one that fits best
        double stepPower          = Math.pow(10, -Math.floor(Math.log10(Math.abs(roughStep))));
        double normalizedStep     = roughStep * stepPower;
        double goodNormalizedStep = Arrays.stream(goodNormalizedSteps).filter(n -> Double.compare(n, normalizedStep) >= 0).findFirst().getAsDouble();
        double niceStep           = goodNormalizedStep / stepPower;

        // Determine the scale limits based on the chosen niceStep.
        double niceMin = minimum < 0 ? Math.floor(minimum / niceStep) * niceStep : Math.ceil(minimum / niceStep) * niceStep;
        double niceMax = maximum < 0 ? Math.floor(maximum / niceStep) * niceStep : Math.ceil(maximum / niceStep) * niceStep;

        if (MIN % niceStep == 0) { niceMin = MIN; }
        if (MAX % niceStep == 0) { niceMax = MAX; }

        double niceRange = niceMax - niceMin;

        return new double[] { niceMin, niceMax, niceRange, niceStep };
    }

    /**
     * Can be used to implement discrete steps e.g. on a slider.
     * @param MIN_VALUE          The min value of the range
     * @param MAX_VALUE          The max value of the range
     * @param VALUE              The value to snap
     * @param MINOR_TICK_COUNT   The number of ticks between 2 major tick marks
     * @param MAJOR_TICK_UNIT    The distance between 2 major tick marks
     * @return The value snapped to the next tick mark defined by the given parameters
     */
    public static double snapToTicks(final double MIN_VALUE, final double MAX_VALUE, final double VALUE, final int MINOR_TICK_COUNT, final double MAJOR_TICK_UNIT) {
        double v = VALUE;
        int    minorTickCount = clamp(0, 10, MINOR_TICK_COUNT);
        double majorTickUnit  = Double.compare(MAJOR_TICK_UNIT, 0.0) <= 0 ? 0.25 : MAJOR_TICK_UNIT;
        double tickSpacing;

        if (minorTickCount != 0) {
            tickSpacing = majorTickUnit / (Math.max(minorTickCount, 0) + 1);
        } else {
            tickSpacing = majorTickUnit;
        }

        int    prevTick      = (int) ((v - MIN_VALUE) / tickSpacing);
        double prevTickValue = prevTick * tickSpacing + MIN_VALUE;
        double nextTickValue = (prevTick + 1) * tickSpacing + MIN_VALUE;

        v = nearest(prevTickValue, v, nextTickValue);

        return clamp(MIN_VALUE, MAX_VALUE, v);
    }

    /**
     * Returns a "niceScaling" number approximately equal to the range.
     * Rounds the number if ROUND == true.
     * Takes the ceiling if ROUND = false.
     *
     * @param RANGE the value range (maxValue - minValue)
     * @param ROUND whether to round the result or ceil
     * @return a "niceScaling" number to be used for the value range
     */
    public static final double calcNiceNumber(final double RANGE, final boolean ROUND) {
        double niceFraction;
        double exponent = Math.floor(Math.log10(RANGE));   // exponent of range
        double fraction = RANGE / Math.pow(10, exponent);  // fractional part of range

        if (ROUND) {
            if (Double.compare(fraction, 1.5) < 0) {
                niceFraction = 1;
            } else if (Double.compare(fraction, 3)  < 0) {
                niceFraction = 2;
            } else if (Double.compare(fraction, 7) < 0) {
                niceFraction = 5;
            } else {
                niceFraction = 10;
            }
        } else {
            if (Double.compare(fraction, 1) <= 0) {
                niceFraction = 1;
            } else if (Double.compare(fraction, 2) <= 0) {
                niceFraction = 2;
            } else if (Double.compare(fraction, 5) <= 0) {
                niceFraction = 5;
            } else {
                niceFraction = 10;
            }
        }
        return niceFraction * Math.pow(10, exponent);
    }

    public static final Color getColorOfSection(final List<Section> SECTIONS, final double VALUE, final Color DEFAULT_COLOR) {
        for (Section section : SECTIONS) {
            if (section.contains(VALUE)) return section.getColor();
        }
        return DEFAULT_COLOR;
    }

    public static final void adjustTextSize(final Text TEXT, final double MAX_WIDTH, final double FONT_SIZE) {
        final String FONT_NAME          = TEXT.getFont().getName();
        double       adjustableFontSize = FONT_SIZE;

        while (TEXT.getBoundsInLocal().getWidth() > MAX_WIDTH && adjustableFontSize > MIN_FONT_SIZE) {
            adjustableFontSize -= 0.05;
            TEXT.setFont(new Font(FONT_NAME, adjustableFontSize));
        }
    }
    public static final void adjustTextSize(final Label TEXT, final double MAX_WIDTH, final double FONT_SIZE) {
        final String FONT_NAME          = TEXT.getFont().getName();
        double       adjustableFontSize = FONT_SIZE;

        while (TEXT.getBoundsInLocal().getWidth() > MAX_WIDTH && adjustableFontSize > MIN_FONT_SIZE) {
            adjustableFontSize -= 0.05;
            TEXT.setFont(new Font(FONT_NAME, adjustableFontSize));
        }
    }

    public static final void fitNodeWidth(final Node NODE, final double MAX_WIDTH) {
        NODE.setVisible(NODE.getLayoutBounds().getWidth() < MAX_WIDTH);
        //enableNode(NODE, NODE.getLayoutBounds().getWidth() < MAX_WIDTH);
    }

    public static final DateTimeFormatter getDateFormat(final Locale LOCALE) {
        if (Locale.US == LOCALE) {
            return DateTimeFormatter.ofPattern("MM/dd/YYYY");
        } else if (Locale.CHINA == LOCALE) {
            return DateTimeFormatter.ofPattern("YYYY.MM.dd");
        } else {
            return DateTimeFormatter.ofPattern("dd.MM.getY()YYY");
        }
    }
    public static final DateTimeFormatter getLocalizedDateFormat(final Locale LOCALE) {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(LOCALE);
    }

    public static final void enableNode(final Node NODE, final boolean ENABLE) {
        NODE.setManaged(ENABLE);
        NODE.setVisible(ENABLE);
    }

    public static final String colorToCss(final Color COLOR) {
        return COLOR.toString().replace("0x", "#");
    }

    public static final ThreadFactory getThreadFactory(final String THREAD_NAME, final boolean IS_DAEMON) {
        return runnable -> {
            Thread thread = new Thread(runnable, THREAD_NAME);
            thread.setDaemon(IS_DAEMON);
            return thread;
        };
    }

    public static final void stopTask(ScheduledFuture<?> task) {
        if (null == task) return;
        task.cancel(true);
        task = null;
    }

    public static final boolean isMonochrome(final Color COLOR) {
        return Double.compare(COLOR.getRed(), COLOR.getGreen()) == 0 && Double.compare(COLOR.getGreen(), COLOR.getBlue()) == 0;
    }

    public static final double colorDistance(final Color COLOR_1, final Color COLOR_2) {
        final double DELTA_R = (COLOR_2.getRed()   - COLOR_1.getRed());
        final double DELTA_G = (COLOR_2.getGreen() - COLOR_1.getGreen());
        final double DELTA_B = (COLOR_2.getBlue()  - COLOR_1.getBlue());

        return Math.sqrt(DELTA_R * DELTA_R + DELTA_G * DELTA_G + DELTA_B * DELTA_B);
    }

    public static double[] colorToYUV(final Color COLOR) {
        final double WEIGHT_FACTOR_RED   = 0.299;
        final double WEIGHT_FACTOR_GREEN = 0.587;
        final double WEIGHT_FACTOR_BLUE  = 0.144;
        final double U_MAX               = 0.436;
        final double V_MAX               = 0.615;
        double y = clamp(0, 1, WEIGHT_FACTOR_RED * COLOR.getRed() + WEIGHT_FACTOR_GREEN * COLOR.getGreen() + WEIGHT_FACTOR_BLUE * COLOR.getBlue());
        double u = clamp(-U_MAX, U_MAX, U_MAX * ((COLOR.getBlue() - y) / (1 - WEIGHT_FACTOR_BLUE)));
        double v = clamp(-V_MAX, V_MAX, V_MAX * ((COLOR.getRed() - y) / (1 - WEIGHT_FACTOR_RED)));
        return new double[] { y, u, v };
    }

    public static final boolean isBright(final Color COLOR) { return Double.compare(colorToYUV(COLOR)[0], 0.5) >= 0.0; }
    public static final boolean isDark(final Color COLOR) { return colorToYUV(COLOR)[0] < 0.5; }

    public static final Color getContrastColor(final Color COLOR) {
        return COLOR.getBrightness() > 0.5 ? Color.BLACK : Color.WHITE;
    }

    public static final Color getColorWithOpacity(final Color COLOR, final double OPACITY) {
        return Color.color(COLOR.getRed(), COLOR.getGreen(), COLOR.getBlue(), clamp(0.0, 1.0, OPACITY));
    }

    public static final List<Color> createColorPalette(final Color FROM_COLOR, final Color TO_COLOR, final int NO_OF_COLORS) {
        int    steps        = clamp(1, 12, NO_OF_COLORS) - 1;
        double step         = 1.0 / steps;
        double deltaRed     = (TO_COLOR.getRed()     - FROM_COLOR.getRed())     * step;
        double deltaGreen   = (TO_COLOR.getGreen()   - FROM_COLOR.getGreen())   * step;
        double deltaBlue    = (TO_COLOR.getBlue()    - FROM_COLOR.getBlue())    * step;
        double deltaOpacity = (TO_COLOR.getOpacity() - FROM_COLOR.getOpacity()) * step;

        List<Color> palette      = new ArrayList<>(NO_OF_COLORS);
        Color       currentColor = FROM_COLOR;
        palette.add(currentColor);
        for (int i = 0 ; i < steps ; i++) {
            double red     = clamp(0d, 1d, (currentColor.getRed()     + deltaRed));
            double green   = clamp(0d, 1d, (currentColor.getGreen()   + deltaGreen));
            double blue    = clamp(0d, 1d, (currentColor.getBlue()    + deltaBlue));
            double opacity = clamp(0d, 1d, (currentColor.getOpacity() + deltaOpacity));
            currentColor   = Color.color(red, green, blue, opacity);
            palette.add(currentColor);
        }
        return palette;
    }

    public static final Color[] createColorVariations(final Color COLOR, final int NO_OF_COLORS) {
        int    noOfColors = clamp(1, 12, NO_OF_COLORS);
        double step       = 0.8 / noOfColors;
        double hue        = COLOR.getHue();
        double brg        = COLOR.getBrightness();
        Color[] colors = new Color[noOfColors];
        for (int i = 0 ; i < noOfColors ; i++) { colors[i] = Color.hsb(hue, 0.2 + i * step, brg); }
        return colors;
    }

    public static final Color getColorAt(final List<Stop> STOP_LIST, final double POSITION_OF_COLOR) {
        Map<Double, Stop> STOPS = new TreeMap<>();
        for (Stop stop : STOP_LIST) { STOPS.put(stop.getOffset(), stop); }

        if (STOPS.isEmpty()) return Color.BLACK;

        double minFraction = Collections.min(STOPS.keySet());
        double maxFraction = Collections.max(STOPS.keySet());

        if (Double.compare(minFraction, 0d) > 0) { STOPS.put(0.0, new Stop(0.0, STOPS.get(minFraction).getColor())); }
        if (Double.compare(maxFraction, 1d) < 0) { STOPS.put(1.0, new Stop(1.0, STOPS.get(maxFraction).getColor())); }

        final double POSITION = clamp(0d, 1d, POSITION_OF_COLOR);
        final Color COLOR;
        if (STOPS.size() == 1) {
            final Map<Double, Color> ONE_ENTRY = (Map<Double, Color>) STOPS.entrySet().iterator().next();
            COLOR = STOPS.get(ONE_ENTRY.keySet().iterator().next()).getColor();
        } else {
            Stop lowerBound = STOPS.get(0.0);
            Stop upperBound = STOPS.get(1.0);
            for (Double fraction : STOPS.keySet()) {
                if (Double.compare(fraction,POSITION) < 0) {
                    lowerBound = STOPS.get(fraction);
                }
                if (Double.compare(fraction, POSITION) > 0) {
                    upperBound = STOPS.get(fraction);
                    break;
                }
            }
            COLOR = interpolateColor(lowerBound, upperBound, POSITION);
        }
        return COLOR;
    }
    public static final Color interpolateColor(final Stop LOWER_BOUND, final Stop UPPER_BOUND, final double POSITION) {
        final double POS  = (POSITION - LOWER_BOUND.getOffset()) / (UPPER_BOUND.getOffset() - LOWER_BOUND.getOffset());

        final double DELTA_RED     = (UPPER_BOUND.getColor().getRed()     - LOWER_BOUND.getColor().getRed())     * POS;
        final double DELTA_GREEN   = (UPPER_BOUND.getColor().getGreen()   - LOWER_BOUND.getColor().getGreen())   * POS;
        final double DELTA_BLUE    = (UPPER_BOUND.getColor().getBlue()    - LOWER_BOUND.getColor().getBlue())    * POS;
        final double DELTA_OPACITY = (UPPER_BOUND.getColor().getOpacity() - LOWER_BOUND.getColor().getOpacity()) * POS;

        double red     = clamp(0, 1, (LOWER_BOUND.getColor().getRed()     + DELTA_RED));
        double green   = clamp(0, 1, (LOWER_BOUND.getColor().getGreen()   + DELTA_GREEN));
        double blue    = clamp(0, 1, (LOWER_BOUND.getColor().getBlue()    + DELTA_BLUE));
        double opacity = clamp(0, 1, (LOWER_BOUND.getColor().getOpacity() + DELTA_OPACITY));

        return Color.color(red, green, blue, opacity);
    }

    public static final void scaleNodeTo(final Node NODE, final double TARGET_WIDTH, final double TARGET_HEIGHT) {
        NODE.setScaleX(TARGET_WIDTH / NODE.getLayoutBounds().getWidth());
        NODE.setScaleY(TARGET_HEIGHT / NODE.getLayoutBounds().getHeight());
    }

    public static final String normalize(final String TEXT) {
        String normalized = TEXT.replaceAll("\u00fc", "ue")
                                .replaceAll("\u00f6", "oe")
                                .replaceAll("\u00e4", "ae")
                                .replaceAll("\u00df", "ss");

        normalized = normalized.replaceAll("\u00dc(?=[a-z\u00fc\u00f6\u00e4\u00df ])", "Ue")
                               .replaceAll("\u00d6(?=[a-z\u00fc\u00f6\u00e4\u00df ])", "Oe")
                               .replaceAll("\u00c4(?=[a-z\u00fc\u00f6\u00e4\u00df ])", "Ae");

        normalized = normalized.replaceAll("\u00dc", "UE")
                               .replaceAll("\u00d6", "OE")
                               .replaceAll("\u00c4", "AE");
        return normalized;
    }

    public static final boolean equals(final double A, final double B) { return A == B || Math.abs(A - B) < EPSILON; }
    public static final boolean biggerThan(final double A, final double B) { return (A - B) > EPSILON; }
    public static final boolean lessThan(final double A, final double B) { return (B - A) > EPSILON; }

    public static final List<Point> subdividePoints(final List<Point> POINTS, final int SUB_DEVISIONS) {
        Point[] points = POINTS.toArray(new Point[0]);
        return Arrays.asList(subdividePoints(points, SUB_DEVISIONS));
    }
    public static final Point[] subdividePoints(final Point[] POINTS, final int SUB_DEVISIONS) {
        assert POINTS != null;
        assert POINTS.length >= 3;
        int    noOfPoints = POINTS.length;

        Point[] subdividedPoints = new Point[((noOfPoints - 1) * SUB_DEVISIONS) + 1];

        double increments = 1.0 / (double) SUB_DEVISIONS;

        for (int i = 0 ; i < noOfPoints - 1 ; i++) {
            Point p0 = i == 0 ? POINTS[i] : POINTS[i - 1];
            Point p1 = POINTS[i];
            Point p2 = POINTS[i + 1];
            Point p3 = (i+2 == noOfPoints) ? POINTS[i + 1] : POINTS[i + 2];

            CatmullRom crs = new CatmullRom(p0, p1, p2, p3);

            for (int j = 0; j <= SUB_DEVISIONS; j++) {
                subdividedPoints[(i * SUB_DEVISIONS) + j] = crs.q(j * increments);
            }
        }

        return subdividedPoints;
    }

    public static final Point[] smoothSparkLine(final List<Double> DATA_LIST, final double MIN_VALUE, final double MAX_VALUE, final Rectangle GRAPH_BOUNDS, final int NO_OF_DATAPOINTS) {
        int     size   = DATA_LIST.size();
        Point[] points = new Point[size];

        double low  = Statistics.getMin(DATA_LIST);
        double high = Statistics.getMax(DATA_LIST);
        if (Helper.equals(low, high)) {
            low  = MIN_VALUE;
            high = MAX_VALUE;
        }
        double range = high - low;

        double minX  = GRAPH_BOUNDS.getX();
        double maxX  = minX + GRAPH_BOUNDS.getWidth();
        double minY  = GRAPH_BOUNDS.getY();
        double maxY  = minY + GRAPH_BOUNDS.getHeight();
        double stepX = GRAPH_BOUNDS.getWidth() / (NO_OF_DATAPOINTS - 1);
        double stepY = GRAPH_BOUNDS.getHeight() / range;

        for (int i = 0 ; i < size ; i++) {
            points[i] = new Point(minX + i * stepX, maxY - Math.abs(low - DATA_LIST.get(i)) * stepY);
        }

        return Helper.subdividePoints(points, 16);
    }

    public static final Map<String, List<CountryPath>> getHiresCountryPaths() {
        if (null == hiresCountryProperties) { hiresCountryProperties = readProperties(HIRES_COUNTRY_PROPERTIES); }

        Map<String, List<CountryPath>> hiresCountryPaths = new ConcurrentHashMap<>();
        hiresCountryProperties.forEach((key, value) -> {
            String            name     = key.toString();
            List<CountryPath> pathList = new ArrayList<>();
            for (String path : value.toString().split(";")) { pathList.add(new CountryPath(name, path)); }
            hiresCountryPaths.put(name, pathList);
        });
        return hiresCountryPaths;
    }
    public static final Map<String, List<CountryPath>> getLoresCountryPaths() {
        if (null == loresCountryProperties) { loresCountryProperties = readProperties(LORES_COUNTRY_PROPERTIES); }
        Map<String, List<CountryPath>> loresCountryPaths = new ConcurrentHashMap<>();
        loresCountryProperties.forEach((key, value) -> {
           String            name     = key.toString();
           List<CountryPath> pathList = new ArrayList<>();
           for (String path : value.toString().split(";")) { pathList.add(new CountryPath(name, path)); }
           loresCountryPaths.put(name, pathList);
        });
        return loresCountryPaths;
    }
    private static final Properties readProperties(final String FILE_NAME) {
        final ClassLoader LOADER     = Thread.currentThread().getContextClassLoader();
        final Properties  PROPERTIES = new Properties();
        try(InputStream resourceStream = LOADER.getResourceAsStream(FILE_NAME)) {
            PROPERTIES.load(resourceStream);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return PROPERTIES;
    }

    public static final void drawRoundedRect(final GraphicsContext CTX, final CtxBounds BOUNDS, final CtxCornerRadii RADII) {
        double x           = BOUNDS.getX();
        double y           = BOUNDS.getY();
        double width       = BOUNDS.getWidth();
        double height      = BOUNDS.getHeight();
        double xPlusWidth  = x + width;
        double yPlusHeight = y + height;

        CTX.beginPath();
        CTX.moveTo(x + RADII.getTopLeft(), y);
        CTX.lineTo(xPlusWidth - RADII.getTopRight(), y);
        CTX.quadraticCurveTo(xPlusWidth, y, xPlusWidth, y + RADII.getTopRight());
        CTX.lineTo(xPlusWidth, yPlusHeight - RADII.getBottomRight());
        CTX.quadraticCurveTo(xPlusWidth, yPlusHeight, xPlusWidth - RADII.getBottomRight(), yPlusHeight);
        CTX.lineTo(x + RADII.getBottomLeft(), yPlusHeight);
        CTX.quadraticCurveTo(x, yPlusHeight, x, yPlusHeight - RADII.getBottomLeft());
        CTX.lineTo(x, y + RADII.getTopLeft());
        CTX.quadraticCurveTo(x, y, x + RADII.getTopLeft(), y);
        CTX.closePath();
    }

    // Smooth given path defined by it's list of path elements
    public static final Path smoothPath(final ObservableList<PathElement> ELEMENTS, final boolean FILLED) {
        if (ELEMENTS.isEmpty()) { return new Path(); }
        final Point[] dataPoints = new Point[ELEMENTS.size()];
        for (int i = 0; i < ELEMENTS.size(); i++) {
            final PathElement element = ELEMENTS.get(i);
            if (element instanceof MoveTo) {
                MoveTo move   = (MoveTo) element;
                dataPoints[i] = new Point(move.getX(), move.getY());
            } else if (element instanceof LineTo) {
                LineTo line   = (LineTo) element;
                dataPoints[i] = new Point(line.getX(), line.getY());
            }
        }
        double                 zeroY               = ((MoveTo) ELEMENTS.get(0)).getY();
        List<PathElement>      smoothedElements    = new ArrayList<>();
        Pair<Point[], Point[]> result              = calcCurveControlPoints(dataPoints);
        Point[]                firstControlPoints  = result.getKey();
        Point[]                secondControlPoints = result.getValue();
        // Start path dependent on filled or not
        if (FILLED) {
            smoothedElements.add(new MoveTo(dataPoints[0].getX(), zeroY));
            smoothedElements.add(new LineTo(dataPoints[0].getX(), dataPoints[0].getY()));
        } else {
            smoothedElements.add(new MoveTo(dataPoints[0].getX(), dataPoints[0].getY()));
        }
        // Add curves
        for (int i = 2; i < dataPoints.length; i++) {
            final int ci = i - 1;
            smoothedElements.add(new CubicCurveTo(
                firstControlPoints[ci].getX(), firstControlPoints[ci].getY(),
                secondControlPoints[ci].getX(), secondControlPoints[ci].getY(),
                dataPoints[i].getX(), dataPoints[i].getY()));
        }
        // Close the path if filled
        if (FILLED) {
            smoothedElements.add(new LineTo(dataPoints[dataPoints.length - 1].getX(), zeroY));
            smoothedElements.add(new ClosePath());
        }
        return new Path(smoothedElements);
    }
    private static final Pair<Point[], Point[]> calcCurveControlPoints(final Point[] DATA_POINTS) {
        Point[] firstControlPoints;
        Point[] secondControlPoints;
        int n = DATA_POINTS.length - 1;
        if (n == 1) { // Special case: Bezier curve should be a straight line.
            firstControlPoints     = new Point[1];
            // 3P1 = 2P0 + P3
            firstControlPoints[0]  = new Point((2 * DATA_POINTS[0].getX() + DATA_POINTS[1].getX()) / 3, (2 * DATA_POINTS[0].getY() + DATA_POINTS[1].getY()) / 3);
            secondControlPoints    = new Point[1];
            // P2 = 2P1 – P0
            secondControlPoints[0] = new Point(2 * firstControlPoints[0].getX() - DATA_POINTS[0].getX(), 2 * firstControlPoints[0].getY() - DATA_POINTS[0].getY());
            return new Pair<>(firstControlPoints, secondControlPoints);
        }

        // Calculate first Bezier control points
        // Right hand side vector
        double[] rhs = new double[n];

        // Set right hand side X values
        for (int i = 1; i < n - 1; ++i) {
            rhs[i] = 4 * DATA_POINTS[i].getX() + 2 * DATA_POINTS[i + 1].getX();
        }
        rhs[0]     = DATA_POINTS[0].getX() + 2 * DATA_POINTS[1].getX();
        rhs[n - 1] = (8 * DATA_POINTS[n - 1].getX() + DATA_POINTS[n].getX()) / 2.0;
        // Get first control points X-values
        double[] x = getFirstControlPoints(rhs);

        // Set right hand side Y values
        for (int i = 1; i < n - 1; ++i) {
            rhs[i] = 4 * DATA_POINTS[i].getY() + 2 * DATA_POINTS[i + 1].getY();
        }
        rhs[0]     = DATA_POINTS[0].getY() + 2 * DATA_POINTS[1].getY();
        rhs[n - 1] = (8 * DATA_POINTS[n - 1].getY() + DATA_POINTS[n].getY()) / 2.0;
        // Get first control points Y-values
        double[] y = getFirstControlPoints(rhs);

        // Fill output arrays.
        firstControlPoints  = new Point[n];
        secondControlPoints = new Point[n];
        for (int i = 0; i < n; ++i) {
            // First control point
            firstControlPoints[i] = new Point(x[i], y[i]);
            // Second control point
            if (i < n - 1) {
                secondControlPoints[i] = new Point(2 * DATA_POINTS[i + 1].getX() - x[i + 1], 2 * DATA_POINTS[i + 1].getY() - y[i + 1]);
            } else {
                secondControlPoints[i] = new Point((DATA_POINTS[n].getX() + x[n - 1]) / 2, (DATA_POINTS[n].getY() + y[n - 1]) / 2);
            }
        }
        return new Pair<>(firstControlPoints, secondControlPoints);
    }
    private static final double[] getFirstControlPoints(double[] rhs) {
        int      n   = rhs.length;
        double[] x   = new double[n]; // Solution vector.
        double[] tmp = new double[n]; // Temp workspace.
        double   b   = 2.0;

        x[0] = rhs[0] / b;

        for (int i = 1; i < n; i++) {// Decomposition and forward substitution.
            tmp[i] = 1 / b;
            b      = (i < n - 1 ? 4.0 : 3.5) - tmp[i];
            x[i]   = (rhs[i] - x[i - 1]) / b;
        }
        for (int i = 1; i < n; i++) {
            x[n - i - 1] -= tmp[n - i] * x[n - i]; // Backsubstitution.
        }
        return x;
    }

    public static final boolean isInRectangle(final double X, final double Y,
                                              final double MIN_X, final double MIN_Y,
                                              final double MAX_X, final double MAX_Y) {
        return (Double.compare(X, MIN_X) >= 0 &&
                Double.compare(X, MAX_X) <= 0 &&
                Double.compare(Y, MIN_Y) >= 0 &&
                Double.compare(Y, MAX_Y) <= 0);
    }

    public static final boolean isInEllipse(final double X, final double Y,
                                            final double ELLIPSE_CENTER_X, final double ELLIPSE_CENTER_Y,
                                            final double ELLIPSE_RADIUS_X, final double ELLIPSE_RADIUS_Y) {
        return Double.compare(((((X - ELLIPSE_CENTER_X) * (X - ELLIPSE_CENTER_X)) / (ELLIPSE_RADIUS_X * ELLIPSE_RADIUS_X)) +
                               (((Y - ELLIPSE_CENTER_Y) * (Y - ELLIPSE_CENTER_Y)) / (ELLIPSE_RADIUS_Y * ELLIPSE_RADIUS_Y))), 1) <= 0.0;
    }

    public static final boolean isInPolygon(final double X, final double Y, final Polygon POLYGON) {
        List<Double> points              = POLYGON.getPoints();
        int          noOfPointsInPolygon = POLYGON.getPoints().size() / 2;
        double[]     pointsX             = new double[noOfPointsInPolygon];
        double[]     pointsY             = new double[noOfPointsInPolygon];
        int          pointCounter        = 0;
        
        for (int i = 0, size = points.size() ; i < size - 1 ; i += 2) {
            pointsX[pointCounter] = points.get(i);
            pointsY[pointCounter] = points.get(i + 1);
            pointCounter++;
        }
        return isInPolygon(X, Y, noOfPointsInPolygon, pointsX, pointsY);
    }
    public static final boolean isInPolygon(final double X, final double Y, final int NO_OF_POINTS_IN_POLYGON, final double[] POINTS_X, final double[] POINTS_Y) {
        if (NO_OF_POINTS_IN_POLYGON != POINTS_X.length || NO_OF_POINTS_IN_POLYGON != POINTS_Y.length) { return false; }
        boolean inside = false;
        for (int i = 0, j = NO_OF_POINTS_IN_POLYGON - 1; i < NO_OF_POINTS_IN_POLYGON ; j = i++) {
            if (((POINTS_Y[i] > Y) != (POINTS_Y[j] > Y)) && (X < (POINTS_X[j] - POINTS_X[i]) * (Y - POINTS_Y[i]) / (POINTS_Y[j] - POINTS_Y[i]) + POINTS_X[i])) {
                inside = !inside;
            }
        }
        return inside;
    }

    public static final boolean isInRingSegment(final double X, final double Y,
                                                final double CENTER_X, final double CENTER_Y,
                                                final double OUTER_RADIUS, final double INNER_RADIUS,
                                                final double START_ANGLE, final double SEGMENT_ANGLE) {
        double angleOffset = 90.0;
        double pointRadius = Math.sqrt((X - CENTER_X) * (X - CENTER_X) + (Y - CENTER_Y) * (Y - CENTER_Y));
        double pointAngle  = getAngleFromXY(X, Y, CENTER_X, CENTER_Y, angleOffset);
        double startAngle  = angleOffset - START_ANGLE;
        double endAngle    = startAngle + SEGMENT_ANGLE;

        return (Double.compare(pointRadius, INNER_RADIUS) >= 0 &&
                Double.compare(pointRadius, OUTER_RADIUS) <= 0 &&
                Double.compare(pointAngle, startAngle) >= 0 &&
                Double.compare(pointAngle, endAngle) <= 0);
    }

public static final double distance(final Point P1, final Point P2) {
        return distance(P1.getX(), P1.getY(), P2.getX(), P2.getY());
    }
    public static final double distance(final double P1_X, final double P1_Y, final double P2_X, final double P2_Y) {
        return Math.sqrt((P2_X - P1_X) * (P2_X - P1_X) + (P2_Y - P1_Y) * (P2_Y - P1_Y));
    }

    public static double euclideanDistance(final Point P1, final Point P2) { return euclideanDistance(P1.getX(), P1.getY(), P2.getX(), P2.getY()); }
    public static double euclideanDistance(final double X1, final double Y1, final double X2, final double Y2) {
        double deltaX = (X2 - X1);
        double deltaY = (Y2 - Y1);
        return (deltaX * deltaX) + (deltaY * deltaY);
    }

    public static final Point pointOnLine(final double P1_X, final double P1_Y, final double P2_X, final double P2_Y, final double DISTAINCE_TOP_2) {
        double distanceP1P2 = distance(P1_X, P1_Y, P2_X, P2_Y);
        double t = DISTAINCE_TOP_2 / distanceP1P2;
        return new Point((1 - t) * P1_X + t * P2_X, (1 - t) * P1_Y + t * P2_Y);
    }

    public static int checkLineCircleCollision(final Point P1, final Point P2, final double CENTER_X, final double CENTER_Y, final double RADIUS) {
        return checkLineCircleCollision(P1.getX(), P1.getY(), P2.getX(), P2.getY(), CENTER_X, CENTER_Y, RADIUS);
    }
    public static int checkLineCircleCollision(final double P1_X, final double P1_Y, final double P2_X, final double P2_Y, final double CENTER_X, final double CENTER_Y, final double RADIUS) {
        double A = (P1_Y - P2_Y);
        double B = (P2_X - P1_X);
        double C = (P1_X * P2_Y - P2_X * P1_Y);

        return checkCollision(A, B, C, CENTER_X, CENTER_Y, RADIUS);
    }
    public static int checkCollision(final double a, final double b, final double c, final double centerX, final double centerY, final double radius) {
        // Finding the distance of line from center.
        double dist = (Math.abs(a * centerX + b * centerY + c)) / Math.sqrt(a * a + b * b);
        dist = round(dist, 1);
        if (radius > dist) {
            return 1;  // intersect
        } else if (radius < dist) {
            return -1; // outside
        } else {
            return 0;  // touch
        }
    }

    public static final double getAngleFromXY(final double X, final double Y, final double CENTER_X, final double CENTER_Y) {
        return getAngleFromXY(X, Y, CENTER_X, CENTER_Y, 90.0);
    }
    public static final double getAngleFromXY(final double X, final double Y, final double CENTER_X, final double CENTER_Y, final double ANGLE_OFFSET) {
        // For ANGLE_OFFSET =  0 -> Angle of 0 is at 3 o'clock
        // For ANGLE_OFFSET = 90  ->Angle of 0 is at 12 o'clock
        double deltaX      = X - CENTER_X;
        double deltaY      = Y - CENTER_Y;
        double radius      = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
        double nx          = deltaX / radius;
        double ny          = deltaY / radius;
        double theta       = Math.atan2(ny, nx);
        theta              = Double.compare(theta, 0.0) >= 0 ? Math.toDegrees(theta) : Math.toDegrees((theta)) + 360.0;
        double angle       = (theta + ANGLE_OFFSET) % 360;
        return angle;
    }
    
    public static final Point rotatePointAroundRotationCenter(final Point POINT, final Point ROTATION_CENTER, final double ANGLE) {
        double[] xy = rotatePointAroundRotationCenter(POINT.getX(), POINT.getY(), ROTATION_CENTER.getX(), ROTATION_CENTER.getY(), ANGLE);
        return new Point(xy[0], xy[1]);
    }
    public static final double[] rotatePointAroundRotationCenter(final double X, final double Y, final double RX, final double RY, final double ANGLE) {
        final double rad = Math.toRadians(ANGLE);
        final double sin = Math.sin(rad);
        final double cos = Math.cos(rad);
        final double nX  = RX + (X - RX) * cos - (Y - RY) * sin;
        final double nY  = RY + (X - RX) * sin + (Y - RY) * cos;
        return new double[] { nX, nY };
    }

    public static final Point getPointBetweenP1AndP2(final Point P1, final Point P2) {
        double[] xy = getPointBetweenP1AndP2(P1.getX(), P1.getY(), P2.getX(), P2.getY());
        return new Point(xy[0], xy[1]);
    }
    public static final double[] getPointBetweenP1AndP2(final double P1_X, final double P1_Y, final double P2_X, final double P2_Y) {
        return new double[] { (P1_X + P2_X) * 0.5, (P1_Y + P2_Y) * 0.5 };
    }

    public static int getDegrees(final double DEC_DEG) { return (int) DEC_DEG; }
    public static int getMinutes(final double DEC_DEG) { return (int) ((DEC_DEG - getDegrees(DEC_DEG)) * 60); }
    public static double getSeconds(final double DEC_DEG) { return (((DEC_DEG - getDegrees(DEC_DEG)) * 60) - getMinutes(DEC_DEG)) * 60; }

    public static double getDecimalDeg(final int DEGREES, final int MINUTES, final double SECONDS) {
        return (((SECONDS / 60) + MINUTES) / 60) + DEGREES;
    }

    public static final double[] latLonToXY(final double LATITUDE, final double LONGITUDE) {
        return latLonToXY(LATITUDE, LONGITUDE, MAP_OFFSET_X, MAP_OFFSET_Y);
    }
    public static final double[] latLonToXY(final double LATITUDE, final double LONGITUDE, final double MAP_OFFSET_X, final double MAP_OFFSET_Y) {
        double x = (LONGITUDE + 180) * (MAP_WIDTH / 360) + MAP_OFFSET_X;
        double y = (MAP_HEIGHT / 2) - (MAP_WIDTH * (Math.log(Math.tan((Math.PI / 4) + (Math.toRadians(LATITUDE) / 2)))) / (2 * Math.PI)) + MAP_OFFSET_Y;
        return new double[]{ x, y };
    }

    public static final <T> Predicate<T> not(final Predicate<T> PREDICATE) { return PREDICATE.negate(); }
    
    public static final List<Point> createSmoothedConvexHull(final List<Point> POINTS, final int SUB_DIVISIONS) {
        List<Point> hullPolygon = createConvexHull(POINTS);
        return subdividePoints(hullPolygon, SUB_DIVISIONS);
    }
    public static final <T extends Point> List<T> createConvexHull(final List<T> POINTS) {
        List<T> convexHull = new ArrayList<>();
        if (POINTS.size() < 3) { return new ArrayList<T>(POINTS); }

        int minDataPoint = -1;
        int maxDataPoint = -1;
        int minX         = Integer.MAX_VALUE;
        int maxX         = Integer.MIN_VALUE;

        for (int i = 0; i < POINTS.size(); i++) {
            if (POINTS.get(i).getX() < minX) {
                minX     = (int) POINTS.get(i).getX();
                minDataPoint = i;
            }
            if (POINTS.get(i).getX() > maxX) {
                maxX     = (int) POINTS.get(i).getX();
                maxDataPoint = i;
            }
        }
        T minPoint = POINTS.get(minDataPoint);
        T maxPoint = POINTS.get(maxDataPoint);
        convexHull.add(minPoint);
        convexHull.add(maxPoint);
        POINTS.remove(minPoint);
        POINTS.remove(maxPoint);

        List<T> leftSet  = new ArrayList<>();
        List<T> rightSet = new ArrayList<>();

        for (int i = 0; i < POINTS.size(); i++) {
            T p = POINTS.get(i);
            if (pointLocation(minPoint, maxPoint, p) == -1) { leftSet.add(p); } else if (pointLocation(minPoint, maxPoint, p) == 1) rightSet.add(p);
        }
        hullSet(minPoint, maxPoint, rightSet, convexHull);
        hullSet(maxPoint, minPoint, leftSet, convexHull);

        return convexHull;
    }
    private static final <T extends Point> double distance(final T P1, final T P2, final T P3) {
        double deltaX = P2.getX() - P1.getX();
        double deltaY = P2.getY() - P1.getY();
        double num = deltaX * (P1.getY() - P3.getY()) - deltaY * (P1.getX() - P3.getX());
        return Math.abs(num);
    }
    private static final <T extends Point> void hullSet(final T P1, final T P2, final List<T> POINTS, final List<T> HULL) {
        int insertPosition = HULL.indexOf(P2);

        if (POINTS.size() == 0) { return; }

        if (POINTS.size() == 1) {
            T point = POINTS.get(0);
            POINTS.remove(point);
            HULL.add(insertPosition, point);
            return;
        }

        int dist              = Integer.MIN_VALUE;
        int furthestDataPoint = -1;
        for (int i = 0; i < POINTS.size(); i++) {
            T point    = POINTS.get(i);
            double distance = distance(P1, P2, point);
            if (distance > dist) {
                dist          = (int) distance;
                furthestDataPoint = i;
            }
        }
        T point = POINTS.get(furthestDataPoint);
        POINTS.remove(furthestDataPoint);
        HULL.add(insertPosition, point);

        // Determine who's to the left of AP
        ArrayList<T> leftSetAP = new ArrayList<>();
        for (int i = 0; i < POINTS.size(); i++) {
            T M = POINTS.get(i);
            if (pointLocation(P1, point, M) == 1) { leftSetAP.add(M); }
        }

        // Determine who's to the left of PB
        ArrayList<T> leftSetPB = new ArrayList<>();
        for (int i = 0; i < POINTS.size(); i++) {
            T M = POINTS.get(i);
            if (pointLocation(point, P2, M) == 1) { leftSetPB.add(M); }
        }
        hullSet(P1, point, leftSetAP, HULL);
        hullSet(point, P2, leftSetPB, HULL);
    }
    private static final <T extends Point> int pointLocation(final T P1, final T P2, final T P3) {
        double cp1 = (P2.getX() - P1.getX()) * (P3.getY() - P1.getY()) - (P2.getY() - P1.getY()) * (P3.getX() - P1.getX());
        return cp1 > 0 ? 1 : Double.compare(cp1, 0) == 0 ? 0 : -1;
    }

    public static final String padLeft(final String text, final String filler, final int n) {
        return String.format("%" + n + "s", text).replace(" ", filler);
    }
    public static final String padRight(final String text, final String filler, final int n) {
        return String.format("%-" + n + "s", text).replace(" ", filler);
    }
}
