module be.jensberckmoes.insightfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.opencsv;
    requires static lombok;
    requires java.logging;
    requires org.slf4j;
    requires java.sql;
    requires org.apache.pdfbox;
    requires org.apache.fontbox;
    requires java.desktop;
    requires javafx.swing;

    opens be.jensberckmoes.insightfx to javafx.fxml;
    opens be.jensberckmoes.insightfx.controller to javafx.fxml;

    opens be.jensberckmoes.insightfx.model to com.opencsv;

    exports be.jensberckmoes.insightfx;
    exports be.jensberckmoes.insightfx.controller;
    exports be.jensberckmoes.insightfx.model;
    opens be.jensberckmoes.insightfx.converter to com.opencsv;
    exports be.jensberckmoes.insightfx.service;
    opens be.jensberckmoes.insightfx.service to com.opencsv;
}
