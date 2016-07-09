package com.registration.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.registration.core.Group;
import com.registration.core.TopicComment;
import com.registration.dao.GroupRepository;
import com.registration.dao.TopicCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigInteger;
import java.sql.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
@Configurable
public class VkStreaming {
    public enum RESPONSE_STRUCTURE {response, topics, comments, items};
    public final String TOKEN_KEY = "access_token";
    public final String GET_ALL_GROUP_TOPICS = "https://api.vk.com/method/board.getTopics?group_id=%d&access_token=%s";
    public final String GET_ALL_TOPIC_COMMENTS = "https://api.vk.com/method/board.getComments?group_id=%d" +
            "&topic_id=%s&start_comment_id=%d&access_token=%s&v=5.52";
    public final String GET_SERVER_CODE = "https://oauth.vk.com/access_token?client_id=5538646&client_secret=6I04Vp18QEGvjUaLfOPE" +
            "&redirect_uri=http://www.ancestry.com:8080/authorization&code=%s";

    @Autowired
    GroupRepository groupRepository;
    @Autowired
    TopicCommentRepository commentRepository;

    private RestTemplate restTemplate = new RestTemplate();
    private String serverTemporaryCode;
    private String serverCode;
    private Map<Group, Map<Long, Long>> lastComment = new HashMap<>();

    @Scheduled(cron="* 5 * * * ?")
    private void getStream() {
        if (serverTemporaryCode == null) return;
        if (isAuthorize()) {
            List<Group> actualGroups = groupRepository.getActualGroups();
            actualGroups.stream().forEach(new Consumer<Group>() {
                @Override @Async
                public void accept(final Group group) {
                    updateLastComment(group);
                    String query = String.format(GET_ALL_GROUP_TOPICS, group.getGroupId(), serverCode);
                    final ResponseEntity<String> responseTopics = restTemplate.getForEntity(query, String.class);
                    final Map<String, Object> groupTopics = convertJSON(responseTopics.getBody());
                    if (!groupTopics.isEmpty() && groupTopics.containsKey(RESPONSE_STRUCTURE.response.toString())) {
                        Map<String, Object> response = ((Map<String, Object>) groupTopics.get(RESPONSE_STRUCTURE.response.toString()));
                        ((List)response.get(RESPONSE_STRUCTURE.topics.toString())).forEach(new Consumer() {
                            @Override
                            public void accept(Object topic) {
                                if (topic instanceof Map) {
                                    getTopicComments(group, (LinkedHashMap)topic);
                                }
                            }
                        });
                        }
                    }
            });
        }
    }
    @Async
    private void getTopicComments(final Group group, final LinkedHashMap topicRecord) {
        final Long topicId = ((Integer)((LinkedHashMap) topicRecord).get("tid")).longValue();
        String query = String.format(GET_ALL_TOPIC_COMMENTS, group.getGroupId(),
                ((LinkedHashMap) topicRecord).get("tid"), getLastComment(group, topicId), serverCode);
        final ResponseEntity<String> responseComments = restTemplate.getForEntity(query, String.class);
        final Map<String, Object> topicComments = convertJSON(responseComments.getBody());

        if (!topicComments.isEmpty() && topicComments.containsKey(RESPONSE_STRUCTURE.response.toString())) {
            Map response = (Map) topicComments.get(RESPONSE_STRUCTURE.response.toString());
            List comments = ((List) response.get(RESPONSE_STRUCTURE.items.toString()));

            comments.forEach(new Consumer() {
                @Override
                public void accept(Object comment) {
                    if (comment instanceof Map) {
                        Map commentRecord = ((LinkedHashMap) comment);
                        TopicComment topicComment = new TopicComment();
                        topicComment.setGroup(group);
                        topicComment.setId(((Integer)commentRecord.get("id")).longValue());
                        topicComment.setTopicId(topicId);
                        topicComment.setText((String) commentRecord.get("text"));
                        topicComment.setDate(new Date(((Integer)commentRecord.get("date")).longValue()));
                        commentRepository.save(topicComment);
                    }

                }
            });
        }
    }

    private boolean isAuthorize() {
        boolean status = false;
        if (serverCode == null) {
            String query = String.format(GET_SERVER_CODE, serverTemporaryCode);
            final ResponseEntity<String> serverToken = restTemplate.getForEntity(query, String.class);
            if (serverToken.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> map = convertJSON(serverToken.getBody());
                if (map != null) {
                    serverCode = (String)map.get(TOKEN_KEY);
                    status = true;
                }
            }
        } else {
            status = true;
        }
        return status;
    }

    public void setServerTemporaryCode(String serverTemporaryCode) {
        this.serverTemporaryCode = serverTemporaryCode;
    }

    private Map<String, Object> convertJSON(String json) {
        Map<String, Object> map = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            map = mapper.readValue(json, HashMap.class);
        } catch (Exception e) {
            System.out.println("Can\'t retrieve token");
        }
        return map;
    }

    private Long getLastComment(Group group, Long topicId) {
        Long value = 0l;
        Map<Long, Long> commentDetail = lastComment.get(group);
        if (!commentDetail.isEmpty()) {
            value = commentDetail.get(topicId);
        }
        return value;
    }

    private void updateLastComment(Group group) {
        int topicIdPosition = 0;
        int commentIdPosition = 1;
        Map<Long, Long> commentDetail = new HashMap<>();
        for (Object entry : commentRepository.findLastTopicComment(group.getGroupId())) {
            Object[] keyValue = (Object[]) entry;
            commentDetail.put(((BigInteger)keyValue[topicIdPosition]).longValue(),
                    ((BigInteger)keyValue[commentIdPosition]).longValue());
        }
        lastComment.put(group, commentDetail);
    }
}
