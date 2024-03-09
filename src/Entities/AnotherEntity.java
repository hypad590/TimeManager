package Entities;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;

public class AnotherEntity{
    private static SimpleObjectProperty<LocalDate> date1 = null;
    private static SimpleObjectProperty<LocalDate> date = null;
    private final SimpleStringProperty startTime;
    private final SimpleStringProperty endTime;
    private static  SimpleObjectProperty<LocalDate> date2 = null;
    private final SimpleStringProperty total;

    public AnotherEntity(LocalDate date,LocalDate date1, LocalDate date2, String startTime, String endTime, String total) {

        this.date = new SimpleObjectProperty<>(date);
        AnotherEntity.date1 = new SimpleObjectProperty<>(date1);
        AnotherEntity.date2 = new SimpleObjectProperty<>(date2);
        this.startTime = new SimpleStringProperty(startTime);
        this.endTime = new SimpleStringProperty(endTime);
        this.total = new SimpleStringProperty(total);

    }
    public String getTotal() {
        return total.get();
    }

    public SimpleStringProperty totalProperty() {
        return total;
    }

    public void setTotal(String total) {
        this.total.set(total);
    }

    public static LocalDate getDate1() {
        return date1.get();
    }
    public static LocalDate getDate(){return date.get(); }

    public SimpleObjectProperty<LocalDate> date1Property() {
        return date1;
    }
    public SimpleObjectProperty<LocalDate> dateProperty() {return  date;}

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

    public static LocalDate getDate2() {
        return date2.get();
    }

    public SimpleObjectProperty<LocalDate> date2Property() {
        return date2;
    }

}
