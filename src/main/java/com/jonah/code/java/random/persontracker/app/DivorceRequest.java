package com.jonah.code.java.random.persontracker.app;

public class DivorceRequest {
    private String person1Id;
    private String person2Id;
    private String year;

    public String getPerson1Id() {
        return person1Id;
    }

    public void setPerson1Id(String person1Id) {
        this.person1Id = person1Id;
    }

    public String getPerson2Id() {
        return person2Id;
    }

    public void setPerson2Id(String person2Id) {
        this.person2Id = person2Id;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
