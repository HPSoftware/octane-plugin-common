package com.hpe.adm.octane.ideplugins.integrationtests.util;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.octane.ideplugins.Constants;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskUtils {

    @Inject
    private OctaneProvider octaneProvider;

    /**
     * Creates a Task
     *
     * @param userStory
     *            - user story to attach the task to
     * @param taskName
     *            - the name of the task
     * @return the built entityModel
     */
    public EntityModel createTask(EntityModel userStory, String taskName) {
        EntityModel taskEntityModel = new EntityModel(Constants.TYPE, Entity.TASK.getEntityName());
        taskEntityModel.setValue(new StringFieldModel(Constants.NAME, taskName));
        taskEntityModel.setValue(new ReferenceFieldModel(Entity.USER_STORY.getSubtypeName(), userStory));
        Entity entity = Entity.getEntityType(taskEntityModel);
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(taskEntityModel)).execute().iterator().next();
    }

    public List<EntityModel> getTasks() {
        Octane octane = octaneProvider.getOctane();
        return new ArrayList<>(octane.entityList(Entity.TASK.getApiEntityName()).get().execute());
    }

}