package com.hypad.main;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try {
            TimeTrackingApp.main(args);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
