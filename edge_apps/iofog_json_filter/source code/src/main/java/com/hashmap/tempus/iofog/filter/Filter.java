package com.hashmap.tempus.iofog.filter;

import com.google.gson.JsonObject;
import com.hashmap.tempus.iofog.filter.expression.Computable;


public class Filter implements Computable{

    private Computable expression;


    @Override
    public Boolean compute(JsonObject jsonObject) {
        return expression.compute(jsonObject);
    }
}
