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

package eu.hansolo.tilesfx;

import javafx.scene.paint.Color;


/**
 * Created by hansolo on 21.12.16.
 */
public enum Country {
    AD, AE, AF, AG, AI, AL, AM, AO, AR, AS, AT, AU, AW, AX, AZ,
    BA, BB, BD, BE, BF, BG, BH, BI, BJ, BL, BN, BO, BM, BQ, BR, BS, BT, BV, BW, BY, BZ,
    CA, CC, CD, CF, CG, CH, CI, CK, CL, CM, CN, CO, CR, CU, CV, CW, CX, CY, CZ,
    DE, DJ, DK, DM, DO, DZ,
    EC, EG, EE, EH, ER, ES, ET,
    FI, FJ, FK, FM, FO, FR,
    GA, GB, GE, GD, GF, GG, GH, GI, GL, GM, GN, GO, GP, GQ, GR, GS, GT, GU, GW, GY,
    HK, HM, HN, HR, HT, HU,
    ID, IE, IL, IM, IN, IO, IQ, IR, IS, IT,
    JE, JM, JO, JP, JU,
    KE, KG, KH, KI, KM, KN, KP, KR, XK, KV, KW, KY, KZ,
    LA, LB, LC, LI, LK, LR, LS, LT, LU, LV, LY,
    MA, MC, MD, MG, ME, MF, MH, MK, ML, MO, MM, MN, MP, MQ, MR, MS, MT, MU, MV, MW, MX, MY, MZ,
    NA, NC, NE, NF, NG, NI, NL, NO, NP, NR, NU, NZ,
    OM,
    PA, PE, PF, PG, PH, PK, PL, PM, PN, PR, PS, PT, PW, PY,
    QA,
    RE, RO, RS, RU, RW,
    SA, SB, SC, SD, SE, SG, SH, SI, SJ, SK, SL, SM, SN, SO, SR, SS, ST, SV, SX, SY, SZ,
    TC, TD, TF, TG, TH, TJ, TK, TL, TM, TN, TO, TR, TT, TV, TW, TZ,
    UA, UG, UM_DQ, UM_FQ, UM_HQ, UM_JQ, UM_MQ, UM_WQ, US, UY, UZ,
    VA, VC, VE, VG, VI, VN, VU,
    WF, WS,
    YE, YT,
    ZA, ZM, ZW;

    private ValueObject value;
    private Color       color;


    // ******************** Constructors **************************************
    Country() {
        value = null;
        color = null;
    }


    // ******************** Methods *******************************************
    public String getName() { return name(); }

    public ValueObject getValue() { return value; }
    public void setValue(final ValueObject VALUE) { value = VALUE; }

    public Color getColor() { return color; }
    public void setColor(final Color COLOR) { color = COLOR; }
}
