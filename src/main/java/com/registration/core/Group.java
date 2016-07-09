package com.registration.core;

import javax.persistence.*;

@Entity
@Table( name = "vk_group")
public class Group {
    @Id
    private Long groupId;

    private String name;
    @Column(nullable = false) private String link;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") //, nullable = false
    private User user;

    public Group() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
