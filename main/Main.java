package main;

import db.Marketplace;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter DB Username");
        String user = sc.next();
        System.out.println("Enter DB Password");
        String pass = sc.next();
        Marketplace p = new Marketplace(user, pass);

        while(true) {
            System.out.println("\n1. List products in inventory");
            System.out.println("2. Create new product");
            System.out.println("3. Modify product inventory");
            System.out.println("4. Delete product");
            System.out.println("5. Most popular products");
            System.out.println("6. Least popular products");
            System.out.println("7. Inactive users");
            System.out.println("0. Quit");
            System.out.print("Enter your choice: ");

            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();

            switch (choice) {
                case 1 -> p.listProducts();
                case 2 -> p.createProduct();
                case 3 -> p.modifyProductQuantity();
                case 4 -> p.deleteProduct();
                case 5 -> p.listMostPopularProducts();
                case 6 -> p.listLeastPopularProducts();
                case 7 -> p.listInactiveUsers();
                case 0 -> {
                    System.out.println("Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }
}
