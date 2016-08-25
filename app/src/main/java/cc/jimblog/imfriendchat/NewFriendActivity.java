package cc.jimblog.imfriendchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import entity.UserInfoEntity;
import util.JianPanUtils;
import util.LogUtils;

/**
 * Created by jimhao on 16/8/25.
 */
public class NewFriendActivity extends AppCompatActivity {
    @BindView(R.id.newfriend_edit)
    EditText newfriendEdit;
    @BindView(R.id.newfriend_toolbar)
    Toolbar newfriendToolbar;
    @BindView(R.id.newfriend_qr_layout)
    LinearLayout newfriendQrLayout;
    @BindView(R.id.newfriend_add_contacts_layout)
    LinearLayout newfriendAddContactsLayout;
    @BindView(R.id.search_snackbar_layout)
    CoordinatorLayout searchSnackbarLayout;
    @BindView(R.id.newfriend_list)
    RecyclerView newfriendList;
    @BindView(R.id.newfriend_refresh)
    SwipeRefreshLayout newfriendRefresh;

    private Gson gson;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newfirend);
        ButterKnife.bind(this);
        gson = new Gson();
        newfriendToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(newfriendToolbar);
        newfriendToolbar.setOnMenuItemClickListener(onMenuItemClick);
    }

    @Override
    protected void onResume() {
        super.onResume();
        newfriendToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NewFriendActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        JianPanUtils.openKeybord(newfriendEdit, this);

    }

    @OnClick({R.id.newfriend_qr_layout, R.id.newfriend_add_contacts_layout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.newfriend_qr_layout:
                break;
            case R.id.newfriend_add_contacts_layout:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_friend_menu, menu);
        return true;
    }

    private void queryUserInfo(String userId) {
        BmobQuery<UserInfoEntity> query = new BmobQuery<UserInfoEntity>("userinfo");
        query.addWhereContains("userId", userId);
        query.setLimit(5);  //查询数量
        query.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray jsonArray, BmobException e) {
                if (e == null) {
                    displayUserInfoList(jsonArray.toString());
                } else {
                    showSnackBar(getString(R.string.newfriend_not_found_info_hint));
                }
            }
        });
    }

    private void displayUserInfoList(String json) {
        List<UserInfoEntity> mList = jsonToList(json);

    }

    /**
     * 展示SnackBar
     *
     * @param message 消息对象
     */
    public void showSnackBar(String message) {
        Snackbar.make(searchSnackbarLayout, message, 1300).show();
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.friend_menu_search:
                    JianPanUtils.closeKeybord(newfriendEdit, NewFriendActivity.this);
                    LogUtils.i("点击了");
                    String userId = newfriendEdit.getText().toString();
                    if (userId == null || userId.equals("")) {
                        showSnackBar(getString(R.string.newfriend_not_edit_hint));
                        return false;
                    }
                    queryUserInfo(userId);
                    break;
            }
            return true;
        }
    };

    /**
     * @param json 将JSON转换为List集合
     * @return 实体集合
     */
    public List<UserInfoEntity> jsonToList(String json) {
        List<UserInfoEntity> entityList = gson.fromJson(json, new TypeToken<List<UserInfoEntity>>() {

        }.getType());
        return entityList;
    }
}
