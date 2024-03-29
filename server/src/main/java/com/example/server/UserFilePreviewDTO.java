package com.example.server;

import java.time.LocalDate;

public class UserFilePreviewDTO {

    private long id;
    private String name;
    private String type;
    private LocalDate dateUploaded;
    private byte[] filePreview;
    private long size;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public LocalDate getDateUploaded() {
        return dateUploaded;
    }

    public void setDateUploaded(LocalDate dateUploaded) {
        this.dateUploaded = dateUploaded;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public byte[] getFilePreview() {
        return filePreview;
    }

    public void setFilePreview(byte[] filePreview) {
        this.filePreview = filePreview;
    }

}

