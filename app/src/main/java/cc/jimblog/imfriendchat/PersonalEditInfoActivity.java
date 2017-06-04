package cc.jimblog.imfriendchat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;

import org.json.JSONArray;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import cn.carbswang.android.numberpickerview.library.NumberPickerView;
import entity.ContextSave;
import entity.UserInfoEntity;
import image.MyBitmapCacheUtil;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import util.BitmapUtils;
import util.FileTools;
import util.JsonUtil;
import util.LogUtils;
import util.PermissionCheckerUtil;
import util.ToastUtils;
import view.CircleImageView;

/**
 * Created by Ran on 2016/9/17.
 */
public class PersonalEditInfoActivity extends SwipeBackActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.personal_info_image)
    CircleImageView personalInfoImage;
    @BindView(R.id.personal_info_image_layout)
    RelativeLayout personalInfoImageLayout;
    @BindView(R.id.personal_info_name_layout)
    RelativeLayout personalInfoNameLayout;
    @BindView(R.id.personal_info_sex_layout)
    RelativeLayout personalInfoSexLayout;
    @BindView(R.id.personal_info_name)
    TextView personalInfoName;
    @BindView(R.id.personal_info_sex)
    ImageView personalInfoSex;
    @BindView(R.id.personal_info_sign)
    TextView personalInfoSign;
    @BindView(R.id.personal_info_sign_layout)
    RelativeLayout personalInfoSignLayout;
    @BindView(R.id.personal_info_picker)
    NumberPickerView personalInfoPicker;
    @BindView(R.id.personal_info_picker_btn)
    Button personalInfoPickerBtn;
    @BindView(R.id.personal_info_picker_layout)
    LinearLayout personalInfoPickerLayout;

    private String[] sexArr = new String[]{"男","女"};
    private boolean isRequireCheck; // 是否需要系统权限检测
    private static final int REQUEST_CODE = 0; // 请求码
    private int RESULT_IMAGE_CODE = 200;
    private String[] PERMISSIONS = new String[]{Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private SwipeBackLayout mBackLayout;   //侧滑关闭Activity所用
    private MyBitmapCacheUtil cacheUtil;

    private String objectId ;
    private String nickName ;
    private boolean sexBool ;
    private String signText ;
    private BmobFile bmobimage ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);
        ButterKnife.bind(this);
        cacheUtil = new MyBitmapCacheUtil();
        initToolBar();
        initPermissionState();
        //侧滑关闭Activity的重要方法
        mBackLayout = getSwipeBackLayout();
        mBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
    }

    private void initToolBar() {
        toolbar.setTitle(R.string.personal_info_title_text);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PersonalEditInfoActivity.this, PersonCenterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.personal_info_menu_submit:
                        submitUserInfo();
                        break;
                }
                return true;
            }
        });
    }

    /**
     * 提交数据
     */
    public void submitUserInfo(){
        UserInfoEntity entity = new UserInfoEntity();
        if(nickName != null){
            entity.setUserName(nickName);
        }
        if(bmobimage != null){
            entity.setUserImg(bmobimage);
        }
        if(signText != null){
            entity.setUserSign(signText);
        }
        entity.setSex(sexBool);
        entity.update(objectId,new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e == null){
                    ToastUtils.showShort(PersonalEditInfoActivity.this,"资料修改成功");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    },2000);
                }else{
                    ToastUtils.showShort(PersonalEditInfoActivity.this,"资料修改失败");
                    LogUtils.e("Error:",e.toString());
                }
            }
        });
    }
    /**
     * 加载已保存的数据
     */
    private void loadData() {
        BmobQuery<UserInfoEntity> query = new BmobQuery<UserInfoEntity>("userinfo");
        String userId = EMClient.getInstance().getCurrentUser();
        query.addWhereEqualTo("userId",userId);
        query.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray jsonArray, BmobException e) {
                if (e == null) {
                    List<UserInfoEntity> userInfo = new JsonUtil().jsonToList(jsonArray.toString());
                    for (UserInfoEntity entity : userInfo) {
                        String userName = entity.getUserName();
                        BmobFile userImage = entity.getUserImg();
                        boolean isDefImage = entity.isDefImg();
                        boolean sex = entity.isSex();
                        String sign = entity.getUserSign();
                        Bitmap bitmap = null;
                        personalInfoName.setText(!userName.equals("") || userName != null ? userName : "");
                        personalInfoSign.setText(!sign.equals("") || sign != null ? sign : "");
                        if (isDefImage) {
                            int position = Integer.parseInt(entity.getDefImgPosition());
                            bitmap = BitmapUtils.getBitmapById(PersonalEditInfoActivity.this, ContextSave.defPicArray[position]);
                            personalInfoImage.setImageBitmap(bitmap);
                        } else {
                            String url = entity.getUserImg().getUrl();
                            cacheUtil.disPlayImage(personalInfoImage, url);
                        }
                        if (sex) {
                            personalInfoSex.setImageResource(R.mipmap.sex_boy_img_24dp);
                        } else {
                            personalInfoSex.setImageResource(R.mipmap.sex_girl_img_24dp);
                        }
                        sexBool = sex ;
                        nickName = userName ;
                        signText = sign ;
                        objectId = entity.getObjectId();
                        LogUtils.i("ObjectId",objectId);
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.personal_info_menu, menu);
        return true;
    }

    @OnClick({R.id.personal_info_image_layout, R.id.personal_info_name_layout, R.id.personal_info_sex_layout, R.id.personal_info_sign_layout,R.id.personal_info_picker_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.personal_info_image_layout:
                if(personalInfoPickerLayout.getVisibility() == View.VISIBLE){
                    personalInfoPickerLayout.setVisibility(View.GONE);
                    break;
                }
                Intent picture = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(picture, RESULT_IMAGE_CODE);
                break;
            case R.id.personal_info_name_layout:
                if(personalInfoPickerLayout.getVisibility() == View.VISIBLE){
                    personalInfoPickerLayout.setVisibility(View.GONE);
                    break;
                }
                showEditDialog("设置昵称", view);
                break;
            case R.id.personal_info_sex_layout:
                personalInfoPickerLayout.setVisibility(View.VISIBLE);
                personalInfoPicker.refreshByNewDisplayedValues(sexArr);
                if (personalInfoPicker.getWrapSelectorWheelAbsolutely()) {
                    personalInfoPicker.setWrapSelectorWheel(!personalInfoPicker.getWrapSelectorWheelAbsolutely());
                }
                break;
            case R.id.personal_info_sign_layout:
                if(personalInfoPickerLayout.getVisibility() == View.VISIBLE){
                    personalInfoPickerLayout.setVisibility(View.GONE);
                    break;
                }
                showEditDialog("设置签名", view);
                break;
            case R.id.personal_info_picker_btn:
                personalInfoPickerLayout.setVisibility(View.GONE);
                String[] content = personalInfoPicker.getDisplayedValues();
                if (content != null) {
                    String contentStr = content[personalInfoPicker.getValue() - personalInfoPicker.getMinValue()];
                    if(contentStr.equals("男")){
                        personalInfoSex.setImageResource(R.mipmap.sex_boy_img_24dp);
                        sexBool = true ;
                    }else{
                        personalInfoSex.setImageResource(R.mipmap.sex_girl_img_24dp);
                        sexBool = false ;
                    }
                }
                break;
        }
    }

    /**
     * 显示Dialog
     * @param title
     * @param view
     */
    private void showEditDialog(String title, final View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        final EditText editText = new EditText(this);
        editText.setHintTextColor(getResources().getColor(R.color.second_text_color));
        String content = null ;
        if (view.getId() == R.id.personal_info_name_layout) {
            content = personalInfoName.getText().toString();
        } else if (view.getId() == R.id.personal_info_sign_layout) {
            content = personalInfoSign.getText().toString();
        }
        editText.setText(content);
        editText.setSelection(editText.getText().length());
        builder.setView(editText, 50, 50, 50, 50);
        builder.setNegativeButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String content = editText.getText().toString();
                if (view.getId() == R.id.personal_info_name_layout) {
                    personalInfoName.setText(content);
                    nickName = content;
                } else if (view.getId() == R.id.personal_info_sign_layout) {
                    personalInfoSign.setText(content);
                    signText = content;
                }
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //通过RequestCode判断
        if (requestCode == RESULT_IMAGE_CODE && resultCode == Activity.RESULT_OK && null != data) {
            //发送图片的方法
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = this.getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            final String picturePath = c.getString(columnIndex);
            c.close();
            if (FileTools.isFoundFilePath(picturePath)) {
                if(bmobimage != null){
                    bmobimage = null ;
                }
                bmobimage = new BmobFile(new File(picturePath));
                bmobimage.uploadblock(new UploadFileListener() {

                    @Override
                    public void done(BmobException e) {
                        if(e==null){
                            //bmobFile.getFileUrl()--返回的上传文件的完整地址
                            ToastUtils.showShort(PersonalEditInfoActivity.this,"图片上传成功");
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 2;
                            Bitmap bitmap = BitmapUtils.getBitmapByFile(picturePath, options);
                            personalInfoImage.setImageBitmap(bitmap);
                        }else{
                            ToastUtils.showShort(PersonalEditInfoActivity.this,"图片上传失败");
                        }

                    }
                    @Override
                    public void onProgress(Integer value) {
                        // 返回的上传进度（百分比）
                    }
                });
            }
        }
    }

    /**
     * 初始化权限
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void initPermissionState() {
        if (PermissionCheckerUtil.getInstance().lacksPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE);
        } else {
            //如果包含所有权限
            loadData();
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
            loadData();
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
