package qyb.cn.myapt.food;

import qyb.cn.qyb_anno.Factory;

@Factory(id = "hamburger", type = Food.class)
public class Hamburger implements Food {
    @Override
    public float getPrice() {
        return 15.5F;
    }
}
