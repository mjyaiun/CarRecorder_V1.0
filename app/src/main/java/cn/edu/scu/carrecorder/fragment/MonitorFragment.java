package cn.edu.scu.carrecorder.fragment;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetDataCallback;
import com.avos.avoscloud.ProgressCallback;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.edu.scu.carrecorder.R;
import cn.edu.scu.carrecorder.activity.MainActivity;
import cn.edu.scu.carrecorder.util.Byte2ImageUtil;
import cn.edu.scu.carrecorder.util.PublicDate;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class MonitorFragment extends Fragment{
    private static MonitorFragment monitorFragment = new MonitorFragment();

    public static MonitorFragment getFragment() {
        return monitorFragment;
    }

    @InjectView(R.id.toolbar_monitor)
    Toolbar toolbar;
    @InjectView(R.id.picture)
    ImageView picture;
    @InjectView(R.id.progressWheel)
    ProgressWheel progressWheel;
    @InjectView(R.id.tips_monitor)
    TextView tips;
    @InjectView(R.id.confirm)
    Button confirm;
    @InjectView(R.id.progress)
    TextView progress;
    @InjectView(R.id.clear)
    Button clear;
    @InjectView(R.id.nativePhoneNumber)
    EditText nativePhoneNumber;
    @InjectView(R.id.phoneNumber)
    EditText oppPhoneNumber;

    String nativeNumber;
    String filepath;
    private static final int IMAGE_LOADED = 299;
    private static final int IMAGE_LOAD_START = 495;
    private static final int IMAGE_LOADING = 659;
    Runnable getPic;
    Handler handler;
    String directory;
    String filename;
    Timer getPicTimer;
    TimerTask picTask;
    int retrieveCount = 0;
    String oppNumber;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monitor, container, false);
        ButterKnife.inject(this, view);

        initToolbar();

        nativeNumber = getNativePhoneNumber();

        getPic = new Runnable() {
            @Override
            public void run() {
                retrieveCount += 1;
                handler.sendEmptyMessage(IMAGE_LOAD_START);
                retrieveImage();
            }
        };

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case IMAGE_LOADED:
                        File file = new File(filepath);
                        if (file.exists()) {
                            Bitmap pic = BitmapFactory.decodeFile(filepath);
                            picture.setImageBitmap(pic);
                            progressWheel.setVisibility(View.GONE);
                            picture.setVisibility(View.VISIBLE);
                            tips.setVisibility(View.GONE);
                            progress.setVisibility(View.GONE);
                        }
                        confirm.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        confirm.setClickable(true);
                        clear.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        clear.setClickable(true);
                        getPicTimer.cancel();
                        retrieveCount = 0;
                        break;
                    case IMAGE_LOAD_START:
                        tips.setVisibility(View.GONE);
                        progressWheel.setVisibility(View.VISIBLE);
                        progress.setText("0%");
                        progress.setVisibility(View.VISIBLE);
                        if (retrieveCount > 30) {
                            tips.setText("等待超过2分钟，请重新发送短信");
                            progressWheel.setVisibility(View.GONE);
                            progress.setVisibility(View.GONE);
                            tips.setVisibility(View.VISIBLE);
                            confirm.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                            confirm.setClickable(true);
                            clear.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                            clear.setClickable(true);
                            retrieveCount = 0;
                        }
                        break;
                    case IMAGE_LOADING:
                        Bundle data = msg.getData();
                        int progressNum = data.getInt("Progress");
                        progress.setText(progressNum + "%");
                        break;
                }
            }
        };

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nativeNumber = nativePhoneNumber.getText().toString();
                oppNumber = oppPhoneNumber.getText().toString();
                if (nativeNumber.length() < 11 || oppNumber.length() < 11) {
                    SweetAlertDialog dialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE);
                    dialog.setTitleText("警告");
                    dialog.setContentText("本机与对机号码格式错误！");
                    dialog.setConfirmText("确定");
                    dialog.show();
                    return;
                }

                SweetAlertDialog dialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE);
                dialog.setConfirmText("继续");
                dialog.setTitleText("警告");
                dialog.setContentText("该操作会发送一条短信(内容为Photo)的短信到作为监控的手机上！");
                dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        SmsManager manager = SmsManager.getDefault();
                        manager.sendTextMessage(oppNumber, nativeNumber, "Photo", null, null);
                        sweetAlertDialog.dismissWithAnimation();
                        sendSmsAndGetPic();
                    }
                });
                dialog.setCancelText("取消");
                dialog.setCancelClickListener(null);
                dialog.show();

            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picture.setImageBitmap(null);
                picture.setVisibility(View.GONE);
                progressWheel.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
                tips.setVisibility(View.VISIBLE);

                if (filepath != null) {
                    File file = new File(filepath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
        });

        return view;
    }

    private void sendSmsAndGetPic() {
        picture.setImageBitmap(null);
        picture.setVisibility(View.GONE);
        progressWheel.setVisibility(View.VISIBLE);
        tips.setVisibility(View.GONE);
        confirm.setClickable(false);
        confirm.setBackgroundColor(Color.GRAY);
        clear.setBackgroundColor(Color.GRAY);
        clear.setClickable(false);

        makeFile();
        getPicTimer = new Timer();
        picTask = new TimerTask() {
            @Override
            public void run() {
                new Thread(getPic).start();
            }
        };
        getPicTimer.schedule(picTask, 1000, 4000);
    }


    private void makeFile() {
        directory = getActivity().getFilesDir() + "/pics_download/";
        filename = nativeNumber + "_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()) + ".jpg";

        File dire = new File(directory);
        if (! dire.exists()) {
            dire.mkdir();
        }
        filepath = directory + filename;
    }

    @Override
    public void onPause() {
        super.onPause();
        clear.performClick();
    }

    private void retrieveImage() {
        AVQuery<AVObject> query = new AVQuery<>("_File");
        query.whereContains("name", nativeNumber);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    if (list.size() == 0) {
                        return;
                    }
                    final AVFile file = AVFile.parseFileWithAVObject(list.get(0));
                    file.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] bytes, AVException e) {
                            Byte2ImageUtil.data2file(bytes, filepath);
                            handler.sendEmptyMessage(IMAGE_LOADED);
                            file.deleteEventually();
                        }
                    }, new ProgressCallback() {
                        @Override
                        public void done(Integer integer) {
                            Message msg = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putInt("Progress", integer);
                            msg.setData(bundle);
                            msg.what = IMAGE_LOADING;
                            handler.sendMessage(msg);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.main, menu);
    }

    private void initToolbar() {
        ((MainActivity)getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle("");
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                getActivity(), PublicDate.drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        PublicDate.drawer.setDrawerListener(toggle);
        toggle.syncState();
    }

    public String getNativePhoneNumber() {
        TelephonyManager telephonyManager = (TelephonyManager) getActivity()
                .getSystemService(Context.TELEPHONY_SERVICE);
        String NativePhoneNumber=null;
        NativePhoneNumber=telephonyManager.getLine1Number();
        return NativePhoneNumber;
    }

}
