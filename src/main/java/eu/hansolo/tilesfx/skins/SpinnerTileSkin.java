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

package eu.hansolo.tilesfx.skins;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.addons.ImageSpinner;
import eu.hansolo.tilesfx.addons.SpinnerBuilder;
import eu.hansolo.tilesfx.addons.SpinnerType;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.LinkedList;
import java.util.List;


public class SpinnerTileSkin extends TileSkin {
    private Text               titleText;
    private Text               text;
    private List<Character>    characters;
    private List<ImageSpinner> spinners;
    private Text               unitText;
    private Rectangle          clip;
    private HBox               spinnerBox;
    private Label              description;
    private Timeline           timeline;
    private int                noOfSpinners;


    // ******************** Constructors **************************************
    public SpinnerTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        timeline = new Timeline();

        super.initGraphics();
        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getText());
        text.setFill(tile.getUnitColor());
        Helper.enableNode(text, tile.isTextVisible());

        String valueText = tile.getCustomDecimalFormatEnabled() ? decimalFormat.format(tile.getValue()) : String.format(locale, formatString, tile.getValue());
        characters = Helper.splitStringInCharacters(valueText);

        clip = new Rectangle();

        noOfSpinners = calcNoOfSpinners();

        spinners = new LinkedList<>();

        spinnerBox = new HBox();
        spinnerBox.setBackground(new Background(new BackgroundFill(tile.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY)));
        spinnerBox.setAlignment(Pos.CENTER);
        spinnerBox.setFillHeight(false);
        spinnerBox.setClip(clip);

        createSpinners();

        unitText = new Text(tile.getUnit());
        unitText.setTextOrigin(VPos.CENTER);
        unitText.setFill(tile.getUnitColor());
        Helper.enableNode(unitText, !tile.getUnit().isEmpty());

        if (!tile.getUnit().isEmpty()) {
            spinnerBox.getChildren().add(unitText);
        }

        description = new Label(tile.getText());
        description.setAlignment(tile.getDescriptionAlignment());
        description.setWrapText(true);
        description.setTextFill(tile.getTextColor());
        Helper.enableNode(description, tile.isTextVisible());

        getPane().getChildren().addAll(titleText, text, spinnerBox, description);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        tile.backgroundColorProperty().addListener((o, ov, nv) -> {
            spinnerBox.setBackground(new Background(new BackgroundFill(nv, CornerRadii.EMPTY, Insets.EMPTY)));
            spinners.forEach(spinner -> spinner.setSnapshotBackground(nv));
        });
        tile.valueColorProperty().addListener((o, ov, nv) -> spinners.forEach(spinner -> spinner.setForegroundColor(nv)));
        tile.rangeProperty().addListener(o -> {
            noOfSpinners = calcNoOfSpinners();
            createSpinners();
            resize();
        });
        tile.unitProperty().addListener((o, ov, nv) -> {
            if (ov.isEmpty() && !nv.isEmpty()) {
                if (spinnerBox.getChildren().contains(unitText)) { return; }
                spinnerBox.getChildren().add(unitText);
            } else if (!ov.isEmpty() && nv.isEmpty()) {
                if (!spinnerBox.getChildren().contains(unitText)) { return ; }
                spinnerBox.getChildren().remove(unitText);
            }
            resize();
        });
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
        }
    }

    @Override protected void handleCurrentValue(final double VALUE) {
        String valueText = tile.getCustomDecimalFormatEnabled() ? decimalFormat.format(VALUE) : String.format(locale, formatString, VALUE);
        characters = Helper.splitStringInCharacters(valueText);
        if (characters.size() > spinners.size()) {
            spinners.clear();
            for (int i = 0 ; i < characters.size() ; i++) {
                spinners.add(SpinnerBuilder.create()
                                           .type(SpinnerType.NUMERIC_0_9)
                                           .backgroundColor(tile.getBackgroundColor())
                                           .foregroundColor(tile.getForegroundColor())
                                           .overlayVisible(false)
                                           .backgroundVisible(false)
                                           .buildImageSpinner());
            }
            spinnerBox.getChildren().setAll(spinners);
        }
        set(characters);
        resizeDynamicText();
    }

    private void set(final List<Character> characters) {
        if (characters.size() > spinners.size()) { return; }
        List<KeyValue>     kf0Values      = new LinkedList<>();
        List<KeyValue>     kf1Values      = new LinkedList<>();
        int                noOfSpinners   = spinners.size() - 1;
        int                spinnerCounter = 0;
        ParallelTransition fader          = new ParallelTransition();
        for (int i = characters.size() - 1 ; i >= 0 ; i--) {
            ImageSpinner spinner   = spinners.get(noOfSpinners - spinnerCounter);
            char         character = characters.get(i).charValue();
            int          targetValue;
            if (characters.get(i).toString().equals(".")) {
                targetValue = (int) character == 32 ? 1 : (int) character - 45;
                spinner.setSpinnerType(SpinnerType.SIGNS);
            } else if (characters.get(i).toString().equals("-")) {
                targetValue = (int) character == 32 ? 1 : (int) character - 45;
                spinner.setSpinnerType(SpinnerType.SIGNS);
            } else {
                targetValue = (int) character == 32 ? 10 : (int) character - 48;
                spinner.setSpinnerType(SpinnerType.NUMERIC_0_9);
            }
            if (spinner.getValue() != targetValue) {
                KeyValue kv0 = new KeyValue(spinner.valueProperty(), spinner.getValue(), Interpolator.LINEAR);
                KeyValue kv1 = new KeyValue(spinner.valueProperty(), targetValue, Interpolator.LINEAR);
                kf0Values.add(kv0);
                kf1Values.add(kv1);
            }
            if (spinner.getOpacity() == 0) { fader.getChildren().add(fadeInSpinner(spinner)); }
            spinnerCounter++;
        }
        for (int i = spinnerCounter ; i <= noOfSpinners ; i++) {
            ImageSpinner spinner = spinners.get(noOfSpinners - i);
            fader.getChildren().add(fadeOutSpinner(spinner));
            if (spinner.getValue() != 0) {
                KeyValue kv0 = new KeyValue(spinner.valueProperty(), spinner.getValue(), Interpolator.LINEAR);
                KeyValue kv1 = new KeyValue(spinner.valueProperty(), 0, Interpolator.LINEAR);
                kf0Values.add(kv0);
                kf1Values.add(kv1);
            }
        }

        if (!fader.getChildren().isEmpty()) {
            fader.play();
        }

        KeyFrame kf0 = new KeyFrame(Duration.ZERO, kf0Values.toArray(new KeyValue[0]));
        KeyFrame kf1 = new KeyFrame(Duration.millis(tile.getAnimationDuration()), kf1Values.toArray(new KeyValue[0]));

        timeline.getKeyFrames().setAll(kf0, kf1);
        timeline.play();
    }

    private FadeTransition fadeOutSpinner(final ImageSpinner spinner) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), spinner);
        fadeOut.setFromValue(spinner.getOpacity());
        fadeOut.setToValue(0.0);
        return fadeOut;
    }
    private FadeTransition fadeInSpinner(final ImageSpinner spinner) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), spinner);
        fadeIn.setFromValue(spinner.getOpacity());
        fadeIn.setToValue(1.0);
        return fadeIn;
    }

    private int calcNoOfSpinners() {
        int noOfSpinners = Long.toString((long) tile.getRange()).length();
        if (tile.getDecimals() > 0) {
            noOfSpinners += tile.getDecimals() + 1; // +1 for the dot
        }
        if (tile.getMinValue() < 0) {
            noOfSpinners++;
        }
        return noOfSpinners;
    }

    private void createSpinners() {
        spinners.clear();
        for (int i = 0 ; i < noOfSpinners ; i++) {
            spinners.add(SpinnerBuilder.create()
                                       .type(SpinnerType.NUMERIC_0_9)
                                       .snapshotBackground(tile.getBackgroundColor())
                                       .backgroundColor(tile.getBackgroundColor())
                                       .foregroundColor(tile.getValueColor())
                                       .overlayVisible(false)
                                       .backgroundVisible(false)
                                       .buildImageSpinner());
        }
        spinnerBox.getChildren().setAll(spinners);
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = width - size * 0.1;
        double fontSize = size * 0.24;

    }
    @Override protected void resizeStaticText() {
        double maxWidth = width - size * 0.1;
        double fontSize = size * textSize.factor;

        boolean customFontEnabled = tile.isCustomFontEnabled();
        Font    customFont        = tile.getCustomFont();
        Font    font              = (customFontEnabled && customFont != null) ? Font.font(customFont.getFamily(), fontSize) : Fonts.latoRegular(fontSize);

        titleText.setFont(font);
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        switch(tile.getTitleAlignment()) {
            default    :
            case LEFT  : titleText.relocate(size * 0.05, size * 0.05); break;
            case CENTER: titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.5, size * 0.05); break;
            case RIGHT : titleText.relocate(width - (size * 0.05) - titleText.getLayoutBounds().getWidth(), size * 0.05); break;
        }

        //maxWidth = size * 0.9;
        text.setText(tile.getText());
        text.setFont(font);
        if (text.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(text, maxWidth, fontSize); }
        switch(tile.getTextAlignment()) {
            default    :
            case LEFT  : text.setX(size * 0.05); break;
            case CENTER: text.setX((width - text.getLayoutBounds().getWidth()) * 0.5); break;
            case RIGHT : text.setX(width - (size * 0.05) - text.getLayoutBounds().getWidth()); break;
        }
        text.setY(height - size * 0.05);

        maxWidth = width - (width - size * 0.275);

        fontSize = size * 0.12;
        unitText.setFont(Fonts.latoRegular(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }

        fontSize = size * 0.1;
        description.setFont(Fonts.latoRegular(fontSize));
    }

    @Override protected void resize() {
        super.resize();

        unitText.setText(tile.getUnit());

        resizeDynamicText();
        resizeStaticText();

        final double spinnerHeight = spinners.get(0).getSpinnerHeight() - 2;
        clip.setX(0);
        clip.setY((144 - spinnerHeight) * 0.5);
        clip.setWidth(width);
        clip.setHeight(spinnerHeight);
        spinnerBox.setPrefWidth(width - size * 0.1);
        spinnerBox.relocate(size * 0.05,  contentBounds.getMinY() + (contentBounds.getHeight() - spinnerBox.getHeight()) * 0.5);

        double translateY = spinners.isEmpty() ? 0 : (spinnerHeight - spinners.get(0).getDigitHeight()) * 1.1;
        unitText.setTranslateY(translateY);

        description.setPrefSize(width - size * 0.1, size * 0.43);
        description.relocate(size * 0.05, titleText.isVisible() ? height * 0.42 : height * 0.32);
    }

    @Override protected void redraw() {
        super.redraw();

        titleText.setText(tile.getTitle());
        text.setText(tile.getText());

        description.setText(tile.getDescription());
        description.setAlignment(tile.getDescriptionAlignment());

        resizeDynamicText();
        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
        unitText.setFill(tile.getUnitColor());
        description.setTextFill(tile.getDescriptionColor());
    }
}
