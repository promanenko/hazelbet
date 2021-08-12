package com.hazelcast.hazelbet.controller;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.hazelbet.controller.model.Match;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static com.hazelcast.hazelbet.utils.HzDistributedObjectNames.MATCHES_IMAP;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/matches")
public class MatchController {

    private final HazelcastInstance hazelcast;

    @GetMapping
    public List<Match> listMatches() {
        return hazelcast.<Long, Match>getMap(MATCHES_IMAP).values().stream()
                .sorted()
                .collect(Collectors.toList());
    }

}
