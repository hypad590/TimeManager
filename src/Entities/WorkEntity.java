package Entities;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;

public class WorkEntity {
    private final SimpleObjectProperty<LocalDate> date;
    private final SimpleStringProperty startTime;
    private final SimpleStringProperty endTime;
    private final SimpleStringProperty total;
    private final SimpleStringProperty exit;
    private final SimpleStringProperty path;

    public WorkEntity(
            LocalDate date, String startTime,String endTime,String total,String path, String exit
    ) {
        this.date = new SimpleObjectProperty<>(date);
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
    public LocalDate getDate() {
        return date.get();
    }

    public SimpleObjectProperty<LocalDate> dateProperty() {
        return date;
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
}
