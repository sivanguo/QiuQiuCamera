package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaMetadata;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class CameraController {


    private Size mPreviewSize = new Size(2280, 1080);
    private Size mCaptureSize = new Size(2280, 1080);
    private Size mVideoSize = new Size(2280, 1080);
    private File mFile;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession mCaptureSession;
    private String mCameraId = "0";
    private CameraDevice mCameraDevice;
    private ImageReader mImageReader;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private AutoFitTextureView mPreviewView;
    private MainActivity mActivity;
    private CameraManager manager;
    private float mTargetRatio = 2f;
    public static final float PREVIEW_SIZE_RATIO_OFFSET = 0.01f;
    private MediaRecorder mMediaRecorder;
    private Status status;
    private ImageButton previve;
    private Bitmap compressBitmap;


    public CameraController(MainActivity activity, AutoFitTextureView mPreviewView, Status status, ImageButton preview) {
        this.mPreviewView = mPreviewView;
        this.mActivity = activity;
        this.status=status;
        this.previve=preview;
    }

    public void changeCameraBtn(){
        closeSession();
        if (mCameraDevice!=null){
            mCameraDevice.close();
            mCameraDevice = null;
        }

        if (mCameraId.equals("0")){
            mCameraId="1";
        }else if (mCameraId.equals("1")){
            mCameraId="0";
        }
        System.out.println("--------------------------------------------"+mCameraId);

        openCamera();
    }

    public void beginTackPicture() {
        CaptureRequest.Builder captureRequestBuilder2 = null;
        mFile = new File(Environment.getExternalStorageDirectory(), "DCIM/Camera/" + System.currentTimeMillis() + ".jpg");
        try {System.out.println(mCameraDevice+"--------------------------------------------------------");
            captureRequestBuilder2 = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureRequestBuilder2.addTarget(mImageReader.getSurface());

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
                super.onCaptureProgressed(session, request, partialResult);
            }

            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
            }
        };
        try {
            mCaptureSession.capture(captureRequestBuilder2.build(), CaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    public void openCamera() {
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(mActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(mActivity, "权限不够", Toast.LENGTH_SHORT).show();
            return;
        }
        manager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
        System.out.println(status+"--------------------------------------------------------------------");
        try {
            if (status==Status.RECORD) {
                mMediaRecorder = new MediaRecorder();
            }
                manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void choosePreviewAndCaptureSize() {
        CameraCharacteristics characteristics = null;
        try {
            characteristics = manager.getCameraCharacteristics(mCameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] previewSizeMap = map.getOutputSizes(SurfaceTexture.class);
        Size[] videoSizeMap = map.getOutputSizes(MediaRecorder.class);

        int screenWidth = getScreenWidth(mActivity.getApplicationContext());
        mPreviewSize = getPreviewSize(previewSizeMap, 2f, screenWidth);
        mVideoSize = mPreviewSize;
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPreviewView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            }
        });
    }

    private Size getPreviewSize(Size[] previewSizeMap, float mTargetRatio, int screenWidth) {
        Size previewSize = null;
        int minOffSize = Integer.MAX_VALUE;
        for (int i = 0; i < previewSizeMap.length; i++) {
            float ratio = previewSizeMap[i].getWidth() / (float) previewSizeMap[i].getHeight();
            if (Math.abs(ratio - mTargetRatio) > PREVIEW_SIZE_RATIO_OFFSET) {
                continue;
            }
            int diff = Math.abs(previewSizeMap[i].getHeight() - screenWidth);
            if (diff < minOffSize) {
                previewSize = previewSizeMap[i];
                minOffSize = diff;
            } else if ((diff == minOffSize) && (previewSizeMap[i].getHeight() > screenWidth)) {
                previewSize = previewSizeMap[i];
            }
        }
        return previewSize;
    }

    private int getScreenWidth(Context applicationContext) {
        return applicationContext.getResources().getDisplayMetrics().widthPixels;
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;

            if (status==Status.RECORD){
                choosePreviewAndCaptureSize();
                System.out.println("-------------------------------------------------------------"+mCameraDevice);
            }else if(status==Status.PHOTOGRAPH){
                createImageReader();
            }
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
            mActivity.finish();
        }
    };

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture surfaceTexture = mPreviewView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface surface = new Surface(surfaceTexture);
            captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);


            if (status==Status.RECORD) {
                mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        if (null == mCameraDevice) {
                            return;
                        }
                        mCaptureSession = session;
                        setRequestBuilderParams();
                        updatePreview();
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    }
                }, null);
            } else {
                mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        if (null == mCameraDevice) {
                            return;
                        }
                        mCaptureSession = session;
                        CaptureRequest captureRequest = captureRequestBuilder.build();
                        try {
                            mCaptureSession.setRepeatingRequest(captureRequest, mCaptureCallback, mBackgroundHandler);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                    }
                }, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        CaptureRequest captureRequest = captureRequestBuilder.build();
        try {
            mCaptureSession.setRepeatingRequest(captureRequest, mCaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setRequestBuilderParams() {
        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
    }

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }
    };

    private void createImageReader() {



        CameraCharacteristics characteristics = null;
        try {
            characteristics = manager.getCameraCharacteristics(mCameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] previewSizeMap = map.getOutputSizes(SurfaceTexture.class);
        int screenWidth = getScreenWidth(mActivity.getApplicationContext());
        mPreviewSize = getPreviewSize(previewSizeMap, mTargetRatio, screenWidth);




        mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.JPEG, 2);
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPreviewView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());//设置PreviewView长宽比例
            }
        });
    }

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            saveImage(reader);
        }
    };

    private void saveImage(ImageReader reader) {
        Image mImage = reader.acquireNextImage();
        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        FileOutputStream outputStream = null;
        compressbySample(bytes);
        try {
            outputStream = new FileOutputStream(mFile);
            if (false){
                outputStream.write(bytes);
            }else {
                addWaterMark(bytes).compress(Bitmap.CompressFormat.JPEG,100,outputStream);
            }

            Toast.makeText(mActivity, "保存路径：" + mFile.toString(), Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mImage.close();
            if (null != outputStream) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Bitmap addWaterMark(byte[] bytes){
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Paint mPaint = new Paint();
        mPaint.setTextSize(100);
        mPaint.setColor(Color.BLACK);
        Bitmap bitmap1 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap1);
        canvas.drawBitmap(bitmap,0,0,null);
        canvas.drawText("1111111111",100,100,mPaint);
        return bitmap1;
    }
    public void compressbySample(byte[] bytes){
        ByteArrayInputStream isBy = new ByteArrayInputStream(bytes);
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inPreferredConfig=Bitmap.Config.RGB_565;
        options.inPurgeable=true;
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeStream(isBy,null,options);
        options.inSampleSize=calculateInSampleSize(options,130,130);
        options.inMutable=true;
        try {
            Bitmap bitmap=Bitmap.createBitmap(options.outWidth,options.outHeight,Bitmap.Config.RGB_565);
            if (bitmap!=null){
                options.inBitmap=bitmap;
            }
        } catch (Exception e) {
            options.inBitmap=null;
            System.gc();
        }
        options.inJustDecodeBounds=false;
        isBy.reset();
        try {
            compressBitmap =BitmapFactory.decodeStream(isBy,null,options);
        } catch (Exception e) {
            compressBitmap =null;
            System.gc();
        }
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                previve.setImageBitmap(compressBitmap);;
            }
        });

    }
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int originalWidth = options.outWidth;//1080
        int originalHeight = options.outHeight;//2400
        //275
        int inSampleSize = 1;
        if (originalHeight > reqHeight || originalWidth > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) originalHeight / (float) reqHeight);
            final int widthRatio = Math.round((float) originalWidth / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }



    public void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    public void stopRecordingVideo() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
        }
        closeSession();
        createCameraPreviewSession();
    }

    private void closeSession() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
    }

    public void setVideoPath(File file) {
        mFile = file;
    }

    public void startRecordingVideo() {

        try {
            closeSession();
            choosePreviewAndCaptureSize();
            setUpMediaRecorder();
            SurfaceTexture texture=mPreviewView.getSurfaceTexture();
            assert texture!=null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(),mPreviewSize.getHeight());
            captureRequestBuilder=mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            Surface surface = new Surface(texture);
            captureRequestBuilder.addTarget(surface);
            captureRequestBuilder.addTarget(mMediaRecorder.getSurface());
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mMediaRecorder.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCaptureSession=session;
                    updatePreview();
                    mCameraCallback.startRecordVideo();
                    mMediaRecorder.start();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            },null);

        } catch (IOException | CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpMediaRecorder() throws IOException {
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(mFile.getPath());
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.prepare();
    }

    private CameraControllerInterFaceCallback mCameraCallback;

    public void daXiaoGaiBian() {
        closeSession();
        if (mCameraDevice!=null){
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (mTargetRatio==2f){
            mTargetRatio=1.333f;
        }else{
            mTargetRatio=2f;
        }
        openCamera();
    }

    interface CameraControllerInterFaceCallback{
        void startRecordVideo();
    }

    public void setCameraControllerInterFaceCallback(CameraControllerInterFaceCallback cameraCallback){
        mCameraCallback=cameraCallback;
    }

    public void p2v(Status status){
        if (mImageReader != null) {
            mImageReader.close();
        }
        this.status=status;
        closeSession();
        openCamera();
    }
    public void v2p(Status status){
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
        }
        this.status=status;
        closeSession();
        openCamera();
    }


}
