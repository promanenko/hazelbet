package com.hazelcast.hazelbet.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class HzDistributedObjectNames {

    public static String INPUT_BETS_IMAP = "inputBets";
    public static String PROCESSED_BETS_IMAP = "processedBets";
    public static String MATCHES_IMAP = "matches";
    public static String SUSPENDED_MATCHES_IMAP = "suspendedMatches";
    public static String LAST_MIN_BETS_SUM = "lastMinBetsSum";
}
