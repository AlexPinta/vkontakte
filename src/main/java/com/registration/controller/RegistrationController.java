package com.registration.controller;

import com.registration.core.User;
import com.registration.core.UserCredentialDetails;
import com.registration.dao.UserCredentialDetailsRepository;
import com.registration.dao.UserRepository;
import com.registration.task.VkStreaming;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
        import java.util.Map;


@RestController
public class RegistrationController {
    public final String CLIENT_REGISTRATION = "client";
    public final String SERVER_REGISTRATION = "serverCode";
    public final Integer DEFAULT_EXPIRED = 86400;
    @Value("${custom.domain}") String cookieDomain;
    @Value("${custom.clientID}") String clientId;
    @Value("${custom.redirectUrl}") String redirectUrl;

    @Autowired private UserCredentialDetailsRepository credentialDetailsRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private VkStreaming streaming;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView main(HttpServletResponse response) {
        addCookie(response, "client_Id", clientId, DEFAULT_EXPIRED, cookieDomain);
        addCookie(response, "redirect_URI", redirectUrl, DEFAULT_EXPIRED, cookieDomain);
        return new ModelAndView("index.html");
    }

    @RequestMapping(value = "/authorization", method = RequestMethod.GET)
    public ModelAndView registration(@RequestParam(value = "code", required = false) String tempCode,
                                     @RequestParam(value = "state", required = false) String state) {
        ModelAndView view = new ModelAndView();
        if (SERVER_REGISTRATION.equals(state)) {
            streaming.setServerTemporaryCode(tempCode);
            view.setViewName("index.html");
            view.addObject("status", "Success");
        } else {
            view.setViewName("groupList.html");
        }
        return view;
    }

    @RequestMapping(value = "/authorization", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getUserToken(HttpServletResponse response,
                                               @CookieValue(name = "access_token", required = false) String existedToken,
                                               @RequestBody(required = false) Map<String, String> credentials) {
        //TODO need to rewrite on hibernate validator
        if (!CLIENT_REGISTRATION.equals(credentials.get("state")))
            return new ResponseEntity<>("Incorrect credentials", HttpStatus.BAD_REQUEST);

        try {
            Long userId = Long.parseLong(credentials.get("user_id"));
            String token = credentials.get("access_token");
            Integer expired = Integer.parseInt(credentials.get("expires_in"));

            //set cookies
            if (existedToken == null || !token.equals(existedToken)) {
                addCookie(response, "access_token", token, expired, cookieDomain);
            }
            User user = userRepository.findByUserId(userId);
            if (user == null) {
                user = new User();
                user.setUserId(userId);
                userRepository.save(user);
            }
            UserCredentialDetails userTemporaryRecord = new UserCredentialDetails();
            userTemporaryRecord.setId(userId);
            userTemporaryRecord.setExpired(expired);
            userTemporaryRecord.setToken(token);
            userTemporaryRecord.setUser(user);
            credentialDetailsRepository.save(userTemporaryRecord);
            return new ResponseEntity<>("Authorization success", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Incorrect credentials", HttpStatus.BAD_REQUEST);
        }
    }

    private void addCookie(HttpServletResponse response, String cookie, String value, Integer expired, String domain) {
        Cookie token_cookie = new Cookie(cookie, value);
        token_cookie.setMaxAge(expired);
        token_cookie.setDomain(domain);
        response.addCookie(token_cookie);
    }
}
