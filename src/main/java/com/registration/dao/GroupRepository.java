package com.registration.dao;

import com.registration.core.Group;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GroupRepository extends CrudRepository<Group, Long> {
    Group findByGroupId(Long id);
    Group save(Group group);

//    @Query(value = "select gr.* from temp_users_credentials as cr INNER JOIN " +
//            "vk_group as gr ON cr.user_id = gr.user_id", nativeQuery = true) TODO need to get data
    @Query(value = "select gr.* from vk_group as gr", nativeQuery = true)
    List<Group> getActualGroups();

}
