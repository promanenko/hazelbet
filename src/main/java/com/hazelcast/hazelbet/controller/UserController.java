package com.hazelcast.hazelbet.controller;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.hazelbet.controller.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.hazelcast.hazelbet.utils.HzDistributedObjectNames.USERS_IMAP;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/users")
public class UserController {

    @Autowired
    private HazelcastInstance hazelcast;

    @GetMapping(path = "/{userId}")
    public User getUser(@PathVariable Long userId) {
        return hazelcast.<Long, User>getMap(USERS_IMAP).get(userId);
    }

}
