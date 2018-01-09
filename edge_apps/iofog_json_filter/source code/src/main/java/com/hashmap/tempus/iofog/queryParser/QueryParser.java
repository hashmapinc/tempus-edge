package com.hashmap.tempus.iofog.queryParser;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hashmap.tempus.iofog.filter.AndFilter;
import com.hashmap.tempus.iofog.filter.Filter;
import com.hashmap.tempus.iofog.filter.ORFilter;
import com.hashmap.tempus.iofog.filter.expression.BooleanExpression;
import com.hashmap.tempus.iofog.filter.expression.Computable;
import com.hashmap.tempus.iofog.filter.expression.DoubleExpression;
import com.hashmap.tempus.iofog.filter.expression.StringExpression;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class QueryParser {


    public static Computable getFilter(JsonObject jsonObject)
    {
        return getComputable(jsonObject);
    }

    private static Computable getComputableObject(String computable,JsonObject jsonObject)
    {
        computable=computable.toUpperCase();
        switch (computable)
        {
            case "AND":
                 AndFilter andFilter=  new AndFilter();
                 andFilter.setExp1(getComputable( jsonObject.getAsJsonObject("exp1")));
                 andFilter.setExp2(getComputable( jsonObject.getAsJsonObject("exp2")));
                   return  andFilter;
            case "OR":
                ORFilter oRFilter=  new ORFilter();
                oRFilter.setExp1(getComputable( jsonObject.getAsJsonObject("exp1")));
                oRFilter.setExp2(getComputable( jsonObject.getAsJsonObject("exp2")));
                return oRFilter;

            case "STRING":

                return new StringExpression(jsonObject);
            case "BOOLEAN":
                return new BooleanExpression(jsonObject);
            case "DOUBLE":
                return  new DoubleExpression(jsonObject);
            default: return null;
        }
    }
    private static Computable getComputable(JsonObject jsonObject)
    {
        if(jsonObject.size()>1)
            return null;
        Map.Entry<String, JsonElement> itr=jsonObject.entrySet().iterator().next();
        return getComputableObject(itr.getKey(),itr.getValue().getAsJsonObject());

    }

    public static void main(String[] args)
    {
       String filter="{\"String\":{\"term\":\"value.a.a\",\"OP\":\"NEQ\",\"value\":\"11.0\"}}";
       String filter2="{\"BOOLEAN\":{\"term\":\"value.b\",\"OP\":\"NEQ\",\"value\":\"true\"}}";
       String filter3="{\"AND\":{ \"exp1\":"+filter+",\"exp2\":"+filter2+"}}";
        String filter4="{\"OR\":{ \"exp1\":"+filter+",\"exp2\":"+filter2+"}}";
        String filter5="{\"OR\":{ \"exp1\":"+filter3+",\"exp2\":"+filter4+"}}";

        Gson gson = new Gson();
       System.out.println(gson.toJson(QueryParser.getFilter(gson.fromJson(filter3,JsonObject.class))));
        System.out.println(QueryParser.getFilter(gson.fromJson(filter3,JsonObject.class)).getClass());
        System.out.println(gson.toJson(QueryParser.getFilter(gson.fromJson(filter,JsonObject.class))));
        System.out.println(QueryParser.getFilter(gson.fromJson(filter,JsonObject.class)).getClass());
        System.out.println(gson.toJson(QueryParser.getFilter(gson.fromJson(filter2,JsonObject.class))));
        System.out.println(QueryParser.getFilter(gson.fromJson(filter2,JsonObject.class)).getClass());
        System.out.println(gson.toJson(QueryParser.getFilter(gson.fromJson(filter5,JsonObject.class))));
        System.out.println(QueryParser.getFilter(gson.fromJson(filter5,JsonObject.class)).getClass());


        String valueJson="{\"value\":{\"a\":{\"a\":\"11.0\",\"b\":\"false\"},\"b\":\"false\"}}";
        System.out.println(gson.fromJson(valueJson,JsonObject.class).get("value").toString());

        System.out.println(QueryParser.getFilter(gson.fromJson(filter3,JsonObject.class)).compute(gson.fromJson(valueJson,JsonObject.class)));
        System.out.println(QueryParser.getFilter(gson.fromJson(filter4,JsonObject.class)).compute(gson.fromJson(valueJson,JsonObject.class)));
        System.out.println(QueryParser.getFilter(gson.fromJson(filter2,JsonObject.class)).compute(gson.fromJson(valueJson,JsonObject.class)));



    }
}
