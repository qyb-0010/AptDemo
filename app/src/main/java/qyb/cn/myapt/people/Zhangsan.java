package qyb.cn.myapt.people;

import qyb.cn.qyb_anno.Factory;

@Factory(id = "zhangsan", type = People.class)
public class Zhangsan implements People {
    @Override
    public String name() {
        return "zhangsan";
    }
}
