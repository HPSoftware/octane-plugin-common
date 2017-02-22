package com.hpe.adm.octane.integrationtests;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.inject.*;
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.authentication.SimpleUserAuthentication;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.google.GoogleHttpClient;
import com.hpe.adm.octane.integrationtests.util.ConfigurationUtil;
import com.hpe.adm.octane.services.TestService;
import com.hpe.adm.octane.services.connection.*;
import com.hpe.adm.octane.services.util.ClientType;


/**
 * DI module for integration tests
 */
class TestModule extends AbstractModule {

    protected final Supplier<Injector> injectorSupplier;

    public TestModule() {
        injectorSupplier = Suppliers.memoize(() -> Guice.createInjector(this));
    }

    public <T> T getInstance(Class<T> type) {
        return injectorSupplier.get().getInstance(type);
    }

    @Override
    protected void configure() {

        bind(TestService.class);

        bind(ConnectionSettingsProvider.class).toProvider(()-> {
            ConnectionSettings connectionSettings = new ConnectionSettings();

            //Set form config
            connectionSettings.setBaseUrl(ConfigurationUtil.getString(ConfigurationUtil.PropertyKeys.URL));
            connectionSettings.setSharedSpaceId(ConfigurationUtil.getLong(ConfigurationUtil.PropertyKeys.SHAREDSPACE));
            connectionSettings.setWorkspaceId(ConfigurationUtil.getLong(ConfigurationUtil.PropertyKeys.WORKSPACE));
            connectionSettings.setUserName(ConfigurationUtil.getString(ConfigurationUtil.PropertyKeys.USERNAME));
            connectionSettings.setPassword(ConfigurationUtil.getString(ConfigurationUtil.PropertyKeys.PASSWORD));

            return new BasicConnectionSettingProvider(connectionSettings);
        }).in(Singleton.class);

    }


    private ConnectionSettings octaneProviderPreviousConnectionSettings = new ConnectionSettings();
    private ConnectionSettings httpClientPreviousConnectionSettings = new ConnectionSettings();
    private Octane octane;
    private OctaneHttpClient octaneHttpClient;


    /**
     * @return authenticated instance of Octane, with current connection settings
     */
    @Provides
    OctaneProvider getOctane(){
        return () -> {
            ConnectionSettings currentConnectionSettings = getInstance(ConnectionSettingsProvider.class).getConnectionSettings();
            if (!currentConnectionSettings.equals(octaneProviderPreviousConnectionSettings) || octane == null) {
                octane = new Octane
                        .Builder(new SimpleUserAuthentication(currentConnectionSettings.getUserName(), currentConnectionSettings.getPassword(), ClientType.HPE_MQM_UI.name()))
                        .Server(currentConnectionSettings.getBaseUrl())
                        .sharedSpace(currentConnectionSettings.getSharedSpaceId())
                        .workSpace(currentConnectionSettings.getWorkspaceId())
                        .build();

                octaneProviderPreviousConnectionSettings = currentConnectionSettings;
            }
            return octane;
        };
    }
    @Provides
    HttpClientProvider geOctaneHttpClient(){
        return ()->{
            ConnectionSettings currentConnectionSettings = getInstance(ConnectionSettingsProvider.class).getConnectionSettings();
            if (!currentConnectionSettings.equals(httpClientPreviousConnectionSettings) || null == octaneHttpClient) {
                octaneHttpClient =  new GoogleHttpClient(currentConnectionSettings.getBaseUrl(), ClientType.HPE_MQM_UI.name());
                httpClientPreviousConnectionSettings = currentConnectionSettings;
            }
            SimpleUserAuthentication userAuthentication =  new SimpleUserAuthentication(currentConnectionSettings.getUserName(),currentConnectionSettings.getPassword(), ClientType.HPE_MQM_UI.name());
            octaneHttpClient.authenticate(userAuthentication);

            return octaneHttpClient;
        };
    }

}
