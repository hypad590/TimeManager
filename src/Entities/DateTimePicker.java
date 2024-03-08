package Entities;

import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class DateTimePicker extends HBox {
    private final DatePicker datePicker;
    private final TextField timeField;

    public DateTimePicker() {
        datePicker = new DatePicker();
        timeField = new TextField();

        this.getChildren().addAll(datePicker, timeField);
    }

    public DatePicker getDatePicker() {
        return datePicker;
    }

    public TextField getTimeField() {
        return timeField;
    }

    public String getDateTime() {
        String date = datePicker.getValue().toString();
        String time = timeField.getText();
        return date + " " + time;
    }
}
