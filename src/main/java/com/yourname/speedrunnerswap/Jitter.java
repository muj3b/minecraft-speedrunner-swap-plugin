package com.yourname.speedrunnerswap;

import java.util.concurrent.ThreadLocalRandom;

public class Jitter {
    public static int gaussianSeconds(int mean, int stddev, int min, int max, boolean clamp) {
        double result = ThreadLocalRandom.current().nextGaussian() * stddev + mean;
        if (clamp) {
            return Math.max(min, Math.min(max, (int) Math.round(result)));
        }
        return (int) Math.round(result);
    }
}
