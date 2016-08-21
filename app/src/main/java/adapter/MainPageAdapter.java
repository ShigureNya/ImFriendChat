package adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 主页Fragment适配器
 * Created by Ran on 2016/8/10.
 */
public class MainPageAdapter extends FragmentStatePagerAdapter {
    List<Fragment> mFragmentList  = new ArrayList<Fragment>();
    List<String> mTitleList = new ArrayList<String>();
    public MainPageAdapter(FragmentManager fm , List<Fragment> fragmentList , List<String> titleList ) {
        super(fm);
        mFragmentList = fragmentList ;
        mTitleList = titleList;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitleList.get(position);
    }
}
