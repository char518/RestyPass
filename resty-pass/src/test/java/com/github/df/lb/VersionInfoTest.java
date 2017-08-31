package com.github.df.lb;

import com.github.df.restypass.lb.server.VersionInfo;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * unit test
 * Created by darrenfu on 17-8-29.
 */
public class VersionInfoTest {

    @Test
    public void testVersionCreateRelease() {
        String originVersion = "1.0.2-RELEASE";
        VersionInfo versionInfo = VersionInfo.create(originVersion);
        Assert.assertTrue(BigDecimal.valueOf(1.02).compareTo(versionInfo.getVersionNumber()) == 0);
        Assert.assertEquals("RELEASE", versionInfo.getVersionStage());
        Assert.assertEquals(originVersion, versionInfo.getOriginVersion());
    }

    @Test
    public void testVersionCreateSnapshot() {
        String originVersion = "4.1.2-snapshot";
        VersionInfo versionInfo = VersionInfo.create(originVersion);
        Assert.assertTrue(BigDecimal.valueOf(4.12).compareTo(versionInfo.getVersionNumber()) == 0);
        Assert.assertEquals("SNAPSHOT", versionInfo.getVersionStage());
        Assert.assertEquals(originVersion, versionInfo.getOriginVersion());
    }

    @Test
    public void testVersionCreateNoDash() {
        String originVersion = "3snapshot";
        VersionInfo versionInfo = VersionInfo.create(originVersion);
        Assert.assertTrue(BigDecimal.valueOf(3).compareTo(versionInfo.getVersionNumber()) == 0);
        Assert.assertEquals("SNAPSHOT", versionInfo.getVersionStage());
        Assert.assertEquals(originVersion, versionInfo.getOriginVersion());
    }


    @Test
    public void testVersionCreateSimple() {
        String originVersion = "4.1.2";
        VersionInfo versionInfo = VersionInfo.create(originVersion);
        Assert.assertTrue(BigDecimal.valueOf(4.12).compareTo(versionInfo.getVersionNumber()) == 0);
        Assert.assertEquals(null, versionInfo.getVersionStage());
        Assert.assertEquals(originVersion, versionInfo.getOriginVersion());
    }


}
