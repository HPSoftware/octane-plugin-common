/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hpe.adm.octane.ideplugins.services.nonentity;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.network.OctaneHttpResponse;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.HttpClientProvider;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceRuntimeException;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.filtering.PredefinedEntityComparator;
import com.hpe.adm.octane.ideplugins.services.util.TextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class EntitySearchService {

    private static final String JSON_DATA_NAME = "data";
    private static final String GLOBAL_TEXT_SEARCH_RESULT_TAG = "global_text_search_result";

    @Inject
    protected ConnectionSettingsProvider connectionSettingsProvider;
    @Inject
    protected HttpClientProvider httpClientProvider;

    public Collection<EntityModel> searchGlobal(String queryString, int limit, Entity... entity) {
        final String escapedQueryString = TextUtil.escapeText(queryString);
        Map<Entity, Collection<EntityModel>> result = new ConcurrentHashMap<>();

        Arrays
            .stream(entity)
            .parallel()
            .forEach(entityType -> result.put(entityType, searchGlobal(escapedQueryString , limit, entityType)));

        return result
                .keySet()
                .stream()
                .sorted(PredefinedEntityComparator.instance)
                .flatMap(key -> result.get(key).stream())
                .collect(Collectors.toList());
    }


    public Collection<EntityModel> searchGlobal(String queryString, int limit, Entity entity) {

        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        OctaneHttpClient httpClient = httpClientProvider.geOctaneHttpClient();
        if(null == httpClient){
            throw new ServiceRuntimeException("Failed to authenticate with current connection settings");
        }

        URIBuilder uriBuilder = new URIBuilder();

        //set the scheme, the protocol must be removed for the uri builder setHost method
        if(connectionSettings.getBaseUrl().contains("https")){
            uriBuilder.setScheme("https");
            uriBuilder.setHost(connectionSettings.getBaseUrl().replace("https://", ""));
        }
        else if (connectionSettings.getBaseUrl().contains("http")){
            uriBuilder.setScheme("http");
            uriBuilder.setHost(connectionSettings.getBaseUrl().replace("http://", ""));
        } else {
            throw new ServiceRuntimeException("Cannot find http/https protocol is connections settings base URL");
        }

        uriBuilder.setPath(
                "/api"
                + "/shared_spaces/" + connectionSettings.getSharedSpaceId()
                + "/workspaces/" + connectionSettings.getWorkspaceId()
                + "/" + entity.getApiEntityName());



        uriBuilder.setParameter("text_search", "{\"type\":\"global\",\"text\":\""+queryString+"\"}");
        uriBuilder.setParameter("limit", limit + "");
        uriBuilder.setParameter("order_by","id");

        if(entity.isSubtype()) {
            uriBuilder.setParameter("query", "\"((subtype='"+entity.getSubtypeName()+"'))\"");
        }

        try {
            OctaneHttpRequest request = new OctaneHttpRequest.GetOctaneHttpRequest( uriBuilder.build().toASCIIString());
            OctaneHttpResponse response = httpClient.execute(request);
            String responseString = response.getContent();

            if(response.isSuccessStatusCode() && StringUtils.isNotBlank(responseString)){
                return searchResponseToEntityModels(responseString);
            } else {
                throw new ServiceRuntimeException("Failed to get search response JSON");
            }

        } catch (Exception ex) {
            throw new ServiceRuntimeException(ex);
        }
    }

    /**
     * Convert it to standard entity model for re-using exiting UI,
     * only has: id, name, type/subtype, description
     * @param responseString
     * @return
     */
    private Collection<EntityModel> searchResponseToEntityModels(String responseString){

        Collection<EntityModel> result = new ArrayList<>();

        JSONObject json = new JSONObject(responseString);
        JSONArray data = json.getJSONArray(JSON_DATA_NAME);

        data.forEach(jsonObj -> {
            JSONObject jsonObject = (JSONObject) jsonObj;

            //Create an entity model from the json, the json format is fixed
            String name = getStringOrBlank(jsonObject.getJSONObject(GLOBAL_TEXT_SEARCH_RESULT_TAG), "name" );
            String description = getStringOrBlank(jsonObject.getJSONObject(GLOBAL_TEXT_SEARCH_RESULT_TAG), "description" );
            String id = getStringOrBlank(jsonObject, "id" );
            String type = getStringOrBlank(jsonObject, "type" );
            String subtype = getStringOrBlank(jsonObject, "subtype" );

            EntityModel entityModel = new EntityModel();
            entityModel.setValue(new StringFieldModel("name", name));
            entityModel.setValue(new StringFieldModel("description", description));
            entityModel.setValue(new StringFieldModel("id", id));
            entityModel.setValue(new StringFieldModel("type", type));
            entityModel.setValue(new StringFieldModel("subtype", subtype));

            result.add(entityModel);
        });

        return result;
    }

    private static String getStringOrBlank(JSONObject jsonObject, String key){
        if(jsonObject.has(key) && !jsonObject.isNull(key)){
            return jsonObject.getString(key);
        } else {
            return "";
        }
    }

}