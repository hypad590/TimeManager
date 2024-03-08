package Model;

import Entities.AnotherEntity;
import Entities.WorkEntity;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TimeTrackingApp extends Application {
    private Connection connection;
    private TableView<WorkEntity> tableView;
    private ContextMenu contextMenu;

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

        tableView.getColumns().add(dateColumn);
        tableView.getColumns().add(startColumn);
        tableView.getColumns().add(endColumn);

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

    private void loadDataFromDB() throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM work_entries")) {
            while (resultSet.next()) {
                String dateString = resultSet.getString("date");
                if (dateString != null) {
                    LocalDate date = LocalDate.parse(dateString, formatter);
                    String startTime = resultSet.getString("start_time");
                    String endTime = resultSet.getString("end_time");
                    tableView.getItems().add(new WorkEntity(date, startTime, endTime));
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
        List<String> errorMessages = new ArrayList<>();

        // Обработчик ввода для полей начала и конца
        startTimeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                startTimeField.setText(newValue.replaceAll("[^\\d]", ""));
                errorMessages.add("Invalid Data");
            } else {
                errorMessages.remove("Invalid Data");
            }
        });

        endTimeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                endTimeField.setText(newValue.replaceAll("[^\\d]", ""));
                errorMessages.add("Invalid Data");
            } else {
                errorMessages.remove("Invalid Data");
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
        errorLabel.setTextFill(Color.RED);
        gridPane.add(errorLabel, 1, 3);

        Button addButton = new Button("Добавить");
        addButton.setOnAction(event -> {
            LocalDate date = datePicker.getValue(); LocalDate date1 = datePicker1.getValue();
            if (date == null) {
                errorLabel.setText("Invalid Data");
                return;
            }

            if (!errorMessages.isEmpty()) {
                errorLabel.setText(errorMessages.get(0)); // Отображаем первое сообщение об ошибке
                return;
            }

            String startTime = startTimeField.getText();
            String endTime = endTimeField.getText();

            WorkEntity newWorkEntity = new WorkEntity(date, startTime, endTime);
            AnotherEntity anotherEntity = new AnotherEntity(date,date1, startTime,endTime);

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
    private void setupContextMenu() {
        tableView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                WorkEntity selectedItem = tableView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    // Проверяем, есть ли уже открытое контекстное меню
                    if (contextMenu != null && contextMenu.isShowing()) {
                        contextMenu.hide(); // Закрываем текущее меню
                    }

                    contextMenu = new ContextMenu();

                    MenuItem editItem = new MenuItem("Редактировать");
                    editItem.setOnAction(e -> {
                        // Реализуйте логику редактирования
                    });

                    MenuItem deleteItem = new MenuItem("Удалить");
                    deleteItem.setOnAction(e -> {
                        try {
                            deleteDataFromDB(selectedItem);
                            tableView.getItems().remove(selectedItem);
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    });

                    contextMenu.getItems().addAll(editItem, deleteItem);
                    contextMenu.show(tableView, event.getScreenX(), event.getScreenY());
                }
            }
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
        String sql = "INSERT INTO work_entries (date, start_time, end_time) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            // Проверяем, что дата не null, перед вызовом toString()
            if (AnotherEntity.getDate1() != null) {
                preparedStatement.setString(1, AnotherEntity.getDate1().toString());
            } else {
                preparedStatement.setString(1, null); // или используйте другое значение по умолчанию
            }
            preparedStatement.setString(2, workEntity.getStartTime());
            preparedStatement.setString(3, workEntity.getEndTime());
            preparedStatement.executeUpdate();
        }
    }

    private void createTable() throws SQLException {
        Statement statement = connection.createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS work_entries (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "date TEXT," +
                "start_time TEXT," +
                "end_time TEXT)";
        statement.executeUpdate(sql);
    }

    public static void main(String[] args) {
        launch(args);
    }
}