package Model;

import Entities.AnotherEntity;
import Entities.WorkEntity;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeTrackingApp extends Application {
    private static Connection connection;
    private static TableView<WorkEntity> tableView;
    private ContextMenu currentContextMenu = null;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Учет рабочего времени");

        Button addEmpl = new Button("Добавить сотрудника");
        addEmpl.setOnAction(event -> showAddEmplDialog());

        tableView = new TableView<>();
        tableView.setEditable(true);

        root.setTop(addEmpl);
        root.setCenter(tableView);

        TableColumn<WorkEntity, LocalDate> dateColumn = new TableColumn<>("Дата");
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        TableColumn<WorkEntity, String> startColumn = new TableColumn<>("Начало");
        startColumn.setCellValueFactory(cellData -> cellData.getValue().startTimeProperty());

        TableColumn<WorkEntity, String> endColumn = new TableColumn<>("Конец");
        endColumn.setCellValueFactory(cellData -> cellData.getValue().endTimeProperty());

        TableColumn<WorkEntity, String> totalColumn = new TableColumn<>("Всего");
        totalColumn.setCellValueFactory(cellData -> cellData.getValue().totalProperty());

        tableView.getColumns().add(dateColumn);
        tableView.getColumns().add(startColumn);
        tableView.getColumns().add(endColumn);
        tableView.getColumns().add(totalColumn);

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:work_time.db");
            loadDataFromDB();
            createTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        root.setCenter(tableView);

        setupContextMenu();
        // Отображение сцены
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static void loadDataFromDB() throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM work_entries")) {
            while (resultSet.next()) {
                String dateString = resultSet.getString("date");
                if (dateString != null) {
                    LocalDate date = LocalDate.parse(dateString, formatter);
                    String startTime = resultSet.getString("start_time");
                    String endTime = resultSet.getString("end_time");
                    String total = resultSet.getString("total");
                    for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                        System.out.print(resultSet.getMetaData().getColumnName(i) + ": " + resultSet.getString(i) + ", ");
                    }
                    System.out.println();
                    tableView.getItems().add(new WorkEntity(date, startTime, endTime, total));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Печать или логирование исключения
        }
    }

    private void showAddEmplDialog() {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setTitle("Добавить сотрудника");

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

        // Список для хранения сообщений об ошибках
        final String[] errorMessages1 = new String[1];
        final String[] errorMessages2 = new String[1];

        // Обработчик ввода для полей начала и конца
        startTimeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")){
                startTimeField.setText(newValue);
                errorMessages1[0] = "";
            }
            else{
                errorMessages1[0] = "Invalid Start Time Format";
            }
        });

        endTimeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")){
                endTimeField.setText(newValue);
                errorMessages2[0] = "";
            }
            else{
                errorMessages2[0] = "Invalid End Time Format";
            }
        });

        gridPane.add(new Label("Выберите дату:"), 0, 0);
        gridPane.add(datePicker, 1, 0);
        gridPane.add(new Label("Начало:"), 0, 1);
        gridPane.add(startTimeField, 1, 1);
        gridPane.add(datePicker1,2,1);
        gridPane.add(new Label("Конец:"), 0, 2);
        gridPane.add(endTimeField, 1, 2);
        gridPane.add(datePicker2,2,2);

        Label errorLabel = new Label();
        Label errorLabel0 = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel0.setTextFill(Color.RED);

        gridPane.add(errorLabel, 1, 3);
        gridPane.add(errorLabel0, 1, 4);

        Button addButton = new Button("Добавить");
        addButton.setOnAction(event -> {
            LocalDate date = datePicker.getValue(); LocalDate date1 = datePicker1.getValue();
            LocalDate date2 = datePicker2.getValue();
            if (date == null) {
                errorLabel.setText("Invalid Data");
                return;
            }

            if(errorMessages1[0] == null || errorMessages2[0] == null){
                if(errorMessages1[0] == null){
                    errorLabel.setText("Invalid Start Time format");
                }
                if(errorMessages2[0] == null){
                    errorLabel0.setText("Invalid End Time format");
                }
                return;
            }

            if(!errorMessages1[0].isEmpty() || !errorMessages2[0].isEmpty()){
                errorLabel.setText(errorMessages1[0]); // Отображаем первое сообщение об ошибке
                errorLabel0.setText(errorMessages2[0]); // 2
                return;
            }

            String startTime = startTimeField.getText();
            String endTime = endTimeField.getText();

            String total = calculate(date1,startTime,date2,endTime);
            WorkEntity newWorkEntity = new WorkEntity(date, startTime, endTime, total);
            AnotherEntity anotherEntity = new AnotherEntity(date,date1,date2, startTime,endTime,total);

            tableView.getItems().add(newWorkEntity);
            try {
                insertDataIntoDB(anotherEntity);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            dialogStage.close();
        });

        gridPane.add(addButton, 0, 4, 2, 1);

        Scene dialogScene = new Scene(gridPane, 300, 200);
        dialogStage.setScene(dialogScene);
        dialogStage.show();
    }
    private String calculate(LocalDate date1, String startTime, LocalDate date2, String endTime){
        long startUnix = date1.toEpochDay() * 86400 + LocalTime.parse(startTime).toSecondOfDay();
        long endUnix = date2.toEpochDay() * 86400 + LocalTime.parse(endTime).toSecondOfDay();

        float rawData = Math.abs(endUnix - startUnix);

        int obj2 = (int) (Float.parseFloat("0."+String.valueOf(rawData/3600).split("\\.")[1]) * 60);

        String rawDataStr = String.valueOf(obj2);

        if (obj2 >= 0 && obj2 <= 9) {
            rawDataStr = "0"+rawDataStr;
        }

        return (int) Math.floor(rawData / 3600) + ":" + rawDataStr;
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
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            rowMenu.getItems().add(deleteItem);

            // Удалять предыдущее контекстное меню перед открытием нового
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
    }
    private void deleteDataFromDB(WorkEntity workEntity) throws SQLException {
        String sql = "DELETE FROM work_entries WHERE date = ? AND start_time = ? AND end_time = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, workEntity.getDate().toString());
            preparedStatement.setString(2, workEntity.getStartTime());
            preparedStatement.setString(3, workEntity.getEndTime());
            preparedStatement.executeUpdate();
        }
    }
    private void insertDataIntoDB(AnotherEntity workEntity) throws SQLException {
        String sql = "INSERT INTO work_entries (date, date1, date2, start_time, end_time, total) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            // Проверяем, что дата не null, перед вызовом toString()
            if(AnotherEntity.getDate() != null){
                preparedStatement.setString(1,AnotherEntity.getDate().toString());
            }
            else{
                preparedStatement.setString(1,null);
            }
            if (AnotherEntity.getDate1() != null) {
                preparedStatement.setString(2, AnotherEntity.getDate1().toString());
            } else {
                preparedStatement.setString(2, null);
            }
            if(AnotherEntity.getDate2() != null){
                preparedStatement.setString(3, AnotherEntity.getDate2().toString());
            }
            else{
                preparedStatement.setString(3,null);
            }
            preparedStatement.setString(4, workEntity.getStartTime());
            preparedStatement.setString(5, workEntity.getEndTime());
            preparedStatement.setString(6,workEntity.getTotal());
            preparedStatement.executeUpdate();
        }
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
                "total TEXT)";
        statement.executeUpdate(sql);
    }

    public static void main(String[] args) throws SQLException {
        launch(args);
        loadDataFromDB();
    }
}