package com.hazelcast.hazelbet.controller.model;

public enum MatchOutcomeTrend {

    UP, DOWN, STABLE;

    public static MatchOutcomeTrend fromDiff(double diff) {
        if (diff > 0.01) {
            return UP;
        }  if (diff < -0.01) {
            return DOWN;
        }  else {
            return STABLE;
        }
    }

}
