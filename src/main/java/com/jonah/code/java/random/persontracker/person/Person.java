package com.jonah.code.java.random.persontracker.person;

import java.util.HashMap;
import java.util.Scanner;

public class Person {
    public HashMap<String, String> person = new HashMap<>();
    public static Scanner sc = new Scanner(System.in);
    public String name() {
        System.out.print("Name, please.\n>> ");
        return sc.nextLine();
    }
    public String address() {
        System.out.print("Address, please.\n>> ");
        return sc.nextLine();
    }
    public String phone() {
        System.out.print("Phone, please.\n>> ");
        int number = 0;
        String string_number = "";
        try {
            if (sc.hasNextInt()) {
                number = sc.nextInt();
                string_number = Integer.toString(number);
            } else {
                throw new Exception();
            }
            if (number <= 2119999999L) {
                System.exit(-1);
            } else if (number >= 9999999999L) {
                System.exit(-1);
            } else {
                if (string_number.substring(2, 5).equals("555")) {
                    System.exit(-1);
                }
            }
        } catch (Exception ValueError) {System.out.println("Uh oh! ur phone # is invalid!");}
        return string_number;
    }
    public void populate() {
        person.put("name", name());
        person.put("address", address());
        person.put("phone", phone());
    }
    public static void main(String[] args) {
        System.out.println("Person initalized.");
    }
}
