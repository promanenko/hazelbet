package com.hazelcast.hazelbet.controller.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bet implements Serializable {

    String id;
    @Builder.Default
    long createAt = System.currentTimeMillis();
    Long userId;
    long matchId;
    MatchOutcome outcome;
    double amount;
    double coefficient;
}
