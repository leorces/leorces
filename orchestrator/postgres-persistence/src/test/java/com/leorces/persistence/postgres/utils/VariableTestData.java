package com.leorces.persistence.postgres.utils;

import com.leorces.model.runtime.variable.Variable;

public class VariableTestData {

    public static Variable createOrderVariable() {
        return Variable.builder()
                .varKey("order")
                .varValue("{\"number\":1234}")
                .type("map")
                .build();
    }


    public static Variable createClientVariable() {
        return Variable.builder()
                .varKey("client")
                .varValue("{\"firstName\":\"Json\",\"lastName\":\"Statement\"}")
                .type("map")
                .build();
    }

}
