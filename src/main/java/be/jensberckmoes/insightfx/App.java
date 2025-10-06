package be.jensberckmoes.insightfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(final Stage stage) throws IOException {
        final FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("main-view.fxml"));
        final Scene scene = new Scene(fxmlLoader.load(), 820, 513);
        stage.setTitle("InsightFX!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(final String[] args) {
        launch();
    }
}