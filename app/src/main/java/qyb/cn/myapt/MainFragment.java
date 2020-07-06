package qyb.cn.myapt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import qyb.cn.myapt.food.Food;
import qyb.cn.myapt.food.FoodFactory;
import qyb.cn.myapt.people.People;
import qyb.cn.myapt.people.PeopleFactory;
import qyb.cn.qyb_anno.BindView;
import qyb.cn.view_injector.ViewInjector;

public class MainFragment extends Fragment {

    @BindView(R.id.tv2)
    public TextView tv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewInjector.inject(this, view);

        Food hamburger = new FoodFactory().create("hamburger");
        People p = new PeopleFactory().create("zhangsan");
        tv.setText(p.name() + " order a hamburger which price is " + hamburger.getPrice());
    }
}
