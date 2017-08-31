package com.github.df.lb;

import com.github.df.restypass.lb.rule.VersionRule;
import com.github.df.restypass.lb.server.VersionInfo;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * versionCondition test
 * Created by darrenfu on 17-8-30.
 */
public class VersionRouteRuleTest {

    @Test
    public void testVersionConditionCreate() {
        VersionRule condition = VersionRule.create("testServer", ">=1.2.0-RELEASE");
        Assert.assertEquals("testServer", condition.getServiceName());
        Assert.assertEquals(VersionRule.VersionCompareEnum.GTE, condition.getNumCompare());
        Assert.assertTrue(BigDecimal.valueOf(1.20).compareTo(condition.getVersionNum()) == 0);
        Assert.assertEquals(VersionRule.VersionCompareEnum.EQ, condition.getStageCompare());
        Assert.assertEquals("RELEASE", condition.getVersionStage());

        VersionRule condition2 = VersionRule.create("testServer2", "<1.2.3.4!Snapshot");
        Assert.assertEquals("testServer2", condition2.getServiceName());
        Assert.assertEquals(VersionRule.VersionCompareEnum.LT, condition2.getNumCompare());
        Assert.assertTrue(BigDecimal.valueOf(1.234).compareTo(condition2.getVersionNum()) == 0);
        Assert.assertEquals(VersionRule.VersionCompareEnum.NEQ, condition2.getStageCompare());
        Assert.assertEquals("SNAPSHOT", condition2.getVersionStage());

    }

    @Test
    public void testVersionMatch() {
        VersionRule condition = VersionRule.create("testServer", ">=1.2.0-RELEASE");
        VersionInfo versionInfo = VersionInfo.create("1.2.0");
        Assert.assertTrue(condition.match(versionInfo));

        versionInfo = VersionInfo.create("1.1.9");
        Assert.assertTrue("compare with 1.19", !condition.match(versionInfo));

        versionInfo = VersionInfo.create("1.1.9.9");
        Assert.assertTrue("compare with 1.199", !condition.match(versionInfo));

        versionInfo = VersionInfo.create("2.0.0.0.1");
        Assert.assertTrue("compare with 2.0001", condition.match(versionInfo));

        versionInfo = VersionInfo.create("1.3.0-SNAPSHOT");
        Assert.assertTrue("stage is not release", !condition.match(versionInfo));

        versionInfo = VersionInfo.create("1.3.0-RELEASE");
        Assert.assertTrue("stage is release", condition.match(versionInfo));

        versionInfo = VersionInfo.create("RELEASE");
        Assert.assertTrue("stage is release", condition.match(versionInfo));

        condition = VersionRule.create("testServer", "!SNAPSHOT");

        versionInfo = VersionInfo.create("SNAPSHOT");
        Assert.assertTrue("stage is SNAPSHOT", !condition.match(versionInfo));


        condition = VersionRule.create("testServer", "!=1.2.0!RELEASE");

        versionInfo = VersionInfo.create("1.2.0-RELEASE");
        Assert.assertTrue(" can not be 1.2.0 and not release", !condition.match(versionInfo));

        versionInfo = VersionInfo.create("1.3.0-RELEASE");
        Assert.assertTrue(" can not be 1.2.0 and not release", !condition.match(versionInfo));

        versionInfo = VersionInfo.create("1.2.0-SNAPSHOT");
        Assert.assertTrue(" can not be 1.2.0 and not release", !condition.match(versionInfo));

        versionInfo = VersionInfo.create("1.3.0-SNAPSHOT");
        Assert.assertTrue(" can not be 1.2.0 and not release", condition.match(versionInfo));


    }

}
