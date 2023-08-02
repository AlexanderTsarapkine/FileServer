package com.example.server;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
public class UserController {

    final String DbFolderPath = "./DbStorage/";

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists.");
        }

        User user = new User();

        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());

        // Create User Directory
        User savedUser = userRepository.save(user);

        String userFolderPath = DbFolderPath + File.separator + savedUser.getId();
        File userFolder = new File(userFolderPath);
        userFolder.mkdir();

        String filesFolderPath = userFolderPath + File.separator + "files";
        File filesSubdirectory = new File(filesFolderPath);
        filesSubdirectory.mkdir();

        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    // remove eventually
    @GetMapping("/users/all")
    public ResponseEntity<List<User>> getUsers() {
        Iterable<User> usersIterable = userRepository.findAll();
        List<User> users = new ArrayList<>();
        for (User user : usersIterable) {
            users.add(user);
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUser(@RequestBody UserDTO userDTO) {
        Optional<User> userOptional = userRepository.findByEmail(userDTO.getEmail());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if(user.getPassword().equals(userDTO.getPassword())) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials.");
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/users")
    public ResponseEntity<?> deleteUser(@RequestBody UserDTO userDTO) {
        if (!userRepository.existsByEmail(userDTO.getEmail())) {
            return ResponseEntity.notFound().build();
        } else {
            Optional<User> userOptional = userRepository.findByEmail(userDTO.getEmail());
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                if(user.getPassword().equals(userDTO.getPassword())) {
                    String userFolderPath = DbFolderPath + File.separator + user.getId();
                    File userFolder = new File(userFolderPath);
                    if (userFolder.exists()) {
                        try {
                            deleteFolder(userFolder);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                        }
                    }
                    userRepository.deleteById(user.getId());
                    return ResponseEntity.ok("User deleted successfully.");
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials.");
                }
            } else {
                return ResponseEntity.notFound().build();
            }
            
        }        
    }

    // @PostMapping("/users/uploadFile")
    // public ResponseEntity<User> uploadFile(@RequestParam("userId") Long userId,
    //                                        @ModelAttribute FileDTO fileDTO) {
    //     // // Fetch the User from the database
    //     // User user = userService.findUserById(userId);
    //     // if (user == null) {
    //     //     return ResponseEntity.notFound().build();
    //     // }
        
    //     // try {
    //     //     // Save the File entity and associate it with the User
    //     //     fileService.saveFile(user, fileDTO);
    //     // } catch (IOException e) {
    //     //     // Handle the exception as needed
    //     //     e.printStackTrace();
    //     //     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    //     // }

    //     // // Save the updated User to the database
    //     // User savedUser = userService.saveUser(user);
    //     // return ResponseEntity.ok(savedUser);
    // }

    private void deleteFolder(File folder) throws IOException {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteFolder(file);
                }
            }
        }
        if (!folder.delete()) {
            throw new IOException("Internal Server Error: Failed to delete folder");
        }
    }
}
