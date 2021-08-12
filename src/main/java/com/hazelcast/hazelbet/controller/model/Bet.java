package com.hazelcast.hazelbet.controller.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Bet {

    long matchId;
    MatchOutcome outcome;
    double amount;
    double coefficient;
}
