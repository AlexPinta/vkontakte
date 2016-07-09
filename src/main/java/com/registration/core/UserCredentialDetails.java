package com.registration.core;

import javax.persistence.*;

@Entity
@Table(name = "temp_users_credentials")
public class UserCredentialDetails {
    @Id @GeneratedValue private Long id;
    @OneToOne @JoinColumn(name = "userId")
    private User user;
    private String token;
    private Integer expired; //TODO need to correctly define

    public UserCredentialDetails() {}

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public Integer getExpired() {
        return expired;
    }
    public void setExpired(Integer expired) {
        this.expired = expired;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
}
