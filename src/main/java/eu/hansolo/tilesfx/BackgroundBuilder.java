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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;

/**
 * Created by Naoghuman on 10.02.17.
 */
public final class BackgroundBuilder {
    
    private final ObservableList<BackgroundImage> backgroundImages = FXCollections.observableArrayList();
    private final ObservableList<BackgroundFill> backgroundFills = FXCollections.observableArrayList();

    // ******************** Constructors **************************************
    private BackgroundBuilder() {}
    
    // ******************** Methods *******************************************
    public static final BackgroundBuilder create() {
        return new BackgroundBuilder();
    }

    public final BackgroundBuilder fills(final BackgroundFill... BACKGROUND_FILLS) {
        backgroundFills.addAll(BACKGROUND_FILLS);
        return this;
    }

    public final BackgroundBuilder images(final BackgroundImage... BACKGROUND_IMAGES) {
        backgroundImages.addAll(BACKGROUND_IMAGES);
        return this;
    }

    public final Background build() {
        final Background background = new Background(backgroundFills, backgroundImages);
        return background;
    }

}
