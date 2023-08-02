package com.example.server;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

public class UserDTO {
    private String email;
    private String password;

    @OneToMany(cascade = CascadeType.ALL)
    private List<UserFileDTO> files = new ArrayList<>();

    public List<UserFileDTO> getFiles() {
        return files;
    }

    public void addFile(UserFileDTO file) {
        files.add(file);
    }

    public void removeFile(UserFileDTO file) {
        files.remove(file);
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
