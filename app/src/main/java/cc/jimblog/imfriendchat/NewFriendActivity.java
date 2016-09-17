package cc.jimblog.imfriendchat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import util.JianPanUtils;
import util.LogUtils;
import util.SnackBarUtil;

/**
 * Created by jimhao on 16/8/25.
 */
public class NewFriendActivity extends SwipeBackActivity {
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
    private SwipeBackLayout mBackLayout;   //侧滑关闭Activity所用
    private List<UserInfoEntity> mList  ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newfirend);
        ButterKnife.bind(this);
        //侧滑关闭Activity的重要方法
        mBackLayout = getSwipeBackLayout();
        mBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

        gson = new Gson();
        newfriendToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(newfriendToolbar);
        newfriendToolbar.setOnMenuItemClickListener(onMenuItemClick);
        addFriend();
    }
    private void addFriend(){
        Intent intent = getIntent();
        String name = intent.getStringExtra("Name");
        String reason = intent.getStringExtra("Reason");
        if(name!=null && reason != null){
            showAddFriendDialog(name,reason);
        }
    }
    /**
     * 显示添加好友的Dialog
     */
    public void showAddFriendDialog(final String name , String reason){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            //关闭软键盘的重要方法
            imm.hideSoftInputFromWindow(newfriendEdit.getWindowToken(), 0);
            //关闭后使EditText失去焦点
            newfriendEdit.clearFocus();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(NewFriendActivity.this);
        builder.setTitle("来自"+name+"的好友申请");
        builder.setMessage(reason);
        builder.setNegativeButton(getString(R.string.newfriend_yes_add_friend_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //同意添加好友
                try {
                    EMClient.getInstance().contactManager().acceptInvitation(name);
                    finish();
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setPositiveButton(getString(R.string.newfriend_no_add_friend_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //拒绝
                try {
                    EMClient.getInstance().contactManager().declineInvitation(name);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.create().show();
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
        adapter.setOnBtnClickListener(new OnItemsClickListener());
    }
    private class OnItemsClickListener implements NewFriendAdapter.BtnOnClickListener{

        @Override
        public void onClick(View view, int position) {
            view.setBackgroundResource(R.drawable.newfriend_add_btn_shape);
            //参数为要添加的好友的username和添加理由
            final String userName = mList.get(position).getUserId();
            final AlertDialog.Builder builder = new AlertDialog.Builder(NewFriendActivity.this);
            builder.setTitle("发送好友请求");
            final EditText editText = new EditText(NewFriendActivity.this);
            editText.setHint("请输入申请的理由");
            editText.setHintTextColor(getResources().getColor(R.color.second_text_color));
            builder.setView(editText,50,50,50,50);
            builder.setNegativeButton("发送请求", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String reason = editText.getText().toString().trim();
                    try {
                        EMClient.getInstance().contactManager().addContact(userName, reason);
                        showSnackBar("已向"+userName+"发送了好友申请");
                    } catch (HyphenateException e) {
                        e.printStackTrace();
                        Snackbar snackbar = SnackBarUtil.shortSnackbar(searchSnackbarLayout,"发送好友请求失败,请重新尝试！",SnackBarUtil.Alert);
                        snackbar.show();
                    }
                }
            });
            builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    builder.create().dismiss();
                }
            });
            builder.create().show();
        }
    }
    /**
     * 展示SnackBar
     *
     * @param message 消息对象
     */
    public void showSnackBar(String message) {
        Snackbar.make(searchSnackbarLayout, message, 2100).show();
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
        String userId = newfriendEdit.getText().toString();
        if (userId == null || userId.equals("")) {
            showSnackBar(getString(R.string.newfriend_not_edit_hint));
            return ;
        }
        queryUserInfo(userId);
    }
}
