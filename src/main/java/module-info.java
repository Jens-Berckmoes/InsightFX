module be.jensberckmoes.insightfx {
    requires javafx.controls;
    requires javafx.fxml;


    opens be.jensberckmoes.insightfx to javafx.fxml;
    exports be.jensberckmoes.insightfx;
}