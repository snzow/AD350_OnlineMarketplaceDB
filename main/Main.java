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


    }
}
