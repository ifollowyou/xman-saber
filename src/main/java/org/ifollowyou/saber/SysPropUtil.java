package org.ifollowyou.saber;

public class SysPropUtil {
    public static String getTempDir() {
        return System.getenv("TMP");
    }
}