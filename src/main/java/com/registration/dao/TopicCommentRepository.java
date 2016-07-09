package com.registration.dao;

import com.registration.core.TopicComment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface TopicCommentRepository extends CrudRepository<TopicComment, Long> {
    TopicComment save(TopicComment topicComment);

    @Query(value = "SELECT topic_id, MAX(id) as id FROM topic_comments WHERE group_id = ?1 GROUP BY topic_id", nativeQuery = true)
    List<Object> findLastTopicComment(Long groupId);
}
