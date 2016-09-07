package entity;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

/**
 * Created by jimhao on 16/8/25.
 */
public class UserInfoEntity extends BmobObject{
    public UserInfoEntity(){
        this.setTableName("userinfo");
    }
    private String userName ;
    private String userId ; //mainkey
    private BmobFile userImg ;
    private boolean isDefImg ;  //是否为默认头像
    private BmobFile bg ;   //用户主页背景图

    public BmobFile getBg() {
        return bg;
    }

    public void setBg(BmobFile bg) {
        this.bg = bg;
    }

    private String defImgPosition ;     //默认头像的编号

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BmobFile getUserImg() {
        return userImg;
    }

    public void setUserImg(BmobFile userImg) {
        this.userImg = userImg;
    }

    public boolean isDefImg() {
        return isDefImg;
    }

    public void setDefImg(boolean defImg) {
        isDefImg = defImg;
    }

    public String getDefImgPosition() {
        return defImgPosition;
    }

    public void setDefImgPosition(String defImgPosition) {
        this.defImgPosition = defImgPosition;
    }


}
