package com.hazelcast.hazelbet.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.function.FunctionEx;
import com.hazelcast.hazelbet.controller.model.Bet;
import com.hazelcast.hazelbet.controller.model.Match;
import com.hazelcast.hazelbet.controller.model.User;
import com.hazelcast.hazelbet.service.model.ProcessedBet;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.Sources;
import com.hazelcast.map.IMap;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import static com.hazelcast.hazelbet.controller.model.MatchOutcome.WIN_1;
import static com.hazelcast.jet.pipeline.JournalInitialPosition.START_FROM_OLDEST;

@Component
@RequiredArgsConstructor
public class Initializer implements Serializable {

    private final HazelcastInstance hazelcast;

    @PostConstruct
    public void setUp() {
        initMatches();
        initUsers();
        initSuspendedMatches();
        streamInputBets();
        logProcessedBets();
    }

    private void initUsers() {
        IMap<Long, User> users = hazelcast.getMap("users");
        users.put(1L, new User(1L, "hazelcast_user", 500));
        users.put(2L, new User(2L, "dummy_user_2", 500_000));
        users.put(3L, new User(3L, "dummy_user_3", 500_000));
        users.put(4L, new User(4L, "dummy_user_4", 500_000));
        users.put(5L, new User(5L, "dummy_user_5", 500_000));
    }

    private void initSuspendedMatches() {
        IMap<Long, Long> suspendedMatches = hazelcast.getMap("suspendedMatches");
        suspendedMatches.put(1L, 1L);
    }

    private void initMatches() {
        IMap<Long, Match> matches = hazelcast.getMap("matches");
        matches.put(1L, Match.builder().id(1L).firstTeam("Barcelona").secondTeam("Madrid").winFirst(2.1).draw(2.2).winSecond(1.7).build());
        matches.put(2L, Match.builder().id(2L).firstTeam("Dynamo").secondTeam("Zorya").winFirst(1.5).draw(2.7).winSecond(3.3).build());
        matches.put(3L, Match.builder().id(3L).firstTeam("Man Utd").secondTeam("Arsenal").winFirst(1.6).draw(2.4).winSecond(2.8).build());
        matches.put(4L, Match.builder().id(4L).firstTeam("Liverpool").secondTeam("Tottenham").winFirst(1.5).draw(2.7).winSecond(3.3).build());
        matches.put(5L, Match.builder().id(5L).firstTeam("Bayern").secondTeam("Dortmund").winFirst(1.45).draw(2.0).winSecond(3.5).build());
        matches.put(6L, Match.builder().id(6L).firstTeam("Milan").secondTeam("Juventus").winFirst(2.1).draw(2.2).winSecond(1.7).build());
        matches.put(7L, Match.builder().id(7L).firstTeam("Minaj").secondTeam("Man City").winFirst(7.1).draw(5.8).winSecond(1.1).build());
        matches.put(8L, Match.builder().id(8L).firstTeam("Fenerbahce").secondTeam("Besiktas").winFirst(1.9).draw(1.9).winSecond(1.9).build());
    }

    @Scheduled(fixedDelay = 1000)
    public void mockBets() {
        hazelcast.getMap("inputBets").put(UUID.randomUUID().toString(), Bet.builder()
                .userId(1L)
                .matchId(1)
                .amount(100)
                .coefficient(1.2)
                .outcome(WIN_1)
                .build()
        );
    }

    private void streamInputBets() {
        Pipeline pipeline = Pipeline.create();
        pipeline.readFrom(Sources.<String, Bet>mapJournal("inputBets", START_FROM_OLDEST))
                .withIngestionTimestamps()
                .map(stringBetEntry -> {
                    Bet bet = stringBetEntry.getValue();
                    return ProcessedBet.builder()
                            .id(stringBetEntry.getKey())
                            .amount(bet.getAmount())
                            .userId(bet.getUserId())
                            .matchId(bet.getMatchId())
                            .coefficient(bet.getCoefficient())
                            .outcome(bet.getOutcome())
                            .build();
                }).setName("assignBetId")
                .mapUsingIMap("users", ProcessedBet::getUserId, (ProcessedBet processedBet, User user) -> {
                    if (processedBet.getAmount() > user.getBalance()) {
                        processedBet.setRejected(true);
                        processedBet.setReason("No money");
                    }
                    return processedBet;
                }).setName("checkUserBalance")
                .mapUsingIMap("suspendedMatches", ProcessedBet::getMatchId, (ProcessedBet processedBet, Long match) -> {
                    if (match != null) {
                        processedBet.setRejected(true);
                        processedBet.setReason("Match is suspended");
                    }
                    return processedBet;
                }).setName("checkSuspendedMatches")
                .writeTo(Sinks.map("processedBets", ProcessedBet::getId, FunctionEx.identity()));
        hazelcast.getJet().newJob(pipeline, new JobConfig().setName("processBets"));
    }

    private void logProcessedBets() {
        Pipeline pipeline = Pipeline.create();
        pipeline.readFrom(Sources.<String, Bet>mapJournal("processedBets", START_FROM_OLDEST))
                .withIngestionTimestamps()
                .map(Map.Entry::getValue)
                .writeTo(Sinks.logger());
        hazelcast.getJet().newJob(pipeline, new JobConfig().setName("logProcessedBets"));
    }

}
