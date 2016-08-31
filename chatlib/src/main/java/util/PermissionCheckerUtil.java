package util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * Created by jimhao on 16/8/31.
 */
public class PermissionCheckerUtil {

    private PermissionCheckerUtil(){}
    public static PermissionCheckerUtil getInstance(){
        return Nested.instance;
    }

    static class Nested{
        private static PermissionCheckerUtil instance = new PermissionCheckerUtil();
    }

    // 判断权限集合
    public boolean lacksPermissions(Context mContext ,String... permissions) {
        for (String permission : permissions) {
            if (lacksPermission(mContext,permission)) {
                return true;
            }
        }
        return false;
    }

    // 判断是否缺少权限
    private boolean lacksPermission(Context mContext ,String permission) {
        return ContextCompat.checkSelfPermission(mContext, permission) ==
                PackageManager.PERMISSION_DENIED;
    }
}
