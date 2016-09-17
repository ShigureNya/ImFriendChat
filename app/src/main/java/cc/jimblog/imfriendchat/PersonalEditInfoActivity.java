package cc.jimblog.imfriendchat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import image.ImageDownload;
import util.BitmapUtils;
import util.FileTools;
import util.JsonUtil;
import view.CircleImageView;

/**
 * Created by Ran on 2016/9/17.
 */
public class PersonalEditInfoActivity extends AppCompatActivity {
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

    private int RESULT_IMAGE_CODE = 200;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);
        ButterKnife.bind(this);
        initToolBar();
        loadData();

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

                        break;
                }
                return true;
            }
        });
    }

    /**
     * 加载已保存的数据
     */
    private void loadData(){
        BmobQuery<UserInfoEntity> query = new BmobQuery<UserInfoEntity>("userinfo");
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
                        Bitmap bitmap = null ;
                        personalInfoName.setText(!userName.equals("")||userName!=null?userName:"");
                        personalInfoSign.setText(!sign.equals("")||sign!=null?sign:"");
                        if(isDefImage){
                            int position = Integer.parseInt(entity.getDefImgPosition());
                            bitmap = BitmapUtils.getBitmapById(PersonalEditInfoActivity.this, ContextSave.defPicArray[position]);
                        }else{
                            bitmap = ImageDownload.getInstance().downLoadBitmap(userImage.getUrl());
                        }
                        personalInfoImage.setImageBitmap(bitmap);

                        if(sex){
                            personalInfoSex.setImageResource(R.mipmap.sex_boy_img_24dp);
                        }else{
                            personalInfoSex.setImageResource(R.mipmap.sex_girl_img_24dp);
                        }
                    }
                } else {

                }
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.personal_info_menu, menu);
        return true;
    }

    @OnClick({R.id.personal_info_image_layout, R.id.personal_info_name_layout, R.id.personal_info_sex_layout,R.id.personal_info_sign_layout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.personal_info_image_layout:
                Intent picture = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(picture, RESULT_IMAGE_CODE);
                break;
            case R.id.personal_info_name_layout:
                showEditDialog("设置昵称",view);
                break;
            case R.id.personal_info_sex_layout:

                break;
            case R.id.personal_info_sign_layout:
                showEditDialog("设置签名",view);
                break;
        }
    }
    private void showEditDialog(String title, final View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        final EditText editText = new EditText(this);
        editText.setHintTextColor(getResources().getColor(R.color.second_text_color));
        builder.setView(editText,50,50,50,50);
        builder.setNegativeButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String content = editText.getText().toString();
                if(view.getId() == R.id.personal_info_name_layout){
                    personalInfoName.setText(content);
                }else if(view.getId() == R.id.personal_info_sign_layout){
                    personalInfoSign.setText(content);
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
            String picturePath = c.getString(columnIndex);
            c.close();
            if (FileTools.isFoundFilePath(picturePath)) {
                Bitmap bitmap = BitmapUtils.getBitmapByFile(picturePath);
                personalInfoImage.setImageBitmap(bitmap);
            }
        }
    }
}
