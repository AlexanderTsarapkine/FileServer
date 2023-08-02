package com.example.server;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;

// maybe in future add file sharing?

@Entity
public class UserFile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private LocalDate dateUploaded;
    private String name;
    private String type;
    private String filePath;
    private String filePreviewPath;
    private String[] tags;
    private String description;
    private long size;

    @ManyToOne
    private User owner;

    // maybe replace some setters (or just call them) in constructor if they arent going to be changed.
    public UserFile() {
        this.dateUploaded = LocalDate.now();
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
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

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePreviewPath() {
        return this.filePreviewPath;
    }

    public void setFilePreviewPath(String filePreviewPath) {
        this.filePreviewPath = filePreviewPath;
    }

    public String[] getTags() {
        return this.tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
