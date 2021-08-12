package com.hazelcast.hazelbet.config;

import com.hazelcast.hazelbet.controller.model.Bet;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.test.TestSources;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.hazelcast.hazelbet.controller.model.MatchOutcome.WIN_1;

@Configuration
public class StreamingConfiguration {

    @Bean
    public StreamSource<Bet> backgroundBets() {
        // TODO: use match test data for bets stream
        return TestSources.itemStream(5, (timestamp, sequence) -> Bet.builder()
                .matchId(5)
                .amount(15)
                .coefficient(1.2)
                .outcome(WIN_1)
                .build()
        );
    }
}
