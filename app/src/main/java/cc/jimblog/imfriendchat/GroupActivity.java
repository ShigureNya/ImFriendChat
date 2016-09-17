package cc.jimblog.imfriendchat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.List;

import adapter.GroupAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import util.NetWorkUtils;
import util.SnackBarUtil;

/**
 * 我的群组
 * Created by jimhao on 16/8/29.
 */
public class GroupActivity extends SwipeBackActivity {
    @BindView(R.id.tool_bar)
    Toolbar toolBar;
    @BindView(R.id.group_listView)
    RecyclerView groupListView;
    @BindView(R.id.group_search_view)
    FloatingSearchView groupSearchView;
    @BindView(R.id.group_snackbar_layout)
    CoordinatorLayout groupSnackbarLayout;

    private SwipeBackLayout mBackLayout;   //侧滑关闭Activity所用
    private Snackbar snackBar ;
    private List<EMGroup> mList = new ArrayList<EMGroup>();
    private GroupAdapter adapter ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        ButterKnife.bind(this);
        initToolbar();
        //侧滑关闭Activity的重要方法
        mBackLayout = getSwipeBackLayout();
        mBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        initAdapter();
    }

    private void initToolbar() {
        toolBar.setTitle(getString(R.string.group_title_text));
        toolBar.setTitleTextColor(Color.WHITE);
        toolBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolBar);
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        toolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.group_menu_add:

                        break;
                    case R.id.group_menu_search:

                        break;
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_menu, menu);
        return true;
    }

    /**
     * 初始化Adapter
     */
    private void initAdapter(){
        //设置adapter
        adapter = new GroupAdapter(mList,GroupActivity.this);
        //通过New一个LinearLayoutManager的布局管理器对象来设置RecycleView的布局管理器
        groupListView.setLayoutManager(new LinearLayoutManager(GroupActivity.this));
        //设置Item的过渡动画，使用默认的即可
        groupListView.setItemAnimator(new DefaultItemAnimator());
        //设置item为固定大小
        groupListView.setHasFixedSize(true);
        //加载Adapter
        groupListView.setAdapter(adapter);
        //加载群组数据
        getGroupList();
        //设置点击事件
        adapter.setOnGroupClickListener(new GroupAdapter.OnGroupClickListener() {
            @Override
            public void onClick(View view, int position) {
                view.setBackgroundResource(R.drawable.recycler_bg);
                EMGroup group = mList.get(position);
                String groupId = group.getGroupId();
                String groupName = group.getGroupName();
                int groupMemberNum = group.getMembers().size();
                Intent intent = new Intent(GroupActivity.this,GroupChatActivity.class);
                intent.putExtra("GroupId",groupId);
                intent.putExtra("GroupName",groupName);
                intent.putExtra("GroupNum",groupMemberNum);
                startActivity(intent);
            }
        });
    }
    /**
     * 得到群组列表
     */
    private void getGroupList() {
        Observable<List<EMGroup>> observable = Observable.create(new Observable.OnSubscribe<List<EMGroup>>() {
            @Override
            public void call(Subscriber<? super List<EMGroup>> subscriber) {
                List<EMGroup> grouplist = null;
                if (NetWorkUtils.isNetworkAvailable(GroupActivity.this)) {
                    //从服务器获取自己加入的和创建的群组列表，此api获取的群组sdk会自动保存到内存和db。
                    try {
                        grouplist = EMClient.getInstance().groupManager().getJoinedGroupsFromServer();//需异步处理
                    } catch (HyphenateException e) {
                        e.printStackTrace();
                        subscriber.onError(new Throwable("网络错误"));
                    }
                } else {
                    grouplist = EMClient.getInstance().groupManager().getAllGroups();
                }
                if (grouplist != null && grouplist.size() != 0) {
                    subscriber.onNext(grouplist);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new Throwable("无群组"));
                }
            }
        });
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<EMGroup>>() {
                    @Override
                    public void onCompleted() {
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        snackBar = SnackBarUtil.shortSnackbar(groupSnackbarLayout,"还没有群组,快添加一个吧",Color.WHITE);
                        snackBar.setDuration(2300);
                        snackBar.show();
                    }

                    @Override
                    public void onNext(List<EMGroup> emGroups) {
                        mList.clear();
                        for(EMGroup group : emGroups){
                            mList.add(group);
                        }
                    }
                });
    }
}
