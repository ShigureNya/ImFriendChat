package cc.jimblog.imfriendchat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import entity.ContextSave;
import entity.UserInfoEntity;
import image.LocalCacheUtil;
import image.MemoryCacheUtil;
import image.MyBitmapCacheUtil;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import qrcode.QRCodeUtil;
import util.BitmapUtils;
import util.JsonUtil;
import util.LogUtils;
import util.PermissionCheckerUtil;
import util.ToastUtils;
import view.CircleImageView;

/**
 * Created by jimhao on 16/9/19.
 */
public class MyQRCodeActivity extends SwipeBackActivity {

    @BindView(R.id.tool_bar)
    Toolbar toolBar;
    @BindView(R.id.qrcode_user_image)
    CircleImageView qrcodeUserImage;
    @BindView(R.id.qrcode_user_name)
    TextView qrcodeUserName;
    @BindView(R.id.qrcode_user_sign)
    TextView qrcodeUserSign;
    @BindView(R.id.qrcode_user_code)
    ImageView qrcodeUserCode;
    private SwipeBackLayout mBackLayout;   //侧滑关闭Activity所用
    private MyBitmapCacheUtil cacheUtil ;
    private LocalCacheUtil localUtil;
    private MemoryCacheUtil memoryUtil ;

    private boolean isRequireCheck; // 是否需要系统权限检测

    private String[] PERMISSIONS = new String[]{Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_CODE = 0; // 请求码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myqrcode);
        ButterKnife.bind(this);
        mBackLayout = getSwipeBackLayout();
        mBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        toolBar.setTitle(R.string.qrcode_title);
        toolBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolBar);
        cacheUtil = new MyBitmapCacheUtil();
        localUtil = new LocalCacheUtil();
        memoryUtil = new MemoryCacheUtil();

        initPermissionState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * 查询用户的信息
     */
    private void initUserInfo(){
        BmobQuery<UserInfoEntity> query = new BmobQuery<UserInfoEntity>("userinfo");
        query.addWhereEqualTo("userId", ContextSave.userId);
        query.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray jsonArray, BmobException e) {
                if(e == null){
                    List<UserInfoEntity> list = new JsonUtil().jsonToList(jsonArray.toString());
                    //从Bmob服务器中取出用户昵称和头像签名等信息设置给控件
                    for(UserInfoEntity entity : list){
                        String nickName = entity.getUserName();
                        String sign = entity.getUserSign();
                        boolean flag = entity.isDefImg();
                        if (flag) {   //是否使用默认的用户头像
                            int position = Integer.parseInt(entity.getDefImgPosition());
                            LogUtils.d("Position" + position);
                            Bitmap bitmap = BitmapUtils.getBitmapById(MyQRCodeActivity.this, ContextSave.defPicArray[position]);
                            qrcodeUserImage.setImageBitmap(bitmap);
                            initQRCodeImage(bitmap);
                        } else {
                            String url = entity.getUserImg().getUrl();
                            cacheUtil.disPlayImage(qrcodeUserImage,url);
                            initQRCodeImage(url);   //在初始化用户信息的时候初始化二维码图片
                        }
                        qrcodeUserName.setText(nickName);
                        qrcodeUserSign.setText(sign);
                    }
                }else{
                    ToastUtils.showShort(MyQRCodeActivity.this,"获取用户信息失败");
                }
            }
        });
    }

    /**
     * 单独使用RxJava异步加载二维码图像
     */
    private void initQRCodeImage(final String url){
        Bitmap bitmap = null ;
        bitmap = memoryUtil.getBitmapFromMemory(url);
        if(bitmap == null){
            bitmap = localUtil.getBitmapFromLocal(url);
        }
        String requestInfo = "UserId:"+ContextSave.userId;
        Bitmap requestBitmap = QRCodeUtil.createQRCodeWithLogo(requestInfo, 500, bitmap);
        qrcodeUserCode.setImageBitmap(requestBitmap);
    }
    /**
     * 单独使用RxJava异步加载二维码图像
     */
    private void initQRCodeImage(final Bitmap bitmap){
        String requestInfo = "UserId:"+ContextSave.userId;
        Bitmap requestBitmap = QRCodeUtil.createQRCodeWithLogo(requestInfo, 500, bitmap);
        qrcodeUserCode.setImageBitmap(requestBitmap);
    }
    @TargetApi(Build.VERSION_CODES.M)
    private void initPermissionState() {
        if(PermissionCheckerUtil.getInstance().lacksPermissions(this,PERMISSIONS)){
            ActivityCompat.requestPermissions(this,PERMISSIONS,REQUEST_CODE);
        }else{
            //如果包含所有权限
            initUserInfo();
        }
    }
    /**
     * 用户权限处理,
     * 如果全部获取, 则直接过.
     * 如果权限缺失, 则提示Dialog.
     *
     * @param requestCode  请求码
     * @param permissions  权限
     * @param grantResults 结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE && hasAllPermissionsGranted(grantResults)) {
            isRequireCheck = true;
            initUserInfo();
        } else {
            isRequireCheck = false;
            finish();
        }
    }
    // 含有全部的权限
    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }
}
