package com.jonah.code.java.random.persontracker.person.personfileeditor;

import com.jonah.code.java.random.persontracker.person.fileeditor.FileEditor;
import com.jonah.code.java.random.persontracker.person.Person;

public class PersonFileEditor {
    public static void main(String[] args) {
        Person p1 = new Person();
        p1.populate();
        String content = "{\"name\":\"" + p1.person.get("name")
                + "\",\"phone\":\"" + p1.person.get("phone")
                + "\",\"address\":\"" + p1.person.get("address") + "\"}";
        String[] people = {p1.person.get("name")};
        FileEditor fe = new FileEditor("new", people, content);
    }
}
