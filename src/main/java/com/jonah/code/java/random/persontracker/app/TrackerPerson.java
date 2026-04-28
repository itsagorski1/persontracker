package com.jonah.code.java.random.persontracker.app;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TrackerPerson {
    private String id;
    private String name;
    private String address;
    private String phone;
    private String gender;
    private Integer age;
    private boolean married;
    private boolean divorced;
    private boolean single;
    private String spouseId;
    private String yrDivorced;
    private List<String> history;

    public TrackerPerson() {
        this.id = UUID.randomUUID().toString();
        this.history = new ArrayList<>();
        this.single = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public boolean isMarried() {
        return married;
    }

    public void setMarried(boolean married) {
        this.married = married;
    }

    public boolean isDivorced() {
        return divorced;
    }

    public void setDivorced(boolean divorced) {
        this.divorced = divorced;
    }

    public boolean isSingle() {
        return single;
    }

    public void setSingle(boolean single) {
        this.single = single;
    }

    public String getSpouseId() {
        return spouseId;
    }

    public void setSpouseId(String spouseId) {
        this.spouseId = spouseId;
    }

    public String getYrDivorced() {
        return yrDivorced;
    }

    public void setYrDivorced(String yrDivorced) {
        this.yrDivorced = yrDivorced;
    }

    public List<String> getHistory() {
        if (history == null) {
            history = new ArrayList<>();
        }
        return history;
    }

    public void setHistory(List<String> history) {
        this.history = history == null ? new ArrayList<>() : history;
    }
}
