package cn.edu.scu.carrecorder.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.CamcorderProfile;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.edu.scu.carrecorder.R;
import cn.edu.scu.carrecorder.activity.MainActivity;
import cn.edu.scu.carrecorder.customview.GroupButtonView;
import cn.edu.scu.carrecorder.util.DisplayUtils;
import cn.edu.scu.carrecorder.util.PublicDate;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class SettingFragment extends Fragment {
    private static SettingFragment settingFragment = new SettingFragment();

    public static SettingFragment getFragment() {
        return settingFragment;
    }

    @InjectView(R.id.toolbar_monitor)
    Toolbar toolbar;

    @InjectView(R.id.toggle_button_powersaving)
    ToggleButton togBtnPowerSaving;
    @InjectView(R.id.img_button_powersaving_layout)
    RelativeLayout layoutImgBtnPowerSaving;

    @InjectView(R.id.toggle_button_audio_on)
    ToggleButton togBtnAudioOn;
    @InjectView(R.id.img_button_audio_on_layout)
    RelativeLayout layoutImgBtnAudioOn;

    @InjectView(R.id.toggle_button_pathrecon)
    ToggleButton togBtnPathRecOn;
    @InjectView(R.id.img_button_pathrecon_layout)
    RelativeLayout layoutImgBtnPathRecOn;

    @InjectView(R.id.toggle_button_autostop)
    ToggleButton togBtnAutoStopOn;
    @InjectView(R.id.img_button_autostop_layout)
    RelativeLayout layoutImgBtnAutoStopOn;

    @InjectView(R.id.quality_selector)
    GroupButtonView radioBtnQuality;
    @InjectView(R.id.duration_selector)
    GroupButtonView radioBtnDuration;
    @InjectView(R.id.filesize_selector)
    GroupButtonView radioBtnFilesize;
    @InjectView(R.id.stoptime_selector)
    GroupButtonView radioBtnStopTime;

    @InjectView(R.id.layout_stoptime)
    LinearLayout layoutStopTime;

    @InjectView(R.id.layout_clear)
    LinearLayout clearAll;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        ButterKnife.inject(this, view);
        initToolbar();
        initView();
        setListener();
        return view;
    }

    private void initView() {
        SharedPreferences sp = getActivity().getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        initSwitches(sp);
        int quality = sp.getInt("Quality", PublicDate.defaultQuality);
        int maxDuration = sp.getInt("MaxDuration", PublicDate.defaultDuration);
        long maxFileSize = sp.getLong("MaxFileSize", PublicDate.defaultFileSize);
        int stoptime = sp.getInt("AutoStopInterval", PublicDate.defaultInterval);
        radioBtnQuality.checkChild(1 - quality);
        radioBtnDuration.checkChild(maxDuration / 10 / 60 / 1000 - 1);
        radioBtnFilesize.checkChild((int) ((maxFileSize / 256 / 1024 / 1024) - 1));
        radioBtnStopTime.checkChild(stoptime / 60 / 1000 / 5 - 1);
    }

    private void initSwitches(SharedPreferences sp) {
        boolean audioOn = sp.getBoolean("AudioOn", true);
        boolean powerSaving = sp.getBoolean("PowerSaving", true);
        boolean pathRecOn = sp.getBoolean("PathRecOn", true);
        boolean autoStopOn = sp.getBoolean("AutoStopOn", true);

        togBtnAudioOn.setChecked(audioOn);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layoutImgBtnAudioOn
                .getLayoutParams();

        if(audioOn) {
            params.addRule(RelativeLayout.ALIGN_LEFT, -1);
            params.addRule(RelativeLayout.ALIGN_RIGHT, R.id.toggle_button_audio_on_layout1);
            layoutImgBtnAudioOn.setLayoutParams(params);
            togBtnAudioOn.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        } else {
            params.addRule(RelativeLayout.ALIGN_RIGHT, -1);
            params.addRule(RelativeLayout.ALIGN_LEFT,R.id.toggle_button_audio_on_layout1);
            layoutImgBtnAudioOn.setLayoutParams(params);
            togBtnAudioOn.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        }

        togBtnPowerSaving.setChecked(powerSaving);
        RelativeLayout.LayoutParams params3 = (RelativeLayout.LayoutParams) layoutImgBtnPowerSaving
                .getLayoutParams();
        if(powerSaving) {
            params3.addRule(RelativeLayout.ALIGN_LEFT, -1);
            params3.addRule(RelativeLayout.ALIGN_RIGHT, R.id.toggle_button_powersaving_layout1);
            layoutImgBtnPowerSaving.setLayoutParams(params3);
            togBtnPowerSaving.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        } else {
            params3.addRule(RelativeLayout.ALIGN_RIGHT, -1);
            params3.addRule(RelativeLayout.ALIGN_LEFT, R.id.toggle_button_powersaving_layout1);
            layoutImgBtnPowerSaving.setLayoutParams(params3);
            togBtnPowerSaving.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        }

        togBtnPathRecOn.setChecked(pathRecOn);
        params = (RelativeLayout.LayoutParams) layoutImgBtnPathRecOn
                .getLayoutParams();

        if(pathRecOn) {
            params.addRule(RelativeLayout.ALIGN_LEFT, -1);
            params.addRule(RelativeLayout.ALIGN_RIGHT, R.id.toggle_button_pathrecon_layout1);
            layoutImgBtnPathRecOn.setLayoutParams(params);
            togBtnPathRecOn.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        } else {
            params.addRule(RelativeLayout.ALIGN_RIGHT, -1);
            params.addRule(RelativeLayout.ALIGN_LEFT,R.id.toggle_button_pathrecon_layout1);
            layoutImgBtnPathRecOn.setLayoutParams(params);
            togBtnPathRecOn.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        }

        togBtnAutoStopOn.setChecked(autoStopOn);
        params = (RelativeLayout.LayoutParams) layoutImgBtnAutoStopOn
                .getLayoutParams();

        if(autoStopOn) {
            params.addRule(RelativeLayout.ALIGN_LEFT, -1);
            params.addRule(RelativeLayout.ALIGN_RIGHT, R.id.toggle_button_autostop_layout1);
            layoutImgBtnAutoStopOn.setLayoutParams(params);
            togBtnAutoStopOn.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            layoutStopTime.setVisibility(View.VISIBLE);
        } else {
            params.addRule(RelativeLayout.ALIGN_RIGHT, -1);
            params.addRule(RelativeLayout.ALIGN_LEFT,R.id.toggle_button_autostop_layout1);
            layoutImgBtnAutoStopOn.setLayoutParams(params);
            togBtnAutoStopOn.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            layoutStopTime.setVisibility(View.GONE);
        }
    }

    private void setListener() {

        radioBtnDuration.setOnGroupBtnClickListener(new GroupButtonView.OnGroupBtnClickListener() {
            @Override
            public void groupBtnClick(String code) {
                SharedPreferences sp = getActivity().getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("MaxDuration", Integer.parseInt(code) * 60 * 1000);
                editor.commit();
            }
        });

        radioBtnFilesize.setOnGroupBtnClickListener(new GroupButtonView.OnGroupBtnClickListener() {
            @Override
            public void groupBtnClick(String code) {
                SharedPreferences sp = getActivity().getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putLong("MaxFileSize", Integer.parseInt(code) * 1024 * 1024);
                editor.commit();
            }
        });

        radioBtnQuality.setOnGroupBtnClickListener(new GroupButtonView.OnGroupBtnClickListener() {
            @Override
            public void groupBtnClick(String code) {
                SharedPreferences sp = getActivity().getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("Quality", Integer.parseInt(code));
                editor.commit();
            }
        });

        radioBtnStopTime.setOnGroupBtnClickListener(new GroupButtonView.OnGroupBtnClickListener() {
            @Override
            public void groupBtnClick(String code) {
                SharedPreferences sp = getActivity().getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("AutoStopInterval", Integer.parseInt(code)* 60 * 1000);
                editor.commit();
            }
        });

        clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SweetAlertDialog dialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("确定清空？")
                        .setContentText("文件清空后无法恢复")
                        .setConfirmText("确定")
                        .setCancelText("取消")
                        .setCancelClickListener(null)
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                deleteAllVideo();
                                sDialog.setTitleText("清空成功")
                                        .setConfirmClickListener(null)
                                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                            }
                        });
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }

            private void deleteAllVideo() {
                String videoFilePath = getActivity().getFilesDir().getAbsolutePath();
                File dir = new File(videoFilePath);
                File[] files = dir.listFiles();
                for (File file: files) {
                    if(file.isFile()) {
                        file.delete();
                    }
                }
            }
        });

        View.OnClickListener clickToToggleListenerAutoStopOn = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togBtnAutoStopOn.toggle();
            }
        };

        layoutImgBtnAutoStopOn.setOnClickListener(clickToToggleListenerAutoStopOn);

        togBtnAutoStopOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = getActivity().getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("AutoStopOn", isChecked);
                editor.commit();

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layoutImgBtnAutoStopOn
                        .getLayoutParams();
                if(isChecked) {
                    TranslateAnimation animation = new TranslateAnimation(DisplayUtils.dip2px(getActivity(), -40), 0, 0, 0);
                    animation.setDuration(200);
                    layoutImgBtnAutoStopOn.startAnimation(animation);
                    params.addRule(RelativeLayout.ALIGN_LEFT, -1);
                    params.addRule(RelativeLayout.ALIGN_RIGHT,R.id.toggle_button_autostop_layout1);
                    layoutImgBtnAutoStopOn.setLayoutParams(params);
                    togBtnAutoStopOn.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                    layoutStopTime.setVisibility(View.VISIBLE);
                } else {
                    TranslateAnimation animation = new TranslateAnimation(DisplayUtils.dip2px(getActivity(), 40), 0, 0, 0);
                    animation.setDuration(200);
                    layoutImgBtnAutoStopOn.startAnimation(animation);
                    params.addRule(RelativeLayout.ALIGN_RIGHT, -1);
                    params.addRule(RelativeLayout.ALIGN_LEFT,R.id.toggle_button_autostop_layout1);
                    layoutImgBtnAutoStopOn.setLayoutParams(params);
                    togBtnAutoStopOn.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                    layoutStopTime.setVisibility(View.GONE);
                }

            }
        });

        View.OnClickListener clickToToggleListenerPathRecOn = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togBtnPathRecOn.toggle();
            }
        };

        layoutImgBtnPathRecOn.setOnClickListener(clickToToggleListenerPathRecOn);

        togBtnPathRecOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = getActivity().getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("PathRecOn", isChecked);
                editor.commit();

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layoutImgBtnPathRecOn
                        .getLayoutParams();
                if(isChecked) {
                    TranslateAnimation animation = new TranslateAnimation(DisplayUtils.dip2px(getActivity(), -40), 0, 0, 0);
                    animation.setDuration(200);
                    layoutImgBtnPathRecOn.startAnimation(animation);
                    params.addRule(RelativeLayout.ALIGN_LEFT, -1);
                    params.addRule(RelativeLayout.ALIGN_RIGHT,R.id.toggle_button_pathrecon_layout1);
                    layoutImgBtnPathRecOn.setLayoutParams(params);
                    togBtnPathRecOn.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

                } else {
                    TranslateAnimation animation = new TranslateAnimation(DisplayUtils.dip2px(getActivity(), 40), 0, 0, 0);
                    animation.setDuration(200);
                    layoutImgBtnPathRecOn.startAnimation(animation);
                    params.addRule(RelativeLayout.ALIGN_RIGHT, -1);
                    params.addRule(RelativeLayout.ALIGN_LEFT,R.id.toggle_button_pathrecon_layout1);
                    layoutImgBtnPathRecOn.setLayoutParams(params);
                    togBtnPathRecOn.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                }

            }
        });

        View.OnClickListener clickToToggleListenerAudioOn = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togBtnAudioOn.toggle();
            }
        };

        layoutImgBtnAudioOn.setOnClickListener(clickToToggleListenerAudioOn);

        View.OnClickListener clickToToggleListenerPowerSaving = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togBtnPowerSaving.toggle();
            }
        };

        layoutImgBtnPowerSaving.setOnClickListener(clickToToggleListenerPowerSaving);

        togBtnAudioOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = getActivity().getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("AudioOn", isChecked);
                editor.commit();

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layoutImgBtnAudioOn
                        .getLayoutParams();
                if(isChecked) {
                    TranslateAnimation animation = new TranslateAnimation(DisplayUtils.dip2px(getActivity(), -40), 0, 0, 0);
                    animation.setDuration(200);
                    layoutImgBtnAudioOn.startAnimation(animation);
                    params.addRule(RelativeLayout.ALIGN_LEFT, -1);
                    params.addRule(RelativeLayout.ALIGN_RIGHT,R.id.toggle_button_audio_on_layout1);
                    layoutImgBtnAudioOn.setLayoutParams(params);
                    togBtnAudioOn.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

                } else {
                    TranslateAnimation animation = new TranslateAnimation(DisplayUtils.dip2px(getActivity(), 40), 0, 0, 0);
                    animation.setDuration(200);
                    layoutImgBtnAudioOn.startAnimation(animation);
                    params.addRule(RelativeLayout.ALIGN_RIGHT, -1);
                    params.addRule(RelativeLayout.ALIGN_LEFT,R.id.toggle_button_audio_on_layout1);
                    layoutImgBtnAudioOn.setLayoutParams(params);
                    togBtnAudioOn.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                }

            }
        });

        togBtnPowerSaving.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = getActivity().getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("PowerSaving", isChecked);
                editor.commit();

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layoutImgBtnPowerSaving
                        .getLayoutParams();
                if(isChecked) {
                    params.addRule(RelativeLayout.ALIGN_LEFT, -1);
                    params.addRule(RelativeLayout.ALIGN_RIGHT,R.id.toggle_button_powersaving_layout1);
                    layoutImgBtnPowerSaving.setLayoutParams(params);
                    togBtnPowerSaving.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                    TranslateAnimation animation = new TranslateAnimation(DisplayUtils.dip2px(getActivity(), -40), 0, 0, 0);
                    animation.setDuration(200);
                    layoutImgBtnPowerSaving.startAnimation(animation);
                } else {
                    params.addRule(RelativeLayout.ALIGN_RIGHT, -1);
                    params.addRule(RelativeLayout.ALIGN_LEFT,R.id.toggle_button_powersaving_layout1);
                    layoutImgBtnPowerSaving.setLayoutParams(params);
                    togBtnPowerSaving.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                    TranslateAnimation animation = new TranslateAnimation(DisplayUtils.dip2px(getActivity(), 40), 0, 0, 0);
                    animation.setDuration(200);
                    layoutImgBtnPowerSaving.startAnimation(animation);
                }
            }
        });

    }

    private void initToolbar() {
        ((MainActivity)getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle("");
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                getActivity(), PublicDate.drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        PublicDate.drawer.setDrawerListener(toggle);
        toggle.syncState();
    }
}
