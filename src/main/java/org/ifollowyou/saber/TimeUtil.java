package org.ifollowyou.saber;

public class TimeUtil {
    public static long elapsedSecond(long start) {
        return (System.currentTimeMillis() - start) / 1000;
    }

}