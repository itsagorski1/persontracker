package com.jonah.code.java.random.persontracker.person.relationship.divorce;

import com.jonah.code.java.random.persontracker.person.Person;
import java.util.Scanner;
import com.jonah.code.java.random.persontracker.person.fileeditor.FileEditor;

public class Divorce {
    public static Scanner sc = new Scanner(System.in);
    public static void main(String[] args) {
        System.out.println("Divorce initalized.");
        Person person1 = new Person();
        Person person2 = new Person();
        person1.person.replace("divorced","false","true");
        person2.person.replace("divorced","false","true");
        person1.person.replace("married","true","false");
        person2.person.replace("married","true","false");
        System.out.print("When were you divorced (year only, pls)?\n>> ");
        person1.person.put("yr_divorced",sc.nextLine().trim());
        person2.person.put("yr_divorced",person1.person.get("yr_divorced"));
        System.out.println("Divorce finalized.");
        String[] toPass = {person1.person.get("name"), person2.person.get("name")};
        FileEditor fe = new FileEditor("upd",toPass,"div p1 "+person1.person.get("name")+" div p2 "+person2.person.get("name")+" yr div "+person1.person.get("yr_divorced"));
    }
}
