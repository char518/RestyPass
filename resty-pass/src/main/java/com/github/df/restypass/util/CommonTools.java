package com.github.df.restypass.util;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

/**
 * 通用工具
 * Created by darrenfu on 17-7-19.
 */
public class CommonTools {

    /**
     * Empty to null string.
     *
     * @param string the string
     * @return the string
     */
    public static String emptyToNull(String string) {
        return string == null || string.isEmpty() ? null : string;
    }

    /**
     * Is empty boolean.
     *
     * @param coll the coll
     * @return the boolean
     */
    public static boolean isEmpty(Collection coll) {
        return coll == null || coll.isEmpty();
    }

    /**
     * Is empty boolean.
     *
     * @param map the map
     * @return the boolean
     */
    public static boolean isEmpty(Map map) {
        return map == null || map.isEmpty();
    }

    /**
     * 将版本号转换成 BigDecimal
     * 1.2.3->new BigDecimal("1.23")
     *
     * @param versionNumber the version number
     * @return the big decimal
     */
    public static BigDecimal convertVersionNum(String versionNumber) {
        StringBuffer sb = new StringBuffer(16);
        boolean alreadyAppendDot = false;
        for (char c : versionNumber.toCharArray()) {
            if (Character.isDigit(c)) {
                sb.append(c);
            }
            if (!alreadyAppendDot && c == '.') {
                sb.append(c);
                alreadyAppendDot = true;
            }
        }

        return new BigDecimal(sb.toString());

    }

}
