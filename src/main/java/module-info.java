module be.jensberckmoes.insightfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.opencsv;
    requires static lombok;


    opens be.jensberckmoes.insightfx to javafx.fxml;
    exports be.jensberckmoes.insightfx;
    exports be.jensberckmoes.insightfx.controller;
    opens be.jensberckmoes.insightfx.controller to javafx.fxml;
    exports be.jensberckmoes.insightfx.model to com.opencsv;
}