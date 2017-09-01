package com.github.df.restypass.lb.rule;

import com.github.df.restypass.annotation.RestyService;
import com.github.df.restypass.event.EventConsumer;
import com.github.df.restypass.lb.server.ServerInstance;
import com.github.df.restypass.lb.server.VersionInfo;
import com.github.df.restypass.util.CommonTools;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 版本约束
 * Created by darrenfu on 17-8-29.
 */
@Data
public class VersionRule implements RouteRule<VersionInfo>, EventConsumer {

    /**
     * The constant EVENT_KEY_PREFIX.
     */
    public static final String EVENT_KEY_PREFIX = "ServerInstance_Version";

    /**
     * 缓存不同serverInstance对此版本约束的匹配条件
     * key->value:ServerInstanceId->boolean是否匹配
     * 线程不安全(不需要，一个service的同一个instance同一时间应该只有一个版本)
     */
    private Map<String, Boolean> versionMatchMap = new HashMap(32);

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
     * 使用此条件的服务名称
     */
    private String serviceName;

    /**
     * 版本号比较条件
     */
    private VersionCompareEnum numCompare;

    /**
     * 版本号
     */
    private BigDecimal versionNum;

    /**
     * 版本阶段比较条件
     */
    private VersionCompareEnum stageCompare;

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
     * @param serviceName      the service name
     * @param versionCondition the version condition
     * @return the version condition
     * @see RestyService#version() RestyService#version()
     */
    public static VersionRule create(String serviceName, String versionCondition) {
        VersionRule condition = new VersionRule();
        condition.setServiceName(serviceName);
        versionCondition = versionCondition.trim();
        //分析版本号路由条件
        Matcher numMatcher = VERSION_NUM_REG.matcher(versionCondition);

        if (numMatcher.find()) {
            String number = numMatcher.group();
            int firstNumIdx = findFirstNumberIdx(number);
            //获取版本号
            BigDecimal versionNum = CommonTools.convertVersionNum(number);
            condition.setVersionNum(versionNum);
            //设置版本号的比较条件
            if (firstNumIdx > 0) {
                condition.setNumCompare(VersionCompareEnum.getCompare(number.substring(0, firstNumIdx)));
            } else {
                condition.setNumCompare(VersionCompareEnum.EQ);
            }
        } else {
            //没有版本号条件，则比较条件为ANY，任意版本皆可
            condition.setNumCompare(VersionCompareEnum.ANY);
        }

        //分析版本阶段条件
        Matcher stageMatcher = VERSION_STAGE_REG.matcher(versionCondition);

        if (stageMatcher.find()) {
            String stage = stageMatcher.group();
            int firstLetterIdx = findFirstLetterIdx(stage);
            //设置版本阶段，以及版本阶段匹配条件
            if (firstLetterIdx > 0) {
                String comparePattern = stage.substring(0, firstLetterIdx);
                condition.setStageCompare(VersionCompareEnum.getCompare(comparePattern));
                condition.setVersionStage(stage.substring(firstLetterIdx).toUpperCase());
            } else {
                condition.setVersionStage(stage.toUpperCase());
                condition.setStageCompare(VersionCompareEnum.EQ);
            }
        } else {
            condition.setStageCompare(VersionCompareEnum.ANY);
        }

        //注册事件，服务实例刷新时，重新计算是否匹配路由条件
        condition.onServerInstanceUpdateEvent();
        return condition;
    }


    /**
     * 匹配版本
     *
     * @param versionInfo the version info
     * @return the boolean
     */
    @Override
    public boolean match(VersionInfo versionInfo) {
        if (versionInfo == null || versionInfo == VersionInfo.EMPTY_VERSION) {
            return true;
        }
        Boolean match = versionMatchMap.get(versionInfo.getId());
        return match != null ? match : doMath(versionInfo);
    }

    private boolean doMath(VersionInfo versionInfo) {
        if (versionInfo == null
                || versionInfo == VersionInfo.EMPTY_VERSION
                || (versionInfo.getVersionNumber() == null && StringUtils.isEmpty(versionInfo.getVersionStage()))) {
            return true;
        }
        boolean isVersionNumOk = checkVersionNum(versionInfo.getVersionNumber(), this.versionNum, this.numCompare);
        boolean isVersionStageOk = checkVersionStage(versionInfo.getVersionStage(), this.versionStage, this.stageCompare);
        return isVersionNumOk && isVersionStageOk;
    }


    @Override
    public String getEventKey() {
        return EVENT_KEY_PREFIX + this.serviceName;
    }


    /**
     * 服务实例刷新触发此事件，重新计算版本匹配结果
     *
     * @see ServerInstance#ready()
     */
    private void onServerInstanceUpdateEvent() {
        this.on(getEventKey(), (instance) -> {
            if (instance != null && instance instanceof ServerInstance) {
                ServerInstance serverInstance = (ServerInstance) instance;
                versionMatchMap.put(serverInstance.getVersion().getId(), this.doMath(serverInstance.getVersion()));
            }
        });

    }


    /**
     * 校验版本号是否匹配
     *
     * @param targetVersionNum 待匹配的版本号
     * @param compare          匹配条件
     * @return the boolean
     */
    protected boolean checkVersionNum(BigDecimal targetVersionNum, BigDecimal compareVersionNum, VersionCompareEnum compare) {
        if (targetVersionNum == null) {
            return true;
        }
        switch (compare) {
            case ANY:
                return true;
            case EQ:
                return compareBigDecimal(targetVersionNum, compareVersionNum) == 0;
            case NEQ:
                return compareBigDecimal(targetVersionNum, compareVersionNum) != 0;
            case LT:
                return compareBigDecimal(targetVersionNum, compareVersionNum) < 0;
            case LTE:
                return compareBigDecimal(targetVersionNum, compareVersionNum) <= 0;
            case GT:
                return compareBigDecimal(targetVersionNum, compareVersionNum) > 0;
            case GTE:
                return compareBigDecimal(targetVersionNum, compareVersionNum) >= 0;
            default:
                return true;
        }
    }

    private int compareBigDecimal(BigDecimal x, BigDecimal y) {
        if (x == null || y == null) {
            return 0;
        }
        return x.compareTo(y);
    }


    /**
     * Check version stage boolean.
     *
     * @param targetVersionStage the target version stage
     * @param compare            the compare
     * @return the boolean
     */
    protected boolean checkVersionStage(String targetVersionStage, String compareVersionStage, VersionCompareEnum compare) {
        if (StringUtils.isEmpty(targetVersionStage)) {
            return true;
        }
        switch (compare) {
            case ANY:
                return true;
            case EQ:
                return targetVersionStage.equals(compareVersionStage);
            case NEQ:
                return !targetVersionStage.equals(compareVersionStage);
            default:
                return true;
        }
    }

    /**
     * 版本比较方式 枚举
     */
    public enum VersionCompareEnum {
        // 相等 不相等 大于 大于等于 小于 小于等于

        /**
         * Any version compare enum.
         */
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


        /**
         * Gets compare.
         *
         * @param pattern the pattern
         * @return the compare
         */
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

    /**
     * 查找第一个数字的index
     *
     * @param str
     * @return
     */
    private static int findFirstNumberIdx(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 查找第一个字母的index
     *
     * @param str
     * @return
     */
    private static int findFirstLetterIdx(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.isLetter(str.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

}
