package com.hashmap.tempus.iofog.filter.expression;

import com.google.gson.JsonObject;
import com.hashmap.tempus.iofog.filter.expression.operatios.OperationEnum;

public class DoubleExpression extends Expression {
    private double value;

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
    public DoubleExpression(JsonObject jsonObject) {
        this.term = jsonObject.get("term").getAsString();
        this.value = jsonObject.get("value").getAsDouble();
        this.operation = OperationEnum.valueOf(jsonObject.get("OP").getAsString());
    }
    public DoubleExpression() {
    }
    @Override

    public Boolean compute(JsonObject jsonObject) {
        Double termValue=getTermAsJsonObject(term,jsonObject).getAsDouble();
        if(termValue==null)
            return false;
        Boolean retValue =false;
        switch (operation)
        {
            case EQ: if(termValue==value)
                      retValue=true;
                      break;
            case GT: if(termValue>value)
                      retValue=true;
                      break;
            case LT: if(termValue<value)
                      retValue=true;
                        break;
            case NEQ:if(termValue!=value)
                retValue=true;
                break;
            case GTEQ: if(termValue>=value)
                retValue=true;
                break;
            case LTEQ:if(termValue<=value)
                retValue=true;
                break;
            default:retValue =false;
        }
        return retValue;
    }
}
