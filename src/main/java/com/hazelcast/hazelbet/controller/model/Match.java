package com.hazelcast.hazelbet.controller.model;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

import static com.hazelcast.hazelbet.controller.model.MatchOutcomeTrend.*;

@Value
@Builder(toBuilder = true)
public class Match implements Comparable<Match>, Serializable {

    long id;
    String firstTeam;
    String secondTeam;
    double winFirst;
    double draw;
    double winSecond;
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
}
