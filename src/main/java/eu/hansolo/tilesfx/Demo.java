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

import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.weather.DarkSky;
import eu.hansolo.tilesfx.weather.DarkSky.Language;
import eu.hansolo.tilesfx.weather.DarkSky.Unit;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.time.LocalTime;
import java.util.Locale;
import java.util.Random;


/**
 * User: hansolo
 * Date: 19.12.16
 * Time: 12:54
 */
public class Demo extends Application {
    private static final Random RND = new Random();
    private Tile           percentageTile;
    private Tile           clockTile;
    private Tile           gaugeTile;
    private Tile           sparkLineTile;
    private Tile           lineChartTile;
    private Tile           highLowTile;
    private Tile           timerControlTile;
    private Tile           numberTile;
    private Tile           textTile;
    private Tile           plusMinusTile;
    private Tile           sliderTile;
    private Tile           switchTile;
    private Tile           worldTile;
    private Tile           weatherTile;
    private Tile           timeTile;
    private long           lastTimerCall;
    private AnimationTimer timer;


    @Override public void init() {
        // LineChart Data
        XYChart.Series<String, Number> series1 = new XYChart.Series();
        series1.setName("Inside");
        series1.getData().add(new XYChart.Data("MO", 23));
        series1.getData().add(new XYChart.Data("TU", 21));
        series1.getData().add(new XYChart.Data("WE", 20));
        series1.getData().add(new XYChart.Data("TH", 22));
        series1.getData().add(new XYChart.Data("FR", 24));
        series1.getData().add(new XYChart.Data("SA", 22));
        series1.getData().add(new XYChart.Data("SU", 20));

        XYChart.Series<String, Number> series2 = new XYChart.Series();
        series2.setName("Outside");
        series2.getData().add(new XYChart.Data("MO", 8));
        series2.getData().add(new XYChart.Data("TU", 5));
        series2.getData().add(new XYChart.Data("WE", 0));
        series2.getData().add(new XYChart.Data("TH", 2));
        series2.getData().add(new XYChart.Data("FR", 4));
        series2.getData().add(new XYChart.Data("SA", 3));
        series2.getData().add(new XYChart.Data("SU", 5));

        // WorldMap Data
        for (int i = 0 ; i < Country.values().length ; i++) {
            double value = RND.nextInt(10);
            Color  color;
            if (value > 8) {
                color = Tile.RED;
            } else if (value > 6) {
                color = Tile.ORANGE;
            } else if (value > 4) {
                color = Tile.YELLOW_ORANGE;
            } else if (value > 2) {
                color = Tile.GREEN;
            } else {
                color = Tile.BLUE;
            }
            Country.values()[i].setColor(color);
        }

        // TimeControl Data
        TimeSection timeSection = new TimeSection(LocalTime.now().plusSeconds(20), LocalTime.now().plusHours(1), Tile.GRAY, Tile.RED);
        timeSection.setOnTimeSectionEntered(e -> System.out.println("Section ACTIVE"));
        timeSection.setOnTimeSectionLeft(e -> System.out.println("Section INACTIVE"));

        // Weather (You can get a DarkSky API key here: https://darksky.net/dev/ )
        DarkSky darkSky = new DarkSky("YOUR_DARK_SKY_API_KEY", Unit.CA, Language.ENGLISH, 51.911858, 7.632815);

        // Creating Tiles
        percentageTile = TileBuilder.create()
                                    .skinType(SkinType.PERCENTAGE)
                                    .title("Percentage Tile")
                                    .unit("\u0025")
                                    .maxValue(60)
                                    .build();

        clockTile = TileBuilder.create()
                               .skinType(SkinType.CLOCK)
                               .title("Clock Tile")
                               .subTitle("test")
                               .text("TEST")
                               .textVisible(true)
                               .dateVisible(true)
                               .locale(Locale.US)
                               .running(true)
                               .build();

        gaugeTile = TileBuilder.create()
                               .skinType(SkinType.GAUGE)
                               .title("Gauge Tile")
                               .threshold(75)
                               .build();

        sparkLineTile = TileBuilder.create()
                                   .skinType(SkinType.SPARK_LINE)
                                   .title("SparkLine Tile")
                                   .build();

        lineChartTile = TileBuilder.create()
                                   .skinType(SkinType.LINE_CHART)
                                   .title("LineChart Tile")
                                   .series(series1, series2)
                                   .build();

        highLowTile = TileBuilder.create()
                                 .skinType(SkinType.HIGH_LOW)
                                 .title("HighLow Tile")
                                 .unit("\u0025")
                                 .referenceValue(6.7)
                                 .value(8.2)
                                 .build();

        timerControlTile = TileBuilder.create()
                                      .skinType(SkinType.TIMER_CONTROL)
                                      .title("TimerControl Tile")
                                      .subTitle("test")
                                      .text("TEST")
                                      .secondsVisible(true)
                                      .textVisible(true)
                                      .dateVisible(true)
                                      .timeSections(timeSection)
                                      .running(true)
                                      .build();

        numberTile = TileBuilder.create()
                                .skinType(SkinType.NUMBER)
                                .title("Number Tile")
                                .value(13)
                                .text("Things")
                                .textVisible(true)
                                .build();

        textTile = TileBuilder.create()
                              .skinType(SkinType.TEXT)
                              .title("Text Tile")
                              .text("May the force be with you\n...always")
                              .textVisible(true)
                              .build();

        plusMinusTile = TileBuilder.create()
                                   .skinType(SkinType.PLUS_MINUS)
                                   .maxValue(30)
                                   .minValue(0)
                                   .title("PlusMinus Tile")
                                   .unit("\u00B0C")
                                   .build();

        sliderTile = TileBuilder.create()
                                .skinType(SkinType.SLIDER)
                                .title("Slider Tile")
                                .unit("\u00B0C")
                                .barBackgroundColor(Tile.FOREGROUND)
                                .build();

        switchTile = TileBuilder.create()
                                .skinType(SkinType.SWITCH)
                                .title("Switch Tile")
                                .text("Whatever text")
                                .build();

        worldTile = TileBuilder.create()
                               .skinType(SkinType.WORLDMAP)
                               .title("WorldMap Tile")
                               .text("Whatever text")
                               .textVisible(true)
                               .build();

        // Update the weather information by calling weatherTile.updateWeather()
        weatherTile = TileBuilder.create()
                                 .skinType(SkinType.WEATHER)
                                 .title("YOUR CITY NAME")
                                 .darkSky(darkSky)
                                 .build();

        timeTile = TileBuilder.create()
                              .skinType(SkinType.TIME)
                              .title("Time Tile")
                              .duration(LocalTime.of(1, 22))
                              .text("Average reply time")
                              .textVisible(true)
                              .build();

        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now > lastTimerCall + 3_000_000_000l) {
                    percentageTile.setValue(RND.nextDouble() * percentageTile.getRange() * 1.5 + percentageTile.getMinValue());
                    gaugeTile.setValue(RND.nextDouble() * gaugeTile.getRange() * 1.5 + gaugeTile.getMinValue());
                    sparkLineTile.setValue(RND.nextDouble() * sparkLineTile.getRange() * 1.5 + sparkLineTile.getMinValue());
                    highLowTile.setValue(RND.nextDouble() * 10);
                    //sliderTile.setValue(RND.nextDouble() * sliderTile.getRange() + sliderTile.getMinValue());
                    //plusMinusTile.setValue(RND.nextDouble() * plusMinusTile.getRange() + plusMinusTile.getMinValue());
                    //switchTile.setSelected(RND.nextBoolean());
                    series1.getData().forEach(data -> data.setYValue(RND.nextInt(30)));
                    series2.getData().forEach(data -> data.setYValue(RND.nextInt(10)));
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        FlowPane pane = new FlowPane(percentageTile, clockTile, gaugeTile, sparkLineTile,
                                     lineChartTile, highLowTile, timerControlTile, numberTile, textTile,
                                     plusMinusTile, sliderTile, switchTile, worldTile, timeTile);// , weatherTile);
        pane.setHgap(10);
        pane.setVgap(10);
        pane.setPadding(new Insets(10));
        pane.setPrefSize(1310, 790);
        pane.setBackground(new Background(new BackgroundFill(Color.web("#101214"), CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(pane);

        stage.setTitle("TilesFX");
        stage.setScene(scene);
        stage.show();

        timer.start();
    }

    @Override public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
