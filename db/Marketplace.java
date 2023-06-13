package db;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Scanner;

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

    public void listProducts() {
        String query = "SELECT p.ProductID, p.ProductName, p.Price, i.Quantity FROM Products p INNER JOIN Inventory i ON p.ProductID = i.ProductID";

        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()) {
                System.out.println("Product ID: " + rs.getInt("ProductID") + ", Name: " + rs.getString("ProductName") + ", Price: " + rs.getBigDecimal("Price") + ", Quantity in Inventory: " + rs.getInt("Quantity"));
            }
        } catch(SQLException e) {
            System.out.println("Error executing query");
            e.printStackTrace();
        }
    }

    public void createProduct() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the Product Name: ");
        String productName = scanner.nextLine();

        System.out.println("Enter the Product Description: ");
        String productDescription = scanner.nextLine();

        System.out.println("Enter the Product Price: ");
        BigDecimal productPrice = scanner.nextBigDecimal();

        System.out.println("Enter the Product Category: ");
        String productCategory = scanner.next();

        String productQuery = "INSERT INTO Products (ProductName, Description, Price, Category) VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = conn.prepareStatement(productQuery, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, productName);
            preparedStatement.setString(2, productDescription);
            preparedStatement.setBigDecimal(3, productPrice);
            preparedStatement.setString(4, productCategory);

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        System.out.println("Product created successfully. The Product ID is: " + generatedKeys.getInt(1));

                        System.out.println("Enter the quantity for the inventory: ");
                        int quantity = scanner.nextInt();

                        String inventoryQuery = "INSERT INTO Inventory (ProductID, Quantity) VALUES (?, ?)";

                        try (PreparedStatement inventoryStatement = conn.prepareStatement(inventoryQuery)) {
                            inventoryStatement.setInt(1, generatedKeys.getInt(1));
                            inventoryStatement.setInt(2, quantity);

                            inventoryStatement.executeUpdate();

                            System.out.println("Product added to the inventory successfully.");
                        }
                    }
                }
            }
        } catch(SQLException e) {
            System.out.println("Error executing query");
            e.printStackTrace();
        }
    }

    public void modifyProductQuantity() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the Product ID you want to modify: ");
        int productID = scanner.nextInt();

        System.out.println("Enter the new quantity: ");
        int quantity = scanner.nextInt();

        String query = "UPDATE Inventory SET Quantity = ? WHERE ProductID = ?";

        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setInt(1, quantity);
            preparedStatement.setInt(2, productID);

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Quantity updated successfully.");
            } else {
                System.out.println("Product not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error executing query");
            e.printStackTrace();
        }
    }

    public void deleteProduct() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the Product ID of the product you want to delete: ");
        int productID = scanner.nextInt();

        String queryInventory = "DELETE FROM Inventory WHERE ProductID = ?";
        String queryProduct = "DELETE FROM Products WHERE ProductID = ?";

        try (PreparedStatement preparedStatementInventory = conn.prepareStatement(queryInventory);
             PreparedStatement preparedStatementProduct = conn.prepareStatement(queryProduct)) {

            preparedStatementInventory.setInt(1, productID);
            preparedStatementProduct.setInt(1, productID);

            int affectedRowsInventory = preparedStatementInventory.executeUpdate();
            int affectedRowsProduct = preparedStatementProduct.executeUpdate();

            if (affectedRowsProduct > 0) {
                System.out.println("Product deleted successfully from the Products table.");
            } else {
                System.out.println("Product not found in the Products table.");
            }

            if (affectedRowsInventory > 0) {
                System.out.println("Product deleted successfully from the Inventory table.");
            } else {
                System.out.println("Product not found in the Inventory table.");
            }
        } catch (SQLException e) {
            System.out.println("Error executing query");
            e.printStackTrace();
        }
    }

    public void listMostPopularProducts() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the start date (yyyy-mm-dd): ");
        String startDate = scanner.nextLine();

        System.out.println("Enter the end date (yyyy-mm-dd): ");
        String endDate = scanner.nextLine();

        String query = "SELECT p.ProductName, SUM(td.Quantity) as TotalSold " +
                "FROM Products p " +
                "JOIN TransactionDetails td ON p.ProductID = td.ProductID " +
                "JOIN Transactions t ON td.TransactionID = t.TransactionID " +
                "WHERE t.TransactionDate BETWEEN ? AND ? " +
                "GROUP BY p.ProductID " +
                "ORDER BY TotalSold DESC";

        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, startDate);
            preparedStatement.setString(2, endDate);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String productName = resultSet.getString("ProductName");
                int totalSold = resultSet.getInt("TotalSold");

                System.out.printf("Product: %s, Sold: %d\n", productName, totalSold);
            }
        } catch (SQLException e) {
            System.out.println("Error executing query");
            e.printStackTrace();
        }
    }

    public void listLeastPopularProducts() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the start date (YYYY-MM-DD): ");
        String startDate = scanner.nextLine();

        System.out.println("Enter the end date (YYYY-MM-DD): ");
        String endDate = scanner.nextLine();

        String query = "SELECT p.ProductName, SUM(td.Quantity) as TotalSold " +
                "FROM Products p " +
                "JOIN TransactionDetails td ON p.ProductID = td.ProductID " +
                "JOIN Transactions t ON td.TransactionID = t.TransactionID " +
                "WHERE t.TransactionDate BETWEEN ? AND ? " +
                "GROUP BY p.ProductID " +
                "ORDER BY TotalSold ASC";

        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, startDate);
            preparedStatement.setString(2, endDate);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String productName = resultSet.getString("ProductName");
                int totalSold = resultSet.getInt("TotalSold");

                System.out.printf("Product: %s, Sold: %d\n", productName, totalSold);
            }
        } catch (SQLException e) {
            System.out.println("Error executing query");
            e.printStackTrace();
        }
    }

    public void listInactiveUsers() {
        String query = "SELECT u.UserID, u.FirstName, u.LastName, u.Email, p.ProductName, MAX(td.Quantity) as MostPurchasedQuantity " +
                "FROM Users u " +
                "LEFT JOIN Transactions t ON u.UserID = t.UserID " +
                "LEFT JOIN TransactionDetails td ON t.TransactionID = td.TransactionID " +
                "LEFT JOIN Products p ON td.ProductID = p.ProductID " +
                "WHERE t.TransactionDate < DATE_SUB(NOW(), INTERVAL 3 MONTH) OR t.TransactionID IS NULL " +
                "GROUP BY u.UserID, p.ProductID " +
                "ORDER BY MostPurchasedQuantity DESC";

        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int userId = resultSet.getInt("UserID");
                String firstName = resultSet.getString("FirstName");
                String lastName = resultSet.getString("LastName");
                String email = resultSet.getString("Email");
                String productName = resultSet.getString("ProductName");

                System.out.printf("User: %s %s, Email: %s, Product: %s\n", firstName, lastName, email, productName);
            }
        } catch (SQLException e) {
            System.out.println("Error executing query");
            e.printStackTrace();
        }
    }


}



