package com.hashmap.tempus.iofog.filter.expression;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hashmap.tempus.iofog.filter.expression.operatios.OperationEnum;

public abstract class  Expression implements Computable{

    protected String term;
    protected OperationEnum operation;

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public OperationEnum getOperation() {
        return operation;
    }

    public void setOperation(OperationEnum operation) {
        this.operation = operation;
    }

    protected JsonElement getTermAsJsonObject(String term, JsonObject jsonObject)
    {
        String[] tokens= term.split("\\.");
        JsonObject temp= jsonObject;
        JsonElement returnElement=null;
        try {
            for (int i=0;i<tokens.length-1;i++){
                    temp = temp.getAsJsonObject(tokens[i]);
            }
            returnElement=temp.get(tokens[tokens.length-1]);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            returnElement=null;
        }
        return returnElement;
    }

}
