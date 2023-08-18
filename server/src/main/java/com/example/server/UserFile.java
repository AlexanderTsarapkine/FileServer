package com.example.server;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;

@Entity
public class UserFile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private long count;

    private LocalDate dateUploaded;
    private String name;
    private String type;
    private long size;

    public long getCount() {
        return this.count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public UserFile() {
        this.dateUploaded = LocalDate.now();
    }

    public long getId() {
        return this.id;
    }

    public LocalDate getDateUploaded() {
        return this.dateUploaded;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
