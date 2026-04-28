package com.jonah.code.java.random.persontracker.person.fileeditor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class FileEditor {
    public FileEditor(String type, String[] ppl, String content) {
        String pythonCommand = "python3"; // or "python" depending on your system
        String pythonScript = "'C:\\Users\\jonah\\code\\java\\random\\persontracker\\Epython\\FileEditor.py'";
        ProcessBuilder processBuilder = new ProcessBuilder(pythonCommand, pythonScript, type, Arrays.toString(ppl), content);
        try {
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Python Output: " + line);
                }
            }
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    System.err.println("Python Error: " + line);
                }
            }
            int exitCode = process.waitFor();
            System.out.println("Python process exited with code: " + exitCode);
        } catch (IOException e) {
            System.err.println("Error executing Python command: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Python process was interrupted.");
        }
    }
}