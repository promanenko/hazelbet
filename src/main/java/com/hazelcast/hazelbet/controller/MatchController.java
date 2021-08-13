package com.hazelcast.hazelbet.controller;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.hazelbet.controller.model.Match;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hazelcast.hazelbet.utils.HzDistributedObjectNames.MATCHES_IMAP;
import static com.hazelcast.hazelbet.utils.HzDistributedObjectNames.SUSPENDED_MATCHES_IMAP;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/matches")
public class MatchController {

    private final HazelcastInstance hazelcast;

    @GetMapping
    public List<Match> listMatches() {
        Set<Long> suspendedMatchIds = hazelcast.<Long, Long>getMap(SUSPENDED_MATCHES_IMAP).keySet();
        return hazelcast.<Long, Match>getMap(MATCHES_IMAP).values().stream()
                .sorted()
                .peek(match -> {
                    if (suspendedMatchIds.contains(match.getId())) {
                        match.setSuspended(true);
                    }
                    match.setWinFirst(formatDouble(match.getWinFirst()));
                    match.setDraw(formatDouble(match.getDraw()));
                    match.setWinSecond(formatDouble(match.getWinSecond()));
                })
                .collect(Collectors.toList());
    }

    private double formatDouble(double input) {
        return ((int) (input * 100.0)) / 100.0;
    }

}
