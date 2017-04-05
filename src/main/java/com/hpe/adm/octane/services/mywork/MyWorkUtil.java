package com.hpe.adm.octane.services.mywork;

import com.hpe.adm.nga.sdk.Query;
import com.hpe.adm.nga.sdk.QueryMethod;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.LongFieldModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.octane.services.exception.ServiceRuntimeException;
import com.hpe.adm.octane.services.filtering.Entity;
import com.hpe.adm.octane.services.util.EntityUtil;

import java.util.*;
import java.util.stream.Collectors;

public class MyWorkUtil {

    static final Set<Entity> addToMyWorkEntities = new HashSet<>();
    static {
        addToMyWorkEntities.add(Entity.USER_STORY);
        addToMyWorkEntities.add(Entity.DEFECT);
        addToMyWorkEntities.add(Entity.TASK);
        addToMyWorkEntities.add(Entity.QUALITY_STORY);
        addToMyWorkEntities.add(Entity.MANUAL_TEST);
        addToMyWorkEntities.add(Entity.GHERKIN_TEST);
        addToMyWorkEntities.add(Entity.MANUAL_TEST_RUN);
        addToMyWorkEntities.add(Entity.TEST_SUITE_RUN);
    }

    /**
     * Constructs a metaphase query builder to match "logical_name":"metaphase.entity.phasename",
     *
     * @param entity
     * @param phases
     * @return
     */
    static Query.QueryBuilder createPhaseQuery(Entity entity, String... phases) {
        Query.QueryBuilder phaseQueryBuilder = null;
        for (String phaseName : phases) {
            String phaseLogicalName = "metaphase." + entity.getTypeName() + "." + phaseName;
            Query.QueryBuilder currentPhaseQueryBuilder =
                    Query.statement("metaphase", QueryMethod.EqualTo,
                            Query.statement("logical_name", QueryMethod.EqualTo, phaseLogicalName)
                    );
            if (phaseQueryBuilder == null) {
                phaseQueryBuilder = currentPhaseQueryBuilder;
            } else {
                phaseQueryBuilder = phaseQueryBuilder.or(currentPhaseQueryBuilder);
            }
        }

        return Query.statement("phase", QueryMethod.EqualTo, phaseQueryBuilder);
    }

    /**
     * @param logicalNames
     * @return
     */
    static Query.QueryBuilder createNativeStatusQuery(String... logicalNames) {
        Query.QueryBuilder nativeStatusQueryBuilder = null;
        for (String logicalName : logicalNames) {
            Query.QueryBuilder currentNativeStatusQueryBuilder =
                    Query.statement("logical_name", QueryMethod.EqualTo, logicalName);
            if (nativeStatusQueryBuilder == null) {
                nativeStatusQueryBuilder = currentNativeStatusQueryBuilder;
            } else {
                nativeStatusQueryBuilder = nativeStatusQueryBuilder.or(currentNativeStatusQueryBuilder);
            }
        }
        return  Query.statement("native_status", QueryMethod.EqualTo, nativeStatusQueryBuilder);
    }

    static Query.QueryBuilder createUserQuery(String fieldName, Long userId) {
        return  Query.statement(fieldName, QueryMethod.EqualTo,
                Query.statement("id", QueryMethod.EqualTo, userId));
    }

    /**
     * Fetch the entity model wrapped in the user item for the UI
     * @param userItem
     * @return
     */
    public static EntityModel getEntityModelFromUserItem(EntityModel userItem){
        if(Entity.USER_ITEM != Entity.getEntityType(userItem)){
            throw new ServiceRuntimeException("Given param entity is not of type: user_item, type is: " + Entity.getEntityType(userItem));
        }
        String followField = "my_follow_items_" + userItem.getValue("entity_type").getValue();
        return (EntityModel) userItem.getValue(followField).getValue();
    }

    public static Collection<EntityModel> getEntityModelsFromUserItems(Collection<EntityModel> userItems) {
        return userItems
                .stream()
                .map(MyWorkUtil::getEntityModelFromUserItem)
                .collect(Collectors.toList());
    }

    /**
     * Used for backwards compatibility for older server version
     * For older server version plain entity models will be wrapped in user items
     * @param entityModel
     * @return
     */
    static EntityModel wrapEntityModelIntoUserItem(EntityModel entityModel, long origin){
        EntityModel userItem = new EntityModel();

        String followField = "my_follow_items_" + getEntityTypeName(Entity.getEntityType(entityModel));
        userItem.setValue(new ReferenceFieldModel(followField, entityModel));
        userItem.setValue(new StringFieldModel("entity_type", getEntityTypeName(Entity.getEntityType(entityModel))));

        userItem.setValue(new LongFieldModel("origin", origin));
        userItem.setValue(new StringFieldModel("type", "user_item"));
        userItem.setValue(new StringFieldModel("id", "-1"));

        return userItem;
    }

    static Collection<EntityModel> wrapCollectionIntoUserItem(Collection<EntityModel> entityModels, long origin){
        Collection<EntityModel> result = new ArrayList<>();
        entityModels.forEach(entityModel -> result.add(wrapEntityModelIntoUserItem(entityModel, origin)));
        return result;
    }

    static boolean containsUserItem(Collection<EntityModel> userItems, EntityModel userItem){
        return userItems
                .stream()
                .map(MyWorkUtil::getEntityModelFromUserItem)
                .anyMatch(entityModel -> EntityUtil.areEqual(entityModel, MyWorkUtil.getEntityModelFromUserItem(userItem)));
    }

    static String getEntityTypeName(Entity entity){
        if(entity.isSubtype()){
            return entity.getSubtypeOf().getTypeName();
        } else {
            return entity.getTypeName();
        }
    }

    static Map<Entity, Set<String>> cloneFieldListMap(Map<Entity, Set<String>> fieldListMap) {
        Map<Entity, Set<String>> fieldListMapCopy = new HashMap<>();
        if (fieldListMap == null) {
            fieldListMapCopy = null;
        } else {
            for (Entity key : fieldListMap.keySet()) {
                Set<String> value = fieldListMap.get(key);
                if (value == null) {
                    fieldListMapCopy.put(key, null);
                } else {
                    fieldListMapCopy.put(key, new HashSet<>(value));
                }
            }
        }
        return fieldListMapCopy;
    }

    private static void verifyUserItem(EntityModel entityModel){
        if(Entity.USER_ITEM != Entity.getEntityType(entityModel)){
            throw new ServiceRuntimeException("Given param entity is not of type: user_item, type is: " + Entity.getEntityType(entityModel));
        }
    }

    /**
     * Check if the user item is dismissible or not, uses field "origin" from user item. <br>
     * Origin field value meaning:
     *     <ul>
     *         <li>0: was added by business rule (matched some query, for eg. user story in phase new</li>
     *         <li>1: was added by right click -> context menu -> "Add to My Work"</li>
     *     </ul>
     * @param userItem
     * @return
     */
    public static boolean isUserItemDismissible(EntityModel userItem){
        verifyUserItem(userItem);
        return userItem.getValue("origin").getValue().equals(1L);
    }

}