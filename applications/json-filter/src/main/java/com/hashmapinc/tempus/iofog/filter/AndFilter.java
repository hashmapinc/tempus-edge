package com.hashmapinc.tempus.iofog.filter;

import com.google.gson.JsonObject;
import com.hashmapinc.tempus.iofog.filter.expression.Computable;


public class AndFilter  implements Computable{

    Computable exp1;
    Computable exp2;

    public Computable getExp1() {
        return exp1;
    }

    public void setExp1(Computable exp1) {
        this.exp1 = exp1;
    }

    public Computable getExp2() {
        return exp2;
    }

    public void setExp2(Computable exp2) {
        this.exp2 = exp2;
    }

    @Override
    public Boolean compute(JsonObject jsonObject) {

        return exp1.compute(jsonObject)&& exp2.compute(jsonObject);
    }
}
