package cc.jimblog.imfriendchat;

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
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import adapter.MainPageAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fragment.ChatFragment;
import fragment.ContactsFragment;
import fragment.FuncationFragment;

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

    private List<String> mTitleList = new ArrayList<String>();
    private ChatFragment chatFragment = null ;
    private ContactsFragment contactsFragment = null ;
    private FuncationFragment funcationFragment = null ;
    private List<Fragment> mFragmentList = new ArrayList<Fragment>();
    private long exit_time = 0 ;
    private ActionBarDrawerToggle mDrawerToggle ;
    private MainPageAdapter mAdapter ;
    /**
     * 初始化数据的方法
     */
    private void initData() {
        chatFragment = new ChatFragment();
        contactsFragment = new ContactsFragment() ;
        funcationFragment = new FuncationFragment();

        mFragmentList.add(chatFragment);
        mFragmentList.add(contactsFragment);
        mFragmentList.add(funcationFragment);

        mTitleList.add(getString(R.string.main_chat_fragment_title));
        mTitleList.add(getString(R.string.main_contacts_fragment_title));
        mTitleList.add(getString(R.string.main_function_fragment_title));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mainToolBar.setTitle(R.string.main_chat_fragment_title);
        setSupportActionBar(mainToolBar);
        initData();

        //initDrawerLayout
        mDrawerToggle = new ActionBarDrawerToggle(this,mainDrawerLayout,mainToolBar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        mDrawerToggle.syncState();  //init
        mainDrawerLayout.setDrawerListener(mDrawerToggle);
        mainNavigationView.setNavigationItemSelectedListener(new MyNavigationItemListener());

        //initTabLayout
        mainTabLayout.setTabMode(TabLayout.MODE_FIXED);
        for(int i = 0 ; i <= mTitleList.size(); i ++){
            mainTabLayout.addTab(mainTabLayout.newTab().setText(mTitleList.get(i)));
        }

        //initFragmentAdapter
        mAdapter = new MainPageAdapter(getSupportFragmentManager(),mFragmentList,mTitleList);
        mainViewpager.setCurrentItem(0);    //设置默认加载为聊天窗口
        mainTabLayout.setupWithViewPager(mainViewpager);
        mainTabLayout.setTabsFromPagerAdapter(mAdapter);
    }

    @OnClick(R.id.main_navigation_view)
    public void onClick() {

    }
    class MyNavigationItemListener implements NavigationView.OnNavigationItemSelectedListener{

        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            switch (item.getItemId()){

            }
            item.setCheckable(true);    //设为选中
            mainDrawerLayout.closeDrawer(GravityCompat.START);  //关闭抽屉
            return true ;
        }
    }
}
