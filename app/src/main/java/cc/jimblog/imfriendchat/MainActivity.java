package cc.jimblog.imfriendchat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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

import java.util.ArrayList;
import java.util.List;

import adapter.MainPageAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import entity.ContextSave;
import entity.UserInfoEntity;
import fragment.ChatFragment;
import fragment.ContactsFragment;
import fragment.FuncationFragment;
import fragment.SettingFragment;
import image.MyBitmapCacheUtil;
import service.MessageService;
import util.BitmapUtils;
import util.JsonUtil;
import util.LogUtils;
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

    private List<String> mTitleList = new ArrayList<String>();  //存放标题
    private ChatFragment chatFragment = null;  //聊天选项卡
    private ContactsFragment contactsFragment = null;  //联系人
    private FuncationFragment funcationFragment = null;       //功能页面
    private SettingFragment settingFragment = null;    //设置页面
    private List<Fragment> mFragmentList = new ArrayList<Fragment>();   //存放Fragment对象
    private ActionBarDrawerToggle mDrawerToggle;   //监听DrawerLayout滑动和弹出事件
    private MainPageAdapter mAdapter;  //主页适配器

    public static final int IS_BACKGROUND_PERMISSON = 1;  //权限管理

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
        mainToolBar.setOnMenuItemClickListener(new OnToolBarListener());
        initData();
        initDrawerLayout();
        initTabLayout();
        initFragmentAdapter();
        registerService();  //注册消息广播监听
        initHeaderUserInfo();   //初始化侧滑数据
    }

    /**
     * 初始化DrawerLayout
     */
    public void initDrawerLayout() {
        //initDrawerLayout
        mDrawerToggle = new ActionBarDrawerToggle(this, mainDrawerLayout, mainToolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerToggle.syncState();  //init
        mainDrawerLayout.setDrawerListener(mDrawerToggle);
        mainNavigationView.setNavigationItemSelectedListener(new MyNavigationItemListener());
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
                    mainViewpager.setCurrentItem(0);
                    break;
                case R.id.drawer_function_contacts:
                    mainViewpager.setCurrentItem(1);
                    break;
                case R.id.drawer_online_cloud:

                    break;
                case R.id.drawer_online_qrcode:

                    break;
                case R.id.drawer_setting_set:

                    break;
                case R.id.drawer_setting_share:

                    break;
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
                case R.id.main_menu_add:
                    ToastUtils.showShort(MainActivity.this, "点击了更多功能按钮");
                    break;
                case R.id.main_menu_search:
                    ToastUtils.showShort(MainActivity.this, "点击了搜索按钮");
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
        if(mainDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mainDrawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }
    private CircleImageView userImageView ;
    private TextView userName ;
    private ImageButton userInfoEdit;
    private RelativeLayout userLayout ;
    private MyBitmapCacheUtil cacheUtil ;

    /**
     * 初始化NavigationView数据的方法
     */
    public void initHeaderUserInfo(){
        View mView= mainNavigationView.getHeaderView(0);
        userImageView = (CircleImageView) mView.findViewById(R.id.main_header_userimg);
        userName = (TextView) mView.findViewById(R.id.main_header_username);
        userInfoEdit = (ImageButton) mView.findViewById(R.id.main_header_edit);
        userLayout = (RelativeLayout)mView.findViewById(R.id.main_header_background);
        cacheUtil = new MyBitmapCacheUtil();

        String name = EMClient.getInstance().getCurrentUser();
        userName.setText(name);
        queryUserInfoImage(name,userImageView);
        userImageView.setImageResource(R.mipmap.user_image);
        userImageView.setOnClickListener(new UserImageClickListener());
        userInfoEdit.setOnClickListener(new EditUserInfoClickListener());
    }

    /**
     * 查询并设置用户头像
     * @param userId
     * @param imageView
     */
    private void queryUserInfoImage(String userId , final ImageView imageView){
        BmobQuery<UserInfoEntity> query = new BmobQuery<UserInfoEntity>("userinfo");
        query.addWhereEqualTo("userId",userId);
        query.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray jsonArray, BmobException e) {
                List<UserInfoEntity> userInfo = new JsonUtil().jsonToList(jsonArray.toString());
                for(UserInfoEntity entity : userInfo){
                    boolean flag = entity.isDefImg();
                    if(flag){   //是否使用默认的用户头像
                        int position = Integer.parseInt(entity.getDefImgPosition());
                        LogUtils.d("Position"+position);
                        Bitmap bitmap = BitmapUtils.getBitmapById(MainActivity.this,ContextSave.defPicArray[position]);
                        if(ContextSave.userBitmap == null){
                            ContextSave.userBitmap = bitmap ;
                        }
                        imageView.setImageBitmap(bitmap);
                    }else{
                        String url = entity.getUserImg().getUrl();
                        Bitmap bitmap = BitmapUtils.returnBitMap(url);
                        cacheUtil.disPlayImage(imageView,url);

                        if(ContextSave.userBitmap == null){
                            ContextSave.userBitmap = bitmap ;
                        }
                    }
                }
            }
        });
    }
    class UserImageClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {

        }
    }
    class EditUserInfoClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {

        }
    }
}

