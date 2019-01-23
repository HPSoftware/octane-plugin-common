package com.hpe.adm.octane.ideplugins.services.nonentity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.network.OctaneHttpResponse;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.HttpClientProvider;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class CommitMessageService {
    @Inject
    protected ConnectionSettingsProvider connectionSettingsProvider;
    @Inject
    protected HttpClientProvider httpClientProvider;

    public boolean validateCommitMessage(String commitMessage, Entity entityType, long entityId) {
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        OctaneHttpClient httpClient = httpClientProvider.getOctaneHttpClient();
        if (null != httpClient) {
            try {

                OctaneHttpRequest request = new OctaneHttpRequest.GetOctaneHttpRequest(
                        connectionSettings.getBaseUrl() + "/internal-api/shared_spaces/" + connectionSettings.getSharedSpaceId() + "/workspaces/" + connectionSettings.getWorkspaceId() + "/ali/validateCommitPattern?comment=" + URLEncoder.encode(commitMessage, "UTF-8"));
                OctaneHttpResponse response = httpClient.execute(request);
                String jsonString = response.getContent();

                JsonArray matchedIdsArray = new JsonParser().parse(jsonString).getAsJsonObject().get(entityType.getSubtypeName())
                        .getAsJsonArray();
                for (JsonElement element : matchedIdsArray) {
                    if (element.getAsLong() == entityId) {
                        return true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public List<String> getCommitPatternsForStoryType(Entity entityType) {
        String type;
        switch (entityType) {
            case DEFECT:
                type = "Defect";
                break;
            case USER_STORY:
                type = "User story";
                break;
            case QUALITY_STORY:
                type = "Quality story";
                break;
            default:
                return null;
        }

        List<String> commitPatterns = new ArrayList<>();
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        OctaneHttpClient httpClient = httpClientProvider.getOctaneHttpClient();
        if (null != httpClient) {
                OctaneHttpRequest request = new OctaneHttpRequest.GetOctaneHttpRequest(connectionSettings.getBaseUrl() + "/api/shared_spaces/" + connectionSettings.getSharedSpaceId() +
                        "/workspaces/" + connectionSettings.getWorkspaceId() + "/scm_commit_patterns");
                OctaneHttpResponse response = httpClient.execute(request);
                String jsonString = response.getContent();

                JsonArray dataArray = new JsonParser().parse(jsonString).getAsJsonObject().get("data").getAsJsonArray();
                for (JsonElement elem : dataArray) {
                    String name = elem.getAsJsonObject().get("entity_type").getAsJsonObject().get("name").getAsString();
                    if (name.equals(type)) {
                        commitPatterns.add(elem.getAsJsonObject().get("pattern").getAsString());
                    }
                }
                return commitPatterns;
        }
        return null;
    }
}
