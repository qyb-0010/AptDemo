package qyb.cn.myapt.food;

import qyb.cn.qyb_anno.Factory;

@Factory(id = "fried_chicken", type = Food.class)
public class FriedChicken implements Food {
    @Override
    public float getPrice() {
        return 10F;
    }
}
