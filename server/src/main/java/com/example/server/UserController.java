package com.example.server;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import org.imgscalr.Scalr;

@RestController
public class UserController {


    private final String frontendEndpoint = "http://localhost:3000";
    private final String DbFolderPath = "./DbStorage";

    private static final Map<String, String> contentTypeToExtension = new HashMap<>();
    static {
        contentTypeToExtension.put("image/png", ".png");
        contentTypeToExtension.put("image/jpeg", ".jpeg");
        contentTypeToExtension.put("video/quicktime", ".mov");
    }

    @Autowired
    private UserRepository userRepository;

    private final String TOKEN_INFO_ENDPOINT = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=";

    public OAuthUser verifyJwtToken(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
    
        String userInfoUrl = TOKEN_INFO_ENDPOINT + accessToken;
    
        try {
            ResponseEntity<OAuthUser> response = restTemplate.getForEntity(userInfoUrl, OAuthUser.class);
    
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                System.out.println("Request failed with status code: " + response.getStatusCode());
                return null;
            }
        } catch (HttpClientErrorException.Unauthorized unauthorizedException) {
            System.out.println("Token verification failed: Unauthorized");
            return null;
        } catch (Exception e) {
            System.out.println("Error while verifying JWT token: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping("/verify")
    @CrossOrigin(origins = frontendEndpoint)
    public ResponseEntity<?> verifyToken(@RequestBody Map<String, String> requestBody) {
        String accessToken = requestBody.get("access_token");
        if (accessToken == null || accessToken.isBlank()) {
            return ResponseEntity.badRequest().body("Access token is missing or invalid");
        }
    
        OAuthUser payload = verifyJwtToken(accessToken);
    
        if (payload == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token out of date or invalid");
        }
    
        return ResponseEntity.ok(payload);
    }

    // Create User
    @PostMapping("/users")
    @CrossOrigin(origins = frontendEndpoint)
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        OAuthUser oauthUser = verifyJwtToken(userDTO.getToken());
        if(oauthUser == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token Mismatch");
        }

        if (userRepository.existsById(oauthUser.getSub())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists.");
        }

        User user = new User();

        user.setEmail(oauthUser.getEmail());
        user.setId(oauthUser.getSub());

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
    // @GetMapping("/users/all")
    // public ResponseEntity<List<User>> getUsers() {
    //     Iterable<User> usersIterable = userRepository.findAll();
    //     List<User> users = new ArrayList<>();
    //     for (User user : usersIterable) {
    //         users.add(user);
    //     }
    //     return ResponseEntity.ok(users);
    // }

    // Get user and all files associated with them
    // @GetMapping("/users")
    // @CrossOrigin(origins = frontendEndpoint)
    // public ResponseEntity<?> getUser(@RequestBody UserDTO userDTO) {
    //     OAuthUser oauthUser = verifyJwtToken(userDTO.getToken());
    //     if(oauthUser == null) {
    //         return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token Mismatch");
    //     }

    //     Optional<User> userOptional = userRepository.findById(oauthUser.getSub());

    //     if (userOptional.isPresent()) {
    //         User user = userOptional.get();
    //         return ResponseEntity.ok(user);

    //     } else {
    //         return ResponseEntity.notFound().build();
    //     }
    // }

    // Delete user and all files associated with them
    @DeleteMapping("/users")
    @CrossOrigin(origins = frontendEndpoint)
    public ResponseEntity<?> deleteUser(@RequestBody UserDTO userDTO) {
        OAuthUser oauthUser = verifyJwtToken(userDTO.getToken());
        if(oauthUser == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token Mismatch");
        }

        if (!userRepository.existsById(oauthUser.getSub())) {
            return ResponseEntity.notFound().build();
        } else {
            Optional<User> userOptional = userRepository.findById(oauthUser.getSub());
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
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
                return ResponseEntity.notFound().build();
            }
            
        }        
    }

    // Get all previews from a user
    @PostMapping("/users/preview")
    @CrossOrigin(origins = frontendEndpoint)
    public ResponseEntity<?> getUserPreview(@RequestBody UserDTO userDTO) {
        OAuthUser oauthUser = verifyJwtToken(userDTO.getToken());
        if(oauthUser == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token Mismatch");
        }

        if (!userRepository.existsById(oauthUser.getSub())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Does Not Exist");
        }

        Optional<User> userOptional = userRepository.findById(oauthUser.getSub());
        // if (userOptional.isEmpty()) {
        //     return ResponseEntity.notFound().build();
        // }

         User user = userOptional.get();

        List<UserFilePreviewDTO> previewFiles = new ArrayList<>();

        for (UserFile file : user.getFiles()) {
            UserFilePreviewDTO preview = new UserFilePreviewDTO();
            preview.setId(file.getId());
            preview.setName(file.getName());
            preview.setType(file.getType());
            preview.setSize(file.getSize());
            preview.setDateUploaded(file.getDateUploaded());

            try {
                String userPreviewPath = DbFolderPath + 
                                                File.separator + 
                                                user.getId() + 
                                                File.separator + 
                                                "previews" + 
                                                File.separator + 
                                                generateFileName(file.getName(), file.getCount()) + 
                                                ".png";

                preview.setFilePreview(readImageFileAsBytes(userPreviewPath));
                previewFiles.add(preview);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ResponseEntity.ok(previewFiles);
    }

    private byte[] readImageFileAsBytes(String filePath) throws IOException {
        try (InputStream inputStream = new FileInputStream(filePath)) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Get file by file id
    @PostMapping("/users/files/download")
    @CrossOrigin(origins = frontendEndpoint)
    public ResponseEntity<?> getFile(@RequestBody UserDTO userDTO, @RequestParam("id") long fileId) {
        OAuthUser oauthUser = verifyJwtToken(userDTO.getToken());
        if(oauthUser == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token Mismatch");
        }

        if (!userRepository.existsById(oauthUser.getSub())) {
            return ResponseEntity.notFound().build();
        }

        Optional<User> userOptional = userRepository.findById(oauthUser.getSub());
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();

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
                    MediaType mediaType = getMediaTypeFromFile(file);

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
    private MediaType getMediaTypeFromFile(File file) throws IOException {
        Path path = Paths.get(file.toURI());
        String contentType = Files.probeContentType(path);
        return MediaType.parseMediaType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }

    // delete file by file id
    @DeleteMapping("/users/files")
    @CrossOrigin(origins = frontendEndpoint)
    public ResponseEntity<?> deleteFile(@RequestBody UserDTO userDTO, @RequestParam("id") long fileId) {
        OAuthUser oauthUser = verifyJwtToken(userDTO.getToken());
        if(oauthUser == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token Mismatch");
        }

         if (!userRepository.existsById(oauthUser.getSub())) {
            return ResponseEntity.notFound().build();
        }

        Optional<User> userOptional = userRepository.findById(oauthUser.getSub());
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();

        UserFile file = userRepository.findUserFileByUserIdAndFileId(user.getId(), fileId);
         
        if (!userRepository.deleteUserFileByUserIdAndFileId(user.getId(), fileId)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
        }

        String fileNamePrefix = file.getCount() > 0 ? "(" + file.getCount() + ")" : "";
        String fileNameType = contentTypeToExtension.getOrDefault(file.getType(), "");

        String previewFilePath = DbFolderPath + 
                                 File.separator + 
                                 user.getId() + 
                                 File.separator + 
                                 "previews" + 
                                 File.separator + 
                                 fileNamePrefix +
                                 file.getName() + 
                                 ".png";

        String filePath = DbFolderPath + 
                          File.separator + 
                          user.getId() + 
                          File.separator + 
                          "files" + 
                          File.separator + 
                          fileNamePrefix +
                          file.getName() + 
                          fileNameType;

        if (!deleteFile(previewFilePath) || !deleteFile(filePath)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting the file from db ");
        }
        
        return ResponseEntity.ok("File Deleted Successfully");      
    }

    private boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    // upload file
    @PostMapping("/users/files/upload")
    @CrossOrigin(origins = frontendEndpoint)
    public ResponseEntity<?> uploadFile(@RequestParam("token") String token,
                                        @RequestParam("file") MultipartFile file) {
        
        OAuthUser oauthUser = verifyJwtToken(token);
        if(oauthUser == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token Mismatch");
        }

        if (!userRepository.existsById(oauthUser.getSub())) {
            return ResponseEntity.notFound().build();
        } else {
            Optional<User> userOptional = userRepository.findById(oauthUser.getSub());

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                try {
                    String originalFilename = file.getOriginalFilename();
                    String fileNameWithoutExtension = getFileNameWithoutExtension(originalFilename);
                    String fileExtension = getFileExtension(originalFilename);

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

                    String userFolderPath = DbFolderPath + 
                                            File.separator + 
                                            user.getId() + 
                                            File.separator + 
                                            "files" + 
                                            File.separator + 
                                            generateFileName(fileNameWithoutExtension,maxCount+1) + 
                                            fileExtension;

                    saveMultipartFileToLocalDisk(file, userFolderPath);

                    String userPreviewPath = DbFolderPath + 
                                            File.separator + 
                                            user.getId() + 
                                            File.separator + 
                                            "previews" + 
                                            File.separator + 
                                            generateFileName(fileNameWithoutExtension, maxCount + 1) + 
                                            fileExtension;

                    if (file.getContentType().startsWith("image")) {
                        resizeAndSaveImagePreview(userPreviewPath, file);
                    } else if (file.getContentType().startsWith("video")) {
                        extractVideoFrameAndSavePreview(userPreviewPath, file);
                    }

                    return ResponseEntity.ok("File Created.");
                } catch (IOException e) {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        }
    }

    private void resizeAndSaveImagePreview(String outputPath, MultipartFile file) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        
        BufferedImage resizedImage = Scalr.resize(originalImage, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, 192, 108);
        
        Path outputDirectoryPath = Paths.get(outputPath).getParent();
        if (!Files.exists(outputDirectoryPath)) {
            Files.createDirectories(outputDirectoryPath);
        }
        
        String formatName = file.getContentType().split("/")[1];
        
        ImageIO.write(resizedImage, formatName, new File(outputPath));
    }

    private void extractVideoFrameAndSavePreview(String outputPath, MultipartFile file) throws IOException {
        try {
            InputStream inputStream = file.getInputStream();
            FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(inputStream);
            frameGrabber.start();
    
            Frame frame;
            if ((frame = frameGrabber.grabImage()) != null) {
                Java2DFrameConverter converter = new Java2DFrameConverter();
                BufferedImage bufferedImage = converter.convert(frame);
                converter.close();
                frameGrabber.close();

                BufferedImage resizedImage = resizeImage(bufferedImage, 192, 108);

                saveImageToFile(outputPath, resizedImage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        BufferedImage resizedImage = Scalr.resize(originalImage, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, width, height);
        return resizedImage;
    }
    
    private void saveImageToFile(String outputPath, BufferedImage image) {
        try {
            String newOutputPath = outputPath.substring(0, outputPath.lastIndexOf(".")) + ".png";
            File outputFile = new File(newOutputPath);
    
            File outputDirectory = outputFile.getParentFile();
    
            if (!outputDirectory.exists()) {
                if (!outputDirectory.mkdirs()) {
                    throw new IOException("Failed to create the directory");
                }
            }
    
            ImageIO.write(image, "png", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    private void saveMultipartFileToLocalDisk(MultipartFile multipartFile, String destinationPath) throws IOException {
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
