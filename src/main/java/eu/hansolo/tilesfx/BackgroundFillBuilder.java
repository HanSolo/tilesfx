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

import java.awt.Color;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.Insets;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Paint;

/**
 * Created by Naoghuman on 10.02.17.
 */
public final class BackgroundFillBuilder {

    // ******************** Constructors **************************************
    private BackgroundFillBuilder() {}

    // ******************** Methods *******************************************
    public static final FillBackgroundFillBuilder create() {
        return new BackgroundFillImpl();
    }

    // ******************** Interfaces for internal steps *********************
    public static interface FillBackgroundFillBuilder {
        public CornerRadiiBackgroundFillBuilder fill(final Paint PAINT);
    }

    public static interface CornerRadiiBackgroundFillBuilder {
        public InsetsBackgroundFillBuilder cornerRadii(final CornerRadii CORNER_RADII);
        public BuildBackgroundFillBuilder insets(final Insets INSETS);
        public BackgroundFill build();
    }

    public static interface InsetsBackgroundFillBuilder {
        public BuildBackgroundFillBuilder insets(final Insets INSETS);
        public BackgroundFill build();
    }

    public static interface BuildBackgroundFillBuilder {
        public BackgroundFill build();
    }

    // ******************** Internal impl-class *******************************
    private static final class BackgroundFillImpl implements CornerRadiiBackgroundFillBuilder, FillBackgroundFillBuilder, 
            InsetsBackgroundFillBuilder, BuildBackgroundFillBuilder
    {
        private final ObservableMap<String, Property> properties = FXCollections.observableHashMap();

        BackgroundFillImpl() {
            properties.put("cornerRadii", new SimpleObjectProperty<>(CornerRadii.EMPTY));
            properties.put("fill", new SimpleObjectProperty<>(Color.TRANSLUCENT));
            properties.put("insets", new SimpleObjectProperty<>(Insets.EMPTY));
        }

        @Override
        public final InsetsBackgroundFillBuilder cornerRadii(final CornerRadii CORNER_RADII) {
            properties.put("cornerRadii", new SimpleObjectProperty<>(CORNER_RADII));
            return this;
        }

        @Override
        public final BuildBackgroundFillBuilder insets(final Insets INSETS) {
            properties.put("insets", new SimpleObjectProperty<>(INSETS));
            return this;
        }

        @Override
        public final CornerRadiiBackgroundFillBuilder fill(final Paint PAINT) {
            properties.put("fill", new SimpleObjectProperty<>(PAINT));
            return this;
        }

        @Override
        public final BackgroundFill build() {
            final ObjectProperty<Paint> fillObjectProperty = (ObjectProperty<Paint>) properties.get("fill");
            final Paint fill = fillObjectProperty.get();

            final ObjectProperty<CornerRadii> cornerRadiiObjectProperty = (ObjectProperty<CornerRadii>) properties.get("cornerRadii");
            final CornerRadii cornerRadii = cornerRadiiObjectProperty.get();

            final ObjectProperty<Insets> insetsObjectProperty = (ObjectProperty<Insets>) properties.get("insets");
            final Insets insets = insetsObjectProperty.get();

            final BackgroundFill backgroundFill = new BackgroundFill(fill, cornerRadii, insets);

            return backgroundFill;
        }

    }

}
