package cc.jimblog.imfriendchat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.turing.androidsdk.InitListener;
import com.turing.androidsdk.SDKInit;
import com.turing.androidsdk.SDKInitBuilder;
import com.turing.androidsdk.TuringApiManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import adapter.RebotAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import entity.RebotEntity;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import turing.os.http.core.ErrorMessage;
import turing.os.http.core.HttpConnectionListener;
import turing.os.http.core.RequestResult;
import util.LogUtils;
import util.PermissionCheckerUtil;
import util.ToastUtils;

/**
 * 机器人聊天Activity
 * Created by jimhao on 16/9/3.
 */
public class RebotChatActivity extends SwipeBackActivity {
    @BindView(R.id.tool_bar)
    Toolbar toolBar;
    @BindView(R.id.rebot_toolbar_layout)
    RelativeLayout rebotToolbarLayout;
    @BindView(R.id.rebot_edit_function_btn)
    ImageButton rebotEditFunctionBtn;
    @BindView(R.id.rebot_edit_send_btn)
    ImageButton rebotEditSendBtn;
    @BindView(R.id.rebot_edit_editText)
    EditText rebotEditEditText;
    @BindView(R.id.rebot_edit_layout)
    LinearLayout rebotEditLayout;
    @BindView(R.id.rebot_list)
    RecyclerView rebotList;

    private List<RebotEntity> mDatas = new ArrayList<RebotEntity>();

    private RebotAdapter adapter ;

    private static final String TULING_SECRET = "4145a1cb5f92901b";
    private static final String TULING_KEY = "d975f8141aa550cea27b7f48dd50c48d";

    private TuringApiManager apiManager = null ;
    private boolean isRequireCheck; // 是否需要系统权限检测

    private String[] PERMISSIONS = new String[]{Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_CODE = 0; // 请求码
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_rebot);
        ButterKnife.bind(this);
        initToolBar();
        initAdapter();
        initPermissionState();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initPermissionState() {
        if(PermissionCheckerUtil.getInstance().lacksPermissions(this,PERMISSIONS)){
            ActivityCompat.requestPermissions(this,PERMISSIONS,REQUEST_CODE);
        }else{
            //如果包含所有权限
            initTuLing();
        }
    }

    @OnClick({R.id.rebot_edit_function_btn, R.id.rebot_edit_send_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rebot_edit_function_btn:
                break;
            case R.id.rebot_edit_send_btn:
                sendMessage();
                break;
        }
    }

    /**
     * 初始化ToolBar
     */
    private void initToolBar(){
//        toolBar.setTitle(getString(R.string.fwh_rebot_chat_title));
        toolBar.setTitle("机器人助手");
        toolBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolBar.setBackgroundColor(getResources().getColor(R.color.rebot_main_style));
        setSupportActionBar(toolBar);
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RebotChatActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }
    /**
     * 初始化adapter
     */
    private void initAdapter() {
        //默认装填一条消息
        mDatas.add(new RebotEntity(RebotEntity.Type.INPUT,"欢迎回来父亲大人!"));
        //设置adapter
        adapter = new RebotAdapter(RebotChatActivity.this, mDatas);
        //通过New一个LinearLayoutManager的布局管理器对象来设置RecycleView的布局管理器
        rebotList.setLayoutManager(new LinearLayoutManager(this));
        //设置Item的过渡动画，使用默认的即可
        rebotList.setItemAnimator(new DefaultItemAnimator());
        //设置item为固定大小
        rebotList.setHasFixedSize(true);
        //加载数据
        rebotList.setAdapter(adapter);
    }

    private void initTuLing(){
        //初始化图灵机器人
        SDKInitBuilder builder = new SDKInitBuilder(RebotChatActivity.this).setSecret(TULING_SECRET).setTuringKey(TULING_KEY).setUniqueId("Test");
        SDKInit.init(builder, new InitListener() {
            @Override
            public void onFail(String error) {
                LogUtils.e("图灵机器人初始化失败:"+error);
                ToastUtils.showShort(RebotChatActivity.this,"娜柯酱正在喝红茶,过会儿再来看看吧!");
            }

            @Override
            public void onComplete() {
                LogUtils.i("图灵机器人初始化完成");
                // 获取userid成功后，才可以请求Turing服务器，需要请求必须在此回调成功，才可正确请求
                apiManager = new TuringApiManager(RebotChatActivity.this);
                apiManager.setHttpListener(myHttpConnectionListener);
            }
        });
    }
    /**
     * 向图灵机器人发送消息
     */
    private void sendMessage(){
        String content = rebotEditEditText.getText().toString();
        if (content == null || content.equals("")) {
            return;
        }
        apiManager.requestTuringAPI(content); //发送消息

        RebotEntity entity = new RebotEntity(RebotEntity.Type.OUTPUT,content);
        mDatas.add(entity);
        adapter.notifyDataSetChanged();
        rebotEditEditText.setText("");
        rebotList.scrollToPosition(adapter.getItemCount() - 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //捕获ChatList的点击事件
        rebotList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    //关闭软键盘的重要方法
                    imm.hideSoftInputFromWindow(rebotEditEditText.getWindowToken(), 0);
                    //关闭后使EditText失去焦点
                    rebotEditEditText.clearFocus();
                }
                return false;
            }
        });
    }
    HttpConnectionListener myHttpConnectionListener = new HttpConnectionListener() {
        @Override
        public void onError(ErrorMessage errorMessage) {
            LogUtils.e("图灵Http监听发生错误:"+errorMessage);
            ToastUtils.showShort(RebotChatActivity.this,"啊...产生了不可名状的错误QwQ");
        }

        @Override
        public void onSuccess(RequestResult requestResult) {
            if (requestResult != null) {
                LogUtils.i("onSuccess: " + requestResult.getContent().toString());
                try {
                    JSONObject jsonObj = new JSONObject(requestResult.getContent().toString());
                    if (jsonObj.has("text")){
                        String result = jsonObj.get("text").toString(); //接收到的消息
                        RebotEntity entity = new RebotEntity(RebotEntity.Type.INPUT,result);
                        mDatas.add(entity);
                        adapter.notifyDataSetChanged();
                        rebotList.scrollToPosition(adapter.getItemCount()-1);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    /**
     * 用户权限处理,
     * 如果全部获取, 则直接过.
     * 如果权限缺失, 则提示Dialog.
     *
     * @param requestCode  请求码
     * @param permissions  权限
     * @param grantResults 结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE && hasAllPermissionsGranted(grantResults)) {
            isRequireCheck = true;
            initTuLing();
        } else {
            isRequireCheck = false;
            finish();
        }
    }
    // 含有全部的权限
    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }
}
