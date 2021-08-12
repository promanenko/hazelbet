package com.hazelcast.hazelbet.controller;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.hazelbet.controller.model.Match;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/matches")
public class MatchController {

    @Autowired
    private HazelcastInstance hazelcast;

    @GetMapping
    public List<Match> listMatches() {
        return hazelcast.<Long, Match>getMap("matches").values().stream()
                .sorted()
                .collect(Collectors.toList());
    }

}
