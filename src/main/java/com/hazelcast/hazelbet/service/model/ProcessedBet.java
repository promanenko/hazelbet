package com.hazelcast.hazelbet.service.model;

import com.hazelcast.hazelbet.controller.model.MatchOutcome;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.io.Serializable;

@Data
@Builder
public class ProcessedBet implements Serializable {
    String id;
    long userId;
    boolean rejected;
    String reason;
    boolean success;
    long matchId;
    MatchOutcome outcome;
    double amount;
    double coefficient;
}
