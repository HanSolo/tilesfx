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

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.events.SpinnerEvent;
import eu.hansolo.tilesfx.events.SpinnerEventType;
import eu.hansolo.tilesfx.events.SpinnerObserver;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.DefaultProperty;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;


@DefaultProperty("children")
public class ImageSpinner extends Region implements Spinner {
    protected static final double                                 PREFERRED_WIDTH     = 100;
    protected static final double                                 PREFERRED_HEIGHT    = 144;
    protected static final double                                 MINIMUM_WIDTH       = 10;
    protected static final double                                 MINIMUM_HEIGHT      = 10;
    protected static final double                                 MAXIMUM_WIDTH       = 1024;
    protected static final double                                 MAXIMUM_HEIGHT      = 1024;
    private final          SpinnerEvent                           VALUE_CHANGED_EVENT = new SpinnerEvent(ImageSpinner.this, SpinnerEventType.VALUE_CHANGED);
    private final          SpinnerEvent                           ZERO_PASSED_EVENT   = new SpinnerEvent(ImageSpinner.this, SpinnerEventType.ZERO_PASSED);
    private static         double                                 aspectRatio         = PREFERRED_HEIGHT / PREFERRED_WIDTH;
    private                boolean                                keepAspect;
    private                double                                 width;
    private                double                                 height;
    private                double                                 extendedHeight;
    private                ImageView                              imageView;
    private                Rectangle                              overlay;
    private                Canvas                                 imageCanvas;
    private                GraphicsContext                        imageCtx;
    private                WritableImage                          image;
    private                Pane                                   pane;
    private                SnapshotParameters                     snapshotParameters;
    private                double                                 digitWidth;
    private                double                                 digitHeight;
    private                double                                 columnHeight;
    private                double                                 verticalSpace;
    private                double                                 zeroOffset;
    private                Color                                  _backgroundColor;
    private                ObjectProperty<Color>                  backgroundColor;
    private                Color                                  _foregroundColor;
    private                ObjectProperty<Color>                  foregroundColor;
    private                Font                                   font;
    private                double                                 oldValue;
    private                double                                 _value;
    private                DoubleProperty                         value;
    private                boolean                                _backgroundVisible;
    private                BooleanProperty                        backgroundVisible;
    private                boolean                                _overlayVisible;
    private                BooleanProperty                        overlayVisible;
    private                boolean                                initialized;
    private                SpinnerType                            spinnerType;
    private                Timeline                               timeline;
    private                Map<SpinnerObserver, SpinnerEventType> observers;
    private                BooleanBinding                         showing;


    // ******************** Constructors **************************************
    public ImageSpinner() {
        this(SpinnerType.NUMERIC_0_9);
    }
    public ImageSpinner(final SpinnerType SpinnerTYPE) {
        getStylesheets().add(ImageSpinner.class.getResource("spinner.css").toExternalForm());
        width              = PREFERRED_WIDTH;
        height             = PREFERRED_HEIGHT;
        keepAspect         = true;
        _backgroundColor   = Tile.BACKGROUND;
        _foregroundColor   = Tile.FOREGROUND;
        oldValue           = 0;
        _value             = 0;
        _backgroundVisible = true;
        _overlayVisible    = true;
        initialized        = false;
        spinnerType = SpinnerTYPE;
        timeline           = new Timeline();
        observers          = new ConcurrentHashMap<>();
        snapshotParameters = new SnapshotParameters();
        snapshotParameters.setFill(Tile.BACKGROUND);
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 || Double.compare(getWidth(), 0.0) <= 0 ||
            Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(width, height);
            }
        }

        getStyleClass().add("spinner");

        imageView = new ImageView();

        imageCanvas = new Canvas(width, height);

        imageCtx = imageCanvas.getGraphicsContext2D();
        imageCtx.setLineWidth(1);
        imageCtx.setTextAlign(TextAlignment.CENTER);
        imageCtx.setTextBaseline(VPos.CENTER);


        LinearGradient overlayGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                                                            new Stop(0, Color.rgb(0, 0, 0, 1)),
                                                            new Stop(0.1, Color.rgb(0, 0, 0, 0.4)),
                                                            new Stop(0.33, Color.rgb(255, 255, 255, 0.45)),
                                                            new Stop(0.46, Color.rgb(255, 255, 255, 0)),
                                                            new Stop(0.9, Color.rgb(0, 0, 0, 0.4)),
                                                            new Stop(1, Color.rgb(0, 0, 0, 1)));
        overlay = new Rectangle();
        overlay.setFill(overlayGradient);

        pane = new Pane(imageView, overlay);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
        if (null != getScene()) {
            setupBinding();
        } else {
            sceneProperty().addListener((o1, ov1, nv1) -> {
                if (null == nv1) { return; }
                if (null != getScene().getWindow()) {
                    setupBinding();
                } else {
                    sceneProperty().get().windowProperty().addListener((o2, ov2, nv2) -> {
                        if (null == nv2) { return; }
                        setupBinding();
                    });
                }
            });
        }
    }

    private void setupBinding() {
        showing = Bindings.createBooleanBinding(() -> {
            if (getScene() != null && getScene().getWindow() != null) {
                return getScene().getWindow().isShowing();
            } else {
                return false;
            }
        }, sceneProperty(), getScene().windowProperty(), getScene().getWindow().showingProperty());

        showing.addListener(o -> {
            if (showing.get()) {
                redraw();
            }
        });
    }

    private void init() {
        if (!isShowing()) { return; }

        setBackground(new Background(new BackgroundFill(getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY)));

        imageCtx.clearRect(0, 0, digitWidth, extendedHeight);

        if (isBackgroundVisible()) {
            imageCtx.setFill(getBackgroundColor());
            imageCtx.fillRect(0, 0, digitWidth, extendedHeight);
            imageCtx.setStroke(Color.web("#f0f0f0"));
            imageCtx.strokeLine(0, 0, 0, extendedHeight);
            imageCtx.setStroke(Color.web("#202020"));
            imageCtx.strokeLine(digitWidth, 0, digitWidth, extendedHeight);
        }
        imageCtx.setFont(font);
        imageCtx.setFill(getForegroundColor());

        switch(spinnerType) {
            case ALPHABETIC: // A - Z
                for (int i = 0 ; i < 28 ; i++) {
                    imageCtx.fillText(spinnerType.getCharacters()[i % 27], digitWidth * 0.5, verticalSpace * (i + 0) + verticalSpace / 2);
                }
                break;
            case NUMERIC_0_9: // 0 - 9
                for (int i = 9 ; i < 21 ; i++) {
                    imageCtx.fillText(spinnerType.getCharacters()[i % 10], digitWidth * 0.5, verticalSpace * (i - 9) + verticalSpace / 2);
                }
                break;
            case NUMERIC_0_5: // 0 - 5
                for (int i = 5 ; i < 13 ; i++) {
                    imageCtx.fillText(spinnerType.getCharacters()[i % 6], digitWidth * 0.5, verticalSpace * (i - 5) + verticalSpace / 2);
                }
                break;
            case NUMERIC_0_3: // 0 - 3
                for (int i = 3 ; i < 9 ; i++) {
                    imageCtx.fillText(spinnerType.getCharacters()[i % 4], digitWidth * 0.5, verticalSpace * (i - 3) + verticalSpace / 2);
                }
                break;
            case NUMERIC_0_2: // 0 - 2
                for (int i = 2 ; i < 7 ; i++) {
                    imageCtx.fillText(spinnerType.getCharacters()[i % 3], digitWidth * 0.5, verticalSpace * (i - 2) + verticalSpace / 2);
                }
                break;
            case NUMERIC_0_1: // 0 - 1
                for (int i = 1 ; i < 5 ; i++) {
                    imageCtx.fillText(spinnerType.getCharacters()[i % 2], digitWidth * 0.5, verticalSpace * (i - 1) + verticalSpace / 2);
                }
                break;
            case SIGNS: // - .
                for (int i = 0 ; i < 2 ; i++) {
                    imageCtx.fillText(spinnerType.getCharacters()[i % 2], digitWidth * 0.5, verticalSpace * (i + 1) + verticalSpace / 2);
                }
                break;
        }
        image = imageCanvas.snapshot(snapshotParameters, null);
        imageView.setImage(image);

        initialized = true;
    }

    private void initOverlay() {
        if (isOverlayVisible()) {
            LinearGradient overlayGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                                                                new Stop(0, Color.rgb(0, 0, 0, 1)),
                                                                new Stop(0.1, Color.rgb(0, 0, 0, 0.4)),
                                                                new Stop(0.33, Color.rgb(255, 255, 255, 0.45)),
                                                                new Stop(0.46, Color.rgb(255, 255, 255, 0)),
                                                                new Stop(0.9, Color.rgb(0, 0, 0, 0.4)),
                                                                new Stop(1, Color.rgb(0, 0, 0, 1)));
            overlay.setFill(overlayGradient);
        } else {
            LinearGradient overlayGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                                                                new Stop(0, getBackgroundColor()),
                                                                new Stop(0.1, getBackgroundColor()),
                                                                new Stop(0.101, Color.TRANSPARENT),
                                                                new Stop(1, Color.TRANSPARENT));
            overlay.setFill(overlayGradient);
        }
    }


    // ******************** Methods *******************************************
    @Override public void layoutChildren() {
        super.layoutChildren();
    }

    @Override protected double computeMinWidth(final double height) { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double width) { return MINIMUM_HEIGHT; }

    @Override protected double computePrefWidth(final double height) { return super.computePrefWidth(height); }
    @Override protected double computePrefHeight(final double width) { return super.computePrefHeight(width); }

    @Override protected double computeMaxWidth(final double height) { return MAXIMUM_WIDTH; }
    @Override protected double computeMaxHeight(final double width) { return MAXIMUM_HEIGHT; }

    @Override public ObservableList<Node> getChildren() { return super.getChildren(); }

    @Override public double getOldValue() { return oldValue; }

    @Override public double getValue() { return null == value ? _value : value.get(); }
    @Override public void setValue(final double value) {
        if (null == this.value) {
            oldValue = _value;
            if (value < 0 || value > spinnerType.getUpperLimit()) {
                _value = 0;
                fireSpinnerEvent(ZERO_PASSED_EVENT);
            } else {
                _value = value;
                if ((int)_value != (int) oldValue) {
                    fireSpinnerEvent(VALUE_CHANGED_EVENT);
                    if ((int) _value == 0) { fireSpinnerEvent(ZERO_PASSED_EVENT); }
                }
            }
            redraw();
        } else {
            this.value.set(value);
        }
    }
    public void setValue(final Character character) {
        if (SpinnerType.NUMERIC_0_9 == spinnerType || SpinnerType.NUMERIC_0_5 == spinnerType) { throw new IllegalArgumentException("Type must be of type ALPHABETIC or SIGN"); }
        setValue((int) character - 65);
    }
    @Override public DoubleProperty valueProperty() {
        if (null == value) {
            value = new DoublePropertyBase(_value) {
                @Override protected void invalidated() {
                    if (get() < 0 || get() > spinnerType.getUpperLimit()) { set(0); }
                    redraw();
                }
                @Override public Object getBean() { return ImageSpinner.this; }
                @Override public String getName() { return "value"; }
            };
            value.addListener((o, ov, nv) -> {
                oldValue = ov.doubleValue();
                if (nv.intValue() != ov.intValue()) {
                    fireSpinnerEvent(VALUE_CHANGED_EVENT);
                    if (nv.intValue() == 0) { fireSpinnerEvent(ZERO_PASSED_EVENT); }
                }
            });
        }
        return value;
    }

    @Override public boolean isBackgroundVisible() { return null == backgroundVisible ? _backgroundVisible : backgroundVisible.get(); }
    @Override public void setBackgroundVisible(final boolean visible) {
        if (null == overlayVisible) {
            _backgroundVisible = visible;
            initialized = false;
            redraw();
        } else {
            backgroundVisible.set(visible);
        }
    }
    @Override public BooleanProperty backgroundVisibleProperty() {
        if (null == backgroundVisible) {
            backgroundVisible = new BooleanPropertyBase(_backgroundVisible) {
                @Override protected void invalidated() {
                    initialized = false;
                    redraw();
                }
                @Override public Object getBean() { return ImageSpinner.this; }
                @Override public String getName() { return "overlayVisible"; }
            };
        }
        return backgroundVisible;
    }

    @Override public boolean isOverlayVisible() { return null == overlayVisible ? _overlayVisible : overlayVisible.get(); }
    @Override public void setOverlayVisible(final boolean visible) {
        if (null == overlayVisible) {
            _overlayVisible = visible;
            initOverlay();
        } else {
            overlayVisible.set(visible);
        }
    }
    @Override public BooleanProperty overlayVisibleProperty() {
        if (null == overlayVisible) {
            overlayVisible = new BooleanPropertyBase(_overlayVisible) {
                @Override protected void invalidated() { initOverlay(); }
                @Override public Object getBean() { return ImageSpinner.this; }
                @Override public String getName() { return "overlayVisible"; }
            };
        }
        return overlayVisible;
    }

    @Override public void spinUp() {
        if (null == value) { value = valueProperty(); }
        double targetValue;
        if (Double.compare(getValue(), spinnerType.upperLimit) == 0) {
            setValue(0);
            targetValue = 1;
        } else {
            targetValue = getValue() + 1;
        }
        KeyValue kv0 = new KeyValue(value, getValue(), Interpolator.LINEAR);
        KeyValue kv1 = new KeyValue(value, targetValue, Interpolator.LINEAR);
        KeyFrame kf0 = new KeyFrame(Duration.ZERO, kv0);
        KeyFrame kf1 = new KeyFrame(Duration.millis(500), kv1);
        timeline.stop();
        timeline.getKeyFrames().setAll(kf0, kf1);
        timeline.play();
    }
    @Override public void spinDown() {
        if (null == value) { value = valueProperty(); }
        KeyValue kv0 = new KeyValue(value, getValue(), Interpolator.LINEAR);
        KeyValue kv1 = new KeyValue(value, getValue() - 1, Interpolator.LINEAR);
        KeyFrame kf0 = new KeyFrame(Duration.ZERO, kv0);
        KeyFrame kf1 = new KeyFrame(Duration.millis(500), kv1);
        timeline.stop();
        timeline.getKeyFrames().setAll(kf0, kf1);
        timeline.play();
    }

    @Override public Color getBackgroundColor() { return null == backgroundColor ? _backgroundColor : backgroundColor.get(); }
    @Override public void setBackgroundColor(final Color color) {
        if (null == backgroundColor) {
            _backgroundColor = color;
            initialized      = false;
            redraw();
        } else {
            backgroundColor.set(color);
        }
    }
    @Override public ObjectProperty<Color> backgroundColorProperty() {
        if (null == backgroundColor) {
            backgroundColor = new ObjectPropertyBase<Color>(_backgroundColor) {
                @Override protected void invalidated() {
                    initialized = false;
                    redraw();
                }
                @Override public Object getBean() { return ImageSpinner.this; }
                @Override public String getName() { return "backgroundColor"; }
            };
            _backgroundColor = null;
        }
        return backgroundColor;
    }

    @Override public Color getForegroundColor() { return null == foregroundColor ? _foregroundColor : foregroundColor.get(); }
    @Override public void setForegroundColor(final Color color) {
        if (null == foregroundColor) {
            _foregroundColor = color;
            initialized      = false;
            redraw();
        } else {
            foregroundColor.set(color);
        }
    }
    @Override public ObjectProperty<Color> foregroundColorProperty() {
        if (null == foregroundColor) {
            foregroundColor = new ObjectPropertyBase<Color>(_foregroundColor) {
                @Override protected void invalidated() {
                    initialized = false;
                    redraw();
                }
                @Override public Object getBean() { return ImageSpinner.this; }
                @Override public String getName() { return "foregroundColor"; }
            };
            _foregroundColor = null;
        }
        return foregroundColor;
    }

    @Override public SpinnerType getSpinnerType() { return spinnerType; }
    @Override public void setSpinnerType(final SpinnerType spinnerType) {
        this.spinnerType = spinnerType;
        initialized = false;
        redraw();
    }

    @Override public boolean isShowing() { return null != showing && showing.get(); }

    @Override public void setSnapshotBackground(final Color color) {
        snapshotParameters.setFill(color);
        initialized = false;
        redraw();
    }

    @Override public void reInit() {
        initialized = false;
        redraw();
    }

    @Override public double getSpinnerWidth() { return width; }
    @Override public double getSpinnerHeight() { return height; }

    @Override public double getDigitHeight() { return digitHeight; }

    @Override public double getExtendedHeight() { return extendedHeight; }

    private void drawCharacters() {
        double value = getValue();
        int    num;
        int    numb;
        double fraction;
        String numbString;

        numb       = (int) Math.floor(value);
        fraction   = value - numb;
        numbString = Integer.toString(numb);

        int idx = numbString.length() - 1;
        num = idx < 0 ? 0 : numb;

        Rectangle2D viewPort = new Rectangle2D(0, (verticalSpace * (num + fraction) + zeroOffset), width, height);
        imageView.setViewport(viewPort);
    }


    // ******************** Event handling ************************************
    @Override public void setOnValueChanged(final SpinnerObserver observer) {
        if (!observers.containsKey(observer)) { observers.put(observer, SpinnerEventType.VALUE_CHANGED); }
    }
    @Override public void setOnZeroPassed(final SpinnerObserver observer) {
        if (!observers.containsKey(observer)) { observers.put(observer, SpinnerEventType.ZERO_PASSED); }
    }
    @Override public void setOnSpinnerEvent(final SpinnerObserver observer) {
        if (!observers.containsKey(observer)) { observers.put(observer, SpinnerEventType.ANY); }
    }
    @Override public void removeSpinnerObserver(final SpinnerObserver observer) { if (observers.containsKey(observer)) { observers.remove(observer); } }
    @Override public void removeAllObservers() { observers.clear(); }

    @Override public void fireSpinnerEvent(final SpinnerEvent evt) {
        final SpinnerEventType                           spinnerEventType = evt.getType();
        Stream<Entry<SpinnerObserver, SpinnerEventType>> stream           = observers.entrySet().stream();
        switch (spinnerEventType) {
            case VALUE_CHANGED:
                stream.filter(entry -> entry.getValue() == SpinnerEventType.VALUE_CHANGED).forEach(entry -> entry.getKey().onSpinnerEvent(evt));
                break;
            case ZERO_PASSED:
                stream.filter(entry -> entry.getValue() == SpinnerEventType.ZERO_PASSED).forEach(entry -> entry.getKey().onSpinnerEvent(evt));
                break;
            case ANY:
            default :
                stream.filter(entry -> entry.getValue() == SpinnerEventType.ANY).forEach(entry -> entry.getKey().onSpinnerEvent(evt));
                break;
        }
    }


    // ******************** Resizing ******************************************
    private void resize() {
        width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();

        if (keepAspect) {
            if (aspectRatio * width > height) {
                width = 1 / (aspectRatio / height);
            } else if (1 / (aspectRatio / height) > width) {
                height = aspectRatio * width;
            }
        }

        if (width > 0 && height > 0) {
            digitHeight    = Math.floor(height * 0.85);
            digitWidth     = isBackgroundVisible() ? Math.floor(height * 0.68) : width;
            width          = digitWidth;
            font           = Font.font(digitHeight);
            switch(spinnerType) {
                case ALPHABETIC:
                    columnHeight  = digitHeight * 28;
                    verticalSpace = columnHeight / 29;
                    break;
                case SIGNS:
                case NUMERIC_0_9:
                case NUMERIC_0_5:
                case NUMERIC_0_3:
                case NUMERIC_0_2:
                case NUMERIC_0_1:
                    columnHeight  = digitHeight * 11;
                    verticalSpace = columnHeight / 12;
                    break;
            }

            extendedHeight = columnHeight * 1.1;
            zeroOffset     = verticalSpace * 0.81;

            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);
            pane.relocate((getWidth() - width) * 0.5, (getHeight() - height) * 0.5);

            imageView.setFitWidth(width);
            imageView.setFitHeight(height);

            imageCanvas.setWidth(digitWidth);
            imageCanvas.setHeight(extendedHeight);

            overlay.setWidth(digitWidth);
            overlay.setHeight(height);

            initialized = false;

            redraw();
        }
    }

    private void redraw() {
        if (!initialized) { init(); }
        drawCharacters();
    }
}
