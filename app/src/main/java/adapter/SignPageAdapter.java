package adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 注册页的Fragment适配器
 * Created by Ran on 2016/8/8.
 */
public class SignPageAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragmentList = new ArrayList<Fragment>();

    public SignPageAdapter(FragmentManager fm , List<Fragment> fragmentList) {
        super(fm);
        this.fragmentList = fragmentList ;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }
}
