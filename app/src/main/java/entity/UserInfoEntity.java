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
}
