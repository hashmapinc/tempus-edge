package com.hashmapinc.tempus.iofog.filter.expression;

import com.google.gson.JsonObject;
import com.hashmapinc.tempus.iofog.filter.expression.operatios.OperationEnum;


public class BooleanExpression extends Expression{
    private boolean value;
    public BooleanExpression(JsonObject jsonObject) {
        this.term = jsonObject.get("term").getAsString();
        this.value = jsonObject.get("value").getAsBoolean();
        this.operation = OperationEnum.valueOf(jsonObject.get("OP").getAsString());
    }
    public BooleanExpression() {

    }
    public boolean isValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override

    public Boolean compute(JsonObject jsonObject) {

        Boolean termValue=getTermAsJsonObject(term,jsonObject).getAsBoolean();
        if(termValue==null)
            return false;
        Boolean retValue =false;
        switch (operation)
        {
            case EQ: if(termValue==value) {
                retValue = true;
            }
            break;
            case NEQ:if(termValue==!value) {
                retValue = true;
            }
            break;
            default:retValue =false;
        }
        return retValue;
    }
}
