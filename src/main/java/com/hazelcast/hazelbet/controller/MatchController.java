package com.hazelcast.hazelbet.controller;

import com.hazelcast.hazelbet.controller.model.Match;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/matches")

public class MatchController {

    @GetMapping
    public List<Match> listMatches() {
        // TODO read from map
        return List.of(
                Match.builder().id(1l).firstTeam("Barcelona").secondTeam("Madrid").winFirst(2.1).draw(2.2).winSecond(1.7).build(),
                Match.builder().id(2l).firstTeam("Dynamo").secondTeam("Zorya").winFirst(1.5).draw(2.7).winSecond(3.3).build()
        );
    }

}
