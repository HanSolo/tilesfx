/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2016-2021 Gerrit Grunwald.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.hansolo.tilesfx.tools;

import eu.hansolo.tilesfx.ValueObject;
import eu.hansolo.tilesfx.icons.Flag;
import javafx.scene.paint.Color;

import java.util.Arrays;


/**
 * Created by hansolo on 21.12.16.
 */
public enum Country {
    AD("Andorra", new Location(42.546245,1.601554)),
    AE("United Arab Emirates", new Location(23.424076,53.847818)),
    AF("Afghanistan", new Location(33.93911,67.709953)),
    AG("Antigua and Barbuda", new Location(17.060816,-61.796428)),
    AI("Anguilla", new Location(18.220554,-63.068615)),
    AL("Albania", new Location(41.153332,20.168331)),
    AM("Armenia", new Location(40.069099,45.038189)),
    AN("Netherlands Antilles", new Location(12.226079,-69.060087)),
    AO("Angola", new Location(-11.202692,17.873887)),
    AQ("Antarctica", new Location(-75.250973,-0.071389)),
    AR("Argentina", new Location(-38.416097,-63.616672)),
    AS("American Samoa", new Location(-14.270972,-170.132217)),
    AT("Austria", new Location(47.516231,14.550072)),
    AU("Australia", new Location(-25.274398,133.775136)),
    AW("Aruba", new Location(12.52111,-69.968338)),
    AX("\u00C5land Islands", new Location(60.241034, 20.063198)),
    AZ("Azerbaijan", new Location(40.143105,47.576927)),
    BA("Bosnia and Herzegovina", new Location(43.915886,17.679076)),
    BB("Barbados", new Location(13.193887,-59.543198)),
    BD("Bangladesh", new Location(23.684994,90.356331)),
    BE("Belgium", new Location(50.503887,4.469936)),
    BF("Burkina Faso", new Location(12.238333,-1.561593)),
    BG("Bulgaria", new Location(42.733883,25.48583)),
    BH("Bahrain", new Location(25.930414,50.637772)),
    BI("Burundi", new Location(-3.373056,29.918886)),
    BJ("Benin", new Location(9.30769,2.315834)),
    BL("Saint Barth\u00E9lemy", new Location(17.901325, -62.823085)),
    BM("Bermuda", new Location(32.321384,-64.75737)),
    BN("Brunei", new Location(4.535277,114.727669)),
    BO("Bolivia", new Location(-16.290154,-63.588653)),
    BQ("Bonaire", new Location(12.137575, -68.264719)),
    BR("Brazil", new Location(-14.235004,-51.92528)),
    BS("Bahamas", new Location(25.03428,-77.39628)),
    BT("Bhutan", new Location(27.514162,90.433601)),
    BV("Bouvet Island", new Location(-54.423199,3.413194)),
    BW("Botswana", new Location(-22.328474,24.684866)),
    BY("Belarus", new Location(53.709807,27.953389)),
    BZ("Belize", new Location(17.189877,-88.49765)),
    CA("Canada", new Location(56.130366,-106.346771)),
    CC("Cocos [Keeling] Islands", new Location(-12.164165,96.870956)),
    CD("Congo [DRC]", new Location(-4.038333,21.758664)),
    CF("Central African Republic", new Location(6.611111,20.939444)),
    CG("Congo [Republic]", new Location(-0.228021,15.827659)),
    CH("Switzerland", new Location(46.818188,8.227512)),
    CI("Côte d'Ivoire", new Location(7.539989,-5.54708)),
    CK("Cook Islands", new Location(-21.236736,-159.777671)),
    CL("Chile", new Location(-35.675147,-71.542969)),
    CM("Cameroon", new Location(7.369722,12.354722)),
    CN("China", new Location(35.86166,104.195397)),
    CO("Colombia", new Location(4.570868,-74.297333)),
    CR("Costa Rica", new Location(9.748917,-83.753428)),
    CU("Cuba", new Location(21.521757,-77.781167)),
    CV("Cape Verde", new Location(16.002082,-24.013197)),
    CW("Cura\u00E7ao", new Location(12.122552,-68.873150)),
    CX("Christmas Island", new Location(-10.447525,105.690449)),
    CY("Cyprus", new Location(35.126413,33.429859)),
    CZ("Czech Republic", new Location(49.817492,15.472962)),
    DE("Germany", new Location(51.165691,10.451526)),
    DJ("Djibouti", new Location(11.825138,42.590275)),
    DK("Denmark", new Location(56.26392,9.501785)),
    DM("Dominica", new Location(15.414999,-61.370976)),
    DO("Dominican Republic", new Location(18.735693,-70.162651)),
    DZ("Algeria", new Location(28.033886,1.659626)),
    EC("Ecuador", new Location(-1.831239,-78.183406)),
    EE("Estonia", new Location(58.595272,25.013607)),
    EG("Egypt", new Location(26.820553,30.802498)),
    EH("Western Sahara", new Location(24.215527,-12.885834)),
    ER("Eritrea", new Location(15.179384,39.782334)),
    ES("Spain", new Location(40.463667,-3.74922)),
    ET("Ethiopia", new Location(9.145,40.489673)),
    FI("Finland", new Location(61.92411,25.748151)),
    FJ("Fiji", new Location(-16.578193,179.414413)),
    FK("Falkland Islands [Islas Malvinas]", new Location(-51.796253,-59.523613)),
    FM("Micronesia", new Location(7.425554,150.550812)),
    FO("Faroe Islands", new Location(61.892635,-6.911806)),
    FR("France", new Location(46.227638,2.213749)),
    GA("Gabon", new Location(-0.803689,11.609444)),
    GB("United Kingdom", new Location(55.378051,-3.435973)),
    GD("Grenada", new Location(12.262776,-61.604171)),
    GE("Georgia", new Location(42.315407,43.356892)),
    GF("French Guiana", new Location(3.933889,-53.125782)),
    GG("Guernsey", new Location(49.465691,-2.585278)),
    GH("Ghana", new Location(7.946527,-1.023194)),
    GI("Gibraltar", new Location(36.137741,-5.345374)),
    GL("Greenland", new Location(71.706936,-42.604303)),
    GM("Gambia", new Location(13.443182,-15.310139)),
    GN("Guinea", new Location(9.945587,-9.696645)),
    GP("Guadeloupe", new Location(16.995971,-62.067641)),
    GQ("Equatorial Guinea", new Location(1.650801,10.267895)),
    GR("Greece", new Location(39.074208,21.824312)),
    GS("South Georgia and the South Sandwich Islands", new Location(-54.429579,-36.587909)),
    GT("Guatemala", new Location(15.783471,-90.230759)),
    GU("Guam", new Location(13.444304,144.793731)),
    GW("Guinea-Bissau", new Location(11.803749,-15.180413)),
    GY("Guyana", new Location(4.860416,-58.93018)),
    GZ("Gaza Strip", new Location(31.354676,34.308825)),
    HK("Hong Kong", new Location(22.396428,114.109497)),
    HM("Heard Island and McDonald Islands", new Location(-53.08181,73.504158)),
    HN("Honduras", new Location(15.199999,-86.241905)),
    HR("Croatia", new Location(45.1,15.2)),
    HT("Haiti", new Location(18.971187,-72.285215)),
    HU("Hungary", new Location(47.162494,19.503304)),
    ID("Indonesia", new Location(-0.789275,113.921327)),
    IE("Ireland", new Location(53.41291,-8.24389)),
    IL("Israel", new Location(31.046051,34.851612)),
    IM("Isle of Man", new Location(54.236107,-4.548056)),
    IN("India", new Location(20.593684,78.96288)),
    IO("British Indian Ocean Territory", new Location(-6.343194,71.876519)),
    IQ("Iraq", new Location(33.223191,43.679291)),
    IR("Iran", new Location(32.427908,53.688046)),
    IS("Iceland", new Location(64.963051,-19.020835)),
    IT("Italy", new Location(41.87194,12.56738)),
    JE("Jersey", new Location(49.214439,-2.13125)),
    JM("Jamaica", new Location(18.109581,-77.297508)),
    JO("Jordan", new Location(30.585164,36.238414)),
    JP("Japan", new Location(36.204824,138.252924)),
    KE("Kenya", new Location(-0.023559,37.906193)),
    KG("Kyrgyzstan", new Location(41.20438,74.766098)),
    KH("Cambodia", new Location(12.565679,104.990963)),
    KI("Kiribati", new Location(-3.370417,-168.734039)),
    KM("Comoros", new Location(-11.875001,43.872219)),
    KN("Saint Kitts and Nevis", new Location(17.357822,-62.782998)),
    KP("North Korea", new Location(40.339852,127.510093)),
    KR("South Korea", new Location(35.907757,127.766922)),
    KW("Kuwait", new Location(29.31166,47.481766)),
    KY("Cayman Islands", new Location(19.513469,-80.566956)),
    KZ("Kazakhstan", new Location(48.019573,66.923684)),
    LA("Laos", new Location(19.85627,102.495496)),
    LB("Lebanon", new Location(33.854721,35.862285)),
    LC("Saint Lucia", new Location(13.909444,-60.978893)),
    LI("Liechtenstein", new Location(47.166,9.555373)),
    LK("Sri Lanka", new Location(7.873054,80.771797)),
    LR("Liberia", new Location(6.428055,-9.429499)),
    LS("Lesotho", new Location(-29.609988,28.233608)),
    LT("Lithuania", new Location(55.169438,23.881275)),
    LU("Luxembourg", new Location(49.815273,6.129583)),
    LV("Latvia", new Location(56.879635,24.603189)),
    LY("Libya", new Location(26.3351,17.228331)),
    MA("Morocco", new Location(31.791702,-7.09262)),
    MC("Monaco", new Location(43.750298,7.412841)),
    MD("Moldova", new Location(47.411631,28.369885)),
    ME("Montenegro", new Location(42.708678,19.37439)),
    MF("Saint Martin", new Location(18.069680, -63.079014)),
    MG("Madagascar", new Location(-18.766947,46.869107)),
    MH("Marshall Islands", new Location(7.131474,171.184478)),
    MK("Macedonia [FYROM]", new Location(41.608635,21.745275)),
    ML("Mali", new Location(17.570692,-3.996166)),
    MM("Myanmar [Burma]", new Location(21.913965,95.956223)),
    MN("Mongolia", new Location(46.862496,103.846656)),
    MO("Macau", new Location(22.198745,113.543873)),
    MP("Northern Mariana Islands", new Location(17.33083,145.38469)),
    MQ("Martinique", new Location(14.641528,-61.024174)),
    MR("Mauritania", new Location(21.00789,-10.940835)),
    MS("Montserrat", new Location(16.742498,-62.187366)),
    MT("Malta", new Location(35.937496,14.375416)),
    MU("Mauritius", new Location(-20.348404,57.552152)),
    MV("Maldives", new Location(3.202778,73.22068)),
    MW("Malawi", new Location(-13.254308,34.301525)),
    MX("Mexico", new Location(23.634501,-102.552784)),
    MY("Malaysia", new Location(4.210484,101.975766)),
    MZ("Mozambique", new Location(-18.665695,35.529562)),
    NA("Namibia", new Location(-22.95764,18.49041)),
    NC("New Caledonia", new Location(-20.904305,165.618042)),
    NE("Niger", new Location(17.607789,8.081666)),
    NF("Norfolk Island", new Location(-29.040835,167.954712)),
    NG("Nigeria", new Location(9.081999,8.675277)),
    NI("Nicaragua", new Location(12.865416,-85.207229)),
    NL("Netherlands", new Location(52.132633,5.291266)),
    NO("Norway", new Location(60.472024,8.468946)),
    NP("Nepal", new Location(28.394857,84.124008)),
    NR("Nauru", new Location(-0.522778,166.931503)),
    NU("Niue", new Location(-19.054445,-169.867233)),
    NZ("New Zealand", new Location(-40.900557,174.885971)),
    OM("Oman", new Location(21.512583,55.923255)),
    PA("Panama", new Location(8.537981,-80.782127)),
    PE("Peru", new Location(-9.189967,-75.015152)),
    PF("French Polynesia", new Location(-17.679742,-149.406843)),
    PG("Papua New Guinea", new Location(-6.314993,143.95555)),
    PH("Philippines", new Location(12.879721,121.774017)),
    PK("Pakistan", new Location(30.375321,69.345116)),
    PL("Poland", new Location(51.919438,19.145136)),
    PM("Saint Pierre and Miquelon", new Location(46.941936,-56.27111)),
    PN("Pitcairn Islands", new Location(-24.703615,-127.439308)),
    PR("Puerto Rico", new Location(18.220833,-66.590149)),
    PS("Palestinian Territories", new Location(31.952162,35.233154)),
    PT("Portugal", new Location(39.399872,-8.224454)),
    PW("Palau", new Location(7.51498,134.58252)),
    PY("Paraguay", new Location(-23.442503,-58.443832)),
    QA("Qatar", new Location(25.354826,51.183884)),
    RE("Réunion", new Location(-21.115141,55.536384)),
    RO("Romania", new Location(45.943161,24.96676)),
    RS("Serbia", new Location(44.016521,21.005859)),
    RU("Russia", new Location(61.52401,105.318756)),
    RW("Rwanda", new Location(-1.940278,29.873888)),
    SA("Saudi Arabia", new Location(23.885942,45.079162)),
    SB("Solomon Islands", new Location(-9.64571,160.156194)),
    SC("Seychelles", new Location(-4.679574,55.491977)),
    SD("Sudan", new Location(12.862807,30.217636)),
    SE("Sweden", new Location(60.128161,18.643501)),
    SG("Singapore", new Location(1.352083,103.819836)),
    SH("Saint Helena", new Location(-24.143474,-10.030696)),
    SI("Slovenia", new Location(46.151241,14.995463)),
    SJ("Svalbard and Jan Mayen", new Location(77.553604,23.670272)),
    SK("Slovakia", new Location(48.669026,19.699024)),
    SL("Sierra Leone", new Location(8.460555,-11.779889)),
    SM("San Marino", new Location(43.94236,12.457777)),
    SN("Senegal", new Location(14.497401,-14.452362)),
    SO("Somalia", new Location(5.152149,46.199616)),
    SR("Suriname", new Location(3.919305,-56.027783)),
    SS("South Sudan", new Location(4.855148, 31.579661)),
    ST("São Tomé and Príncipe", new Location(0.18636,6.613081)),
    SV("El Salvador", new Location(13.794185,-88.89653)),
    SX("Sint Maarten (Dutch part)", new Location(18.043674, -63.063529)),
    SY("Syria", new Location(34.802075,38.996815)),
    SZ("Swaziland", new Location(-26.522503,31.465866)),
    TC("Turks and Caicos Islands", new Location(21.694025,-71.797928)),
    TD("Chad", new Location(15.454166,18.732207)),
    TF("French Southern Territories", new Location(-49.280366,69.348557)),
    TG("Togo", new Location(8.619543,0.824782)),
    TH("Thailand", new Location(15.870032,100.992541)),
    TJ("Tajikistan", new Location(38.861034,71.276093)),
    TK("Tokelau", new Location(-8.967363,-171.855881)),
    TL("Timor-Leste", new Location(-8.874217,125.727539)),
    TM("Turkmenistan", new Location(38.969719,59.556278)),
    TN("Tunisia", new Location(33.886917,9.537499)),
    TO("Tonga", new Location(-21.178986,-175.198242)),
    TR("Turkey", new Location(38.963745,35.243322)),
    TT("Trinidad and Tobago", new Location(10.691803,-61.222503)),
    TV("Tuvalu", new Location(-7.109535,177.64933)),
    TW("Taiwan", new Location(23.69781,120.960515)),
    TZ("Tanzania", new Location(-6.369028,34.888822)),
    UA("Ukraine", new Location(48.379433,31.16558)),
    UG("Uganda", new Location(1.373333,32.290275)),
    UM("U.S. Minor Outlying Islands", new Location(19.280211, 166.647776)),
    US("United States", new Location(37.09024,-95.712891)),
    UY("Uruguay", new Location(-32.522779,-55.765835)),
    UZ("Uzbekistan", new Location(41.377491,64.585262)),
    VA("Vatican City", new Location(41.902916,12.453389)),
    VC("Saint Vincent and the Grenadines", new Location(12.984305,-61.287228)),
    VE("Venezuela", new Location(6.42375,-66.58973)),
    VG("British Virgin Islands", new Location(18.420695,-64.639968)),
    VI("U.S. Virgin Islands", new Location(18.335765,-64.896335)),
    VN("Vietnam", new Location(14.058324,108.277199)),
    VU("Vanuatu", new Location(-15.376706,166.959158)),
    WF("Wallis and Futuna", new Location(-13.768752,-177.156097)),
    WS("Samoa", new Location(-13.759029,-172.104629)),
    XK("Kosovo", new Location(42.602636,20.902977)),
    YE("Yemen", new Location(15.552727,48.516388)),
    YT("Mayotte", new Location(-12.8275,45.166244)),
    ZA("South Africa", new Location(-30.559482,22.937506)),
    ZM("Zambia", new Location(-13.133897,27.849332)),
    ZW("Zimbabwe", new Location(-19.015438,29.154857));

    private ValueObject valueObject;
    private double      value;
    private Color       color;
    private Location    location;
    private String      displayName;


    // ******************** Constructors **************************************
    Country(final String DISPLAY_NAME, final Location LOCATION) {
        valueObject = null;
        value       = 0;
        color       = null;
        location    = LOCATION;
        displayName = DISPLAY_NAME;
    }


    // ******************** Methods *******************************************
    public String getName() { return name(); }

    public ValueObject getValueObject() { return valueObject; }
    public void setValueObject(final ValueObject VALUE) { valueObject = VALUE; }

    public double getValue() { return value; }
    public void setValue(final double VALUE) { value = VALUE; }

    public Color getColor() { return color; }
    public void setColor(final Color COLOR) { color = COLOR; }

    public String getDisplayName() { return displayName; }

    public Location getLocation() { return location; }

    public static final Country iso2(final String iso2) {
        return Arrays.asList(values()).stream().filter(country -> country.name().equalsIgnoreCase(iso2)).findFirst().orElse(null);
    }
}