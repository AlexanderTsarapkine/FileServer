package com.example.server;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, String> {
    boolean existsById(String id);
    Optional<User> findById(String id);

    default UserFile findUserFileByUserIdAndFileId(String userId, long fileId) {
        return findById(userId)
                .map(user -> user.getFiles().stream()
                        .filter(file -> file.getId() == fileId)
                        .findFirst()
                        .orElse(null))
                .orElse(null);
    }

    default boolean deleteUserFileByUserIdAndFileId(String userId, long fileId) {
        User user = findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        UserFile fileToDelete = user.getFiles().stream()
                .filter(file -> file.getId() == fileId)
                .findFirst()
                .orElse(null);

        if (fileToDelete == null) {
            return false;
        }

        user.getFiles().remove(fileToDelete);
        save(user);
        return true;
    }
}
