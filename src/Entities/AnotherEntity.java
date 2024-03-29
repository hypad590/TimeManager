package Entities;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AnotherEntity{
    private static SimpleObjectProperty<LocalDate> date1 = null;
    private static SimpleObjectProperty<LocalDate> date = null;
    private final SimpleStringProperty startTime;
    private final SimpleStringProperty endTime;
    private static  SimpleObjectProperty<LocalDate> date2 = null;
    private final SimpleStringProperty total;
    private final SimpleStringProperty exit;
    private final SimpleStringProperty path;

    public AnotherEntity(
            LocalDate date,LocalDate date1, LocalDate date2, String startTime, String endTime, String total,String path,
            String exit) {

        AnotherEntity.date = new SimpleObjectProperty<>(date);
        AnotherEntity.date1 = new SimpleObjectProperty<>(date1);
        AnotherEntity.date2 = new SimpleObjectProperty<>(date2);
        this.startTime = new SimpleStringProperty(startTime);
        this.endTime = new SimpleStringProperty(endTime);
        this.total = new SimpleStringProperty(total);
        this.path = new SimpleStringProperty(path);
        this.exit = new SimpleStringProperty(exit);

    }
    public String getExit() {
        return exit.get();
    }

    public SimpleStringProperty exitProperty() {
        return exit;
    }

    public void setExit(String exit) {
        this.exit.set(exit);
    }
    public String getPath() {
        return path.get();
    }

    public SimpleStringProperty pathProperty() {
        return path;
    }

    public void setPath(String path) {
        this.path.set(path);
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
    public static String getFormattedDate() {
        return getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

}
