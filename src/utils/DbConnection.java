package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import application.Environment;
import application.EnvironmentProduction;

/**
 * This class is mainly used for jdbc connection using mysql jdbc driver
 * @author anilk
 *
 */
public class DbConnection {
    private static DbConnection instance;
    public Connection connection;

    /**
     * private constructor of DbConnection
     * which call this.getConnetion() method which sets connection of type Connection
     */
    private DbConnection() {
        this.getConnection();
    }

    /**
     * Returns a instance of DbConnection
     * If a instance is already present
     * it returns that instance
     * 
     * @return instance of DbConnection
     */
    public static DbConnection getInstance() {
        if (instance == null) {
            instance = new DbConnection();
        }
        return instance;
    }

    /**
     * This method set DbConnection.connection of Type Connection 
     * By using mysql jdbc driver
     * 
     */
    public void getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
       
            connection = DriverManager.getConnection(Environment.connectionString,Environment.username, Environment.password);
           
            System.out.println("Connected database");
        } catch (SQLException | ClassNotFoundException e1) {
            System.out.println(e1);
            System.out.println("error connecting database");
        } catch (Exception e) {
            System.out.println("Exception" + e);
        }
    }

}
