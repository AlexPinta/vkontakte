package com.registration.controller;

import com.registration.core.Group;
import com.registration.core.TopicComment;
import com.registration.core.UserCredentialDetails;
import com.registration.dao.GroupRepository;
import com.registration.dao.TopicCommentRepository;
import com.registration.dao.UserCredentialDetailsRepository;
import com.registration.dao.UserRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/group")
public class GroupController {
    public final String TOKEN_KEY = "access_token";
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserCredentialDetailsRepository credentialDetailsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    TopicCommentRepository commentRepository;

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public ResponseEntity<String> main(HttpServletRequest request,
                                       @RequestParam("group") String groupName, @RequestParam("link") String groupLink,
                                       @RequestParam("groupId") Long groupId) {
        Group group = groupRepository.findByGroupId(groupId);
        if (group == null) {
            group = new Group();
            group.setGroupId(groupId);
            group.setName(groupName);
            group.setLink(groupLink);
            groupRepository.save(group);
        }
        final UserCredentialDetails details = credentialDetailsRepository.findByToken(getTokenFromCookie(request));
        details.getUser().setGroup(group);
        userRepository.save(details.getUser());

        return new ResponseEntity<String>(HttpStatus.OK);
    }

    @RequestMapping(value = "/getData", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Iterable<TopicComment>> getData() {
        return new ResponseEntity<Iterable<TopicComment>>(commentRepository.findAll(), HttpStatus.OK);
    }

    private String getTokenFromCookie(HttpServletRequest request) {
        String tokenValue = null;
        Cookie[] cookies = request.getCookies();
        for (int i = 0; i < cookies.length; i++) {
            if(TOKEN_KEY.equals(cookies[i].getName())) {
                tokenValue = cookies[i].getValue();
                break;
            }
        }
        return tokenValue;
    }
}
