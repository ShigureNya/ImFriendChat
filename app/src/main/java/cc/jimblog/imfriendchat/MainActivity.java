package cc.jimblog.imfriendchat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
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

import java.util.ArrayList;
import java.util.List;

import adapter.MainPageAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import fragment.ChatFragment;
import fragment.ContactsFragment;
import fragment.FuncationFragment;
import fragment.SettingFragment;
import util.ToastUtils;

public class MainActivity extends AppCompatActivity {

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

    public static final int NET_STATE_PERMISSION = 1;  //权限管理
    /**
     * 初始化数据的方法
     */
    private void initData() {
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
        //Android 6.0权限适配
        if(Build.VERSION.SDK_INT >= 23){
            checkedPermission();
        }

        mainToolBar.setTitle(R.string.main_chat_fragment_title);
        setSupportActionBar(mainToolBar);
        mainToolBar.setOnMenuItemClickListener(new OnToolBarListener());
        initData();
        initDrawerLayout();
        initTabLayout();
        initFragmentAdapter();
    }

    public void initDrawerLayout() {
        //initDrawerLayout
        mDrawerToggle = new ActionBarDrawerToggle(this, mainDrawerLayout, mainToolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerToggle.syncState();  //init
        mainDrawerLayout.setDrawerListener(mDrawerToggle);
        mainNavigationView.setNavigationItemSelectedListener(new MyNavigationItemListener());

    }

    public void initTabLayout() {
        //initTabLayout
        mainTabLayout.setTabMode(TabLayout.MODE_FIXED);
        for (int i = 0; i < mTitleList.size(); i++) {
            mainTabLayout.addTab(mainTabLayout.newTab().setText(mTitleList.get(i)));
        }
    }

    public void initFragmentAdapter() {
        //initFragmentAdapter
        mAdapter = new MainPageAdapter(getSupportFragmentManager(), mFragmentList, mTitleList);
        mainViewpager.setCurrentItem(0);    //设置默认加载为聊天窗口
        mainTabLayout.setupWithViewPager(mainViewpager);
        mainTabLayout.setTabsFromPagerAdapter(mAdapter);
        mainViewpager.setAdapter(mAdapter);
        mainTabLayout.setOnTabSelectedListener(tabSelectedListener);

    }
    class MyNavigationItemListener implements NavigationView.OnNavigationItemSelectedListener {

        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            switch (item.getItemId()) {

            }
            item.setCheckable(true);    //设为选中
            mainDrawerLayout.closeDrawer(GravityCompat.START);  //关闭抽屉
            return true;
        }
    }

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
    @TargetApi(Build.VERSION_CODES.M)
    private void checkedPermission() {
        int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.INTERNET);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.INTERNET}, NET_STATE_PERMISSION);
            return;
        }
    }
}
