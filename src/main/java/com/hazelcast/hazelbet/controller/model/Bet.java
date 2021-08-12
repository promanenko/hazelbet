package com.hazelcast.hazelbet.controller.model;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

@Value
@Builder
public class Bet implements Serializable {

    long matchId;
    MatchOutcome outcome;
    double amount;
    double coefficient;
}
