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

package eu.hansolo.tilesfx.addons;

import eu.hansolo.tilesfx.events.SpinnerObserver;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Dimension2D;
import javafx.scene.paint.Color;

import java.util.HashMap;


public class SpinnerBuilder<B extends SpinnerBuilder<B>> {
    private HashMap<String, Property> properties = new HashMap<>();

    // ******************** Constructors **************************************
    protected SpinnerBuilder() {}


    // ******************** Methods *******************************************
    public static final SpinnerBuilder create() {
        return new SpinnerBuilder();
    }

    public final B value(final double value) {
        properties.put("value", new SimpleDoubleProperty(value));
        return (B)this;
    }

    public final B backgroundVisible(final boolean visible) {
        properties.put("backgroundVisible", new SimpleBooleanProperty(visible));
        return (B)this;
    }

    public final B overlayVisible(final boolean visible) {
        properties.put("overlayVisible", new SimpleBooleanProperty(visible));
        return (B)this;
    }

    public final B snapshotBackground(final Color color) {
        properties.put("snapshotBackground", new SimpleObjectProperty(color));
        return (B)this;
    }

    public final B backgroundColor(final Color color) {
        properties.put("backgroundColor", new SimpleObjectProperty<>(color));
        return (B)this;
    }

    public final B foregroundColor(final Color color) {
        properties.put("foregroundColor", new SimpleObjectProperty<>(color));
        return (B)this;
    }

    public final B type(final SpinnerType spinnerType) {
        properties.put("spinnerType", new SimpleObjectProperty(spinnerType));
        return (B)this;
    }

    public final B prefSize(final double width, final double height) {
        properties.put("prefSize", new SimpleObjectProperty(new Dimension2D(width, height)));
        return (B)this;
    }

    public final B onValueChanged(final SpinnerObserver observer) {
        properties.put("onValueChanged", new SimpleObjectProperty(observer));
        return (B)this;
    }

    public final B onZeroPassed(final SpinnerObserver observer) {
        properties.put("onZeroPassed", new SimpleObjectProperty<>(observer));
        return (B)this;
    }

    public final B onSpinnerEvent(final SpinnerObserver observer) {
        properties.put("onSpinnerEvent", new SimpleObjectProperty(observer));
        return (B)this;
    }

    private final void build(final Spinner spinner) {
        for (String key : properties.keySet()) {
            if ("value".equals(key)) {
                spinner.setValue(((DoubleProperty) properties.get(key)).get());
            } else if ("snapshotBackground".equals(key)) {
                spinner.setSnapshotBackground(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("backgroundVisible".equals(key)) {
                spinner.setBackgroundVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("overlayVisible".equals(key)) {
                spinner.setOverlayVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("backgroundColor".equals(key)) {
                spinner.setBackgroundColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("foregroundColor".equals(key)) {
                spinner.setForegroundColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("spinnerType".equals(key)) {
                spinner.setSpinnerType(((ObjectProperty<SpinnerType>) properties.get(key)).get());
            } else if ("onValueChanged".equals(key)) {
                spinner.setOnValueChanged(((ObjectProperty<SpinnerObserver>) properties.get(key)).get());
            } else if ("onZeroPassed".equals(key)) {
                spinner.setOnZeroPassed(((ObjectProperty<SpinnerObserver>) properties.get(key)).get());
            } else if ("onSpinnerEvent".equals(key)) {
                spinner.setOnSpinnerEvent(((ObjectProperty<SpinnerObserver>) properties.get(key)).get());
            }
        }
    }

    public final ImageSpinner buildImageSpinner() {
        final ImageSpinner spinner = new ImageSpinner();
        build(spinner);
        if (properties.containsKey("prefSize")) {
            Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get("prefSize")).get();
            spinner.setPrefSize(dim.getWidth(), dim.getHeight());
        }
        return spinner;
    }
    public final CanvasSpinner buildCanvasSpinner() {
        final CanvasSpinner spinner = new CanvasSpinner();
        build(spinner);
        if (properties.containsKey("prefSize")) {
            Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get("prefSize")).get();
            spinner.setPrefSize(dim.getWidth(), dim.getHeight());
        }
        return spinner;
    }
}
