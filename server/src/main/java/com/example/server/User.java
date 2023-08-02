package com.example.server;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    // public User(String firstName, String lastName, String email, String password) {
    //     this.firstName = firstName;
    //     this.lastName = lastName;
    //     this.email = email;
    //     this.password = password;
    // }


    @OneToMany(cascade = CascadeType.ALL)
    private List<UserFile> files = new ArrayList<>();

    public List<UserFile> getFiles() {
        return files;
    }

    public void addFile(UserFile file) {
        files.add(file);
        file.setOwner(this);
    }

    public void removeFile(UserFile file) {
        files.remove(file);
        file.setOwner(null);
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
