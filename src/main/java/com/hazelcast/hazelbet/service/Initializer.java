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
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.StreamStage;
import com.hazelcast.map.IMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static com.hazelcast.hazelbet.service.CoefficientUtil.*;
import static com.hazelcast.hazelbet.utils.HzDistributedObjectNames.INPUT_BETS_IMAP;
import static com.hazelcast.hazelbet.utils.HzDistributedObjectNames.PROCESSED_BETS_IMAP;
import static com.hazelcast.hazelbet.utils.HzDistributedObjectNames.SUSPENDED_MATCHES_IMAP;
import static com.hazelcast.jet.pipeline.JournalInitialPosition.START_FROM_OLDEST;

@Component
@RequiredArgsConstructor
public class Initializer {

    private final HazelcastInstance hazelcast;

    private final Map<String, Integer> teamStrength;

    @PostConstruct
    public void setUp() {
        initTeamStrength();
        initMatches();
        initUsers();
        initSuspendedMatches();
        streamInputBets();
    }

    private void initTeamStrength() {
        teamStrength.put("Barcelona", 6);
        teamStrength.put("Madrid", 7);
        teamStrength.put("Dynamo", 5);
        teamStrength.put("Zorya", 3);
        teamStrength.put("Man Utd", 7);
        teamStrength.put("Arsenal", 5);
        teamStrength.put("Liverpool", 8);
        teamStrength.put("Tottenham", 6);
        teamStrength.put("Bayern", 9);
        teamStrength.put("Dortmund", 6);
        teamStrength.put("Milan", 6);
        teamStrength.put("Juventus", 7);
        teamStrength.put("Minaj", 2);
        teamStrength.put("Man City", 9);
        teamStrength.put("Fenerbahce", 5);
        teamStrength.put("Besiktas", 5);
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
        IMap<Long, Long> suspendedMatches = hazelcast.getMap(SUSPENDED_MATCHES_IMAP);
        suspendedMatches.put(1L, 1L);
    }

    private void initMatches() {
        putMatch(1L, "Barcelona", "Madrid");
        putMatch(2L, "Dynamo", "Zorya");
        putMatch(3L, "Man Utd", "Arsenal");
        putMatch(4L, "Liverpool", "Tottenham");
        putMatch(5L, "Bayern", "Dortmund");
        putMatch(6L, "Milan", "Juventus");
        putMatch(7L, "Minaj", "Man City");
        putMatch(8L, "Fenerbahce", "Besiktas");
    }

    private void putMatch(long id, String first, String second) {
        List<Double> coefs = calculateCoefficients(teamStrength.get(first) - teamStrength.get(second), 0);
        Match match = Match.builder().id(id).firstTeam(first).secondTeam(second).winFirst(coefs.get(0)).draw(coefs.get(1)).winSecond(coefs.get(2)).build();
        IMap<Long, Match> matches = hazelcast.getMap("matches");
        matches.put(id, match);
    }

    private void streamInputBets() {
        StreamSource<Map.Entry<String, Bet>> inputBetsSource = Sources.mapJournal(INPUT_BETS_IMAP, START_FROM_OLDEST);
        Pipeline pipeline = Pipeline.create();
        StreamStage<Map.Entry<String, Bet>> inputBetsStreamStage = pipeline.readFrom(inputBetsSource)
                .withTimestamps(entry -> entry.getValue().getCreateAt(), 1000);
//        inputBetsStreamStage
//                .map(stringBetEntry -> stringBetEntry.getKey() + " : " + stringBetEntry.getValue())
//                .setName("Stringify");
//                .writeTo(Sinks.logger())
//                .setName("Log input Bets");
        StreamStage<ProcessedBet> processedBetStreamStage = inputBetsStreamStage.map(stringBetEntry -> {
                    Bet bet = stringBetEntry.getValue();
                    return ProcessedBet.builder()
                            .id(stringBetEntry.getKey())
                            .createdAt(bet.getCreateAt())
                            .amount(bet.getAmount())
                            .userId(bet.getUserId())
                            .matchId(bet.getMatchId())
                            .coefficient(bet.getCoefficient())
                            .outcome(bet.getOutcome())
                            .build();
                })
                .setName("Assign processed bet id")
                .mapUsingIMap("users", ProcessedBet::getUserId, (ProcessedBet processedBet, User user) -> {
                    if (processedBet.getAmount() > user.getBalance()) {
                        processedBet.setRejected(true);
                        processedBet.setReason("No money");
                    }
                    return processedBet;
                })
                .setName("Check user balance")
                .mapUsingIMap("suspendedMatches", ProcessedBet::getMatchId, (ProcessedBet processedBet, Long match) -> {
                    if (match != null) {
                        processedBet.setRejected(true);
                        processedBet.setReason("Match is suspended");
                    }
                    return processedBet;
                })
                .setName("Check suspended matches");
//        processedBetStreamStage.writeTo(Sinks.logger())
//                .setName("Log processed bets");

        processedBetStreamStage
//                .filter(ProcessedBet::isRejected).setName("If rejected") // TODO write non-rejected bets when all checks passed
                .writeTo(Sinks.map(PROCESSED_BETS_IMAP, ProcessedBet::getId, FunctionEx.identity()));

        processedBetStreamStage
                .filter(processedBet -> !processedBet.isRejected())
                .groupingKey(ProcessedBet::getMatchId)
                .mapStateful(() -> new double[4], (accumulator, matchId, processedBet) -> {
                    switch (processedBet.getOutcome()) {
                        case WIN_1:
                            accumulator[0] += processedBet.getAmount() * processedBet.getCoefficient();
                            break;
                        case DRAW:
                            accumulator[1] += processedBet.getAmount() * processedBet.getCoefficient();
                            break;
                        case WIN_2:
                            accumulator[2] += processedBet.getAmount() * processedBet.getCoefficient();
                            break;
                        default:
                    }
                    accumulator[3] += processedBet.getAmount();
                    return new CombinedBet(processedBet, accumulator[0], accumulator[1], accumulator[2], accumulator[3]);
                })
//                .mapUsingIMap("matches", (CombinedBet it) -> it.getBet()::getMatchId, (CombinedBet bet, Match match) -> {
//                    MatchOutcome currentOutcome = match.getOutcome();
//                    double[] coefs = new double[3];
//                    switch (currentOutcome) {
//                        case X:
//                            double toPay = bet.getSumX();
//                            double toReceive = bet.getTotal();
//                            if (toPay > toReceive) {
//                                coefs[0] = ;// win1
//                                coefs[1] = match.getDraw() ;// x
//                                coefs[2] =  ;// win2
//                            }
//                            break;
//                    }
//                    return coefs;
//                }) // recalculate new coef

                .writeTo(Sinks.logger());

        hazelcast.getJet().newJob(pipeline, new JobConfig().setName("processBets"));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CombinedBet {
        ProcessedBet bet;
        double sumWin1;
        double sumX;
        double sumWin2;
        double total;
    }

}
