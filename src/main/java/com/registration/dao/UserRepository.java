package com.registration.dao;

import com.registration.core.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByUserId(Long vkId);
    User save(User user);
}
