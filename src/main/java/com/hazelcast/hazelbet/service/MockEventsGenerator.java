package com.hazelcast.hazelbet.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.hazelbet.controller.model.Bet;
import com.hazelcast.hazelbet.controller.model.Match;
import com.hazelcast.hazelbet.controller.model.MatchOutcome;
import com.hazelcast.map.IMap;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static com.hazelcast.hazelbet.utils.HzDistributedObjectNames.INPUT_BETS_IMAP;
import static com.hazelcast.hazelbet.utils.HzDistributedObjectNames.MATCHES_IMAP;

@Component
@RequiredArgsConstructor
public class MockEventsGenerator {

    private final HazelcastInstance hazelcast;

    @Scheduled(initialDelay = 1000, fixedDelay = 20000)
    public void updateMatchScores() {
        IMap<Long, Match> matches = hazelcast.getMap(MATCHES_IMAP);
        long matchId = ThreadLocalRandom.current().nextLong(1, 9);
        boolean first = ThreadLocalRandom.current().nextBoolean();
        matches.computeIfPresent(matchId, (key, match) -> first
                ? match.toBuilder().firstScored(match.getFirstScored() + 1).build()
                : match.toBuilder().secondScored(match.getSecondScored() + 1).build());
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 1000)
    public void mockBets() {
        IMap<Long, Match> matches = hazelcast.getMap(MATCHES_IMAP);

        long matchId = ThreadLocalRandom.current().nextLong(1, 9);
        MatchOutcome outcome = MatchOutcome.values()[ThreadLocalRandom.current().nextInt(0, 3)];
        Match match = matches.get(matchId);
        double amount = ThreadLocalRandom.current().nextInt(0, 10) == 0 ? 200 : 10;
        double coefficient;
        switch (outcome) {
            case WIN_1:
                coefficient = match.getWinFirst();
                break;
            case DRAW:
                coefficient = match.getDraw();
                break;
            case WIN_2:
                coefficient = match.getWinSecond();
                break;
            default:
                coefficient = 0;
        }
        String betId = UUID.randomUUID().toString();
        hazelcast.getMap(INPUT_BETS_IMAP).put(betId, Bet.builder()
                .id(betId)
                .userId(ThreadLocalRandom.current().nextLong(2, 5))
                .matchId(matchId)
                .amount(amount)
                .outcome(outcome)
                .coefficient(coefficient)
                .build()
        );
    }
}
