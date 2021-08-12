package com.hazelcast.hazelbet.controller;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.hazelbet.controller.model.Bet;
import com.hazelcast.hazelbet.controller.model.SubmitBetResponse;
import com.hazelcast.hazelbet.service.model.ProcessedBet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/bets")
public class BetController {

    @Autowired
    private HazelcastInstance hazelcast;

    @PostMapping
    public SubmitBetResponse submitBet(@RequestBody Bet inputBet) {
        long userId = 1L; // for now allow only one active user
        String betId = UUID.randomUUID().toString();
        inputBet.setId(betId);
        inputBet.setUserId(userId);
        hazelcast.getMap("inputBets").put(betId, inputBet);
        return new SubmitBetResponse(betId);
    }

    @GetMapping
    public List<ProcessedBet> listBets() {
        Long userId = 1L; // for now allow only one active user
        return hazelcast.<Long, ProcessedBet>getMap("processedBets").values().stream()
                .filter(bet -> userId.equals(bet.getUserId()))
                .sorted()
                .collect(Collectors.toList());
    }

    @GetMapping(path = "/{betId}")
    public ProcessedBet getBet(@PathVariable String betId) {
        Long userId = 1L; // for now allow only one active user
        return hazelcast.<String, ProcessedBet>getMap("processedBets").get(betId);
    }

}
