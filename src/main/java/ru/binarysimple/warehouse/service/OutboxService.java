package ru.binarysimple.warehouse.service;

import ru.binarysimple.warehouse.model.EventType;
import ru.binarysimple.warehouse.model.ParentType;

public interface OutboxService {

    void saveEvent(EventType eventType, String parentId, ParentType parentType, Object payload, String topic);

    void processOutbox();
}
