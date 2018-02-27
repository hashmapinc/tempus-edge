package com.hashmapinc.tempus.iofog.filter.expression;

import com.google.gson.JsonObject;
import com.hashmapinc.tempus.iofog.filter.expression.operatios.OperationEnum;

public class StringExpression extends Expression{
    private String value;

    public StringExpression()
    {

    }
    public StringExpression(JsonObject jsonObject)
    {
        this.term=jsonObject.get("term").getAsString();
        this.value =jsonObject.get("value").getAsString();
        this.operation= OperationEnum.valueOf(jsonObject.get("OP").getAsString());

    }
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override

    public Boolean compute(JsonObject jsonObject) {
        String termValue=getTermAsJsonObject(term,jsonObject).getAsString();
        if(termValue==null)
            return false;
        Boolean retValue =false;
        switch (operation)
        {
            case EQ: if(termValue.equalsIgnoreCase(value))
                retValue=true;
                break;
            case NEQ:if(!termValue.equalsIgnoreCase(value))
                retValue=true;
            break;
            default:retValue =false;
        }
        return retValue;
    }
}
