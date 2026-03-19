module OrarAsistent {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;

    requires java.desktop;

    // Jackson (ObjectMapper, JsonNode etc.)
    requires com.fasterxml.jackson.databind;

    exports ro.ut.utassistant;
}