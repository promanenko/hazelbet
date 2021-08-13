package com.hazelcast.hazelbet.controller.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

import static com.hazelcast.hazelbet.controller.model.MatchOutcomeTrend.STABLE;

@Data
@Builder(toBuilder = true)
public class Match implements Comparable<Match>, Serializable {

    long id;
    String firstTeam;
    String secondTeam;
    boolean suspended;
    double winFirst;
    double draw;
    double winSecond;
    int firstStrength;
    int secondStrength;
    @Builder.Default
    MatchOutcomeTrend winFirstTrend = STABLE;
    @Builder.Default
    MatchOutcomeTrend drawTrend = STABLE;
    @Builder.Default
    MatchOutcomeTrend winSecondTrend = STABLE;
    @Builder.Default
    int firstScored = 0;
    @Builder.Default
    int secondScored = 0;

    @Override
    public int compareTo(Match o) {
        return Long.compare(id, o.getId());
    }

    public MatchOutcome getOutcome() {
        if (firstScored == secondScored)
            return MatchOutcome.DRAW;
        if (firstScored > secondScored)
            return MatchOutcome.WIN_1;
        else
            return MatchOutcome.WIN_2;
    }

    public int getStrengthDiff() {
        return firstStrength - secondStrength;
    }

    public int getGoalsDiff() {
        return firstScored - secondScored;
    }
}
