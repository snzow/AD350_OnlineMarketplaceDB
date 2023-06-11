package db;
import java.sql.*;

public class Marketplace{
    private String username;
    private String password;
    private Connection conn;
    public Marketplace(String username, String password){
        this.username = username;
        this.password = password;
        try(Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/",username,password)){

            this.conn = conn;


            String sql = "CREATE DATABASE IF NOT EXISTS OnlineShop";
            Statement st = conn.createStatement();
            st.executeUpdate(sql);
            System.out.println("Db CreatedSuccessfully");


        } catch (SQLException e) {
            System.out.println("Database connection failed with " + e.getMessage());
        }


    }

    public void createTables() {
        try (Connection conn = this.conn){
            Statement createProductsTable = conn.createStatement();
            String productsCreate = "CREATE TABLE Products {"
                    + "ProductID INT PRIMARY KEY, "
                    + "ProductName VarChar(255) NOT NULL, "
                    + "Description VarChar(500), "
                    + "Price DECIMAL(10,2) NOT NULL, "
                    + "Category VARCHAR(50) NOT NULL )";
            createProductsTable.execute(productsCreate);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}



