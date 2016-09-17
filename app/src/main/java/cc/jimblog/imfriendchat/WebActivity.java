package cc.jimblog.imfriendchat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import util.NetWorkUtils;

/**
 * 内置浏览器
 * 修改意见 添加透明指示层告知用户可以侧滑关闭
 * Created by Ran on 2016/8/29.
 */
public class WebActivity extends SwipeBackActivity {

    @BindView(R.id.blog_snackbar_layout)
    CoordinatorLayout blogSnackbarLayout;
    @BindView(R.id.blog_webView)
    WebView blogWebView;
    @BindView(R.id.blog_frame_layout)
    FrameLayout blogFrameLayout;

    private WebChromeClient.CustomViewCallback myCallBack = null;
    private long exitTime = 0;
    private static final String DEFAULT_URL = "http://jimblog.cc/";
    private WebChromeClient chromeClient = null;
    private View myView = null;
    private FrameLayout frameLayout = null;
    private boolean isWifi = false;
    private LayoutInflater mInflater;
    private ValueCallback<Uri> mUploadMessage;
    private final static int FILECHOOSER_RESULTCODE = 1;
    private final static String TAG = "WebActivity";
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    private SwipeBackLayout mBackLayout;   //侧滑关闭Activity所用

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        ButterKnife.bind(this);
        mBackLayout = getSwipeBackLayout();
        mBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

        String url = getIntent().getStringExtra("URL");
        if (url != null) {
            initblogWebView(url);
        } else {
            initblogWebView(DEFAULT_URL);
        }
        if (savedInstanceState != null) {
            blogWebView.restoreState(savedInstanceState);
        }

        boolean isConnected = NetWorkUtils.isConnected(this);
        if (isConnected) {
            isWifi = NetWorkUtils.isWifi(this);
            if (!isWifi) {
                showSnackBar("正在使用流量访问，请注意当前流量情况");
            }
        }
    }

    // 覆盖Activity类的onKeyDown(int keyCoder,KeyEvent event)方法
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && blogWebView.canGoBack()) {
            blogWebView.goBack(); // goBack()表示返回WebView的上一页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 初始化方法
     */
    private void initblogWebView(String url) {
        blogWebView.getSettings().setJavaScriptEnabled(true);
        blogWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);


        blogWebView.setWebViewClient(new MyWebviewCient());

        blogWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReachedMaxAppCacheSize(long requiredStorage, long quota, WebStorage.QuotaUpdater quotaUpdater) {
                quotaUpdater.updateQuota(requiredStorage * 2);
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (myView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                frameLayout.removeView(blogWebView);
                frameLayout.addView(view);
                myView = view;
                myCallBack = callback;
            }

            @Override
            public void onHideCustomView() {
                if (myView == null) {
                    return;
                }
                frameLayout.removeView(myView);
                myView = null;
                frameLayout.addView(blogWebView);
                myCallBack.onCustomViewHidden();
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                // TODO Auto-generated method stub
                Log.d("ZR", consoleMessage.message() + " at " + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber());
                return super.onConsoleMessage(consoleMessage);
            }

            // For Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                Log.d(TAG, "openFileChoose(ValueCallback<Uri> uploadMsg)");
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                WebActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            }

            // For Android 3.0+
            public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                Log.d(TAG, "openFileChoose( ValueCallback uploadMsg, String acceptType )");
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                WebActivity.this.startActivityForResult(
                        Intent.createChooser(i, "File Browser"),
                        FILECHOOSER_RESULTCODE);
            }

            //For Android 4.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                Log.d(TAG, "openFileChoose(ValueCallback<Uri> uploadMsg, String acceptType, String capture)");
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                WebActivity.this.startActivityForResult(Intent.createChooser(i, "File Browser"), WebActivity.FILECHOOSER_RESULTCODE);
            }

            // For Android 5.0+
            public boolean onShowFileChooser(WebView blogWebView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                Log.d(TAG, "onShowFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture)");
                mUploadCallbackAboveL = filePathCallback;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                WebActivity.this.startActivityForResult(
                        Intent.createChooser(i, "File Browser"),
                        FILECHOOSER_RESULTCODE);
                return true;
            }
        });
        blogWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        blogWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);

        blogWebView.setHorizontalScrollBarEnabled(false);
        blogWebView.setVerticalScrollBarEnabled(false);

        final String USER_AGENT_STRING = blogWebView.getSettings().getUserAgentString() + " Rong/2.0";
        blogWebView.getSettings().setUserAgentString(USER_AGENT_STRING);
        blogWebView.getSettings().setSupportZoom(false);
        blogWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        blogWebView.getSettings().setLoadWithOverviewMode(true);
        blogWebView.setBackgroundColor(0);
        if (Build.VERSION.SDK_INT >= 19) {
            blogWebView.getSettings().setLoadsImagesAutomatically(true);
        } else {
            blogWebView.getSettings().setLoadsImagesAutomatically(false);
        }
        //设置横向无滚动
        blogWebView.setHorizontalScrollBarEnabled(false);
        //设置blogWebView缓存
        WebSettings webseting = blogWebView.getSettings();
        // 开启JavaScript
        webseting.setJavaScriptEnabled(true);
        // 开启DOM storage API 功能
        webseting.setDomStorageEnabled(true);
        // 应用可以有数据库
        webseting.setDatabaseEnabled(true);
        //设置缓冲大小，我设的是8M
        webseting.setAppCacheMaxSize(1024 * 1024 * 8);
        String appCacheDir = getDir("cache", Context.MODE_PRIVATE).getPath();
        webseting.setAppCachePath(appCacheDir);
        // 允许访问文件
        webseting.setAllowFileAccess(true);
        // 开启缓存
        webseting.setAppCacheEnabled(true);
        webseting.setCacheMode(WebSettings.LOAD_DEFAULT);


        blogWebView.loadUrl(url);

    }

    public class MyWebviewCient extends WebViewClient {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view,
                                                          String url) {
            WebResourceResponse response = null;
            response = super.shouldInterceptRequest(view, url);
            return response;
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            //此处可加载错误提示View
            showSnackBar("无网络连接");
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            //此处可关闭错误提示View
        }
    }

    /**
     * 展示SnackBar
     *
     * @param message 消息对象
     */
    public void showSnackBar(String message) {
        Snackbar.make(blogSnackbarLayout, message, 1300).show();
    }

    @Override
    public void onBackPressed() {
        if (myView == null) {
            super.onBackPressed();
        } else {
            chromeClient.onHideCustomView();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        blogWebView.saveState(outState);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent data) {
        if (requestCode != FILECHOOSER_RESULTCODE
                || mUploadCallbackAboveL == null) {
            return;
        }

        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {

            } else {
                String dataString = data.getDataString();
                ClipData clipData = data.getClipData();

                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }

                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        mUploadCallbackAboveL.onReceiveValue(results);
        mUploadCallbackAboveL = null;
        return;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage && null == mUploadCallbackAboveL) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (mUploadCallbackAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }
    }
}
