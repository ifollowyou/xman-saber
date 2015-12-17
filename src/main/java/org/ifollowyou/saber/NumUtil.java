package org.ifollowyou.saber;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;

public class NumUtil {
    public static String truncateDecimal(double num, int keep) {
        DecimalFormat format = new DecimalFormat("####" + (keep > 0 ? "." + StringUtils.repeat("#", keep) : ""));
        return format.format(num);
    }
}