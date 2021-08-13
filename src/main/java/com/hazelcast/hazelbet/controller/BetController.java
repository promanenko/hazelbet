package com.hazelcast.hazelbet.controller;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.hazelbet.controller.model.Bet;
import com.hazelcast.hazelbet.controller.model.SubmitBetResponse;
import com.hazelcast.hazelbet.service.model.ProcessedBet;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.hazelcast.hazelbet.utils.HzDistributedObjectNames.INPUT_BETS_IMAP;
import static com.hazelcast.hazelbet.utils.HzDistributedObjectNames.PROCESSED_BETS_IMAP;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/bets")
public class BetController {

    // for now allow only one active user
    private static final Long USER_ID = 1L;
    private final HazelcastInstance hazelcast;

    @PostMapping
    public ProcessedBet submitBet(@RequestBody Bet inputBet) {
        String betId = UUID.randomUUID().toString();
        inputBet.setId(betId);
        inputBet.setUserId(USER_ID);
        hazelcast.getMap(INPUT_BETS_IMAP).put(betId, inputBet);
        nap(); // TODO use non-blocking
        return hazelcast.<String, ProcessedBet>getMap(PROCESSED_BETS_IMAP).get(betId);
    }

    @GetMapping
    public List<ProcessedBet> listBets() {
        return hazelcast.<String, ProcessedBet>getMap(PROCESSED_BETS_IMAP)
                .values(entry -> USER_ID.equals(entry.getValue().getUserId()))
                .stream()
                .sorted(Comparator.comparing(ProcessedBet::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @GetMapping(path = "/{betId}")
    public ProcessedBet getBet(@PathVariable String betId) {
        return hazelcast.<String, ProcessedBet>getMap(PROCESSED_BETS_IMAP).get(betId);
    }

    private void nap() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
