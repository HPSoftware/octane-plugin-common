package com.hpe.adm.octane.ideplugins.services.connection;

import java.util.ArrayList;
import java.util.List;

public class BasicConnectionSettingProvider implements ConnectionSettingsProvider {

    protected ConnectionSettings connectionSettings = new ConnectionSettings();

    public BasicConnectionSettingProvider() {
    }

    public BasicConnectionSettingProvider(ConnectionSettings connectionSettings) {
        this.connectionSettings = connectionSettings;
    }

    private List<Runnable> changeHandlers = new ArrayList<>();

    @Override
    public void addChangeHandler(Runnable changeHandler) {
        changeHandlers.add(changeHandler);
    }

    @Override
    public boolean hasChangeHandler(Runnable observer) {
        return changeHandlers.contains(observer);
    }

    private void callChangeHandlers() {
        changeHandlers.forEach(handler -> handler.run());
    }

    @Override
    public ConnectionSettings getConnectionSettings() {
        return ConnectionSettings.getCopy(connectionSettings);
    }

    @Override
    public void setConnectionSettings(ConnectionSettings connectionSettings) {
        this.connectionSettings = connectionSettings;
        callChangeHandlers();
    }

}
