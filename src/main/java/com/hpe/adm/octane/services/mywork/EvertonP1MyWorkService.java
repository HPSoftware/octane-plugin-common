package com.hpe.adm.octane.services.mywork;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.Query;
import com.hpe.adm.nga.sdk.QueryMethod;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.LongFieldModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.octane.services.EntityService;
import com.hpe.adm.octane.services.UserService;
import com.hpe.adm.octane.services.filtering.Entity;

import java.util.*;
import java.util.stream.Collectors;

import static com.hpe.adm.octane.services.mywork.MyWorkUtil.cloneFieldListMap;
import static com.hpe.adm.octane.services.mywork.MyWorkUtil.getEntityTypeName;

class EvertonP1MyWorkService extends EvertonP2MyWorkService implements MyWorkService {

    @Inject
    private EntityService entityService;

    @Inject
    private UserService userService;

    @Inject
    private MyWorkFilterCriteria myWorkFilterCriteria;

    @Override
    public Collection<EntityModel> getMyWork(Map<Entity, Set<String>> fieldListMap) {

        Map<Entity, Collection<EntityModel>> resultMap;

        //Get entities by query
        resultMap = entityService.concurrentFindEntities(myWorkFilterCriteria.getStaticFilterCriteria(), fieldListMap);

        // Wrap into user items, for backwards compatibility with the UI
        // origin is 0 (because they were fetched via the static query (business rule in the future)
        resultMap
                .keySet()
                .forEach(entityType ->
                        resultMap.put(entityType,
                                MyWorkUtil.wrapCollectionIntoUserItem(resultMap.get(entityType), 0))
                );

        //Get items that were added manually
        Map<Entity, Collection<EntityModel>> addedEntities = getAddedItems(fieldListMap);

        //Also wrap the addedEntities with origin 1
        addedEntities
                .keySet()
                .forEach(entityType ->
                        addedEntities.put(entityType,
                                MyWorkUtil.wrapCollectionIntoUserItem(addedEntities.get(entityType), 1))
                );

        //Make sure the result map has all the keys necessary to merge the two maps
        addedEntities
                .keySet()
                .stream()
                .filter(entityType -> !resultMap.containsKey(entityType))
                .forEach(entityType -> resultMap.put(entityType, new ArrayList<>()));

        //Merge the two maps, check to not add duplicates
        addedEntities
                .keySet()
                .forEach(entityType -> {
                    Collection<EntityModel> queryEntitiesByKey = resultMap.get(entityType);
                    Collection<EntityModel> addedEntitiesByKey = addedEntities.get(entityType);

                    for(EntityModel userItem : addedEntitiesByKey){
                        if(!MyWorkUtil.containsUserItem(queryEntitiesByKey, userItem)){
                            resultMap.get(entityType).add(userItem);
                        }
                    }
                });

        //Convert map to a list and return
        return resultMap
                .keySet()
                .stream()
                .sorted(Comparator.comparing(Enum::name))
                .flatMap(entityType -> resultMap.get(entityType).stream())
                .collect(Collectors.toList());
    }

    protected Map<Entity, Collection<EntityModel>> getAddedItems(Map<Entity, Set<String>> fieldListMap) {

        final Map<Entity, Set<String>> fieldListMapCopy = cloneFieldListMap(fieldListMap);

        String addToMyWorkFieldName = "user_item";

        Map<Entity, Query.QueryBuilder> followFilterCriteria = new HashMap<>();

        myWorkFilterCriteria.getStaticFilterCriteria()
                .keySet()
                .stream()
                .filter(this::isAddingToMyWorkSupported)
                .forEach(key -> {
                    Query.QueryBuilder qb;
                    if (key.isSubtype()) {
                        qb = key.createMatchSubtypeQueryBuilder().and(createUserItemQueryBuilder());
                    } else {
                        qb = createUserItemQueryBuilder();
                    }
                    followFilterCriteria.put(key, qb);
                    if (fieldListMapCopy != null && fieldListMapCopy.containsKey(key)) {
                        fieldListMapCopy.get(key).add(addToMyWorkFieldName);
                    }
                });

        return entityService.concurrentFindEntities(followFilterCriteria, fieldListMapCopy);
    }

    @Override
    protected EntityModel createNewUserItem(EntityModel wrappedEntityModel){
        EntityModel newUserItem = new EntityModel();
        newUserItem.setValue(new LongFieldModel("origin", 1L));
        newUserItem.setValue(new ReferenceFieldModel("reason", null));
        String entityType =  getEntityTypeName(Entity.getEntityType(wrappedEntityModel));
        newUserItem.setValue(new StringFieldModel("entity_type", entityType));
        newUserItem.setValue(new ReferenceFieldModel("user", userService.getCurrentUser()));
        String followField = "my_follow_items_" + getEntityTypeName(Entity.getEntityType(wrappedEntityModel));
        newUserItem.setValue(new ReferenceFieldModel(followField, wrappedEntityModel));
        return newUserItem;
    }

    private Query.QueryBuilder createUserItemQueryBuilder() {
        return Query.statement("user_item", QueryMethod.EqualTo,
                Query.statement("user", QueryMethod.EqualTo,
                        Query.statement("id", QueryMethod.EqualTo, userService.getCurrentUserId())
                )
        );
    }

}