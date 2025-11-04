package com.leorces.model.definition.activity;

import java.util.List;
import java.util.Map;

public interface ActivityDefinition {

    String id();

    String parentId();

    String name();

    ActivityType type();

    List<String> incoming();

    List<String> outgoing();

    Map<String, Object> inputs();

    Map<String, Object> outputs();

}
