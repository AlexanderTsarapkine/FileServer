package com.example.server;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

public class UserDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;
    private String profilePicturePath;
    private String accountStatus;

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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

}
