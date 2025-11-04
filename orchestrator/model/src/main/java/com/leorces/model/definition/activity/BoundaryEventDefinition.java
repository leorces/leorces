package com.leorces.model.definition.activity;

public interface BoundaryEventDefinition extends ActivityDefinition {

    String attachedToRef();

    boolean cancelActivity();

}
