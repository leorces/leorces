package com.leorces.model.definition.activity;

public interface ExternalTaskDefinition extends ActivityDefinition {

    String topic();

    Integer retries();

    String timeout();

}
