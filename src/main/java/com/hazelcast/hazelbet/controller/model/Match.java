package com.hazelcast.hazelbet.controller.model;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

import static com.hazelcast.hazelbet.controller.model.MatchOutcomeTrend.*;

@Value
@Builder
public class Match implements Comparable<Match>, Serializable {

    long id;
    String firstTeam;
    String secondTeam;
    double winFirst;
    double draw;
    double winSecond;
    MatchOutcomeTrend winFirstTrend = STABLE;
    MatchOutcomeTrend drawTrend = STABLE;
    MatchOutcomeTrend winSecondTrend = STABLE;
    int firstScored = 0;
    int secondScored = 0;

    @Override
    public int compareTo(Match o) {
        return Long.compare(id, o.getId());
    }
}
