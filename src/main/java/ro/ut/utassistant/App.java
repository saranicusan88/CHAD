package ro.ut.utassistant;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        var lessons = JsonScheduleLoader.loadFromResource("/orar.json");
        var scheduleService = new ScheduleService(lessons);

        var rooms = RoomLegendLoader.loadFromResource("/rooms.csv");
        var roomService = new RoomService(rooms);

        var processor = new CommandProcessor(scheduleService, roomService);
        var view = new MainView(processor);

        Scene scene = new Scene(view.getRoot(), 1000, 650);

        scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
        stage.setTitle("UT Assistant - Orar");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}