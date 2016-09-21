package cc.jimblog.imfriendchat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import adapter.FwhAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import view.DividerItemDecoration;

/**
 * Created by jimhao on 16/9/3.
 */
public class FwhActivity extends SwipeBackActivity {
    @BindView(R.id.tool_bar)
    Toolbar toolBar;
    @BindView(R.id.fwh_listView)
    RecyclerView fwhListView;

    private FwhAdapter adapter ;
    private SwipeBackLayout mBackLayout;   //侧滑关闭Activity所用
    private List<String> mList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fwh);
        ButterKnife.bind(this);
        //侧滑关闭Activity的重要方法
        mBackLayout = getSwipeBackLayout();
        mBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

        initToolbar();
        initAdapter();
    }

    private void initAdapter() {
        //设置adapter
        adapter = new FwhAdapter(FwhActivity.this, mList);
        //通过New一个LinearLayoutManager的布局管理器对象来设置RecycleView的布局管理器
        fwhListView.setLayoutManager(new LinearLayoutManager(FwhActivity.this));
        //设置Item的过渡动画，使用默认的即可
        fwhListView.setItemAnimator(new DefaultItemAnimator());
        //设置item为固定大小
        fwhListView.setHasFixedSize(true);
        //为recyclerView设置分割线
        fwhListView.addItemDecoration(new DividerItemDecoration(FwhActivity.this, DividerItemDecoration.VERTICAL_LIST));
        //加载数据
        fwhListView.setAdapter(adapter);
        //设置头部Adapter
        initHeaderView(fwhListView);
    }

    private LinearLayout mRebotLayout ;
    private void initHeaderView(RecyclerView view) {
        View mHeaderView = LayoutInflater.from(FwhActivity.this).inflate(R.layout.layout_fwh_header,view,false);
        adapter.setmHeaderView(mHeaderView);
        mRebotLayout = (LinearLayout) mHeaderView.findViewById(R.id.fwh_rebot_chat);
        mRebotLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FwhActivity.this,RebotChatActivity.class));
            }
        });
    }
    /**
     * 初始化ToolBar和相关点击事件
     */
    private void initToolbar() {
        toolBar.setTitle(getString(R.string.fwh_title_text));
        toolBar.setTitleTextColor(Color.WHITE);
        toolBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolBar);
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(FwhActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fwh_menu,menu);
        return true ;
    }
}
