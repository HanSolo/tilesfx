package eu.hansolo.tilesfx.skins;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.scene.control.TableView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Arrays;

public class TableTileSkin extends TileSkin {

    private Text           titleText;
    private TableView      tableView;


    public TableTileSkin(Tile TILE) {
        super(TILE);

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        tableView = new TableView();
        tableView.setId("tableTile");
        tableView.getColumns().addAll(Arrays.asList(tile.getTableColumns()));
        tableView.setItems(tile.getTableItems());

        getPane().getChildren().addAll(titleText, tableView);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
        }
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeStaticText() {
        double maxWidth = width - size * 0.1;
        double fontSize = Math.min(size * textSize.factor, 20);
        double y = Math.min(15, size * 0.05);

        boolean customFontEnabled = tile.isCustomFontEnabled();
        Font customFont        = tile.getCustomFont();
        Font font              = (customFontEnabled && customFont != null) ? Font.font(customFont.getFamily(), fontSize) : Fonts.latoRegular(fontSize);

        titleText.setFont(font);
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        switch(tile.getTitleAlignment()) {
            default    :
            case LEFT  : titleText.relocate(size * 0.05, y); break;
            case CENTER: titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.5, y); break;
            case RIGHT : titleText.relocate(width - (size * 0.05) - titleText.getLayoutBounds().getWidth(), y); break;
        }
    }


    @Override protected void resize() {
        super.resize();

        double topOffset = Math.min(tile.getHeight() * 0.15, 50);
        tableView.setPrefWidth(tile.getWidth());
        tableView.setPrefHeight(tile.getHeight() - topOffset);
        tableView.relocate(0, topOffset);
    }


    @Override protected void redraw() {
        super.redraw();

        titleText.setText(tile.getTitle());
        resizeStaticText();
        titleText.setFill(tile.getTitleColor());
    }
}
