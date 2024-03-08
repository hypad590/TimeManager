package Entities;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;

public class AnotherEntity{
    private static SimpleObjectProperty<LocalDate> date1 = null;
    private final SimpleStringProperty startTime;
    private final SimpleStringProperty endTime;
    private final SimpleObjectProperty<LocalDate> date2;

    public AnotherEntity(LocalDate date1, LocalDate date2, String startTime, String endTime) {

        AnotherEntity.date1 = new SimpleObjectProperty<>(date1);
        this.date2 = new SimpleObjectProperty<>(date2);
        this.startTime = new SimpleStringProperty(startTime);
        this.endTime = new SimpleStringProperty(endTime);

    }

    public static LocalDate getDate1() {
        return date1.get();
    }

    public SimpleObjectProperty<LocalDate> date1Property() {
        return date1;
    }

    public String getStartTime() {
        return startTime.get();
    }

    public SimpleStringProperty startTimeProperty() {
        return startTime;
    }

    public String getEndTime() {
        return endTime.get();
    }

    public SimpleStringProperty endTimeProperty() {
        return endTime;
    }

    public LocalDate getDate2() {
        return date2.get();
    }

    public SimpleObjectProperty<LocalDate> date2Property() {
        return date2;
    }

}
