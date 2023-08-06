package com.example.server;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StreamUtils;

@RestController
public class UserController {

    final String DbFolderPath = "./DbStorage";

    private static final Map<String, String> contentTypeToExtension = new HashMap<>();
    static {
        contentTypeToExtension.put("image/png", ".png");
        contentTypeToExtension.put("image/jpeg", ".jpeg");
        contentTypeToExtension.put("video/quicktime", ".mov");
        // Add more mappings for other content types as needed
    }

    @Autowired
    private UserRepository userRepository;

    // Create User
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

    // Remove eventually
    @GetMapping("/users/all")
    public ResponseEntity<List<User>> getUsers() {
        Iterable<User> usersIterable = userRepository.findAll();
        List<User> users = new ArrayList<>();
        for (User user : usersIterable) {
            users.add(user);
        }
        return ResponseEntity.ok(users);
    }

    // Get user and all files associated with them
    @GetMapping("/users")
    public ResponseEntity<?> getUser(@RequestBody UserDTO userDTO) {
        Optional<User> userOptional = userRepository.findByEmail(userDTO.getEmail());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if(user.getPassword().equals(userDTO.getPassword())) {
                // maybe return UserDTO?
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials.");
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // NOT DONEEE
    // Get user and all file previews associated with them
    @GetMapping("/users/preview")
    public ResponseEntity<?> getUserPreview(@RequestBody UserDTO userDTO) {
        Optional<User> userOptional = userRepository.findByEmail(userDTO.getEmail());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if(user.getPassword().equals(userDTO.getPassword())) {
                UserDTO res = new UserDTO();
                res.setEmail(user.getEmail());
                res.setPassword(user.getPassword());
                // iterate through all user files and get the file id along with date saved field and save to array

                return ResponseEntity.ok(res);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials.");
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete user and all files associated with them
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

    @GetMapping("/users/files")
    public ResponseEntity<Resource> getFile(@RequestBody UserDTO userDTO, @RequestParam("id") long fileId) {
        if (!userRepository.existsByEmail(userDTO.getEmail())) {
            return ResponseEntity.notFound().build();
        }

        Optional<User> userOptional = userRepository.findByEmail(userDTO.getEmail());
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();
        if (!user.getPassword().equals(userDTO.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ByteArrayResource(new byte[0]));
        }

        Optional<UserFile> fileOptional = user.getFiles().stream()
                .filter(userFile -> userFile.getId() == fileId)
                .findFirst();

        if (fileOptional.isPresent()) {
            UserFile userFile = fileOptional.get();
            String userFolderPath = DbFolderPath + File.separator + user.getId() + File.separator + "files";

            String fileNamePrefix = userFile.getCount() > 0 ? "(" + userFile.getCount() + ")" : "";
            String fileNameType = contentTypeToExtension.getOrDefault(userFile.getType(), "");
            String filePath = userFolderPath + File.separator + fileNamePrefix + userFile.getName() + fileNameType;

            File file = new File(filePath);
            if (file.exists()) {
                try (InputStream inputStream = new FileInputStream(file)) {
       
                    MimeType mimeType = MimeTypeUtils.parseMimeType(userFile.getType());
                    MediaType mediaType = MediaType.parseMediaType(mimeType.toString());

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(mediaType);
                    headers.setContentDispositionFormData("attachment", fileNamePrefix + userFile.getName() + fileNameType);

                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(new ByteArrayResource(StreamUtils.copyToByteArray(inputStream)));
                } catch (IOException e) {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/users/files")
    public ResponseEntity<?> uploadFile(@RequestPart("userDTO") UserDTO userDTO,
                                        @RequestParam("file") MultipartFile file) {
        if (!userRepository.existsByEmail(userDTO.getEmail())) {
            return ResponseEntity.notFound().build();
        } else {
            Optional<User> userOptional = userRepository.findByEmail(userDTO.getEmail());

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                if (user.getPassword().equals(userDTO.getPassword())) {
                    try {
                        String originalFilename = file.getOriginalFilename();
                        String fileNameWithoutExtension = getFileNameWithoutExtension(originalFilename);
                        String fileExtension = getFileExtension(originalFilename);

                        // Find the highest count of files with the same name
                        long maxCount = user.getFiles().stream()
                                .filter(userFile -> userFile.getName().startsWith(fileNameWithoutExtension))
                                .map(UserFile::getCount)
                                .max(Long::compare)
                                .orElse(-1L);

                        UserFile newFile = new UserFile();
                        newFile.setName(fileNameWithoutExtension);
                        newFile.setType(file.getContentType());
                        newFile.setSize(file.getSize());
                        newFile.setCount(maxCount + 1);

                        user.getFiles().add(newFile);
                        userRepository.save(user);

                        String userFolderPath = DbFolderPath + File.separator + user.getId() + File.separator + "files" + File.separator + generateFileName(fileNameWithoutExtension,maxCount+1) + fileExtension;
                        saveMultipartFileToLocalDisk(file, userFolderPath);

                        return ResponseEntity.ok("File Created.");
                    } catch (IOException e) {
                        e.printStackTrace();
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials.");
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        }
    }

    // Helper method to generate the file name with count
    private String generateFileName(String fileNameWithoutExtension, long count) {
        return (count > 0) ? "(" + count + ")" + fileNameWithoutExtension : fileNameWithoutExtension;
    }


    private String getFileNameWithoutExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }

    public void saveMultipartFileToLocalDisk(MultipartFile multipartFile, String destinationPath) throws IOException {
        InputStream inputStream = multipartFile.getInputStream();
        OutputStream outputStream = new FileOutputStream(new File(destinationPath));

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        outputStream.close();
    }

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
