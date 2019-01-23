package com.hpe.adm.octane.ideplugins.integrationtests.services.noentity;


import com.google.inject.Inject;
import com.hpe.adm.octane.ideplugins.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.ideplugins.services.nonentity.OctaneVersionService;
import com.hpe.adm.octane.ideplugins.services.util.OctaneVersion;
import org.junit.Assert;
import org.junit.Test;

public class OctaneVersionServiceITCase extends IntegrationTestBase {

    @Inject
    private OctaneVersionService versionService;

    @Test
    public void testVersionService() {
        OctaneVersion version = versionService.getOctaneVersion();
        Assert.assertTrue(version != null);
    }

}
