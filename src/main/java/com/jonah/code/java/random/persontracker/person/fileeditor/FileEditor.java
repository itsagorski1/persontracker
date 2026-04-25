package com.jonah.code.java.random.persontracker.person.fileeditor;

import java.io.FileWriter;
import java.io.IOException;

public class FileEditor {
    public FileEditor(String fileName,String type,String content) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(content);
            System.out.println(content+" written to " + fileName);
        } catch (IOException e) {
            System.err.println("Error writing to file: "e.getMessage());
        }
    }
}