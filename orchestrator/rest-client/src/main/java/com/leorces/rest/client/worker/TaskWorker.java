package com.leorces.rest.client.worker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TaskWorker {

    String topic();

    String processDefinitionKey();

    long interval() default 5;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    long initialDelay() default 0;

    int maxConcurrentTasks() default 1;

}
