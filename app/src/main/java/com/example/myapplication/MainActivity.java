package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;


public class MainActivity extends AppCompatActivity implements CameraController.CameraControllerInterFaceCallback {
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
    private ImageButton timbtn;
    private Resources resources;
    private ImageButton sf_btn;
    private ImageButton previve;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestFullScreenActivity();
        setContentView(R.layout.activity_main);
        resources = getApplicationContext().getResources();

        timbtn = findViewById(R.id.img_time);
        mPreviewView = findViewById(R.id.textureV);
        takePicture = findViewById(R.id.takePicture);
        chg_btn = findViewById(R.id.img_chg);
        num = findViewById(R.id.num);
        pho_btn = findViewById(R.id.pho_btn);
        vdo_btn = findViewById(R.id.vdo_btn);
        sf_btn = findViewById(R.id.img_DX);
        previve = findViewById(R.id.preview_view);

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
        sf_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cc.daXiaoGaiBian();
            }
        });
        pho_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status=Status.PHOTOGRAPH;
                sf_btn.setVisibility(View.VISIBLE);
                pho_btn.setBackground(resources.getDrawable(R.drawable.select_btn));
                pho_btn.setTextColor(Color.BLACK);
                vdo_btn.setBackgroundColor(resources.getColor(R.color.white2));
                vdo_btn.setTextColor(Color.WHITE);
                takePicture.setBackground(resources.getDrawable(R.drawable.camera_btn));
                cc.v2p(status);
            }
        });
        vdo_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status=Status.RECORD;
                sf_btn.setVisibility(View.GONE);
                takePicture.setBackground(resources.getDrawable(R.drawable.video_start_btn));
                vdo_btn.setBackground(resources.getDrawable(R.drawable.select_btn));
                vdo_btn.setTextColor(Color.BLACK);
                pho_btn.setBackgroundColor(resources.getColor(R.color.white2));
                pho_btn.setTextColor(Color.WHITE);
                cc.p2v(status);
            }
        });
        timbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status=Status.DELAY_PHOTOGRAPH;
            }
        });



        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                            cc.stopRecordingVideo();
                            Toast.makeText(MainActivity.this, "录像保存在："+mFile.toString(), Toast.LENGTH_SHORT).show();
                        }else {
                            mFile = new File(MainActivity.this.getExternalFilesDir(null),"test.mp4");
                            cc.setVideoPath(mFile);
                            mIsRecordingVideo=true;
                            cc.startRecordingVideo();
                            takePicture.setBackground(resources.getDrawable(R.drawable.video_start_btn));
                        }
                }
            }
        });
        chg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cc.changeCameraBtn();
            }
        });

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
}