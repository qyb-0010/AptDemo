package qyb.cn.myapt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import qyb.cn.qyb_anno.BindView;
import qyb.cn.view_injector.ViewInjector;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.test1)
    public TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewInjector.inject(this);
        tv.setText("hahahaha");
        getSupportFragmentManager().beginTransaction().add(R.id.container, new MainFragment()).commit();
    }
}