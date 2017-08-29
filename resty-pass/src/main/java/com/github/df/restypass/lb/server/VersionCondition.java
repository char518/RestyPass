package com.github.df.restypass.lb.server;

import com.github.df.restypass.annotation.RestyService;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 版本约束
 * Created by darrenfu on 17-8-29.
 */
@Data
public class VersionCondition {

    /**
     * 版本number reg
     * eg. <=1.0.2.3-RELEASE  will find '<=' as compare enum and '1023' as version number
     */
    private static final Pattern VERSION_NUM_REG = Pattern.compile("^(!|!=|=|>=|>|<|<=){0,1}\\d+(\\.\\d)+");

    /**
     * 版本stage reg
     * eg.<=1.0.2.3-!RELEASE will find '!' as compare enum and 'RELEASE' as version stage
     */
    private static final Pattern VERSION_STAGE_REG = Pattern.compile("(!|!=){0,1}[a-zA-z]+$");


    /**
     * 版本号比较条件
     */
    private VersionCompareEnum compareNum;

    /**
     * 版本号
     */
    private Integer versionNum;

    /**
     * 版本阶段比较条件
     */
    private VersionCompareEnum compareStage;

    /**
     * 版本阶段
     */
    private String versionStage;


    /**
     * 创建版本路由条件
     * eg.
     * 配置数据:version="<=1.0.2.3-!RELEASE"
     * 分析结果:
     * 1.版本号:1023 版本号匹配条件 {@link VersionCompareEnum#LTE}
     * 2.版本阶段:RELEASE 版本阶段匹配条件 {@link VersionCompareEnum#NEQ}
     *
     * @param versionCondition the version condition
     * @return the version condition
     * @see RestyService#version()
     */
    public static VersionCondition create(String versionCondition) {
        VersionCondition condition = new VersionCondition();
        versionCondition = versionCondition.trim();
        //分析版本号路由条件
        Matcher numMatcher = VERSION_NUM_REG.matcher(versionCondition);

        if (numMatcher.find()) {
            String number = numMatcher.group();
            int firstNumIdx = findFirstNumberIdx(number);
            //获取版本号
            int versionNum = findNumber(number);
            condition.setVersionNum(versionNum);
            //设置版本号的比较条件
            if (firstNumIdx > 0) {
                condition.setCompareNum(VersionCompareEnum.getCompare(number.substring(0, firstNumIdx)));
            } else {
                condition.setCompareNum(VersionCompareEnum.EQ);
            }
        } else {
            //没有版本号条件，则比较条件为ANY，任意版本皆可
            condition.setCompareNum(VersionCompareEnum.ANY);
        }

        //分析版本阶段条件
        Matcher stageMatcher = VERSION_STAGE_REG.matcher(versionCondition);

        if (stageMatcher.find()) {
            String stage = stageMatcher.group();
            int firstLetterIdx = findFirstLetterIdx(stage);
            //设置版本阶段，以及版本阶段匹配条件
            if (firstLetterIdx > 0) {
                String comparePattern = stage.substring(0, firstLetterIdx);
                condition.setCompareStage(VersionCompareEnum.getCompare(comparePattern));
                condition.setVersionStage(stage.substring(firstLetterIdx));
            } else {
                condition.setVersionStage(stage);
                condition.setCompareStage(VersionCompareEnum.EQ);
            }
        } else {
            condition.setCompareStage(VersionCompareEnum.ANY);
        }

        return condition;
    }


    /**
     * Match boolean.
     *
     * @param versionInfo the version info
     * @return the boolean
     */
    public boolean match(VersionInfo versionInfo) {
        return true;
    }


    /**
     * 版本比较方式 枚举
     */
    public enum VersionCompareEnum {
        // 相等 不相等 大于 大于等于 小于 小于等于

        ANY(),
        /**
         * =
         */
        EQ("="),
        /**
         * ! or !=
         */
        NEQ("!", "!="),
        /**
         * >
         */
        GT(">"),
        /**
         * >=
         */
        GTE(">="),
        /**
         * <
         */
        LT("<"),
        /**
         * <=
         */
        LTE("<=");

        private String[] patterns;

        VersionCompareEnum(String... patterns) {
            this.patterns = patterns;
        }


        public static VersionCompareEnum getCompare(String pattern) {
            switch (pattern) {
                case "=":
                    return EQ;
                case "!":
                    return NEQ;
                case "!=":
                    return NEQ;
                case ">":
                    return GT;
                case ">=":
                    return GTE;
                case "<":
                    return LT;
                case "<=":
                    return LTE;
                default:
                    return ANY;

            }


        }
    }


    private static int findFirstNumberIdx(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {
                return i;
            }
        }
        return -1;
    }


    private static int findNumber(String str) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {
                sb.append(str.charAt(i));
            }
        }
        String number = sb.toString();
        return StringUtils.isNotEmpty(number) ? Integer.valueOf(number) : -1;
    }


    private static int findFirstLetterIdx(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.isLetter(str.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

}
