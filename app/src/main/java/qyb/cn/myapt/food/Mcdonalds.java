package qyb.cn.myapt.food;

import android.text.TextUtils;

public class Mcdonalds {

    public Food order(String name) {
        if (TextUtils.isEmpty(name)) {
            throw new RuntimeException("name cannot be empty");
        }
        if (name.equals("fired_chicken")) {
            return new FriedChicken();
        }
        if (name.equals("hamburger")) {
            return new Hamburger();
        }
        if (name.equals("ice_cream")) {
            return new IceCream();
        }
        throw new RuntimeException("no such food " + name);
    }
}
