package com.hazelcast.hazelbet.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.hazelbet.controller.model.Match;
import com.hazelcast.map.IMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class Initializer {

    @Autowired
    private HazelcastInstance hazelcast;

    @PostConstruct
    public void setUp() {
        IMap<Long, Match> macthes = hazelcast.getMap("matches");
        macthes.put(1L, Match.builder().id(1L).firstTeam("Barcelona").secondTeam("Madrid").winFirst(2.1).draw(2.2).winSecond(1.7).build());
        macthes.put(2L, Match.builder().id(2L).firstTeam("Dynamo").secondTeam("Zorya").winFirst(1.5).draw(2.7).winSecond(3.3).build());
        macthes.put(3L, Match.builder().id(3L).firstTeam("Man Utd").secondTeam("Arsenal").winFirst(1.6).draw(2.4).winSecond(2.8).build());
        macthes.put(4L, Match.builder().id(4L).firstTeam("Liverpool").secondTeam("Tottenham").winFirst(1.5).draw(2.7).winSecond(3.3).build());
        macthes.put(5L, Match.builder().id(5L).firstTeam("Bayern").secondTeam("Dortmund").winFirst(1.45).draw(2.0).winSecond(3.5).build());
        macthes.put(6L, Match.builder().id(6L).firstTeam("Milan").secondTeam("Juventus").winFirst(2.1).draw(2.2).winSecond(1.7).build());
        macthes.put(7L, Match.builder().id(7L).firstTeam("Minaj").secondTeam("Man City").winFirst(7.1).draw(5.8).winSecond(1.1).build());
        macthes.put(8L, Match.builder().id(8L).firstTeam("Fenerbahce").secondTeam("Besiktas").winFirst(1.9).draw(1.9).winSecond(1.9).build());
    }

}
