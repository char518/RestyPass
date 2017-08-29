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
        Assert.assertEquals(BigDecimal.valueOf(102), BigDecimal.valueOf(versionInfo.getVersionNumber()));
        Assert.assertEquals("RELEASE", versionInfo.getVersionStage());
        Assert.assertEquals(originVersion, versionInfo.getOriginVersion());
    }

    @Test
    public void testVersionCreateSnapshot() {
        String originVersion = "4.1.2-snapshot";
        VersionInfo versionInfo = VersionInfo.create(originVersion);
        Assert.assertEquals(BigDecimal.valueOf(412), BigDecimal.valueOf(versionInfo.getVersionNumber()));
        Assert.assertEquals("SNAPSHOT", versionInfo.getVersionStage());
        Assert.assertEquals(originVersion, versionInfo.getOriginVersion());
    }

    @Test
    public void testVersionCreateNoDash() {
        String originVersion = "3snapshot";
        VersionInfo versionInfo = VersionInfo.create(originVersion);
        Assert.assertEquals(BigDecimal.valueOf(3), BigDecimal.valueOf(versionInfo.getVersionNumber()));
        Assert.assertEquals("SNAPSHOT", versionInfo.getVersionStage());
        Assert.assertEquals(originVersion, versionInfo.getOriginVersion());
    }


    @Test
    public void testVersionCreateSimple(){
        String originVersion = "4.1.2";
        VersionInfo versionInfo = VersionInfo.create(originVersion);
        Assert.assertEquals(BigDecimal.valueOf(412), BigDecimal.valueOf(versionInfo.getVersionNumber()));
        Assert.assertEquals(null, versionInfo.getVersionStage());
        Assert.assertEquals(originVersion, versionInfo.getOriginVersion());
    }


}
