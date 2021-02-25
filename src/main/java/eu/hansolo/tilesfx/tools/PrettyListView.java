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

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.util.Set;


public class PrettyListView<T> extends ListView<T> {
    private static String             userAgentStyleSheet;
    private        ScrollBar          vBar;
    private        ScrollBar          hBar;
    private        ParallelTransition parallelFadeIn;
    private        ParallelTransition parallelFadeOut;


    public PrettyListView() {
        super();

        vBar = new ScrollBar();
        hBar = new ScrollBar();

        skinProperty().addListener(skin -> {
            bindScrollBars();
            getChildren().addAll(vBar, hBar);
        });

        getStyleClass().add("pretty-list-view");

        vBar.setManaged(false);
        vBar.setOpacity(0.0);
        vBar.setOrientation(Orientation.VERTICAL);
        vBar.getStyleClass().add("pretty-scroll-bar");
        vBar.visibleProperty().bind(vBar.visibleAmountProperty().isNotEqualTo(0));

        hBar.setManaged(false);
        hBar.setOpacity(0.0);
        hBar.setOrientation(Orientation.HORIZONTAL);
        hBar.getStyleClass().add("pretty-scroll-bar");
        hBar.visibleProperty().bind(hBar.visibleAmountProperty().isNotEqualTo(0));

        FadeTransition fadeInVBar = new FadeTransition(Duration.millis(500), vBar);
        fadeInVBar.setFromValue(0.0);
        fadeInVBar.setToValue(1.0);
        FadeTransition fadeInHBar = new FadeTransition(Duration.millis(500), hBar);
        fadeInHBar.setFromValue(0.0);
        fadeInHBar.setToValue(1.0);
        parallelFadeIn = new ParallelTransition(fadeInVBar, fadeInHBar);

        FadeTransition fadeOutVBar = new FadeTransition(Duration.millis(500), vBar);
        fadeOutVBar.setFromValue(1.0);
        fadeOutVBar.setToValue(0.0);
        FadeTransition fadeOutHBar = new FadeTransition(Duration.millis(500), hBar);
        fadeOutHBar.setFromValue(1.0);
        fadeOutHBar.setToValue(0.0);
        parallelFadeOut = new ParallelTransition(fadeOutVBar, fadeOutHBar);

        setFocusTraversable(false);

        registerListeners();
    }


    private void bindScrollBars() {
        final Set<Node> nodes = lookupAll("VirtualScrollBar");
        nodes.stream().filter(ScrollBar.class::isInstance).forEach(node -> {
            ScrollBar bar = (ScrollBar) node;
            switch(bar.getOrientation()) {
                case VERTICAL  : bindScrollBars(vBar, bar); break;
                case HORIZONTAL: bindScrollBars(hBar, bar); break;
            }
        });
    }

    private void bindScrollBars(final ScrollBar scrollBarA, final ScrollBar scrollBarB) {
        scrollBarA.valueProperty().bindBidirectional(scrollBarB.valueProperty());
        scrollBarA.minProperty().bindBidirectional(scrollBarB.minProperty());
        scrollBarA.maxProperty().bindBidirectional(scrollBarB.maxProperty());
        scrollBarA.visibleAmountProperty().bindBidirectional(scrollBarB.visibleAmountProperty());
        scrollBarA.unitIncrementProperty().bindBidirectional(scrollBarB.unitIncrementProperty());
        scrollBarA.blockIncrementProperty().bindBidirectional(scrollBarB.blockIncrementProperty());
    }

    private void registerListeners() {
        EventHandler<MouseEvent> mouseHandler = e -> {
            EventType<? extends Event> type = e.getEventType();
            if (MouseEvent.MOUSE_ENTERED.equals(type)) {
                parallelFadeIn.play();
            } else if (MouseEvent.MOUSE_EXITED.equals(type)) {
                parallelFadeOut.play();
            }
        };
        addEventFilter(MouseEvent.MOUSE_ENTERED, mouseHandler);
        addEventFilter(MouseEvent.MOUSE_EXITED, mouseHandler);
    }

    @Override protected void layoutChildren() {
        super.layoutChildren();

        final Insets insets    = getInsets();
        final double width     = getWidth();
        final double height    = getHeight();
        final double prefWidth = vBar.prefWidth(-1);
        vBar.resizeRelocate(width - prefWidth - insets.getRight(), insets.getTop(), prefWidth, height - insets.getTop() - insets.getBottom());

        final double prefHeight = hBar.prefHeight(-1);
        hBar.resizeRelocate(insets.getLeft(), height - prefHeight - insets.getBottom(), width - insets.getLeft() - insets.getRight(), prefHeight);
    }

    @Override public String getUserAgentStylesheet() {
        if (null == userAgentStyleSheet) { userAgentStyleSheet = getClass().getResource("pretty-list-view.css").toExternalForm(); }
        return userAgentStyleSheet;
    }
}
