package com.registration.core;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table( name = "topic_comments")
public class TopicComment {
    @Id private Long id;
    @Temporal(TemporalType.TIMESTAMP) private Date date;
    private Long topicId;

    @OneToOne @JoinColumn(name = "group_id")
    private Group group;
    @Column(columnDefinition = "TEXT") private String text;

    public TopicComment() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
