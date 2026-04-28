package com.jonah.code.java.random.persontracker.person.personfileeditor;

import com.jonah.code.java.random.persontracker.person.fileeditor.FileEditor;
import com.jonah.code.java.random.persontracker.person.Person;

public class PersonFileEditor {
    public static void main(String[] args) {
        Person person = new Person();
        person.populate();
        FileEditor fe = new FileEditor("new",person.person.get("name"),"{\"name\":\""person.person.get("name")+"\", \"phone\":"+person.person.get("phone")+"\", \"address\":"+person.person.get("address"));
    }
}