package me.karthikeyang.handcricket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class GameEngine {
    public enum Difficulty { EASY, MEDIUM, HARD, NIGHTMARE }
    public enum Phase { TOSS, BATTING, BOWLING }

    public int playerScore = 0;
    public int computerScore = 0;
    public boolean secondInnings = false;
    public Phase phase = Phase.TOSS;
    public Difficulty difficulty = Difficulty.EASY;
    public Integer lastPlayerInput = null;
    public int sameChoiceCount = 0;

    private final Random rng = new Random();
    private final List<Integer> prevMoves = new ArrayList<>();

    public void reset() {
        playerScore = 0;
        computerScore = 0;
        secondInnings = false;
        phase = Phase.TOSS;
        lastPlayerInput = null;
        sameChoiceCount = 0;
        prevMoves.clear();
    }

    public int generateComputerMove(int playerVal) {
        if (difficulty == Difficulty.NIGHTMARE) {
            if (phase == Phase.BATTING) {
                if (rng.nextDouble() < 0.85) return playerVal;
                return rng.nextInt(3);
            } else {
                int attempts = 0;
                int num;
                do {
                    num = 7 + rng.nextInt(4);
                    attempts++;
                } while (num == playerVal && attempts < 5);
                return num;
            }
        }

        if (difficulty == Difficulty.EASY) {
            if (rng.nextDouble() < 0.15) return playerVal;
            int[] dist = new int[]{0,0,1,1,2,2,3,4,5,6,7};
            return dist[rng.nextInt(dist.length)];
        }

        if (difficulty == Difficulty.MEDIUM) {
            Integer last = prevMoves.size() > 0 ? prevMoves.get(prevMoves.size() - 1) : null;
            if (last != null && rng.nextDouble() < 0.45) {
                int v = last + (rng.nextBoolean() ? 0 : (rng.nextBoolean() ? 1 : -1));
                return Math.max(0, Math.min(10, v));
            }
            return rng.nextInt(11);
        }

        if (difficulty == Difficulty.HARD) {
            HashMap<Integer, Integer> freq = new HashMap<>();
            for (int n : prevMoves) {
                freq.put(n, freq.getOrDefault(n, 0) + 1);
            }
            int predict = rng.nextInt(11);
            int bestCount = -1;
            for (int key : freq.keySet()) {
                int count = freq.get(key);
                if (count > bestCount) {
                    bestCount = count;
                    predict = key;
                }
            }

            if (phase == Phase.BATTING) {
                if (rng.nextDouble() < 0.6) return predict;
                int v = predict + (rng.nextBoolean() ? -1 : 1);
                return Math.max(0, Math.min(10, v));
            } else {
                int candidate = predict + (rng.nextDouble() < 0.7 ? 2 : 1);
                candidate = Math.max(0, Math.min(10, candidate));
                if (candidate == playerVal) candidate = Math.max(0, candidate - 1);
                return candidate;
            }
        }

        return rng.nextInt(11);
    }

    public void recordPlayerMove(int val) {
        prevMoves.add(val);
        if (lastPlayerInput != null && lastPlayerInput == val) {
            sameChoiceCount++;
        } else {
            sameChoiceCount = 1;
        }
        lastPlayerInput = val;
    }
}
