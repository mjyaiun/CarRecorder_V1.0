package cn.edu.scu.carrecorder.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.SaveCallback;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.edu.scu.carrecorder.R;
import cn.edu.scu.carrecorder.activity.MainActivity;
import cn.edu.scu.carrecorder.classes.FileInfo;
import cn.edu.scu.carrecorder.util.Byte2ImageUtil;
import cn.edu.scu.carrecorder.util.PublicDate;
import cn.edu.scu.carrecorder.util.VideoSize;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class RecordFragment extends Fragment implements Callback, Camera.PictureCallback{
    private static  final int FOCUS_AREA_SIZE= 500;
    private Camera mCamera;
    private MediaRecorder mediaRecorder;
    private File tempFile;
    boolean recording = false;
    @InjectView(R.id.button_capture)
    ImageView capture;

    @InjectView(R.id.preview)
    SurfaceView preview;

    @InjectView(R.id.button_ChangeCamera)
    ImageView switchCamera;

    private Context myContext;

    @InjectView(R.id.camera_preview)
    LinearLayout cameraPreview;

    private static boolean cameraFront = false;
    private static boolean flash = false;

    @InjectView(R.id.buttonFlash)
    ImageView buttonFlash;

    @InjectView(R.id.chronoRecordingImage)
    ImageView chronoRecordingImage;

    @InjectView(R.id.textChrono)
    Chronometer chrono;

    @InjectView(R.id.menu)
    ImageView menu;

    @InjectView(R.id.speed)
    TextView speedDetail;

    @InjectView(R.id.position)
    TextView posDetail;
    private LocateFragment locateFragment;
    private long countUp;
    boolean powerSavingOn = false;
    boolean screenLightDecreased = false;
    int powerSavingCount = 0;
    Handler handler;
    private static final int CAMERA_OPENED = 626;
    private static final int CAMERA_OPEN_START = 23;
    SweetAlertDialog pDialog;
    boolean flag = false;
    boolean isOverWritten = false;
    String phoneNumber = "15985725431";

    public void setMonitor(boolean monitor, String number) {
        this.monitor = monitor;
        phoneNumber = number;
    }

    boolean monitor = false;

    private static RecordFragment homeFragment = new RecordFragment();

    public static void setHomeFragment(RecordFragment homeFragment) {
        RecordFragment.homeFragment = homeFragment;
    }

    public static RecordFragment getFragment() {
        if (homeFragment == null) {
            homeFragment = new RecordFragment();
        }
        return homeFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_record, container, false);
        ButterKnife.inject(this, view);

        myContext = getActivity();
        pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("相机初始化中");
        pDialog.setCancelable(false);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CAMERA_OPEN_START:
                        pDialog.show();
                        break;
                    case CAMERA_OPENED:
                        pDialog.dismissWithAnimation();
                        if (monitor) {
                            takePicture(phoneNumber);
                        }
                        break;
                }
            }
        };

        Runnable loadMap = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(300);
                    loadMapFrag();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(loadMap).start();

        initialize();
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void refreshInfo(String address, String speed) {
        posDetail.setText(address);
        speedDetail.setText(speed);
    }

    public void takePicture(String number) {
        phoneNumber = number;
        if (recording) {
            stopRecording();
        }
        mCamera.takePicture(new Camera.ShutterCallback() {
            @Override
            public void onShutter() {

            }
        }, null, RecordFragment.this);
    }

    private void loadMapFrag() {
        FragmentManager fragmentManager =getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        locateFragment = new LocateFragment();
        transaction.add(R.id.mapFragment, locateFragment, "Locate");
        transaction.commit();
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        // Search for the back_arr facing camera
        // get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        // for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }

    @Override
    public void onPause() {
        super.onPause();
        FragmentManager fragmentManager =getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(locateFragment).commit();
        if(recording) {
            stopRecording();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (homeFragment != null
                && getFragmentManager().findFragmentById(homeFragment.getId()) != null
                && getActivity() != null && ! flag) {
            flag = false;
            getFragmentManager().beginTransaction().remove(homeFragment).commit();
            homeFragment = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        flag = true;
    }

    private void openCamera() {
        releaseCamera();
        final boolean frontal = cameraFront;
        int cameraId = findFrontFacingCamera();
        if (cameraId < 0) {
            switchCameraListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(myContext, "无前置摄像头", Toast.LENGTH_LONG).show();
                }
            };
            cameraId = findBackFacingCamera();
        } else if (!frontal) {
            cameraId = findBackFacingCamera();
        }

        mCamera = Camera.open(cameraId);
        try {
            mCamera.autoFocus(mAutoFocusTakePictureCallback);
            Camera.Parameters paras = mCamera.getParameters();
            paras.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            mCamera.setParameters(paras);
            mCamera.setPreviewDisplay(preview.getHolder());
            VideoSize.optimizeCameraDimens(mCamera, myContext);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (flash && mCamera != null) {
            setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            buttonFlash.setImageResource(R.drawable.ic_flash_on_white);
        }
    }

    private void initialize() {
        preview.getHolder().addCallback(this);
        preview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        capture.setOnClickListener(captrureListener);
        switchCamera.setOnClickListener(switchCameraListener);
        buttonFlash.setOnClickListener(flashListener);

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).openDrawer();
            }
        });

        menu.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                takePicture("15985725431");
                return true;
            }
        });

        cameraPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (powerSavingOn) {
                        closePowerSavingMode();
                    }
                    try {
                        focusOnTouch(event);
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });
    }

    View.OnClickListener flashListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!recording && !cameraFront) {
                if (flash) {
                    flash = false;
                    buttonFlash.setImageResource(R.drawable.ic_flash_off_white);
                    setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                } else {
                    flash = true;
                    buttonFlash.setImageResource(R.drawable.ic_flash_on_white);
                    setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                }
            }
        }
    };

    View.OnClickListener switchCameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // get the number of cameras
            if (!recording) {
                int camerasNumber = Camera.getNumberOfCameras();
                if (camerasNumber > 1) {
                    // release the old camera instance
                    // switch camera, from the front and the back_arr and vice versa

                    releaseCamera();
                    chooseCamera();
                } else {
                    Toast toast = Toast.makeText(myContext, "你的手机只有一个摄像头！", Toast.LENGTH_LONG);
                    toast.show();
                }
            } else {
                Toast toast = Toast.makeText(myContext, "录制时无法转换摄像头！", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    };

    private void chooseCamera() {
        if (cameraFront) {
            int cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                mCamera = Camera.open(cameraId);
                refreshCamera(mCamera);
            }
        } else {
            int cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {
                mCamera = Camera.open(cameraId);
                refreshCamera(mCamera);
            }
        }
    }


    private boolean hasCamera(Context context) {
        // check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    View.OnClickListener captrureListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (recording) {

                // stop recording and release camera
                stopRecording();

            } else {
                startRecording(null);
            }
        }
    };

    private void startRecording(File file) {
        if (!prepareMediaRecorder(file == null? null : file.getAbsolutePath())) {
            Toast.makeText(myContext, "无法开始录制！", Toast.LENGTH_LONG).show();
            return;
        }
        buttonFlash.setVisibility(View.GONE);
        switchCamera.setVisibility(View.GONE);
        // work on UiThread for better performance
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                // If there are stories, add them to the table

                try {

                    mediaRecorder.start();
                    if (! isOverWritten) {
                        startChronometer();
                        capture.setImageResource(R.drawable.player_stop);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        recording = true;
        locateFragment.setLineDrawingOn(true);
    }

    public void stopRecording() {
        Collections.reverse(PublicDate.files);
        PublicDate.files.add(new FileInfo(tempFile.getName(), tempFile.getAbsolutePath(), countUp - 1));
        Collections.reverse(PublicDate.files);
        mediaRecorder.stop(); // stop the recording
        releaseMediaRecorder(); // release the MediaRecorder object
        recording = false;
        if (! isOverWritten) {
            locateFragment.setLineDrawingOn(false);
            stopChronometer();
            capture.setImageResource(R.drawable.player_record);
            buttonFlash.setVisibility(View.VISIBLE);
            switchCamera.setVisibility(View.VISIBLE);
            Toast.makeText(myContext, "视频已保存！", Toast.LENGTH_LONG).show();
        }
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            mCamera.lock(); // lock camera for later use
        }
    }

    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED
                    || what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
                isOverWritten = true;
                stopRecording();
                restartChronometer();
                startRecording(tempFile);
                isOverWritten = false;
            }
        }
    };

    private boolean prepareMediaRecorder(String filepath) {

        mediaRecorder = new MediaRecorder();

        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);

        File file = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String date = sdf.format(new Date());

        String videoFilePath = getActivity().getFilesDir().getAbsolutePath() + "/videofiles/";
        File directory = new File(videoFilePath);
        if (! directory.exists()) {
            directory.mkdir();
        }
        if (filepath == null) {
            filepath = videoFilePath  + date + ".mp4";
        }

        try {
            file = new File(filepath);
            file.createNewFile();

            System.out.println();

        } catch (IOException e) {
            e.printStackTrace();
        }
        tempFile = file;

        SharedPreferences sp = getActivity().getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        int quality = sp.getInt("Quality", PublicDate.defaultQuality);
        int maxDuration = sp.getInt("MaxDuration", PublicDate.defaultDuration);
        //long maxFileSize = sp.getLong("MaxFileSize", PublicDate.defaultFileSize);
        boolean audioOn = sp.getBoolean("AudioOn", true);
        powerSavingOn = sp.getBoolean("PowerSaving", true);

        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        if(audioOn) {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        }
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        if(audioOn) {
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        }

        //设置视频录制质量
        CamcorderProfile profile = CamcorderProfile.get(quality);
        mediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);

        //设置视频最长时长和最大文件大小
        mediaRecorder.setMaxDuration(maxDuration);
        //mediaRecorder.setMaxFileSize(maxFileSize);

        mediaRecorder.setOutputFile(filepath);
        mediaRecorder.setOnInfoListener(infoListener);

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;

    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Runnable openCamera = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(300);
                    handler.sendEmptyMessage(CAMERA_OPEN_START);
                    openCamera();
                    Thread.sleep(200);
                    handler.sendEmptyMessage(CAMERA_OPENED);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(openCamera).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera(mCamera);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    private void setFlashMode(String mode) {

        try {
            if (getActivity().getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)
                    && mCamera != null
                    && !cameraFront) {

                Camera.Parameters paras = mCamera.getParameters();
                paras.setFlashMode(mode);
                mCamera.setParameters(paras);
                refreshCamera(mCamera);

            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity().getBaseContext(), "闪光灯转换失败",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void startChronometer() {

        chrono.setVisibility(View.VISIBLE);

        final long startTime = SystemClock.elapsedRealtime();

        chrono.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer arg0) {
                countUp = (SystemClock.elapsedRealtime() - startTime) / 1000;
                powerSavingCount ++;
                if (countUp % 2 == 0) {
                    chronoRecordingImage.setVisibility(View.VISIBLE);
                } else {
                    chronoRecordingImage.setVisibility(View.INVISIBLE);
                }
                if(powerSavingCount == 30 && powerSavingOn ) {
                    openPowerSavingMode();
                }
                String asText = String.format("%02d", countUp / 60) + ":" + String.format("%02d", countUp % 60);
                chrono.setText(asText);
            }
        });
        chrono.start();
    }

    private void closePowerSavingMode() {
        if (screenLightDecreased) {
            powerSavingCount = 0;
            locateFragment.changeLocatRate(2000);
            WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
            lp.screenBrightness = 1.0f;
            getActivity().getWindow().setAttributes(lp);
            screenLightDecreased = false;
            locateFragment.setRateReduced(false);
        }
    }

    private void openPowerSavingMode() {
        locateFragment.changeLocatRate(5000);
        //降低屏幕亮度
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.screenBrightness = 0.2f;
        getActivity().getWindow().setAttributes(lp);
        screenLightDecreased = true;
        locateFragment.setRateReduced(true);
    }



    private void restartChronometer() {
        String asText = String.format("%02d", 0) + ":" + String.format("%02d", 0);
        chrono.setText(asText);
    }

    private void stopChronometer() {
        chrono.stop();
        chronoRecordingImage.setVisibility(View.INVISIBLE);
        chrono.setVisibility(View.INVISIBLE);
    }

    private void focusOnTouch(MotionEvent event) {
        if (mCamera != null ) {

            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.getMaxNumMeteringAreas() > 0){
                Rect rect = calculateFocusArea(event.getX(), event.getY());

                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                meteringAreas.add(new Camera.Area(rect, 800));
                parameters.setFocusAreas(meteringAreas);

                mCamera.setParameters(parameters);
                mCamera.autoFocus(mAutoFocusTakePictureCallback);
            }else {
                mCamera.autoFocus(mAutoFocusTakePictureCallback);
            }
        }
    }

    private Rect calculateFocusArea(float x, float y) {
        int left = clamp(Float.valueOf((x / preview.getWidth()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        int top = clamp(Float.valueOf((y / preview.getHeight()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);

        return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper)+focusAreaSize/2>1000){
            if (touchCoordinateInCameraReper>0){
                result = 1000 - focusAreaSize/2;
            } else {
                result = -1000 + focusAreaSize/2;
            }
        } else{
            result = touchCoordinateInCameraReper - focusAreaSize/2;
        }
        return result;
    }

    private void refreshCamera(Camera camera) {

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        // set preview size and make any resize, rotate or
        // reformatting changes here
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (camera != null) {
                //camera.setDisplayOrientation(90);
            }
        }

        try {
            mCamera.setPreviewDisplay(preview.getHolder());

            VideoSize.optimizeCameraDimens(mCamera, myContext);

            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Camera.AutoFocusCallback mAutoFocusTakePictureCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                // do something...
                Log.i("tap_to_focus","success!");
            } else {
                // do something...
                Log.i("tap_to_focus","fail!");
            }
        }
    };

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        try {
            refreshCamera(mCamera);
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            String time = format.format(date);
            String filename = phoneNumber + "_" + time + ".jpg";

            String directory = getActivity().getFilesDir() + "/pics/";
            final String path = directory + filename;

            File dire = new File(directory);
            if (! dire.exists()) {
                dire.mkdir();
            }

            Byte2ImageUtil.data2file(data, path);

            AVFile file = AVFile.withAbsoluteLocalPath(filename, path);
            file.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    if (e == null) {
                        File fileToDel = new File(path);
                        fileToDel.delete();
                        return;
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*public void InitFTPServerSetting() {
        // TODO Auto-generated method stub
        ftpUtils = FTPUtils.getInstance();
        boolean flag = ftpUtils.initFTPSetting("118.99.43.183", 21, "testuser", "testuser");

    }

    private void uploadFile(String filePath)// 拍照过后上传文件到服务器
    {
        String name = getFileName(filePath);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        InitFTPServerSetting();
        if(ftpUtils.uploadFile(filePath, name)) {
            File file = new File(filePath);
            file.delete();
        }

    }

    public String getFileName(String pathandname) {
        int start = pathandname.lastIndexOf("/");
        int end = pathandname.lastIndexOf(".");
        if (start != -1 && end != -1) {
            return pathandname.substring(start + 1, end);
        } else {
            return null;
        }
    }*/

}
