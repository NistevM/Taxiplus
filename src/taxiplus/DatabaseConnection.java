package taxiplus;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
   
    private static final String URL = "jdbc:mysql://localhost:3306/TaxiManager";
    private static final String USER = "root"; 
    private static final String PASSWORD = "123456"; 

    public static Connection getConnection() throws SQLException {
        try {

            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException ex) {
            // Lanza una SQLException si el driver no se encuentra, indicando el problema.
            throw new SQLException("Error al cargar el driver de MySQL", ex);
        }
    }
}