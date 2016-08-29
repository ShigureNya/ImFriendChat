package cc.jimblog.imfriendchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import org.json.JSONArray;

import java.util.List;

import adapter.NewFriendAdapter;
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
    private Gson gson;
    private NewFriendAdapter adapter ;

    private List<UserInfoEntity> mList  ;
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
        JianPanUtils.openKeybord(newfriendEdit, this);

        newfriendToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NewFriendActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        newfriendEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchContent();
                }
                return false;
            }
        });
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
        if(!json.contains("createdAt")){
           showSnackBar(getString(R.string.newfriend_not_found_info_hint));
            return ;
        }
        mList = jsonToList(json);
        //设置adapter
        adapter = new NewFriendAdapter(NewFriendActivity.this, mList);
        //通过New一个LinearLayoutManager的布局管理器对象来设置RecycleView的布局管理器
        newfriendList.setLayoutManager(new LinearLayoutManager(NewFriendActivity.this));
        //设置Item的过渡动画，使用默认的即可
        newfriendList.setItemAnimator(new DefaultItemAnimator());
        //设置item为固定大小
        newfriendList.setHasFixedSize(true);
        //设置分割线
        //newfriendList.addItemDecoration(new DividerItemDecoration(NewFriendActivity.this, DividerItemDecoration.VERTICAL_LIST));
        //加载数据
        newfriendList.setAdapter(adapter);
        //设置数据监听
        adapter.setOnBtnClickListener(new OnItemClickListener());
    }
    private class OnItemClickListener implements NewFriendAdapter.BtnOnClickListener{

        @Override
        public void onClick(View view, int position) {
            //参数为要添加的好友的username和添加理由
            String userName = mList.get(position).getUserId();
            String reason = "Hello , World";
            try {
                EMClient.getInstance().contactManager().addContact(userName, reason);
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 展示SnackBar
     *
     * @param message 消息对象
     */
    public void showSnackBar(String message) {
        Snackbar.make(searchSnackbarLayout, message, 1500).show();
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.friend_menu_search:
                    searchContent();
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
        LogUtils.i("Json数据:"+json);
        List<UserInfoEntity> entityList = gson.fromJson(json, new TypeToken<List<UserInfoEntity>>() {

        }.getType());
        return entityList;
    }

    @Override
    protected void onPause() {
        super.onPause();
        JianPanUtils.closeKeybord(newfriendEdit,this);
    }

    private void searchContent(){
        if(adapter!=null){
            mList.clear();
            adapter.notifyDataSetChanged();
        }
        JianPanUtils.closeKeybord(newfriendEdit, NewFriendActivity.this);
        LogUtils.i("点击了");
        String userId = newfriendEdit.getText().toString();
        if (userId == null || userId.equals("")) {
            showSnackBar(getString(R.string.newfriend_not_edit_hint));
            return ;
        }
        queryUserInfo(userId);
    }
}
