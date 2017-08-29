package com.github.df.restypass.lb.server;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 格式化后的版本信息
 * Created by darrenfu on 17-8-29.
 */
@Data
public class VersionInfo {


    private static final Pattern VERSION_NUM_REG = Pattern.compile("^\\d+(\\.\\d)+");
    private static final Pattern VERSION_STAGE_REG = Pattern.compile("[a-zA-z]+$");

    /**
     * 原始版本信息 eg.1.2.0-RELEASE
     */
    private String originVersion;

    /**
     * 版本编号 eg.1.2.0 convert to 120  2.3.5 convert to 235
     */
    private Integer versionNumber;

    /**
     * 版本阶段 SNAPSHOT, BETA, RC, RELEASE等等
     */
    private String versionStage;

    private VersionInfo() {
    }

    /**
     * Instantiates a new Version info.
     *
     * @param versionNumber the version number
     * @param versionStage  the version stage
     */
    public VersionInfo(Integer versionNumber, String versionStage) {
        this.versionNumber = versionNumber;
        this.versionStage = versionStage.toUpperCase();
        this.originVersion = this.versionNumber + "-" + this.versionStage;
    }


    /**
     * 创建版本信息
     *
     * @param originVersion 原始版本数据 1.0.0-RELEASE 2.0RELEASE
     * @return the version info
     */
    public static VersionInfo create(String originVersion) {
        VersionInfo versionInfo = new VersionInfo();
        versionInfo.setOriginVersion(originVersion);
        Matcher numMatcher = VERSION_NUM_REG.matcher(originVersion);

        if (numMatcher.find()) {
            String[] splitNum = numMatcher.group().split("\\.");
            versionInfo.setVersionNumber(Integer.valueOf(StringUtils.join(splitNum)));
        }
        Matcher stageMatcher = VERSION_STAGE_REG.matcher(originVersion);
        if (stageMatcher.find()) {
            versionInfo.setVersionStage(stageMatcher.group().toUpperCase());
        }
        return versionInfo;
    }

}
