package com.example.server;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

    default UserFile findUserFileByEmailAndFileId(String email, long fileId) {
        return findByEmail(email)
                .map(user -> user.getFiles().stream()
                        .filter(file -> file.getId() == fileId)
                        .findFirst()
                        .orElse(null))
                .orElse(null);
    }

    default boolean deleteUserFileByEmailAndFileId(String email, long fileId) {
        User user = findByEmail(email).orElse(null);
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
