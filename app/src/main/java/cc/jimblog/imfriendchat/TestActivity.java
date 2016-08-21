package cc.jimblog.imfriendchat;

import android.os.Bundle;
import android.widget.RadioGroup;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

/**
 * Created by Ran on 2016/8/21.
 */
public class TestActivity extends SwipeBackActivity {

    private SwipeBackLayout layout ;

    private static int mBgIndex = 0;

    private String mKeyTrackingMode;

    private RadioGroup mTrackingModeGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        layout = getSwipeBackLayout();
        mTrackingModeGroup = (RadioGroup) findViewById(R.id.tracking_mode);
        mTrackingModeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int edgeFlag;
                switch (checkedId) {
                    case R.id.mode_left:
                        edgeFlag = SwipeBackLayout.EDGE_LEFT;
                        break;
                    case R.id.mode_right:
                        edgeFlag = SwipeBackLayout.EDGE_RIGHT;
                        break;
                    case R.id.mode_bottom:
                        edgeFlag = SwipeBackLayout.EDGE_BOTTOM;
                        break;
                    default:
                        edgeFlag = SwipeBackLayout.EDGE_ALL;
                }
                layout.setEdgeTrackingEnabled(edgeFlag);
            }
        });
    }
}
