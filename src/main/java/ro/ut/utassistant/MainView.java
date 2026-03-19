package ro.ut.utassistant;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.awt.Desktop;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainView {

    private final BorderPane root = new BorderPane();
    private static final Pattern URL_PATTERN = Pattern.compile("(https?://\\S+)");

    // CHAT (bubbles)
    private final VBox messagesBox = new VBox(10);
    private final ScrollPane chatScroll = new ScrollPane(messagesBox);

    private final TextField input = new TextField();
    private final Button sendBtn = new Button("➤");

    // ======== RICH TEXT: alb normal + link rosu (click) ========
    private Node buildRichText(String text) {
        TextFlow flow = new TextFlow();
        flow.setLineSpacing(2);

        Matcher m = URL_PATTERN.matcher(text);
        int last = 0;

        while (m.find()) {
            // text normal înainte de link (ALB)
            if (m.start() > last) {
                Text normalText = new Text(text.substring(last, m.start()));
                normalText.setStyle("-fx-fill: white;");
                flow.getChildren().add(normalText);
            }

            String url = m.group(1);

            Hyperlink link = new Hyperlink(url);
            link.getStyleClass().add("msg-link");
            link.setOnAction(e -> {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new URI(url));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            flow.getChildren().add(link);
            last = m.end();
        }

        // text normal după ultimul link (ALB)
        if (last < text.length()) {
            Text normalText = new Text(text.substring(last));
            normalText.setStyle("-fx-fill: white;");
            flow.getChildren().add(normalText);
        }

        return flow;
    }

    public MainView(CommandProcessor processor) {

        root.getStyleClass().add("app-bg");

        // ================= TOP =================
        Label title = new Label("-Computational Helper for Academic Data-");
        title.getStyleClass().add("title");

        HBox top = new HBox(title);
        top.getStyleClass().add("header");
        top.setAlignment(Pos.CENTER);
        root.setTop(top);

        // ================= LEFT (MENIU) =================
        VBox menu = new VBox(14);
        menu.getStyleClass().add("card");
        menu.setPadding(new Insets(16));
        menu.setPrefWidth(250);

        Label opt = new Label("OPȚIUNI");
        opt.getStyleClass().add("section-title");

        Button helpOrar = new Button("orar");
        Button helpLoc = new Button("locație");
        Button helpPauze = new Button("pauze");

        for (Button b : new Button[]{helpOrar, helpLoc, helpPauze}) {
            b.getStyleClass().add("btn");
            b.setMaxWidth(Double.MAX_VALUE);
        }

        menu.getChildren().addAll(opt, helpOrar, helpLoc, helpPauze);

        // ================= CENTER (IMAGINE FIXĂ + CHENAR FIX) =================
        Image image = new Image(getClass().getResource("/assistant.png").toExternalForm());

        ImageView assistantImage = new ImageView(image);
        assistantImage.setPreserveRatio(true);
        assistantImage.setSmooth(true);

        // controlezi aici dimensiunea pe ecran
        double MAX_H = 520; // 480 / 520 / 560 (cum vrei)
        assistantImage.setFitHeight(MAX_H);

        StackPane imageWrapper = new StackPane(assistantImage);
        imageWrapper.getStyleClass().add("image-card");
        imageWrapper.setPadding(new Insets(14));
        imageWrapper.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        HBox.setHgrow(imageWrapper, Priority.NEVER);

        // ================= RIGHT (CHAT) =================
        VBox right = new VBox(12);
        right.setPrefWidth(460);

        Label chadTitle = new Label("Întreabă-l pe CHAD – expertul în UTCN info");
        chadTitle.getStyleClass().add("chad-title");

        messagesBox.setPadding(new Insets(10));
        messagesBox.setFillWidth(true);

        chatScroll.setFitToWidth(true);
        chatScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        chatScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        chatScroll.getStyleClass().add("chat-scroll");

        VBox chatCard = new VBox(10, chadTitle, chatScroll);
        chatCard.getStyleClass().add("card");
        VBox.setVgrow(chatScroll, Priority.ALWAYS);

        right.getChildren().add(chatCard);

        // ================= MAIN CONTENT (HBox) =================
        HBox content = new HBox(18, menu, imageWrapper, right);
        content.setPadding(new Insets(14));
        content.setAlignment(Pos.TOP_CENTER);

        // doar chat-ul să ia spațiu rămas dacă e nevoie
        HBox.setHgrow(right, Priority.ALWAYS);

        root.setCenter(content);

        // ================= BOTTOM =================
        input.setPromptText("Scrie ceva...");
        sendBtn.getStyleClass().addAll("btn", "btn-primary");

        HBox bottom = new HBox(10, input, sendBtn);
        bottom.getStyleClass().add("bottom-bar");
        bottom.setPadding(new Insets(12));
        HBox.setHgrow(input, Priority.ALWAYS);
        root.setBottom(bottom);

        // welcome
        addBotMessage("Salut! Scrie grupa și semigrupa: ex. 30212 2\nSau tastează 'help'.");

        // actions
        sendBtn.setOnAction(e -> handleSend(processor));
        input.setOnAction(e -> handleSend(processor));

        helpOrar.setOnAction(e -> quick(processor, "help"));
        helpLoc.setOnAction(e -> quick(processor, "unde e biblioteca"));
        helpPauze.setOnAction(e -> quick(processor, "ce ore am azi"));
    }

    private void handleSend(CommandProcessor processor) {
        String text = input.getText();
        if (text == null || text.isBlank()) return;

        addUserMessage(text);

        String ans;
        try {
            ans = processor.handle(text);
        } catch (Exception ex) {
            ans = "Eroare: " + ex.getMessage();
        }

        addBotMessage(ans);
        input.clear();
        scrollToBottom();
    }

    private void quick(CommandProcessor processor, String cmd) {
        addUserMessage(cmd);
        addBotMessage(processor.handle(cmd));
        scrollToBottom();
    }

    private void addUserMessage(String text) {
        Node content = buildRichText(text);

        VBox bubble = new VBox(content);
        bubble.getStyleClass().add("bubble-user");
        bubble.setMaxWidth(320);

        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER_RIGHT);
        row.setPadding(new Insets(0, 4, 0, 24));
        messagesBox.getChildren().add(row);
    }

    private void addBotMessage(String text) {
        Node content = buildRichText(text);

        VBox bubble = new VBox(content);
        bubble.getStyleClass().add("bubble-bot");
        bubble.setMaxWidth(520);

        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(0, 24, 0, 4));
        messagesBox.getChildren().add(row);
    }

    private void scrollToBottom() {
        javafx.application.Platform.runLater(() -> chatScroll.setVvalue(1.0));
    }

    public Parent getRoot() {
        return root;
    }
}