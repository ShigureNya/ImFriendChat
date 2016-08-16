package cc.jimblog.imfriendchat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jimhao on 16/8/16.
 */
public class ChatListActivity extends AppCompatActivity {
    @BindView(R.id.tool_bar)
    Toolbar toolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);
        toolBar.setTitle("Test");
        setSupportActionBar(toolBar);
    }
}
