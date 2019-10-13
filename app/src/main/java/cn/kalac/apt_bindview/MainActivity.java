package cn.kalac.apt_bindview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import cn.kalac.bindview.ViewBinder;
import cn.kalac.bindview_annotation.BindView;

/**
 * @author kalac.
 * @date 2019/10/13 15:54
 */
public class MainActivity extends AppCompatActivity {
    @BindView(R.id.textview)
    public TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewBinder.bind(this);

        textView.setText("132");
    }
}
