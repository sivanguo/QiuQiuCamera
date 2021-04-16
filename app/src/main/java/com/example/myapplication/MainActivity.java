package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.OrientationEventListener;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements CameraController.CameraControllerInterFaceCallback,TwoStateSwitch.SwitchStatusCheckListener, View.OnClickListener {
    private TextureView.SurfaceTextureListener mSurfaceTextureListner=new TextureView.SurfaceTextureListener(){

        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            cc.openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
        }
    };
    private Thread thread=null;
    private AutoFitTextureView mPreviewView;
    private CameraController cc;
    private Status status=Status.PHOTOGRAPH;
    private boolean mIsRecordingVideo=false;
    private File mFile;
    private Button takePicture;
    private Handler handler;
    private Button vdo_btn;
    private Button pho_btn;
    private TextView num;
    private ImageButton chg_btn;
    private TwoStateSwitch timbtn;
    private Resources resources;
    private TwoStateSwitch sf_btn;
    private ImageButton previve;
    private TwoStateSwitch flash;
    private boolean flashButton=true;
    private ImageButton settings;
    public static final int ORIENTATION_HYSTERESIS = 5;
    private int mPhoneOrientation;
    private MyOrientationEventListener mOrientationListener;
    private ValueAnimator valueAnimator;
    private Button human;
    private Button night;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestFullScreenActivity();
        setContentView(R.layout.activity_main);
        resources = getApplicationContext().getResources();
        registerOrientationLister();

        timbtn = findViewById(R.id.img_time);
        mPreviewView = findViewById(R.id.textureV);
        takePicture = findViewById(R.id.takePicture);
        chg_btn = findViewById(R.id.img_chg);
        num = findViewById(R.id.num);
        pho_btn = findViewById(R.id.pho_btn);
        vdo_btn = findViewById(R.id.vdo_btn);
        sf_btn = findViewById(R.id.img_DX);
        previve = findViewById(R.id.preview_view);
        flash = findViewById(R.id.img_SG);
        settings = findViewById(R.id.settings);
        human = findViewById(R.id.human_btn);
        night = findViewById(R.id.night_btn);


        cc = new CameraController(this,mPreviewView,status,previve);
        cc.setCameraControllerInterFaceCallback(this);


        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what==0x01){
                    num.setText(msg.arg1+"");
                    if(msg.arg1==0){
                        num.setText("");
                        cc.beginTackPicture();
                    }
                }
            }
        };


        //---------------------------------------------------------------------------------------监听------------------------------------------------------------------
        sf_btn.setSwitchStatusCheckListener(this);
        flash.setSwitchStatusCheckListener(this);
        pho_btn.setOnClickListener(this::onClick);
        settings.setOnClickListener(this::onClick);
        previve.setOnClickListener(this::onClick);
        vdo_btn.setOnClickListener(this::onClick);
        timbtn.setSwitchStatusCheckListener(this);
        takePicture.setOnClickListener(this::onClick);
        chg_btn.setOnClickListener(this::onClick);
    }
    //----------------------------------------------------------------------------------监听结束--------------------------------------------------------------------------------

    public void ObjectAnim(View o){
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(o, "scaleY", 1f, 3f, 1f);
        objectAnimator.setDuration(1000);
        objectAnimator.start();
    }

    private void registerOrientationLister() {
        mOrientationListener = new MyOrientationEventListener(this);
    }

    public void delayPhoto(){
        if (thread==null||!thread.isAlive()){
            thread=new Thread(){
                @Override
                public void run() {
                    for (int i=9;i>=0;i--){
                        try {
                            Message me=handler.obtainMessage();
                            me.what=0x01;
                            me.arg1=i;
                            handler.sendMessage(me);
                            sleep(1000);
                            System.out.println(i);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            thread.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cc.startBackgroundThread();
        if (mPreviewView.isAvailable()){

        }else {
            mPreviewView.setSurfaceTextureListener(mSurfaceTextureListner);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle = intent.getBundleExtra("flag1");
        boolean wmFlag1 = bundle.getBoolean("WmFlag1");
        cc.setWmFlag(wmFlag1);
        System.out.println("返回后的水印模式为： "+cc.isWmFlag());
    }

    private void requestFullScreenActivity() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void startRecordVideo() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                takePicture.setBackground(resources.getDrawable(R.drawable.video_end_btn));
            }
        });
    }

    @Override
    public void ViewCheckOnMethod(int viewId) {
        switch (viewId){
            case R.id.img_SG:
                cc.openFlashMode();
                break;
            case R.id.img_time:
                status=Status.DELAY_PHOTOGRAPH;
                break;
            case R.id.img_DX:
                cc.daXiaoGaiBian();
                break;

        }
    }

    @Override
    public void ViewCheckOffMethod(int viewId) {
        switch (viewId){
            case R.id.img_SG:
                cc.closeFlashMode();
                break;
            case R.id.img_time:
                status=Status.PHOTOGRAPH;
                break;
            case R.id.img_DX:
                cc.daXiaoGaiBian();
                break;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.human_btn:
                break;
            case R.id.night_btn:
                break;
            case R.id.pho_btn:
                                status=Status.PHOTOGRAPH;
                                sf_btn.setVisibility(View.VISIBLE);
                                pho_btn.setBackground(resources.getDrawable(R.drawable.select_btn));
                                pho_btn.setTextColor(Color.BLACK);
                                vdo_btn.setBackgroundColor(resources.getColor(R.color.white2));
                                vdo_btn.setTextColor(Color.WHITE);
                                takePicture.setBackground(resources.getDrawable(R.drawable.camera_btn));
                                cc.v2p(status);
                break;
            case R.id.vdo_btn:
                                status=Status.RECORD;
                                sf_btn.setVisibility(View.GONE);
                                takePicture.setBackground(resources.getDrawable(R.drawable.video_start_btn));
                                vdo_btn.setBackground(resources.getDrawable(R.drawable.select_btn));
                                vdo_btn.setTextColor(Color.BLACK);
                                pho_btn.setBackgroundColor(resources.getColor(R.color.white2));
                                pho_btn.setTextColor(Color.WHITE);
                                cc.p2v(status);
                break;
            case R.id.settings:
                                ObjectAnim(settings);
                                Intent intent=new Intent(MainActivity.this,SetingsActivity.class);
                                boolean flag=cc.isWmFlag();
                                System.out.println("点击前的水印模式为："+flag);
                                Bundle bundle=new Bundle();
                                bundle.putBoolean("WmFlag",flag);
                                intent.putExtra("flag",bundle);
                                startActivity(intent);
                break;
            case R.id.preview_view:
                                cc.gotoGallery();
                break;
            case R.id.img_chg:
                                ObjectAnimator rotation = ObjectAnimator.ofFloat(chg_btn, "rotation", 0f, 360f);
                                rotation.setDuration(1000);
                                rotation.start();
                                cc.changeCameraBtn();
                break;
            case R.id.takePicture:
                                switch (status){
                                    case PHOTOGRAPH:
                                        cc.beginTackPicture();
                                        break;
                                    case DELAY_PHOTOGRAPH:
                                        delayPhoto();
                                        break;
                                    case RECORD:
                                        if (mIsRecordingVideo){
                                            takePicture.setBackground(resources.getDrawable(R.drawable.video_end_btn));
                                            mIsRecordingVideo = false;
                                            cc.stopRecordingVideo(mFile);
                                            Toast.makeText(MainActivity.this, "录像保存在："+mFile.toString(), Toast.LENGTH_SHORT).show();
                                        }else {
                                            mFile = new File(MainActivity.this.getExternalFilesDir(null),"test.mp4");
                                            cc.setVideoPath(mFile);
                                            mIsRecordingVideo=true;
                                            cc.startRecordingVideo();
                                            takePicture.setBackground(resources.getDrawable(R.drawable.video_start_btn));
                                        }
                                }
                break;

        }
    }


    private class MyOrientationEventListener extends OrientationEventListener {
        public MyOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation==ORIENTATION_UNKNOWN){
                return;
            }
            mPhoneOrientation=roundOrientation(orientation,mPhoneOrientation);
            cc.setPhoneDeviceDegree(mPhoneOrientation);
        }
    }
    public int roundOrientation(int orientation,int orientationHistory){
        boolean changeOrientation = false;
        if (orientationHistory == OrientationEventListener.ORIENTATION_UNKNOWN){
            changeOrientation = true;
        } else {
            int dist = Math.abs(orientation - orientationHistory);
            dist = Math.min(dist,360-dist);
            changeOrientation = (dist >= 45 + ORIENTATION_HYSTERESIS);
        }
        if (changeOrientation){
            return ((orientation + 45) / 90 * 90)% 360;
        }
        return orientationHistory;
    }

}