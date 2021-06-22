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
    AD("Andorra"),
    AE("United Arab Emirates"),
    AF("Afghanistan"),
    AG("Antigua and Barbuda"),
    AI("Anguilla"),
    AL("Albania"),
    AM("Armenia"),
    //AN("Netherlands Antilles"),
    AO("Angola"),
    //AQ("Antarctica"),
    AR("Argentina"),
    AS("American Samoa"),
    AT("Austria"),
    AU("Australia"),
    AW("Aruba"),
    AX("\u00C5land Islands"),
    AZ("Azerbaijan"),
    BA("Bosnia and Herzegovina"),
    BB("Barbados"),
    BD("Bangladesh"),
    BE("Belgium"),
    BF("Burkina Faso"),
    BG("Bulgaria"),
    BH("Bahrain"),
    BI("Burundi"),
    BJ("Benin"),
    BL("Saint Barth\u00E9lemy"),
    BM("Bermuda"),
    BN("Brunei"),
    BO("Bolivia"),
    BQ("Bonaire, Sint Eustatius and Saba"),
    BR("Brazil"),
    BS("Bahamas"),
    BT("Bhutan"),
    BV("Bouvet Island"),
    BW("Botswana"),
    BY("Belarus"),
    BZ("Belize"),
    CA("Canada"),
    CC("Cocos Islands"),
    CD("The Democratic Republic Of Congo"),
    CF("Central African Republic"),
    CG("Congo"),
    CH("Switzerland"),
    CI("C\u00F4te d\u0027Ivoire"),
    CK("Cook Islands"),
    CL("Chile"),
    CM("Cameroon"),
    CN("China"),
    CO("Colombia"),
    CR("Costa Rica"),
    CU("Cuba"),
    CV("Cape Verde"),
    CW("Cura\u00E7ao"),
    CX("Christmas Island"),
    CY("Cyprus"),
    CZ("Czech Republic"),
    DE("Germany"),
    DJ("Djibouti"),
    DK("Denmark"),
    DM("Dominica"),
    DO("Dominican Republic"),
    DZ("Algeria"),
    EC("Ecuador"),
    EE("Estonia"),
    EG("Egypt"),
    EH("Western Sahara"),
    ER("Eritrea"),
    ES("Spain"),
    ET("Ethiopia"),
    FI("Finland"),
    FJ("Fiji"),
    FK("Falkland Islands"),
    FM("Micronesia"),
    FO("Faroe Islands"),
    FR("France"),
    GA("Gabon"),
    GB("United Kingdom"),
    GD("Grenada"),
    GE("Georgia"),
    GF("French Guiana"),
    GG("Guernsey"),
    GH("Ghana"),
    GI("Gibraltar"),
    GL("Greenland"),
    GM("Gambia"),
    GN("Guinea"),
    GO(""),
    GP("Guadeloupe"),
    GQ("Equatorial Guinea"),
    GR("Greece"),
    GS("South Georgia And The South Sandwich Islands"),
    GT("Guatemala"),
    GU("Guam"),
    GW("Guinea-Bissau"),
    GY("Guyana"),
    HK("Hong Kong"),
    HM("Heard Island And McDonald Islands"),
    HN("Honduras"),
    HR("Croatia"),
    HT("Haiti"),
    HU("Hungary"),
    ID("Indonesia"),
    IE("Ireland"),
    IL("Israel"),
    IM("Isle Of Man"),
    IN("India"),
    IO("British Indian Ocean Territory"),
    IQ("Iraq"),
    IR("Iran"),
    IS("Iceland"),
    IT("Italy"),
    JE("Jersey"),
    JM("Jamaica"),
    JO("Jordan"),
    JP("Japan"),
    JU(""),
    KE("Kenya"),
    KG("Kyrgyzstan"),
    KH("Cambodia"),
    KI("Kiribati"),
    KM("Comoros"),
    KN("Saint Kitts And Nevis"),
    KP("North Korea"),
    KR("South Korea"),
    XK("Kosovo"), // might change to KV where XK is temporary
    KW("Kuwait"),
    KY("Cayman Islands"),
    KZ("Kazakhstan"),
    LA("Laos"),
    LB("Lebanon"),
    LC("Saint Lucia"),
    LI("Liechtenstein"),
    LK("Sri Lanka"),
    LR("Liberia"),
    LS("Lesotho"),
    LT("Lithuania"),
    LU("Luxembourg"),
    LV("Latvia"),
    LY("Libya"),
    MA("Morocco"),
    MC("Monaco"),
    MD("Moldova"),
    ME("Montenegro"),
    MF("Saint Martin"),
    MG("Madagascar"),
    MH("Marshall Islands"),
    MK("Macedonia"),
    ML("Mali"),
    MM("Myanmar"),
    MN("Mongolia"),
    MO("Macao"),
    MP("Northern Mariana Islands"),
    MQ("Martinique"),
    MR("Mauritania"),
    MS("Montserrat"),
    MT("Malta"),
    MU("Mauritius"),
    MV("Maldives"),
    MW("Malawi"),
    MX("Mexico"),
    MY("Malaysia"),
    MZ("Mozambique"),
    NA("Namibia"),
    NC("New Caledonia"),
    NE("Niger"),
    NF("Norfolk Island"),
    NG("Nigeria"),
    NI("Nicaragua"),
    NL("Netherlands"),
    NO("Norway"),
    NP("Nepal"),
    NR("Nauru"),
    NU("Niue"),
    NZ("New Zealand"),
    OM("Oman"),
    PA("Panama"),
    PE("Peru"),
    PF("French Polynesia"),
    PG("Papua New Guinea"),
    PH("Philippines"),
    PK("Pakistan"),
    PL("Poland"),
    PM("Saint Pierre And Miquelon"),
    PN("Pitcairn"),
    PR("Puerto Rico"),
    PS("Palestine"),
    PT("Portugal"),
    PW("Palau"),
    PY("Paraguay"),
    QA("Qatar"),
    RE("Reunion"),
    RO("Romania"),
    RS("Serbia"),
    RU("Russia"),
    RW("Rwanda"),
    SA("Saudi Arabia"),
    SB("Solomon Islands"),
    SC("Seychelles"),
    SD("Sudan"),
    SE("Sweden"),
    SG("Singapore"),
    SH("Saint Helena"),
    SI("Slovenia"),
    SJ("Svalbard And Jan Mayen"),
    SK("Slovakia"),
    SL("Sierra Leone"),
    SM("San Marino"),
    SN("Senegal"),
    SO("Somalia"),
    SR("Suriname"),
    SS("South Sudan"),
    ST("Sao Tome And Principe"),
    SV("El Salvador"),
    SX("Sint Maarten (Dutch part)"),
    SY("Syria"),
    SZ("Swaziland"),
    TC("Turks And Caicos Islands"),
    TD("Chad"),
    TF("French Southern Territories"),
    TG("Togo"),
    TH("Thailand"),
    TJ("Tajikistan"),
    TK("Tokelau"),
    TL("Timor-Leste"),
    TM("Turkmenistan"),
    TN("Tunisia"),
    TO("Tonga"),
    TR("Turkey"),
    TT("Trinidad and Tobago"),
    TV("Tuvalu"),
    TW("Taiwan"),
    TZ("Tanzania"),
    UA("Ukraine"),
    UG("Uganda"),
    UM("United States Minor Outlying Islands"),
    UM_DQ(""),
    UM_FQ(""),
    UM_HQ(""),
    UM_JQ(""),
    UM_MQ(""),
    US("United States"),
    UY("Uruguay"),
    UZ("Uzbekistan"),
    VA("Vatican"),
    VC("Saint Vincent And The Grenadines"),
    VE("Venezuela"),
    VG("British Virgin Islands"),
    VI("U.S. Virgin Islands"),
    VN("Vietnam"),
    VU("Vanuatu"),
    WF("Wallis And Futuna"),
    WS("Samoa"),
    YE("Yemen"),
    YT("Mayotte"),
    ZA("South Africa"),
    ZM("Zambia"),
    ZW("Zimbabwe");

    private ValueObject valueObject;
    private double      value;
    private Color       color;
    private String      displayName;


    // ******************** Constructors **************************************
    Country(final String DISPLAY_NAME) {
        valueObject = null;
        value       = 0;
        color       = null;
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

    public static final Country iso2(final String iso2) {
        return Arrays.asList(values()).stream().filter(country -> country.name().equalsIgnoreCase(iso2)).findFirst().orElse(null);
    }
}