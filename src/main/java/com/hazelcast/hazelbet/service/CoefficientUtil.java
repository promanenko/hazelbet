package com.hazelcast.hazelbet.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CoefficientUtil {

    public static List<Double> calculateCoefficients(int teamStrengthDiff, int goalsDiff) {
        List<Integer> probabilities = getProbabilities(teamStrengthDiff, goalsDiff);
        return probabilities.stream()
                .map(probability -> toCoefficient(probability))
                .map(coefficient -> ((double)((int)(coefficient *100.0)))/100.0)
                .collect(Collectors.toList());
    }

    private static Double toCoefficient(Integer probability) {
        double realCoefficient = 100.0 / probability;
        if (realCoefficient > 9) {
            return realCoefficient * 0.75;
        } else if (realCoefficient > 4.5) {
            return realCoefficient * 0.8;
        } else if (realCoefficient > 2.5) {
            return realCoefficient * 0.85;
        } else {
            return realCoefficient * 0.9;
        }
    }

    public static List<Integer> getProbabilities(int teamStrengthDiff, int goalsDiff) {
        int totalDifference = teamStrengthDiff + goalsDiff * 3;
        List<Integer> result;
        switch (Math.abs(totalDifference)) {
            case 0:
                result = List.of(33, 33, 33);
                break;
            case 1:
                result = List.of(42, 34, 24);
                break;
            case 2:
                result = List.of(50, 30, 20);
                break;
            case 3:
                result = List.of(55, 25, 20);
                break;
            case 4:
                result = List.of(60, 25, 15);
                break;
            case 5:
            case 6:
                result = List.of(75, 17, 8);
                break;
            default:
                result = List.of(85, 11, 4);
        }
        if (totalDifference < 0) {
            result = result.stream().sorted(Collections.reverseOrder()).collect(Collectors.toList());
        }
        return result;
    }

}
