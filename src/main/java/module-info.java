module eu.hansolo.tilesfx {

    // Java
    requires java.base;

    // Java-FX
    requires transitive javafx.base;
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires transitive javafx.web;

    exports eu.hansolo.tilesfx;
    exports eu.hansolo.tilesfx.addons;
    exports eu.hansolo.tilesfx.chart;
    exports eu.hansolo.tilesfx.colors;
    exports eu.hansolo.tilesfx.events;
    exports eu.hansolo.tilesfx.fonts;
    exports eu.hansolo.tilesfx.icons;
    exports eu.hansolo.tilesfx.skins;
    exports eu.hansolo.tilesfx.tools;
}