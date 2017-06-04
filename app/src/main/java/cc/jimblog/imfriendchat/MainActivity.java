package cc.jimblog.imfriendchat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import adapter.MainPageAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import entity.ContextSave;
import entity.UserInfoEntity;
import fragment.ChatFragment;
import fragment.ContactsFragment;
import fragment.FuncationFragment;
import fragment.SettingFragment;
import image.LocalCacheUtil;
import image.MyBitmapCacheUtil;
import service.MessageService;
import util.BitmapUtils;
import util.FileTools;
import util.JsonUtil;
import util.LogUtils;
import util.NetWorkUtils;
import util.SnackBarUtil;
import util.StorageUtils;
import util.ToastUtils;
import view.CircleImageView;

public class MainActivity extends AppCompatActivity implements SettingFragment.OnLogOutClickListener {

    @BindView(R.id.main_tool_bar)
    Toolbar mainToolBar;
    @BindView(R.id.main_tab_layout)
    TabLayout mainTabLayout;
    @BindView(R.id.main_viewpager)
    ViewPager mainViewpager;
    @BindView(R.id.main_navigation_view)
    NavigationView mainNavigationView;
    @BindView(R.id.main_drawer_layout)
    DrawerLayout mainDrawerLayout;
    @BindView(R.id.main_snackbar_layout)
    CoordinatorLayout mainSnackbarLayout;

    private List<String> mTitleList = new ArrayList<String>();  //存放标题
    private ChatFragment chatFragment = null;  //聊天选项卡
    private ContactsFragment contactsFragment = null;  //联系人
    private FuncationFragment funcationFragment = null;       //功能页面
    private SettingFragment settingFragment = null;    //设置页面
    private List<Fragment> mFragmentList = new ArrayList<Fragment>();   //存放Fragment对象
    private ActionBarDrawerToggle mDrawerToggle;   //监听DrawerLayout滑动和弹出事件
    private MainPageAdapter mAdapter;  //主页适配器
    private LocalCacheUtil localCacheUtil ; //本地缓存类
    public static final int IS_BACKGROUND_PERMISSON = 1;  //权限管理
    private long maxImageSize = 1024 * 1024 * 2;    //设置文件背景上传的最大大小

    private String objectId ;   //用户在bmob服务器上的Id
    /**
     * 初始化数据的方法
     */
    private void initData() {
        ContextSave.MainActivity = MainActivity.this;

        chatFragment = new ChatFragment();
        contactsFragment = new ContactsFragment();
        funcationFragment = new FuncationFragment();
        settingFragment = new SettingFragment();
        mFragmentList.add(chatFragment);
        mFragmentList.add(contactsFragment);
        mFragmentList.add(funcationFragment);
        mFragmentList.add(settingFragment);

        mTitleList.add(getString(R.string.main_chat_fragment_title));
        mTitleList.add(getString(R.string.main_contacts_fragment_title));
        mTitleList.add(getString(R.string.main_function_fragment_title));
        mTitleList.add(getString(R.string.main_setting_fragment_title));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mainToolBar.setTitle(R.string.main_chat_fragment_title);
        setSupportActionBar(mainToolBar);
        localCacheUtil = new LocalCacheUtil();
        mainToolBar.setOnMenuItemClickListener(new OnToolBarListener());
        initData();
        initDrawerLayout();
        initTabLayout();
        initFragmentAdapter();

        if (NetWorkUtils.isNetworkAvailable(this)) {
            initHeaderUserInfo();   //初始化侧滑数据
            registerService();  //注册消息广播监听
        } else {
            Snackbar snackbar = SnackBarUtil.longSnackbar(mainSnackbarLayout,"当前网络无连接",SnackBarUtil.Info);
            snackbar.setDuration(2100);
            snackbar.show();
        }
    }

    /**
     * 初始化DrawerLayout
     */
    public void initDrawerLayout() {
        //initDrawerLayout
        mDrawerToggle = new ActionBarDrawerToggle(this, mainDrawerLayout, mainToolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerToggle.syncState();  //init
        mainDrawerLayout.setDrawerListener(mDrawerToggle);
        //为NavigationView设置菜单选中监听
        mainNavigationView.setNavigationItemSelectedListener(new MyNavigationItemListener());
        mainNavigationView.setCheckedItem(0);
    }

    /**
     * 为TabLayout填充数据
     */
    public void initTabLayout() {
        //initTabLayout
        mainTabLayout.setTabMode(TabLayout.MODE_FIXED);
        for (int i = 0; i < mTitleList.size(); i++) {
            mainTabLayout.addTab(mainTabLayout.newTab().setText(mTitleList.get(i)));
        }
    }

    /**
     * 为ViewPager填充Adapter数据
     */
    public void initFragmentAdapter() {
        //initFragmentAdapter
        mAdapter = new MainPageAdapter(getSupportFragmentManager(), mFragmentList, mTitleList);
        mainViewpager.setCurrentItem(0);    //设置默认加载为聊天窗口
        mainViewpager.setOffscreenPageLimit(4); //ViewPager缓存
        mainTabLayout.setupWithViewPager(mainViewpager);
        mainTabLayout.setTabsFromPagerAdapter(mAdapter);
        mainViewpager.setAdapter(mAdapter);
        mainTabLayout.setOnTabSelectedListener(tabSelectedListener);

    }

    /**
     * 此处为LoginOut的退出回掉事件 并非Activity的点击事件
     */
    @Override
    public void onClick() {
        StorageUtils.put(this,"HeaderUserName","");
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 抽屉中Menu的点击事件
     */
    class MyNavigationItemListener implements NavigationView.OnNavigationItemSelectedListener {

        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.drawer_function_chat:
                    //会话
                    mainViewpager.setCurrentItem(0);
                    break;
                case R.id.drawer_function_contacts:
                    //联系人
                    mainViewpager.setCurrentItem(1);
                    break;
                case R.id.drawer_online_cloud:
                    //云盘 自建云逻辑中 实质上就是自己上传到云上的文件集合
                    break;
                case R.id.drawer_online_qrcode:
                    //我的二维码
                    startActivity(new Intent(MainActivity.this,MyQRCodeActivity.class));
                    return false;
                case R.id.drawer_setting_set:
                    //设置
                    break;
                case R.id.drawer_setting_share:
                    //分享
                    showShare();
                    mainDrawerLayout.closeDrawer(GravityCompat.START);
                    return false;
            }
            item.setCheckable(true);    //设为选中
            mainDrawerLayout.closeDrawer(GravityCompat.START);  //关闭抽屉
            return true;
        }
    }

    /**
     * Tab的切换事件
     */
    TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            int position = tab.getPosition();
            mainViewpager.setCurrentItem(position); //点击TabLayout时切换页卡，并设置标题，根据得到的Position可以统一进行设置
            mainToolBar.setTitle(mTitleList.get(position));
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    class OnToolBarListener implements Toolbar.OnMenuItemClickListener {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.main_menu_search:
                    ToastUtils.showShort(MainActivity.this, "点击了搜索按钮");
                    break;
                case R.id.main_menu_add_group_chat:
                    //新建群组
                    break;
                case R.id.main_menu_add_new_friend:
                    //添加好友
                    startActivity(new Intent(MainActivity.this,NewFriendActivity.class));
                    break;
                case R.id.main_menu_add_qr_code:
                    //扫一扫二维码
                    startActivity(new Intent(MainActivity.this,QRCodeActivity.class));
                    break;
                case R.id.main_menu_add_send_me:
                    //联系我
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(R.string.main_send_me_title);
                    builder.setMessage(R.string.main_send_me_content);
                    builder.setNeutralButton("返回", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            builder.create().dismiss();
                        }
                    });
                    builder.create().show();
                    break;
            }
            return true;
        }
    }

    private void registerService() {
        Intent serviceIntent = new Intent(MainActivity.this, MessageService.class);
        startService(serviceIntent);
    }

    private void unregisterService() {
        Intent serviceIntent = new Intent(MainActivity.this, MessageService.class);
        stopService(serviceIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterService();
    }

    //回退按钮使DrawerLayout关闭
    @Override
    public void onBackPressed() {
        if (mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mainDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private CircleImageView userImageView;
    private TextView userName;
    private ImageButton userInfoEdit;
    private RelativeLayout userLayout;
    private MyBitmapCacheUtil cacheUtil;
    private TextView userSign ;
    /**
     * 初始化NavigationView数据的方法
     */
    public void initHeaderUserInfo() {
        View mView = mainNavigationView.getHeaderView(0);
        userImageView = (CircleImageView) mView.findViewById(R.id.main_header_userimg);
        userName = (TextView) mView.findViewById(R.id.main_header_username);
        userInfoEdit = (ImageButton) mView.findViewById(R.id.main_header_edit);
        userLayout = (RelativeLayout) mView.findViewById(R.id.main_header_background);
        userSign = (TextView) mView.findViewById(R.id.main_header_sign);
        cacheUtil = new MyBitmapCacheUtil();
        queryUserInfoImage(userImageView);
        userImageView.setImageResource(R.mipmap.user_image);
        userImageView.setOnClickListener(new UserImageClickListener());
        userInfoEdit.setOnClickListener(new EditUserInfoClickListener());
    }

    /**
     * 查询并设置用户头像
     *
     * @param imageView
     */
    private void queryUserInfoImage(final ImageView imageView) {
        String userId = EMClient.getInstance().getCurrentUser();
        ContextSave.userId = userId;
        BmobQuery<UserInfoEntity> query = new BmobQuery<UserInfoEntity>("userinfo");
        query.addWhereEqualTo("userId", userId);
        query.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray jsonArray, BmobException e) {
                if (e == null) {
                    List<UserInfoEntity> userInfo = new JsonUtil().jsonToList(jsonArray.toString());
                    for (UserInfoEntity entity : userInfo) {
                        boolean flag = entity.isDefImg();
                        String sign = entity.getUserSign();
                        objectId = entity.getObjectId();
                        if (flag) {   //是否使用默认的用户头像
                            int position = Integer.parseInt(entity.getDefImgPosition());
                            LogUtils.d("Position" + position);
                            Bitmap bitmap = BitmapUtils.getBitmapById(MainActivity.this, ContextSave.defPicArray[position]);
                            imageView.setImageBitmap(bitmap);
                            ContextSave.userBitmap = bitmap ;
                        } else {
                            String url = entity.getUserImg().getUrl();
                            cacheUtil.disPlayImage(imageView, url);
                            Bitmap bitmap = localCacheUtil.getBitmapFromLocal(url);
                            if(bitmap!=null){
                                ContextSave.userBitmap = bitmap ;
                            }
                        }
                        userSign.setText(sign);
                        userName.setText(entity.getUserName());
                    }
                } else {
                    imageView.setImageResource(R.mipmap.user_image);
                }
            }
        });
    }

    class UserImageClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this,PersonCenterActivity.class);
            String userId = EMClient.getInstance().getCurrentUser();
            intent.putExtra("UserName",userId);
            startActivity(intent);
        }
    }

    private int RESULT_IMAGE_CODE = 200;
    /**
     * 修改背景图片
     */
    class EditUserInfoClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
//            Intent pictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            startActivityForResult(pictureIntent, RESULT_IMAGE_CODE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mainDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mainDrawerLayout.closeDrawer(GravityCompat.START);  //关闭抽屉
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        queryUserInfoImage(userImageView);
    }
    private void showShare() {
        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        // 分享时Notification的图标和文字  2.5.9以后的版本不调用此方法
        //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle("友信");
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl("http://uchat.bmob.cn/");
        // text是分享文本，所有平台都需要这个字段
        oks.setText("我正在使用友信App，实时聊天的新方式，快来体验吧！");
        //分享网络图片，新浪微博分享网络图片需要通过审核后申请高级写入接口，否则请注释掉测试新浪微博
        oks.setImageUrl("http://jimblog.cc/icons.png");
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        //oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl("http://uchat.bmob.cn/");
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        //        oks.setComment("");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl("http://uchat.bmob.cn/");

        // 启动分享GUI
        oks.show(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_IMAGE_CODE && resultCode == Activity.RESULT_OK && null != data) {
            //发送图片的方法
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = this.getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            final String picturePath = c.getString(columnIndex);
            c.close();
            if (FileTools.isFoundFilePath(picturePath)) {
                if(FileTools.getFileSize(picturePath) > maxImageSize){
                    ToastUtils.showShort(MainActivity.this, "图片过大");
                    return ;
                }
                final BmobFile bmobimage = new BmobFile(new File(picturePath));
                bmobimage.uploadblock(new UploadFileListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e == null) {
                            //bmobFile.getFileUrl()--返回的上传文件的完整地址
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 2;
                            Bitmap bitmap = BitmapUtils.getBitmapByFile(picturePath, options);
                            submitBackGround(bmobimage,bitmap);
                        } else {
                            userLayout.setBackgroundResource(R.mipmap.new_header_bg);
                        }
                    }
                    @Override
                    public void onProgress(Integer value) {
                        // 返回的上传进度（百分比）
                    }
                });
            }
        }
    }

    /**
     * 提交到背景图片
     */
    private void submitBackGround(BmobFile bmobFile, final Bitmap bitmap){
        UserInfoEntity entity = new UserInfoEntity();
        entity.setBg(bmobFile);
        entity.update(objectId,new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e == null){
                    ToastUtils.showShort(MainActivity.this, "图片上传成功");
                    userLayout.setBackground(new BitmapDrawable(bitmap));
                }else{
                    ToastUtils.showShort(MainActivity.this, "图片上传失败");
                    userLayout.setBackgroundResource(R.mipmap.new_header_bg);
                }
            }
        });
    }
}

