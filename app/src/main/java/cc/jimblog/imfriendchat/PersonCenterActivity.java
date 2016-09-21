package cc.jimblog.imfriendchat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import org.json.JSONArray;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import entity.ContextSave;
import entity.UserInfoEntity;
import image.MyBitmapCacheUtil;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import util.BitmapUtils;
import util.JsonUtil;
import util.LogUtils;
import util.ToastUtils;
import view.CircleImageView;
import view.RippleView;

/**
 * Created by jimhao on 16/9/3.
 */
public class PersonCenterActivity extends SwipeBackActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout toolbarLayout;
    @BindView(R.id.app_bar)
    AppBarLayout appBar;
    @BindView(R.id.personal_user_image)
    CircleImageView personalUserImage;
    @BindView(R.id.personal_content_name)
    TextView personalContentName;
    @BindView(R.id.personal_content_id)
    TextView personalContentId;
    @BindView(R.id.personal_content_user_sign)
    TextView personalContentUserSign;
    @BindView(R.id.personal_content_msg_btn)
    RippleView personalContentMsgBtn;
    @BindView(R.id.personal_content_video_btn)
    RippleView personalContentVideoBtn;
    @BindView(R.id.personal_content_sex)
    ImageView personalContentSex;
    @BindView(R.id.personal_user_edit_info)
    FloatingActionButton personalUserEditInfo;
    @BindView(R.id.personal_content_add_friend_btn)
    RippleView personalContentAddFriendBtn;

    private SwipeBackLayout mBackLayout;   //侧滑关闭Activity所用
    private String username;
    private MyBitmapCacheUtil cacheUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        username = intent.getStringExtra("UserName");
        initStyleColor();
        initData();
        //侧滑关闭Activity的重要方法
        mBackLayout = getSwipeBackLayout();
        mBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        checkedUser();

        //如果AddFriendState不为空 则说明是扫描二维码来加好友的
        String state = intent.getStringExtra("AddFriendState");
        if (state != null) {
            checkedIsAddFriend();
        }
    }

    private void checkedIsAddFriend() {
        if(username.equals(ContextSave.userId)){
            return;
        }
        //如果好友列表不为空
        if(!ContextSave.friendList.isEmpty() && ContextSave.friendList!=null){
            if (ContextSave.friendList.contains(username)) {
                //如果好友列表数据中包含了该用户 则显示聊天
                personalContentMsgBtn.setVisibility(View.VISIBLE);
                personalContentVideoBtn.setVisibility(View.VISIBLE);
                personalContentAddFriendBtn.setVisibility(View.GONE);
                personalUserEditInfo.setVisibility(View.GONE);
            } else {
                //如果不包含则显示添加好友按钮
                personalContentMsgBtn.setVisibility(View.GONE);
                personalContentVideoBtn.setVisibility(View.GONE);
                personalContentAddFriendBtn.setVisibility(View.VISIBLE);
                personalUserEditInfo.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 初始化Palette更改ToolBar的颜色
     */
    private void initStyleColor() {
        Observable<Bitmap> observable = Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(final Subscriber<? super Bitmap> subscriber) {
                BmobQuery<UserInfoEntity> query = new BmobQuery<UserInfoEntity>("userinfo");
                query.addWhereEqualTo("userId", username);
                query.findObjectsByTable(new QueryListener<JSONArray>() {
                    @Override
                    public void done(JSONArray jsonArray, BmobException e) {
                        if (e == null) {
                            List<UserInfoEntity> userInfo = new JsonUtil().jsonToList(jsonArray.toString());
                            for (UserInfoEntity entity : userInfo) {
                                BmobFile file = entity.getBg();
                                if (file != null) {
                                    String url = entity.getUserImg().getUrl();
                                    Bitmap bitmap = BitmapUtils.returnBitMap(url);
                                    subscriber.onNext(bitmap);
                                } else {
                                    subscriber.onError(new Throwable("空数据"));
                                }
                            }
                        } else {
                            subscriber.onError(new Throwable("读取失败"));
                        }
                    }
                });
            }
        });
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Bitmap>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        toolbarLayout.setBackgroundResource(R.mipmap.new_header_bg);
                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        toolbarLayout.setBackground(new BitmapDrawable(bitmap));
                    }
                });
    }

    /**
     * 初始化Toolbar
     */
    private void initData() {
        toolbar.setTitle(username);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        appBar.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                if (state == State.EXPANDED) {
                    LogUtils.d("STATE", "展开");
                    //展开状态
                    showUserImage();
                } else if (state == State.COLLAPSED) {
                    LogUtils.d("STATE", "折叠");
                    //折叠状态
                    dismissUserImage();
                } else {
                    LogUtils.d("STATE", "中间");
                    //中间状态

                }
            }
        });
        cacheUtil = new MyBitmapCacheUtil();
        queryUserInfoImage(personalUserImage);
    }

    @OnClick({R.id.personal_content_msg_btn, R.id.personal_content_video_btn, R.id.personal_user_image, R.id.personal_user_edit_info,R.id.personal_content_add_friend_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.personal_content_msg_btn:
                //发起会话
                Intent intent = new Intent(PersonCenterActivity.this, ChatActivity.class);
                intent.putExtra("Username", username);
                startActivity(intent);
                finish();
                break;
            case R.id.personal_content_video_btn:
                break;
            case R.id.personal_user_image:

                break;
            case R.id.personal_user_edit_info:
                startActivity(new Intent(PersonCenterActivity.this, PersonalEditInfoActivity.class));
                break;
            case R.id.personal_content_add_friend_btn:
                addFriend();
                break;
        }
    }
    private void addFriend(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(PersonCenterActivity.this);
        builder.setTitle("发送好友请求");
        final EditText editText = new EditText(PersonCenterActivity.this);
        editText.setHint("请输入申请的理由");
        editText.setHintTextColor(getResources().getColor(R.color.second_text_color));
        builder.setView(editText,50,50,50,50);
        builder.setNegativeButton("发送请求", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String reason = editText.getText().toString().trim();
                try {
                    EMClient.getInstance().contactManager().addContact(username, reason);
                    ToastUtils.showShort(PersonCenterActivity.this,"已成功发送好友请求");
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    ToastUtils.showShort(PersonCenterActivity.this,"发送好友请求失败,请重新尝试！");
                }
            }
        });
        builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                builder.create().dismiss();
            }
        });
        builder.create().show();
    }
    /**
     * 监听AppLayout的展开状态
     */
    public abstract static class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {
        public enum State {
            EXPANDED,
            COLLAPSED,
            IDLE
        }

        private State mCurrentState = State.IDLE;

        @Override
        public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
            if (i == 0) {
                if (mCurrentState != State.EXPANDED) {
                    onStateChanged(appBarLayout, State.EXPANDED);
                }
                mCurrentState = State.EXPANDED;
            } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
                if (mCurrentState != State.COLLAPSED) {
                    onStateChanged(appBarLayout, State.COLLAPSED);
                }
                mCurrentState = State.COLLAPSED;
            } else {
                if (mCurrentState != State.IDLE) {
                    onStateChanged(appBarLayout, State.IDLE);
                }
                mCurrentState = State.IDLE;
            }
        }

        public abstract void onStateChanged(AppBarLayout appBarLayout, State state);
    }

    /**
     * 显示用户头像
     */
    private void showUserImage() {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 0, 1,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        //3秒完成动画
        scaleAnimation.setDuration(270);
        //将AlphaAnimation这个已经设置好的动画添加到 AnimationSet中
        animationSet.addAnimation(scaleAnimation);
        animationSet.setFillAfter(true);
        animationSet.setInterpolator(new AccelerateInterpolator());
        //启动动画
        PersonCenterActivity.this.personalUserImage.startAnimation(animationSet);
    }

    /**
     * 关闭用户头像
     */
    private void dismissUserImage() {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1, 0, 1, 0,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        //3秒完成动画
        scaleAnimation.setDuration(270);
        //将AlphaAnimation这个已经设置好的动画添加到 AnimationSet中
        animationSet.addAnimation(scaleAnimation);
        animationSet.setFillAfter(true);
        animationSet.setInterpolator(new AccelerateInterpolator());
        //启动动画
        PersonCenterActivity.this.personalUserImage.startAnimation(animationSet);
    }
    /**
     参数解释：
     第一个参数：X轴水平缩放起始位置的大小（fromX）。1代表正常大小
     第二个参数：X轴水平缩放完了之后（toX）的大小，0代表完全消失了
     第三个参数：Y轴垂直缩放起始时的大小（fromY）
     第四个参数：Y轴垂直缩放结束后的大小（toY）
     第五个参数：pivotXType为动画在X轴相对于物件位置类型
     第六个参数：pivotXValue为动画相对于物件的X坐标的开始位置
     第七个参数：pivotXType为动画在Y轴相对于物件位置类型
     第八个参数：pivotYValue为动画相对于物件的Y坐标的开始位置

     （第五个参数，第六个参数），（第七个参数,第八个参数）是用来指定缩放的中心点
     0.5f代表从中心缩放
     */


    /**
     * 查询并设置用户头像
     *
     * @param imageView
     */
    private void queryUserInfoImage(final ImageView imageView) {
        imageView.setImageResource(R.mipmap.user_image);
        BmobQuery<UserInfoEntity> query = new BmobQuery<UserInfoEntity>("userinfo");
        query.addWhereEqualTo("userId", username);
        query.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray jsonArray, BmobException e) {
                if (e == null) {
                    List<UserInfoEntity> userInfo = new JsonUtil().jsonToList(jsonArray.toString());
                    for (UserInfoEntity entity : userInfo) {
                        boolean flag = entity.isDefImg();
                        if (flag) {   //是否使用默认的用户头像
                            int position = Integer.parseInt(entity.getDefImgPosition());
                            LogUtils.d("Position" + position);
                            Bitmap bitmap = BitmapUtils.getBitmapById(PersonCenterActivity.this, ContextSave.defPicArray[position]);
                            imageView.setImageBitmap(bitmap);
                            addUserData(entity);
                        } else {
                            String url = entity.getUserImg().getUrl();
                            cacheUtil.disPlayImage(imageView, url);
                            addUserData(entity);
                        }
                    }
                } else {
                    imageView.setImageResource(R.mipmap.user_image);
                }
            }
        });
    }

    private void addUserData(UserInfoEntity entity) {
        boolean sex = entity.isSex();
        if (sex) {
            personalContentSex.setImageResource(R.mipmap.sex_boy_img_24dp);
        } else {
            personalContentSex.setImageResource(R.mipmap.sex_girl_img_24dp);
        }
        String idStr = entity.getUserId();
        String username = entity.getUserName();
        String userSign = entity.getUserSign();
        personalContentId.setText("ULineId:" + idStr);
        personalContentName.setText(username);
        personalContentUserSign.setText(userSign);
    }

    private void checkedUser() {
        String currentName = EMClient.getInstance().getCurrentUser();
        if (currentName.equals(username)) {
            personalContentMsgBtn.setVisibility(View.GONE);
            personalContentVideoBtn.setVisibility(View.GONE);
            personalContentAddFriendBtn.setVisibility(View.GONE);
            personalUserEditInfo.setVisibility(View.VISIBLE);
        } else {
            personalUserEditInfo.setVisibility(View.GONE);
            personalContentAddFriendBtn.setVisibility(View.GONE);
            personalContentMsgBtn.setVisibility(View.VISIBLE);
            personalContentVideoBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        queryUserInfoImage(personalUserImage);
    }
}
