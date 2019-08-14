package com.hpe.adm.octane.ideplugins.integrationtests;

import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.authentication.Authentication;
import com.hpe.adm.octane.ideplugins.services.connection.IdePluginsOctaneHttpClient2;
import com.hpe.adm.octane.ideplugins.services.connection.UserAuthentication;
import com.hpe.adm.octane.ideplugins.services.util.ClientType;
import org.junit.Test;

public class TestyTestTest {

    @Test
    public void test() {

        System.setProperty("http.proxyHost", "web-proxy.eu.hpecorp.net");
        System.setProperty("https.proxyHost", "web-proxy.eu.hpecorp.net");
        System.setProperty("http.proxyPort", "8080");
        System.setProperty("https.proxyPort", "8080");

        String url = "https://almoctane-ams.saas.microfocus.com";
        Authentication authentication = new UserAuthentication("atoth@microfocus.com", "Iluvmf71!");

        Octane octane = new Octane.Builder(authentication,
                new IdePluginsOctaneHttpClient2(url, ClientType.OCTANE_IDE_PLUGIN))
                .Server(url)
                .sharedSpace(1001L)
                .workSpace(1002L)
                .build();


    }
}
