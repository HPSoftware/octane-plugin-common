package com.hpe.adm.octane.services.util;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.services.filtering.Entity;
import org.json.JSONObject;

public class PartialEntity extends EntityTypeIdPair {

    String entityName;

    public PartialEntity(EntityModel entityModel){
        super(
                Long.parseLong(entityModel.getValue("id").getValue().toString()),
                Entity.getEntityType(entityModel)
        );
        this.entityName = entityModel.getValue("name").getValue().toString();
    }

    public PartialEntity(Long entityId, String entityName, Entity entityType){
        super(entityId, entityType);
        this.entityName = entityName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public static JSONObject toJsonObject(PartialEntity partialEntity){
        JSONObject jsonObject = EntityTypeIdPair.toJsonObject(partialEntity);
        jsonObject.put("entityName", partialEntity.getEntityName());
        return jsonObject;
    }

    public static PartialEntity fromJsonObject(JSONObject jsonObject){
        if (jsonObject == null)
            return null;
        EntityTypeIdPair entityTypeIdPair = EntityTypeIdPair.fromJsonObject(jsonObject);
        String entityName = jsonObject.getString("entityName");

        PartialEntity partialEntity = new PartialEntity(
                entityTypeIdPair.getEntityId(),
                entityName,
                entityTypeIdPair.getEntityType());

        return partialEntity;
    }
}