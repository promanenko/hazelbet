package com.hazelcast.hazelbet.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.function.FunctionEx;
import com.hazelcast.hazelbet.controller.model.Bet;
import com.hazelcast.hazelbet.controller.model.Match;
import com.hazelcast.hazelbet.controller.model.MatchOutcome;
import com.hazelcast.hazelbet.controller.model.MatchOutcomeTrend;
import com.hazelcast.hazelbet.controller.model.User;
import com.hazelcast.hazelbet.service.model.ProcessedBet;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.datamodel.KeyedWindowResult;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.hazelcast.hazelbet.service.CoefficientUtil.calculateCoefficients;
import static com.hazelcast.hazelbet.service.CoefficientUtil.getProbabilities;
import static com.hazelcast.hazelbet.utils.HzDistributedObjectNames.INPUT_BETS_IMAP;
import static com.hazelcast.hazelbet.utils.HzDistributedObjectNames.LAST_MIN_BETS_SUM;
import static com.hazelcast.hazelbet.utils.HzDistributedObjectNames.MATCHES_IMAP;
import static com.hazelcast.hazelbet.utils.HzDistributedObjectNames.PROCESSED_BETS_IMAP;
import static com.hazelcast.hazelbet.utils.HzDistributedObjectNames.SUSPENDED_MATCHES_IMAP;
import static com.hazelcast.hazelbet.utils.HzDistributedObjectNames.USERS_IMAP;
import static com.hazelcast.jet.Util.entry;
import static com.hazelcast.jet.aggregate.AggregateOperations.summingDouble;
import static com.hazelcast.jet.pipeline.JournalInitialPosition.START_FROM_OLDEST;
import static com.hazelcast.jet.pipeline.WindowDefinition.sliding;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

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
        Integer firstStrength = teamStrength.get(first);
        Integer secondStrength = teamStrength.get(second);
        List<Double> coefs = calculateCoefficients(firstStrength - secondStrength, 0);
        Match match = Match.builder().id(id).firstTeam(first).secondTeam(second)
                .firstStrength(firstStrength).secondStrength(secondStrength)
                .winFirst(coefs.get(0)).draw(coefs.get(1)).winSecond(coefs.get(2))
                .build();
        IMap<Long, Match> matches = hazelcast.getMap("matches");
        matches.put(id, match);
    }

    private void streamInputBets() {
        Pipeline pipeline = Pipeline.create();
        StreamSource<Map.Entry<String, Bet>> inputBetsSource = Sources.mapJournal(INPUT_BETS_IMAP, START_FROM_OLDEST);
        StreamStage<ProcessedBet> processedBetStreamStage = pipeline.readFrom(inputBetsSource)
                .withTimestamps(entry -> entry.getValue().getCreateAt(), 1000)
                .map(stringBetEntry -> {
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

        processedBetStreamStage
//                .filter(ProcessedBet::isRejected).setName("If rejected") // TODO write non-rejected bets when all checks passed
                .writeTo(Sinks.map(PROCESSED_BETS_IMAP, ProcessedBet::getId, FunctionEx.identity()));

        processedBetStreamStage
                .filter(processedBet -> !processedBet.isRejected()).setName("If rejected")
                .writeTo(Sinks.mapWithUpdating(USERS_IMAP, ProcessedBet::getUserId, (User user, ProcessedBet bet) -> {
                    user.setBalance(user.getBalance() - bet.getAmount());
                    return user;
                }));

        processedBetStreamStage
                .filter(processedBet -> !processedBet.isRejected())
                .window(sliding(MINUTES.toMillis(1), SECONDS.toMillis(1)).setEarlyResultsPeriod(1000))
                .groupingKey(ProcessedBet::getMatchId)
                .aggregate(summingDouble(ProcessedBet::getAmount))
                .writeTo(Sinks.map(LAST_MIN_BETS_SUM));
        processedBetStreamStage
                .window(sliding(SECONDS.toMillis(10), SECONDS.toMillis(1)))
                .groupingKey(ProcessedBet::getMatchId)
                .aggregate(summingDouble(ProcessedBet::getAmount))
                .mapUsingIMap(LAST_MIN_BETS_SUM, KeyedWindowResult::getKey, (KeyedWindowResult<Long, Double> matchIdAndLast10Sum, Double lastMinSum) -> {
                    if (lastMinSum == null) return null;
                    Long key = matchIdAndLast10Sum.getKey();
                    return entry(key, matchIdAndLast10Sum.getValue() > lastMinSum ? key : null);
                })
                .filter(Objects::nonNull)
                .writeTo(Sinks.mapWithMerging(SUSPENDED_MATCHES_IMAP, (oldOne, newOne) -> newOne));

        StreamStage<Match> recalculateCoefsStage = processedBetStreamStage
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
                .mapUsingIMap(MATCHES_IMAP, (CombinedBet it) -> it.getBet().getMatchId(), (CombinedBet bet, Match match) -> {
                    // get realistic coefficients
                    List<Integer> probabilities = getProbabilities(match.getStrengthDiff(), match.getGoalsDiff());
                    double win1Loss = bet.getSumWin1() * probabilities.get(0) / 100;
                    double drawLoss = bet.getSumX() * probabilities.get(1) / 100;
                    double win2Loss = bet.getSumWin2() * probabilities.get(2) / 100;
                    double currentMargin = (bet.getTotal() - win1Loss - drawLoss - win2Loss) / bet.getTotal();
//                    if (currentMargin < 0.2) {
                        // what contributes to our lost the most ?
                        Map<MatchOutcome, Double> outcomesToLosses =
                                Map.of(MatchOutcome.WIN_1, win1Loss, MatchOutcome.DRAW, drawLoss, MatchOutcome.WIN_2, win2Loss);
                        List<MatchOutcome> outcomesByImpact = outcomesToLosses.entrySet().stream()
                                .sorted(Map.Entry.comparingByValue())
                                .map(Map.Entry::getKey)
                                .sorted(Collections.reverseOrder())
                                .collect(Collectors.toList());
                        List<Double> newCoefficients = calculateCoefficients(match.getStrengthDiff(), match.getGoalsDiff());
                        // decrease coefficients for the first two results with the biggest impact
                        int biggestOrdinal = outcomesByImpact.get(0).ordinal();
                        newCoefficients.set(biggestOrdinal, newCoefficients.get(biggestOrdinal) * 0.8);
                        int secondOrdinal = outcomesByImpact.get(1).ordinal();
                        newCoefficients.set(secondOrdinal, newCoefficients.get(secondOrdinal) * 0.9);

                        // update coefs
                        match.setWinFirst(newCoefficients.get(0));
                        match.setDraw(newCoefficients.get(1));
                        match.setWinSecond(newCoefficients.get(2));
//                    }
                    return match;
                }).setName("Recalculate coefficients");

        recalculateCoefsStage
                .writeTo(Sinks.logger());

        recalculateCoefsStage
                .writeTo(Sinks.mapWithUpdating(MATCHES_IMAP, Match::getId, (Match oldMatch, Match newMatch) -> {
                    oldMatch.setWinFirstTrend(MatchOutcomeTrend.fromDiff(newMatch.getWinFirst() - oldMatch.getWinFirst()));
                    oldMatch.setWinFirst(newMatch.getWinFirst());
                    oldMatch.setDrawTrend(MatchOutcomeTrend.fromDiff(newMatch.getDraw() - oldMatch.getDraw()));
                    oldMatch.setDraw(newMatch.getDraw());
                    oldMatch.setWinSecondTrend(MatchOutcomeTrend.fromDiff(newMatch.getWinSecond() - oldMatch.getWinSecond()));
                    oldMatch.setWinSecond(newMatch.getWinSecond());
                    return oldMatch;
                }));

        hazelcast.getJet().newJob(pipeline, new JobConfig().setName("processBets"));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CombinedBet implements Serializable {
        ProcessedBet bet;
        double sumWin1;
        double sumX;
        double sumWin2;
        double total;
    }

}
