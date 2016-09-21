package cc.jimblog.imfriendchat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import adapter.SignPageAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import fragment.InputPasswordFragment;
import fragment.InputPhoneFragment;
import view.MyViewPager;

/**
 * 注册页面使用Fragment回调控制
 * Created by Ran on 2016/8/8.
 * 实现的接口为两个回调方法
 */
public class SignActivity extends AppCompatActivity implements InputPhoneFragment.MyInputPhoneListener , InputPasswordFragment.MyInputPwdListener{
    @BindView(R.id.tool_bar)
    Toolbar toolBar;
    @BindView(R.id.login_view_pager)
    MyViewPager loginViewPager;     //此处自定义了ViewPager对象 原因是需要屏蔽ViewPager的滑动事件

    private SignPageAdapter adapter ;   //Adapter适配器
    private InputPhoneFragment phoneFragment ;
    private InputPasswordFragment passwordFragment ;

    private List<Fragment> mFragmentList = new ArrayList<>();   //页卡视图集合
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        ButterKnife.bind(this);
        toolBar.setTitle(getString(R.string.register_button_text));
        toolBar.setTitleTextColor(Color.WHITE);
        toolBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolBar);
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(SignActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        initFragmentView();
    }
    private void initFragmentView(){
        phoneFragment = new InputPhoneFragment() ;
        passwordFragment = new InputPasswordFragment();

        mFragmentList.add(phoneFragment);
        mFragmentList.add(passwordFragment);

        adapter = new SignPageAdapter(this.getSupportFragmentManager(),mFragmentList);
        loginViewPager.setAdapter(adapter);
        loginViewPager.setCurrentItem(0);
        loginViewPager.setScrollble(false);
    }

    @Override
    public void showMessage(int index) {
        if(index == 1){
            loginViewPager.setCurrentItem(1);
        }
    }

    @Override
    public void closeMessage(int index) {
        if(index == 2){
            SignActivity.this.finish();
        }
    }
}
