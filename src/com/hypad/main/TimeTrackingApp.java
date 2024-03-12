package com.hypad.main;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Translate;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public class TimeTrackingApp extends Application {
    private static final Logger logger = Logger.getLogger(TimeTrackingApp.class.getName());
    private static Connection connection;
    private static TableView<WorkEntity> tableView;
    private ContextMenu currentContextMenu = null;
    private static Label totalLabel;
    private static DatePicker datePicker99;
    private static Stage primaryStage;
    private static LocalDate selectedDate;

    public static void main(String[] args) throws SQLException {
        launch(args);
        loadDataFromDB();
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        GridPane root = new GridPane();
        Scene scene = new Scene(root, 750, 500);
        primaryStage.setTitle("Учет рабочего времени");

        Button addEmpl = new Button("Добавить");
        addEmpl.setOnAction(event -> showAddEmplDialog());
        addEmpl.setMinWidth(100);

        Button archiveButton = new Button("Архив");
        archiveButton.setOnAction(event -> showArchiveDialog());
        archiveButton.setMinWidth(100);

        Button resetBtn = new Button("Вернуться");
        resetBtn.setOnAction(event -> {
            try {
                reset();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        resetBtn.setMinWidth(100);

        VBox btnsBox = new VBox(addEmpl, archiveButton, resetBtn);
        btnsBox.setSpacing(20);
        btnsBox.setPadding(new Insets(10));

        root.add(btnsBox, 0, 0);

        tableView = new TableView<>();
        tableView.setEditable(true);

        GridPane.setColumnSpan(tableView, GridPane.REMAINING);
        GridPane.setMargin(tableView, new Insets(10));

        totalLabel = new Label();
        root.add(totalLabel, 1, 1);
        GridPane.setHalignment(totalLabel, HPos.RIGHT);
        GridPane.setMargin(totalLabel, new Insets(10));


        TableColumn<WorkEntity, LocalDate> dateColumn = new TableColumn<>("Дата");
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        dateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                }
            }
        });
        TableColumn<WorkEntity, String> startColumn = new TableColumn<>("Начало");
        startColumn.setCellValueFactory(cellData -> cellData.getValue().startTimeProperty());

        TableColumn<WorkEntity, String> endColumn = new TableColumn<>("Конец");
        endColumn.setCellValueFactory(cellData -> cellData.getValue().endTimeProperty());

        TableColumn<WorkEntity, String> totalColumn = new TableColumn<>("Всего");
        totalColumn.setCellValueFactory(cellData -> cellData.getValue().totalProperty());

        TableColumn<WorkEntity, String> pathColumn = new TableColumn<>("Маршрут");
        pathColumn.setCellValueFactory(cellData -> cellData.getValue().pathProperty());

        TableColumn<WorkEntity, String> exitColumn = new TableColumn<>("Выход");
        exitColumn.setCellValueFactory(cellData -> cellData.getValue().exitProperty());

        tableView.getColumns().add(dateColumn);
        tableView.getColumns().add(startColumn);
        tableView.getColumns().add(endColumn);
        tableView.getColumns().add(totalColumn);
        tableView.getColumns().add(pathColumn);
        tableView.getColumns().add(exitColumn);

        double width = 100.0; // Примерная ширина каждого столбца
        dateColumn.setPrefWidth(width);
        startColumn.setPrefWidth(width);
        endColumn.setPrefWidth(width);
        totalColumn.setPrefWidth(width);
        pathColumn.setPrefWidth(width);
        exitColumn.setPrefWidth(width);

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:work_time.db");
            createTable();
            loadDataFromDB();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        root.add(tableView, 1, 0);

        setupContextMenu();
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            logger.info("App closed");
        });
        logger.info("App started");
    }

    private static void loadDataFromDB() throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM work_entries")) {
            while (resultSet.next()) {
                String dateString = resultSet.getString("date");
                if (dateString != null) {
                    LocalDate date = LocalDate.parse(dateString, formatter);
                    String startTime = resultSet.getString("start_time");
                    String endTime = resultSet.getString("end_time");
                    String total = resultSet.getString("total");
                    String path = resultSet.getString("path");
                    String exit = resultSet.getString("exit");
                    tableView.getItems().add(new WorkEntity(date, startTime, endTime, total, path, exit));
                    logger.info("Data loaded");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        totalLabel.setText(totalSum());
    }

    private void showAddEmplDialog() {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setTitle("Добавить");

        dialogStage.setWidth(525);
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        DatePicker datePicker = new DatePicker();
        DatePicker datePicker1 = new DatePicker();
        DatePicker datePicker2 = new DatePicker();

        TextField startTimeField = new TextField();
        TextField endTimeField = new TextField();
        TextField pathField = new TextField();
        TextField exitField = new TextField();

        final String[] errorMessages1 = new String[1];
        final String[] errorMessages2 = new String[1];

        startTimeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")){
                startTimeField.setText(newValue);
                errorMessages1[0] = "";
            }
            else{
                errorMessages1[0] = "Неверный формат для поля Начало";
            }
        });

        endTimeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")){
                endTimeField.setText(newValue);
                errorMessages2[0] = "";
            }
            else{
                errorMessages2[0] = "Неверный формат для поля Конец";
            }
        });

        gridPane.add(new Label("Дата"), 0, 0);
        gridPane.add(datePicker, 1, 0);
        gridPane.add(new Label("Начало"), 0, 3);
        gridPane.add(startTimeField, 1, 3);
        gridPane.add(datePicker1,2,3);
        gridPane.add(new Label("Конец"), 0, 4);
        gridPane.add(endTimeField, 1, 4);
        gridPane.add(datePicker2,2,4);
        gridPane.add(new Label("Маршрут"), 0,1);
        gridPane.add(pathField, 1,1);
        gridPane.add(new Label("Выход"),0,2);
        gridPane.add(exitField, 1, 2);

        Label errorLabel = new Label();
        Label errorLabel0 = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel0.setTextFill(Color.RED);

        gridPane.add(errorLabel, 1, 5);
        gridPane.add(errorLabel0, 1, 6);

        Button addButton = new Button("Добавить");
        addButton.setOnAction(event -> {
            LocalDate date = datePicker.getValue(); LocalDate date1 = datePicker1.getValue();
            LocalDate date2 = datePicker2.getValue();
            if (date == null) {
                errorLabel.setText("Неверные данные");
                return;
            }

            if(errorMessages1[0] == null || errorMessages2[0] == null){
                if(errorMessages1[0] == null){
                    errorLabel.setText("Неверный формат для поля Начало");
                }
                if(errorMessages2[0] == null){
                    errorLabel0.setText("Неверный формат для поля Конец");
                }
                return;
            }

            if(!errorMessages1[0].isEmpty() || !errorMessages2[0].isEmpty()){
                errorLabel.setText(errorMessages1[0]);
                errorLabel0.setText(errorMessages2[0]);
                return;
            }

            String startTime = startTimeField.getText();
            String endTime = endTimeField.getText();
            String path = pathField.getText();
            String exit = exitField.getText();

            String total = calculate(date1,startTime,date2,endTime);
            WorkEntity newWorkEntity = new WorkEntity(date, startTime, endTime, total, path,exit);
            AnotherEntity anotherEntity = new AnotherEntity(date,date1,date2, startTime,endTime,total, path, exit);

            tableView.getItems().add(newWorkEntity);
            try {
                insertDataIntoDB(anotherEntity);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            totalLabel.setText(totalSum());
            dialogStage.close();
        });

        addButton.getTransforms().add(new Translate(105,0));
        GridPane.setRowIndex(addButton,7);
        GridPane.setColumnIndex(addButton,2);
        gridPane.getChildren().add(addButton);

        Scene dialogScene = new Scene(gridPane, 300, 320);
        dialogStage.setScene(dialogScene);
        dialogStage.show();
        logger.info("DialogShowed");
    }
    private static String calculate(LocalDate date1, String startTime, LocalDate date2, String endTime){
        long startUnix = date1.atTime(LocalTime.parse(startTime)).toEpochSecond(ZoneOffset.UTC);
        long endUnix = date2.atTime(LocalTime.parse(endTime)).toEpochSecond(ZoneOffset.UTC);

        long rawData = Math.abs(endUnix - startUnix);

        long hours = rawData / 3600;
        long minutes = (rawData % 3600) / 60;

        String hoursStr = String.valueOf(hours);
        String minutesStr = String.valueOf(minutes);

        if (hours < 10) {
            hoursStr = "0" + hoursStr;
        }
        if (minutes < 10) {
            minutesStr = "0" + minutesStr;
        }

        return hoursStr + ":" + minutesStr;
    }
    private void setupContextMenu() {
        tableView.setRowFactory(tableView -> {
            final TableRow<WorkEntity> row = new TableRow<>();
            final ContextMenu rowMenu = new ContextMenu();
            final MenuItem deleteItem = new MenuItem("Удалить");

            deleteItem.setOnAction(event -> {
                WorkEntity selectedItem = row.getItem();
                if (selectedItem != null) {
                    try {
                        deleteDataFromDB(selectedItem);
                        tableView.getItems().remove(selectedItem);
                        totalLabel.setText(totalSum());
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            rowMenu.getItems().add(deleteItem);

            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    if (currentContextMenu != null) {
                        currentContextMenu.hide();
                    }
                    rowMenu.show(tableView, event.getScreenX(), event.getScreenY());
                    currentContextMenu = rowMenu;
                }
            });

            return row;
        });

        tableView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (currentContextMenu != null && currentContextMenu.isShowing()) {
                    currentContextMenu.hide();
                    currentContextMenu = null;
                }
            }
        });
    }
    private void deleteDataFromDB(WorkEntity workEntity) throws SQLException {
        String sql = "DELETE FROM work_entries WHERE date = ? AND start_time = ? AND end_time = ? AND path = ? AND exit = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, workEntity.getFormattedDate());
            preparedStatement.setString(2, workEntity.getStartTime());
            preparedStatement.setString(3, workEntity.getEndTime());
            preparedStatement.setString(4,workEntity.getPath());
            preparedStatement.setString(5,workEntity.getExit());
            preparedStatement.executeUpdate();
        }
        logger.info("Deleted");
    }
    private void insertDataIntoDB(AnotherEntity workEntity) throws SQLException {
        String sql = "INSERT INTO work_entries (date, date1, date2, start_time, end_time, total, path, exit) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, AnotherEntity.getFormattedDate());
            preparedStatement.setString(2, AnotherEntity.getDate1() != null ? AnotherEntity.getDate1().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : null);
            preparedStatement.setString(3, AnotherEntity.getDate2() != null ? AnotherEntity.getDate2().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : null);
            preparedStatement.setString(4, workEntity.getStartTime());
            preparedStatement.setString(5, workEntity.getEndTime());
            preparedStatement.setString(6,workEntity.getTotal());
            preparedStatement.setString(7,workEntity.getPath());
            preparedStatement.setString(8,workEntity.getExit());
            preparedStatement.executeUpdate();
        }
        logger.info("Inserted");
    }
    private void reset() throws SQLException {
        tableView.getItems().clear();
        loadDataFromDB();
    }

    private void createTable() throws SQLException {
        Statement statement = connection.createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS work_entries (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "date TEXT," +
                "date1 TEXT," +
                "date2 TEXT," +
                "start_time TEXT," +
                "end_time TEXT,"+
                "total TEXT,"+
                "path TEXT,"+
                "exit TEXT)";
        statement.executeUpdate(sql);
        logger.info("Table created");
    }
    private static void loadDataFromArchive(String sel){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM work_entries")) {
            tableView.getItems().clear();
            while (resultSet.next()) {
                String dateString = resultSet.getString("date");
                if (dateString != null) {
                    LocalDate date = LocalDate.parse(dateString, formatter);
                    String[] s = String.valueOf(date).split("-");
                    String[] s2 = String.valueOf(sel).split("-");

                    String startTime = resultSet.getString("start_time");
                    String endTime = resultSet.getString("end_time");
                    String total = resultSet.getString("total");
                    String path = resultSet.getString("path");
                    String exit = resultSet.getString("exit");


                    if((s[0] + s[1]).equals(s2[0] + s2[1])){
                        tableView.getItems().add(new WorkEntity(date, startTime, endTime, total, path, exit));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        totalLabel.setText(totalSum());
    }
    private void showArchiveDialog() {
        Stage dialogStage = new Stage();
        dialogStage.initOwner(primaryStage);
        dialogStage.setTitle("");

        DatePicker monthPicker = new DatePicker();

        // Устанавливаем отступы вокруг поля выбора даты
        HBox.setMargin(monthPicker, new Insets(10));

        HBox hbox = new HBox(monthPicker);
        hbox.setPadding(new Insets(10));

        Button selectButton = new Button("Выбрать");
        selectButton.setOnAction(event -> {
            LocalDate selectedDate = LocalDate.from(monthPicker.getValue());
            if (selectedDate != null) {
                loadDataFromArchive(String.valueOf(selectedDate));
            }
            dialogStage.close();
        });

        // Устанавливаем отступы вокруг кнопки
        HBox.setMargin(selectButton, new Insets(10));

        hbox.getChildren().add(selectButton);

        Scene dialogScene = new Scene(hbox, 300, 150);
        dialogStage.setScene(dialogScene);
        dialogStage.show();
    }
    private static String totalSum() {
        Duration totalDuration = Duration.ZERO;

        for (WorkEntity workEntity : tableView.getItems()) {
            String startTime = workEntity.getStartTime();
            String endTime = workEntity.getEndTime();

            LocalDate date1 = workEntity.getDate();
            LocalDate date2 = workEntity.getDate();

            LocalTime start = LocalTime.parse(startTime);
            LocalTime end = LocalTime.parse(endTime);

            LocalDateTime startDateTime = LocalDateTime.of(date1, start);
            LocalDateTime endDateTime = LocalDateTime.of(date2, end);

            if (start.isAfter(end)) {
                endDateTime = endDateTime.plusDays(1);
            }

            Duration duration = Duration.between(startDateTime, endDateTime);

            totalDuration = totalDuration.plus(duration);
        }

        long totalHours = totalDuration.toHours();
        long remainingMinutes = totalDuration.toMinutesPart();

        return String.format("Всего за месяц: %02d:%02d", totalHours, remainingMinutes);
    }
}