package com.client.fescaro_client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientApplication extends Application implements EventHandler<ActionEvent> {

    Socket socket;
    TextArea textArea;
    int port = 9876;
    Button connectionButton;
    Button sendButton;
    TextField IPText;
    TextField portText;
    TextField userName;
    TextField input;

    public static void main(String[] args) {
        launch();
    }

    //클라이언트 프로그램 동작 메서드
    public void startClient(String IP, int port) {
        Thread thread = new Thread(() -> {
            try {
                System.out.println("소켓 연결을 시도합니다.");
                socket = new Socket(IP, port);
                System.out.println(socket);
                receive();
            } catch (Exception e) {
                if (socket == null){
                    stopClient();
                    Platform.runLater(() ->
                            textArea.appendText("[서버 연결 실패]\n"));
                    connectionButton.setText("접속하기");
                    input.setDisable(true);
                    sendButton.setDisable(true);

                } else if (!socket.isConnected()) {
                    System.out.println("[서버 접속 실패]");
                    stopClient();
                    Platform.exit();
                }
            }
        });
        thread.start();
    }

    //클라이언트 프로그램 종료 메서드
    public void stopClient() {
        try {
            if (socket != null && !socket.isClosed()) {
                System.out.println("클라이언트에서 접속을 종료합니다.");
                OutputStream out = socket.getOutputStream();
                byte[] buffer = ("exit").getBytes(StandardCharsets.UTF_8);
                out.write(buffer);
                out.flush();
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 서버로부터 메세지를 전달받는 메서드
    public void receive() {
        while (true) {
            try {
                InputStream in = socket.getInputStream();
                byte[] buffer = new byte[512];
                int length = in.read(buffer);
                if (length == -1) throw new IOException();
                String message = new String(buffer, 0, length, StandardCharsets.UTF_8);

                if(message.equals("server close")){
                    Platform.runLater(() -> textArea.appendText("서버가 중지됩니다."));
                    connectionButton.setText("접속하기");
                    stopClient();
                    Platform.runLater(() ->
                            textArea.appendText("[채팅방 퇴장]\n"));
                    input.setDisable(true);
                    sendButton.setDisable(true);
                }

                Platform.runLater(() -> textArea.appendText(message));

            } catch (Exception e) {
                stopClient();
                break;
            }
        }
    }

    //서버로부터 메세지를 전송하는 메서드
    public void send(String message) {
        Thread thread = new Thread(() -> {
            try {
                System.out.println("서버로 메세지를 전송합니다 : "+message);
                OutputStream out = socket.getOutputStream();
                byte[] buffer = message.getBytes(StandardCharsets.UTF_8);
                out.write(buffer);
                out.flush();
            } catch (Exception e) {
                stopClient();
            }
        });
        thread.start();
    }

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(5));

        HBox hBox = new HBox();
        hBox.setSpacing(10);

        userName = new TextField();
        userName.setPrefWidth(150);
        userName.setPromptText("이름을 입력하세요");
        HBox.setHgrow(userName, Priority.ALWAYS);

        IPText = new TextField("127.0.0.1");
        portText = new TextField("9876");
        portText.setPrefWidth(80);

        hBox.getChildren().addAll(userName, IPText, portText);
        root.setTop(hBox);

        textArea = new TextArea();
        textArea.setEditable(false);
        root.setCenter(textArea);

        input = new TextField();
        input.setPrefWidth(Double.MAX_VALUE);
        input.setDisable(true);

        input.setOnAction(this);

        sendButton = new Button("전송");
        sendButton.setDisable(true);

        sendButton.setOnAction(this);

        connectionButton = new Button("접속하기");
        connectionButton.setOnAction(this);

        BorderPane pane = new BorderPane();
        pane.setLeft(connectionButton);
        pane.setCenter(input);
        pane.setRight(sendButton);

        root.setBottom(pane);
        Scene scene = new Scene(root, 800, 400);
        stage.setTitle("[채팅 클라이언트]");
        stage.setOnCloseRequest(windowEvent -> stopClient());
        stage.setScene(scene);

        stage.show();

        connectionButton.requestFocus();
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        if (actionEvent.getSource() == connectionButton) {
            if (connectionButton.getText().equals("접속하기")) {
                try {
                    port = Integer.parseInt(portText.getText());
                } catch (Exception e) {
                    System.out.println("접속 실패");
                    e.printStackTrace();
                }
                startClient(IPText.getText(), port);
                Platform.runLater(() ->
                        textArea.appendText("[채팅방 접속]\n"));
                connectionButton.setText("종료하기");
                input.setDisable(false);
                sendButton.setDisable(false);
                input.requestFocus();
            } else {
                stopClient();
                Platform.runLater(() ->
                        textArea.appendText("[채팅방 퇴장]\n"));
                connectionButton.setText("접속하기");
                input.setDisable(true);
                sendButton.setDisable(true);
            }
        }

        if (actionEvent.getSource() == sendButton) {
            send(userName.getText() + ": " + input.getText() + "\n");
            input.setText("");
            input.requestFocus();
        }

        if (actionEvent.getSource() == input) {
            send(userName.getText() + ": " + input.getText() + "\n");
            input.setText("");
            input.requestFocus();
        }
    }
}