package com.example.server;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String email;
    private String password;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id") // This will create a foreign key column "user_id" in UserFile table
    private List<UserFile> files = new ArrayList<>();

    public List<UserFile> getFiles() {
        return files;
    }

    public void addFile(UserFile file) {
        files.add(file);
    }

    // make this also remove from sql????
    public void removeFile(long id) {
        for (UserFile file : this.getFiles()) {
            if (file.getId() == id) {
                files.remove(file);
            }
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
