package com.example.server;

import org.springframework.data.repository.CrudRepository;

import org.springframework.data.repository.query.Param;

import java.util.List;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "users", path = "users") // optional
// ^ used to customize REST endpoint, if omitted endpoint would be /websiteUsers instead of /users
    public interface UserRepository extends CrudRepository<User, Long> {

    // List<User> findByName(@Param("name") String name);
}
