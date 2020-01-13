package eu.hansolo.tilesfx.skins;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import static eu.hansolo.tilesfx.tools.Helper.clamp;

@SuppressWarnings("Duplicates")
public class LedTileSkin extends TileSkin {
    private static final Color BORDER_COLOR = new Color(0.8, 0.8, 0.8, 1);
    private static final Color OFF_COLOR = new Color(0.4, 0.5, 0.5, 1);
    private static final Color ON_DEFAULT_COLOR = new Color (0.88, 0.18, 0.05, 1);
    private Text titleText;
    private Text text;
    private Label description;
    private Circle ledBorder;
    private Circle led;
    private Ellipse glassEffect;


    // ******************** Constructors **************************************
    public LedTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getText());
        text.setFill(tile.getUnitColor());
        Helper.enableNode(text, tile.isTextVisible());

        description = new Label(tile.getDescription());
        description.setAlignment(tile.getDescriptionAlignment());
        description.setWrapText(true);
        description.setTextFill(tile.getTextColor());
        Helper.enableNode(description, !tile.getDescription().isEmpty());

        ledBorder = new Circle();
        led = new Circle();
        glassEffect = new Ellipse();

        getPane().getChildren().addAll(titleText, text, description, ledBorder, led, glassEffect);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
            Helper.enableNode(description, !tile.getDescription().isEmpty());
        }
    }

    @Override protected void handleCurrentValue(final double VALUE) {
        updateLedStatus();
    }

    private void updateLedStatus () {
        if (tile.getValue() == 0) {
            led.setFill(OFF_COLOR);
        }
        else {
            led.setFill(tile.getActiveColor() == null ? ON_DEFAULT_COLOR : tile.getActiveColor());
        }
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeStaticText() {
        double maxWidth = width - size * 0.1;
        double fontSize = size * textSize.factor;

        boolean customFontEnabled = tile.isCustomFontEnabled();
        Font customFont        = tile.getCustomFont();
        Font font              = (customFontEnabled && customFont != null) ? Font.font(customFont.getFamily(), fontSize) : Fonts.latoRegular(fontSize);

        titleText.setFont(font);
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        switch(tile.getTitleAlignment()) {
            default    :
            case LEFT  : titleText.relocate(size * 0.05, size * 0.05); break;
            case CENTER: titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.5, size * 0.05); break;
            case RIGHT : titleText.relocate(width - (size * 0.05) - titleText.getLayoutBounds().getWidth(), size * 0.05); break;
        }

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

        fontSize = size * 0.1;
        description.setFont(Fonts.latoRegular(fontSize));
    }

    @Override protected void resize() {
        super.resize();

        description.setPrefSize(contentBounds.getWidth(), size * 0.43);
        description.relocate(contentBounds.getX(), height * 0.42);

        double diameter = ledBorder.getRadius() * 2;
        double borderWidth = diameter / 11;
        ledBorder.setRadius(size / 7);
        ledBorder.setStrokeWidth(borderWidth);
        ledBorder.relocate((width - diameter - borderWidth) * 0.5, tile.getDescription().isEmpty() ? (height - diameter - borderWidth) * 0.5 : height - size * 0.40);

        led.setRadius(size / 8.8);
        diameter = led.getRadius() * 2;
        led.relocate((width - diameter) * 0.5, tile.getDescription().isEmpty() ? (height - diameter) * 0.5 : height - size * 0.39);

        glassEffect.setRadiusX(diameter / 2.2);
        glassEffect.setRadiusY(diameter / 3.7);
        glassEffect.relocate((width - (glassEffect.getRadiusX() * 2)) * 0.5, ((height - (glassEffect.getRadiusY() * 2)) / 2) - (diameter / 5));
    }


    @Override protected void redraw() {
        super.redraw();

        titleText.setText(tile.getTitle());
        text.setText(tile.getText());
        description.setText(tile.getDescription());
        description.setAlignment(tile.getDescriptionAlignment());

        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
        description.setTextFill(tile.getDescriptionColor());
        ledBorder.setStroke(BORDER_COLOR);
        updateLedStatus();

        Stop[] stops = new Stop[] { new Stop(0, new Color(1, 1, 1, 0.5)), new Stop(1, new Color(1, 1, 1, 0.05))};
        LinearGradient glassGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        glassEffect.setFill(glassGradient);
    }
}
