package com.registration.core;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "vk_user")
public class User {
    @Id
    private Long userId;

    @OneToMany(cascade = CascadeType.PERSIST, mappedBy="user", targetEntity = Group.class)
    private Set<Group> groupList;

    public User() {}
    public Set<Group> getGroupList() {
        return groupList;
    }
    public void setGroup(Group group) {
        this.groupList.add(group);
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
