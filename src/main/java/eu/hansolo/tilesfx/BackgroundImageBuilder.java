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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.image.Image;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;

/**
 * Created by Naoghuman on 10.02.17.
 */
public final class BackgroundImageBuilder {

    // ******************** Constructors **************************************
    private BackgroundImageBuilder() {
    }

    // ******************** Methods *******************************************
    public static final ImageBackgroundImageBuilder create() {
        return new BackgroundImageBuilderImpl();
    }

    // ******************** Interfaces for internal steps *********************
    public static interface ImageBackgroundImageBuilder {
        public RepeatXBackgroundImageBuilder image(final Image IMAGE);
    }

    public static interface RepeatXBackgroundImageBuilder {
        public RepeatYBackgroundImageBuilder repeatX(final BackgroundRepeat BACKGROUND_REPEAT_X);
        public PositionBackgroundImageBuilder repeatY(final BackgroundRepeat BACKGROUND_REPEAT_Y);
        public SizeBackgroundImageBuilder position(final BackgroundPosition BACKGROUND_POSITION);
        public BuildBackgroundImageBuilder size(final BackgroundSize BACKGROUND_SIZE);
        public BackgroundImage build();
    }

    public static interface RepeatYBackgroundImageBuilder {
        public PositionBackgroundImageBuilder repeatY(final BackgroundRepeat BACKGROUND_REPEAT_Y);
        public SizeBackgroundImageBuilder position(final BackgroundPosition BACKGROUND_POSITION);
        public BuildBackgroundImageBuilder size(final BackgroundSize BACKGROUND_SIZE);
        public BackgroundImage build();
    }

    public static interface PositionBackgroundImageBuilder {
        public SizeBackgroundImageBuilder position(final BackgroundPosition BACKGROUND_POSITION);
        public BuildBackgroundImageBuilder size(final BackgroundSize BACKGROUND_SIZE);
        public BackgroundImage build();
    }

    public static interface SizeBackgroundImageBuilder {
        public BuildBackgroundImageBuilder size(final BackgroundSize BACKGROUND_SIZE);
        public BackgroundImage build();
    }

    public static interface BuildBackgroundImageBuilder {
        public BackgroundImage build();
    }

    // ******************** Internal impl-class *******************************
    private static final class BackgroundImageBuilderImpl implements ImageBackgroundImageBuilder, RepeatXBackgroundImageBuilder,
            RepeatYBackgroundImageBuilder, PositionBackgroundImageBuilder, SizeBackgroundImageBuilder, BuildBackgroundImageBuilder
    {
        private ObservableMap<String, Property> properties = FXCollections.observableHashMap();

        BackgroundImageBuilderImpl() {
            properties.put("repeatX", new SimpleObjectProperty<>(BackgroundRepeat.REPEAT));
            properties.put("repeatY", new SimpleObjectProperty<>(BackgroundRepeat.REPEAT));
            properties.put("position", new SimpleObjectProperty<>(BackgroundPosition.DEFAULT));
            properties.put("size", new SimpleObjectProperty<>(BackgroundSize.DEFAULT));
        }

        @Override
        public final RepeatXBackgroundImageBuilder image(final Image IMAGE) {
            if (IMAGE == null) {
                throw new NullPointerException("IMAGE can't be null.");
            }
            properties.put("image", new SimpleObjectProperty<>(IMAGE));
            return this;
        }

        @Override
        public final RepeatYBackgroundImageBuilder repeatX(final BackgroundRepeat BACKGROUND_REPEAT_X) {
            properties.put("repeatX", new SimpleObjectProperty<>(BACKGROUND_REPEAT_X));
            return this;
        }

        @Override
        public final PositionBackgroundImageBuilder repeatY(final BackgroundRepeat BACKGROUND_REPEAT_Y) {
            properties.put("repeatY", new SimpleObjectProperty<>(BACKGROUND_REPEAT_Y));
            return this;
        }

        @Override
        public final SizeBackgroundImageBuilder position(final BackgroundPosition BACKGROUND_POSITION) {
            properties.put("position", new SimpleObjectProperty<>(BACKGROUND_POSITION));
            return this;
        }

        @Override
        public final BuildBackgroundImageBuilder size(final BackgroundSize BACKGROUND_SIZE) {
            properties.put("size", new SimpleObjectProperty<>(BACKGROUND_SIZE));
            return this;
        }

        @Override
        public final BackgroundImage build() {
            final ObjectProperty<Image> imageObjectProperty = (ObjectProperty<Image>) properties.get("image");
            final Image image = imageObjectProperty.get();

            final ObjectProperty<BackgroundRepeat> repeatXObjectProperty = (ObjectProperty<BackgroundRepeat>) properties.get("repeatX");
            final BackgroundRepeat backgroundRepeatX = repeatXObjectProperty.get();

            final ObjectProperty<BackgroundRepeat> repeatYObjectProperty = (ObjectProperty<BackgroundRepeat>) properties.get("repeatY");
            final BackgroundRepeat backgroundRepeatY = repeatYObjectProperty.get();

            final ObjectProperty<BackgroundPosition> positionObjectProperty = (ObjectProperty<BackgroundPosition>) properties.get("position");
            final BackgroundPosition backgroundPosition = positionObjectProperty.get();

            final ObjectProperty<BackgroundSize> sizeObjectProperty = (ObjectProperty<BackgroundSize>) properties.get("size");
            final BackgroundSize backgroundSize = sizeObjectProperty.get();

            final BackgroundImage backgroundImage = new BackgroundImage(
                    image,
                    backgroundRepeatX, backgroundRepeatY,
                    backgroundPosition, backgroundSize);

            return backgroundImage;
        }

    }

}
