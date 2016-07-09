package com.registration.dao;

import com.registration.core.UserCredentialDetails;
import org.springframework.data.repository.CrudRepository;

public interface UserCredentialDetailsRepository extends CrudRepository<UserCredentialDetails, Long> {
    UserCredentialDetails save(UserCredentialDetails credentialDetails);
    UserCredentialDetails findByToken(String token);
}
