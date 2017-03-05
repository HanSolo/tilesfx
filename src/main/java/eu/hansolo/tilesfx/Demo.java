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

import eu.hansolo.tilesfx.Tile.MapProvider;
import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.Tile.TileColor;
import eu.hansolo.tilesfx.skins.BarChartItem;
import eu.hansolo.tilesfx.skins.LeaderBoardItem;
import eu.hansolo.tilesfx.tools.ChartData;
import eu.hansolo.tilesfx.tools.FlowGridPane;
import eu.hansolo.tilesfx.tools.Location;
import eu.hansolo.tilesfx.weather.DarkSky;
import eu.hansolo.tilesfx.weather.DarkSky.Language;
import eu.hansolo.tilesfx.weather.DarkSky.Unit;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
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
    private static final    Random RND = new Random();
    private static final    double TILE_WIDTH  = 250;
    private static final    double TILE_HEIGHT = 250;
    private BarChartItem    barChartItem1;
    private BarChartItem    barChartItem2;
    private BarChartItem    barChartItem3;
    private BarChartItem    barChartItem4;
    private LeaderBoardItem leaderBoardItem1;
    private LeaderBoardItem leaderBoardItem2;
    private LeaderBoardItem leaderBoardItem3;
    private LeaderBoardItem leaderBoardItem4;
    private ChartData       chartData1;
    private ChartData       chartData2;
    private ChartData       chartData3;
    private ChartData       chartData4;
    private Tile            percentageTile;
    private Tile            clockTile;
    private Tile            gaugeTile;
    private Tile            sparkLineTile;
    private Tile            areaChartTile;
    private Tile            lineChartTile;
    private Tile            highLowTile;
    private Tile            timerControlTile;
    private Tile            numberTile;
    private Tile            textTile;
    private Tile            plusMinusTile;
    private Tile            sliderTile;
    private Tile            switchTile;
    private Tile            worldTile;
    private Tile            weatherTile;
    private Tile            timeTile;
    private Tile            barChartTile;
    private Tile            customTile;
    private Tile            leaderBoardTile;
    private Tile            mapTile;
    private Tile            radialChartTile;
    private Tile            donutChartTile;
    private Tile            circularProgressTile;
    private Tile            stockTile;
    private long            lastTimerCall;
    private AnimationTimer  timer;
    private DoubleProperty  value;


    @Override public void init() {
        value = new SimpleDoubleProperty(0);

        // LineChart Data
        XYChart.Series<String, Number> series1 = new XYChart.Series();
        series1.setName("Whatever");
        series1.getData().add(new XYChart.Data("MO", 23));
        series1.getData().add(new XYChart.Data("TU", 21));
        series1.getData().add(new XYChart.Data("WE", 20));
        series1.getData().add(new XYChart.Data("TH", 22));
        series1.getData().add(new XYChart.Data("FR", 24));
        series1.getData().add(new XYChart.Data("SA", 22));
        series1.getData().add(new XYChart.Data("SU", 20));

        XYChart.Series<String, Number> series2 = new XYChart.Series();
        series2.setName("Inside");
        series2.getData().add(new XYChart.Data("MO", 8));
        series2.getData().add(new XYChart.Data("TU", 5));
        series2.getData().add(new XYChart.Data("WE", 0));
        series2.getData().add(new XYChart.Data("TH", 2));
        series2.getData().add(new XYChart.Data("FR", 4));
        series2.getData().add(new XYChart.Data("SA", 3));
        series2.getData().add(new XYChart.Data("SU", 5));

        XYChart.Series<String, Number> series3 = new XYChart.Series();
        series3.setName("Outside");
        series3.getData().add(new XYChart.Data("MO", 8));
        series3.getData().add(new XYChart.Data("TU", 5));
        series3.getData().add(new XYChart.Data("WE", 0));
        series3.getData().add(new XYChart.Data("TH", 2));
        series3.getData().add(new XYChart.Data("FR", 4));
        series3.getData().add(new XYChart.Data("SA", 3));
        series3.getData().add(new XYChart.Data("SU", 5));

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
        TimeSection timeSection = TimeSectionBuilder.create()
                                        .start(LocalTime.now().plusSeconds(20))
                                        .stop(LocalTime.now().plusHours(1))
                                        //.days(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
                                        .color(Tile.GRAY)
                                        .highlightColor(Tile.RED)
                                        .build();

        timeSection.setOnTimeSectionEntered(e -> System.out.println("Section ACTIVE"));
        timeSection.setOnTimeSectionLeft(e -> System.out.println("Section INACTIVE"));

        // Weather (You can get a DarkSky API key here: https://darksky.net/dev/ )
        DarkSky darkSky = new DarkSky("YOUR DARKSKY API KEY", Unit.CA, Language.ENGLISH, 51.911858, 7.632815);
        //darkSky.update();

        // BarChart Items
        barChartItem1 = new BarChartItem("Gerrit", 47, Tile.BLUE);
        barChartItem2 = new BarChartItem("Sandra", 43, Tile.RED);
        barChartItem3 = new BarChartItem("Lilli", 12, Tile.GREEN);
        barChartItem4 = new BarChartItem("Anton", 8, Tile.ORANGE);

        // LeaderBoard Items
        leaderBoardItem1 = new LeaderBoardItem("Gerrit", 47);
        leaderBoardItem2 = new LeaderBoardItem("Sandra", 43);
        leaderBoardItem3 = new LeaderBoardItem("Lilli", 12);
        leaderBoardItem4 = new LeaderBoardItem("Anton", 8);

        // RadialChart Data
        chartData1 = new ChartData("Item 1", 24.0, Tile.GREEN);
        chartData2 = new ChartData("Item 2", 10.0, Tile.BLUE);
        chartData3 = new ChartData("Item 3", 12.0, Tile.RED);
        chartData4 = new ChartData("Item 4", 13.0, Tile.YELLOW_ORANGE);
        //RadialChartData.animated = false;


        // Creating Tiles
        percentageTile = TileBuilder.create()
                                    .skinType(SkinType.PERCENTAGE)
                                    .prefSize(TILE_WIDTH, TILE_HEIGHT)
                                    .title("Percentage Tile")
                                    .unit("\u0025")
                                    .description("Test")
                                    .maxValue(60)
                                    .build();

        clockTile = TileBuilder.create()
                               .skinType(SkinType.CLOCK)
                               .prefSize(TILE_WIDTH, TILE_HEIGHT)
                               .title("Clock Tile")
                               .text("Whatever text")
                               .dateVisible(true)
                               .locale(Locale.US)
                               .running(true)
                               .build();

        gaugeTile = TileBuilder.create()
                               .skinType(SkinType.GAUGE)
                               .prefSize(TILE_WIDTH, TILE_HEIGHT)
                               .title("Gauge Tile")
                               .unit("V")
                               .threshold(75)
                               .build();

        sparkLineTile = TileBuilder.create()
                                   .skinType(SkinType.SPARK_LINE)
                                   .prefSize(TILE_WIDTH, TILE_HEIGHT)
                                   .title("SparkLine Tile")
                                   .unit("mb")
                                   .gradientStops(new Stop(0, Tile.GREEN),
                                                  new Stop(0.5, Tile.YELLOW),
                                                  new Stop(1.0, Tile.RED))
                                   .strokeWithGradient(true)
                                   .build();

        //sparkLineTile.valueProperty().bind(value);

        areaChartTile = TileBuilder.create()
                                   .skinType(SkinType.AREA_CHART)
                                   .prefSize(TILE_WIDTH, TILE_HEIGHT)
                                   .title("AreaChart Tile")
                                   .series(series1)
                                   .build();

        lineChartTile = TileBuilder.create()
                                   .skinType(SkinType.LINE_CHART)
                                   .prefSize(TILE_WIDTH, TILE_HEIGHT)
                                   .title("LineChart Tile")
                                   .series(series2, series3)
                                   .build();

        highLowTile = TileBuilder.create()
                                 .skinType(SkinType.HIGH_LOW)
                                 .prefSize(TILE_WIDTH, TILE_HEIGHT)
                                 .title("HighLow Tile")
                                 .unit("\u0025")
                                 .description("Test")
                                 .text("Whatever text")
                                 .referenceValue(6.7)
                                 .value(8.2)
                                 .build();

        timerControlTile = TileBuilder.create()
                                      .skinType(SkinType.TIMER_CONTROL)
                                      .prefSize(TILE_WIDTH, TILE_HEIGHT)
                                      .title("TimerControl Tile")
                                      .text("Whatever text")
                                      .secondsVisible(true)
                                      .dateVisible(true)
                                      .timeSections(timeSection)
                                      .running(true)
                                      .build();

        numberTile = TileBuilder.create()
                                .skinType(SkinType.NUMBER)
                                .prefSize(TILE_WIDTH, TILE_HEIGHT)
                                .title("Number Tile")
                                .text("Whatever text")
                                .value(13)
                                .unit("mb")
                                .description("Test")
                                .textVisible(true)
                                .build();

        textTile = TileBuilder.create()
                              .skinType(SkinType.TEXT)
                              .prefSize(TILE_WIDTH, TILE_HEIGHT)
                              .title("Text Tile")
                              .text("Whatever text")
                              .description("May the force be with you\n...always")
                              .descriptionAlignment(Pos.TOP_LEFT)
                              .textVisible(true)
                              .build();

        plusMinusTile = TileBuilder.create()
                                   .skinType(SkinType.PLUS_MINUS)
                                   .prefSize(TILE_WIDTH, TILE_HEIGHT)
                                   .maxValue(30)
                                   .minValue(0)
                                   .title("PlusMinus Tile")
                                   .text("Whatever text")
                                   .description("Test")
                                   .unit("\u00B0C")
                                   .build();

        sliderTile = TileBuilder.create()
                                .skinType(SkinType.SLIDER)
                                .prefSize(TILE_WIDTH, TILE_HEIGHT)
                                .title("Slider Tile")
                                .text("Whatever text")
                                .description("Test")
                                .unit("\u00B0C")
                                .barBackgroundColor(Tile.FOREGROUND)
                                .build();

        switchTile = TileBuilder.create()
                                .skinType(SkinType.SWITCH)
                                .prefSize(TILE_WIDTH, TILE_HEIGHT)
                                .title("Switch Tile")
                                .text("Whatever text")
                                //.description("Test")
                                .build();

        switchTile.setOnSwitchPressed(e -> System.out.println("Switch pressed"));
        switchTile.setOnSwitchReleased(e -> System.out.println("Switch released"));

        worldTile = TileBuilder.create()
                               .prefSize(300, TILE_HEIGHT)
                               .skinType(SkinType.WORLDMAP)
                               .title("WorldMap Tile")
                               .text("Whatever text")
                               .textVisible(false)
                               .build();

        // Update the weather information by calling weatherTile.updateWeather()
        weatherTile = TileBuilder.create()
                                 .skinType(SkinType.WEATHER)
                                 .prefSize(TILE_WIDTH, TILE_HEIGHT)
                                 .title("YOUR CITY NAME")
                                 .text("Whatever text")
                                 .darkSky(darkSky)
                                 .build();

        timeTile = TileBuilder.create()
                              .skinType(SkinType.TIME)
                              .prefSize(TILE_WIDTH, TILE_HEIGHT)
                              .title("Time Tile")
                              .text("Whatever text")
                              .duration(LocalTime.of(1, 22))
                              .description("Average reply time")
                              .textVisible(true)
                              .build();

        barChartTile = TileBuilder.create()
                                  .skinType(SkinType.BAR_CHART)
                                  .prefSize(TILE_WIDTH, TILE_HEIGHT)
                                  .title("BarChart Tile")
                                  .text("Whatever text")
                                  .barChartItems(barChartItem1, barChartItem2, barChartItem3, barChartItem4)
                                  .decimals(0)
                                  .build();

        customTile = TileBuilder.create()
                                .skinType(SkinType.CUSTOM)
                                .prefSize(TILE_WIDTH, TILE_HEIGHT)
                                .title("Custom Tile")
                                .text("Whatever text")
                                .graphic(new Button("Click Me"))
                                .roundedCorners(false)
                                .build();

        leaderBoardTile = TileBuilder.create()
                                     .skinType(SkinType.LEADER_BOARD)
                                     .prefSize(TILE_WIDTH, TILE_HEIGHT)
                                     .title("LeaderBoard Tile")
                                     .text("Whatever text")
                                     .leaderBoardItems(leaderBoardItem1, leaderBoardItem2, leaderBoardItem3, leaderBoardItem4)
                                     .build();

        mapTile = TileBuilder.create()
                             .skinType(SkinType.MAP)
                             .prefSize(TILE_WIDTH, TILE_HEIGHT)
                             .title("Map")
                             .text("Some text")
                             .description("Description")
                             .currentLocation(new Location(51.91178, 7.63379, "Home", TileColor.MAGENTA))
                             .pointsOfInterest(new Location(51.914405, 7.635732, "POI 1", TileColor.RED),
                                               new Location(51.912529, 7.631752, "POI 2", TileColor.BLUE),
                                               new Location(51.923993, 7.628906, "POI 3", TileColor.YELLOW_ORANGE))
                             .mapProvider(MapProvider.TOPO)
                             .build();

        radialChartTile = TileBuilder.create()
                                     .skinType(SkinType.RADIAL_CHART)
                                     .prefSize(TILE_WIDTH, TILE_HEIGHT)
                                     .title("RadialChart")
                                     .text("Some text")
                                     .textVisible(false)
                                     .radialChartData(chartData1, chartData2, chartData3, chartData4)
                                     .build();

        donutChartTile = TileBuilder.create()
                                     .skinType(SkinType.DONUT_CHART)
                                     .prefSize(TILE_WIDTH, TILE_HEIGHT)
                                     .title("DonutChart")
                                     .text("Some text")
                                     .textVisible(false)
                                     .radialChartData(chartData1, chartData2, chartData3, chartData4)
                                     .build();

        circularProgressTile = TileBuilder.create()
                                         .skinType(SkinType.CIRCULAR_PROGRESS)
                                         .prefSize(TILE_WIDTH, TILE_HEIGHT)
                                         .title("CircularProgress")
                                         .text("Some text")
                                         .unit("\u0025")
                                         .build();

        stockTile = TileBuilder.create()
                               .skinType(SkinType.STOCK)
                               .prefSize(TILE_WIDTH, TILE_HEIGHT)
                               .title("Stock")
                               .minValue(0)
                               .maxValue(1000)
                               .averagingPeriod(100)
                               .build();

        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now > lastTimerCall + 3_500_000_000L) {
                    percentageTile.setValue(RND.nextDouble() * percentageTile.getRange() * 1.5 + percentageTile.getMinValue());
                    gaugeTile.setValue(RND.nextDouble() * gaugeTile.getRange() * 1.5 + gaugeTile.getMinValue());

                    sparkLineTile.setValue(RND.nextDouble() * sparkLineTile.getRange() * 1.5 + sparkLineTile.getMinValue());
                    //value.set(RND.nextDouble() * sparkLineTile.getRange() * 1.5 + sparkLineTile.getMinValue());
                    //sparkLineTile.setValue(20);

                    highLowTile.setValue(RND.nextDouble() * 10);
                    series1.getData().forEach(data -> data.setYValue(RND.nextInt(100)));
                    series2.getData().forEach(data -> data.setYValue(RND.nextInt(30)));
                    series3.getData().forEach(data -> data.setYValue(RND.nextInt(10)));

                    chartData1.setValue(RND.nextDouble() * 50);
                    chartData2.setValue(RND.nextDouble() * 50);
                    chartData3.setValue(RND.nextDouble() * 50);
                    chartData4.setValue(RND.nextDouble() * 50);

                    barChartTile.getBarChartItems().get(RND.nextInt(3)).setValue(RND.nextDouble() * 80);

                    leaderBoardTile.getLeaderBoardItems().get(RND.nextInt(3)).setValue(RND.nextDouble() * 80);

                    circularProgressTile.setValue(RND.nextDouble() * 120);

                    stockTile.setValue(RND.nextDouble() * 50 + 500);

                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        FlowGridPane pane = new FlowGridPane(5, 5,
                                         percentageTile, clockTile, gaugeTile, sparkLineTile, areaChartTile,
                                         lineChartTile, timerControlTile, numberTile, textTile,
                                         highLowTile, plusMinusTile, sliderTile, switchTile, timeTile,
                                         barChartTile, customTile, leaderBoardTile, worldTile, mapTile,
                                         radialChartTile, donutChartTile, circularProgressTile, stockTile);//, weatherTile);

        pane.setHgap(5);
        pane.setVgap(5);
        pane.setAlignment(Pos.CENTER);
        pane.setCenterShape(true);
        pane.setPadding(new Insets(5));
        pane.setPrefSize(800, 600);
        pane.setBackground(new Background(new BackgroundFill(Color.web("#101214"), CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(pane);

        stage.setTitle("TilesFX");
        stage.setScene(scene);
        stage.show();

        timer.start();

        mapTile.addPoiLocation(new Location(51.85, 7.75, "Test"));
        mapTile.removePoiLocation(new Location(51.85, 7.75, "Test"));
    }

    @Override public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
