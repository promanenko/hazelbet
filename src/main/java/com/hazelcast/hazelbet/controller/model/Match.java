package com.hazelcast.hazelbet.controller.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Match {

    long id;
    String firstTeam;
    String secondTeam;
    double winFirst;
    double draw;
    double winSecond;

}
