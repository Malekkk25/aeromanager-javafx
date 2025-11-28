package com.example.projet_java_vols;
import  java.sql.Connection;
import  java.sql.DriverManager;
import java.sql.SQLException;

public class ConnexionDB {

    static String url = "jdbc:mysql://localhost:3306/gestionvols?serverTimezone=UTC";
    static String user = "root";
    static String password = ""; // (pour XAMPP typiquement)



    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
