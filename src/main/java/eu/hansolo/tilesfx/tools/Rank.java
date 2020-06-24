/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2016-2020 Gerrit Grunwald.
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


import javafx.scene.paint.Color;


public class Rank implements Comparable<Rank> {
    public static final Rank    DEFAULT = new Rank();
    private             Ranking ranking;
    private             Color   color;


    // ******************** Constructors **************************************
    public Rank() { this(Ranking.NONE, Color.TRANSPARENT); }
    public Rank(final Ranking RANKING, final Color COLOR) {
        ranking = null == RANKING ? Ranking.NONE : RANKING;
        color   = null == COLOR ? Color.TRANSPARENT : COLOR;
    }


    // ******************** Methods *******************************************
    public Ranking getRanking() { return ranking; }
    public void setRanking(final Ranking RANKING) { ranking = null == RANKING ? Ranking.NONE : RANKING; }

    public Color getColor() { return color; }
    public void setColor(final Color COLOR) { color = null == COLOR ? Color.TRANSPARENT : COLOR; }

    @Override public int compareTo(final Rank OTHER) {
        return getRanking().getAsInt() - OTHER.getRanking().getAsInt();
    }
}
