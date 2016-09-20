package cc.jimblog.imfriendchat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import qrcode.camera.CameraManager;
import qrcode.decoding.CaptureActivityHandler;
import qrcode.decoding.InactivityTimer;
import qrcode.decoding.RGBLuminanceSource;
import qrcode.view.ViewfinderView;
import util.LogUtils;
import util.PermissionCheckerUtil;

/**
 * Created by jimhao on 16/9/19.
 */
public class QRCodeActivity extends AppCompatActivity implements SurfaceHolder.Callback{
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private Thread thread = null ;
    private boolean isRuning = false ;
    private Button button_back = null ;

    private static final int REQUEST_CODE = 100;
    private static final int PARSE_BARCODE_SUC = 300;
    private static final int PARSE_BARCODE_FAIL = 303;
    private ProgressDialog mProgress;
    private String photo_path;
    private Bitmap scanBitmap;
    private boolean isRequireCheck; // 是否需要系统权限检测
    private String[] PERMISSIONS = new String[]{Manifest.permission.CAMERA};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        //ViewUtil.addTopView(getApplicationContext(), this, R.string.scan_card);
        initPermissionState();
    }
    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            mProgress.dismiss();
            switch (msg.what) {
                case PARSE_BARCODE_SUC:
                    onResultHandler((String)msg.obj, scanBitmap);
                    break;
                case PARSE_BARCODE_FAIL:
                    Toast.makeText(QRCodeActivity.this, (String)msg.obj, Toast.LENGTH_LONG).show();
                    break;

            }
        }

    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch(requestCode){
                case REQUEST_CODE:
                    //获取选中图片的路径
                    Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
                    if (cursor.moveToFirst()) {
                        photo_path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    }
                    cursor.close();

                    mProgress = new ProgressDialog(QRCodeActivity.this);
                    mProgress.setMessage("正在扫描..");
                    mProgress.setCancelable(false);
                    mProgress.show();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Result result = scanningImage(photo_path);
                            if (result != null) {
                                Message m = mHandler.obtainMessage();
                                m.what = PARSE_BARCODE_SUC;
                                m.obj = result.getText();
                                mHandler.sendMessage(m);
                            } else {
                                Message m = mHandler.obtainMessage();
                                m.what = PARSE_BARCODE_FAIL;
                                m.obj = "Scan failed!";
                                mHandler.sendMessage(m);
                            }
                        }
                    }).start();

                    break;

            }
        }
    }

    /**
     * 扫描二维码图片的方法
     * @param path
     * @return
     */
    public Result scanningImage(String path) {
        if(TextUtils.isEmpty(path)){
            return null;
        }
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); //设置二维码内容的编码方式

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; //先获取原大小
        scanBitmap = BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false; // 获取新的大小
        int sampleSize = (int) (options.outHeight / (float) 200);
        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        scanBitmap = BitmapFactory.decodeFile(path, options);
        RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            return reader.decode(bitmap1, hints);

        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        if(CameraManager.get()!=null){
            CameraManager.get().closeDriver();
        }
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    /**
     * 处理扫描后的结果
     * @param result
     * @param barcode
     */
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String resultString = result.getText();
        //处理结果
        if(resultString.contains("UserId")){
            String userId = resultString.substring(resultString.indexOf(":")+1,resultString.length());
            LogUtils.i("UserId",userId);
            Intent intent = new Intent(QRCodeActivity.this,PersonCenterActivity.class);
            intent.putExtra("UserName",userId);
            intent.putExtra("AddFriendState","Ok");
            startActivity(intent);
            finish();
        }
    }

    /**
     * 跳转到上一个页面
     * @param resultString
     * @param bitmap
     */
    private void onResultHandler(String resultString, Bitmap bitmap){
        if(TextUtils.isEmpty(resultString)){
            Toast.makeText(QRCodeActivity.this, "Scan failed!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent resultIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("result", resultString);
        bundle.putParcelable("bitmap", bitmap);
        resultIntent.putExtras(bundle);
        this.setResult(RESULT_OK, resultIntent);
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats,
                    characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    /**
     * 初始化权限
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void initPermissionState() {
        if (PermissionCheckerUtil.getInstance().lacksPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE);
        } else {
            CameraManager.init(getApplication());
            viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
            hasSurface = false;
            inactivityTimer = new InactivityTimer(this);
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
            CameraManager.init(getApplication());
            viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
            hasSurface = false;
            inactivityTimer = new InactivityTimer(this);
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
