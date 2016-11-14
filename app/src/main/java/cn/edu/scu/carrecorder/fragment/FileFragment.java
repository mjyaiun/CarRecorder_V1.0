package cn.edu.scu.carrecorder.fragment;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yanzhenjie.recyclerview.swipe.Closeable;
import com.yanzhenjie.recyclerview.swipe.OnSwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.edu.scu.carrecorder.R;
import cn.edu.scu.carrecorder.activity.MainActivity;
import cn.edu.scu.carrecorder.activity.RecordPlayActivity;
import cn.edu.scu.carrecorder.adapter.FileMenuAdapter;
import cn.edu.scu.carrecorder.classes.FileInfo;
import cn.edu.scu.carrecorder.customview.ListViewDecoration;
import cn.edu.scu.carrecorder.listener.OnItemClickListener;
import cn.edu.scu.carrecorder.util.PublicDate;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class FileFragment extends Fragment {
    @InjectView(R.id.filelist)
    SwipeMenuRecyclerView filesList;

    @InjectView(R.id.toolbar_file)
    Toolbar toolbar;
    @InjectView(R.id.tips)
    TextView tips;

    FileMenuAdapter mFileMenuAdapter;
    private static final int FILE_LOADED = 391;
    private static final int FILE_LOAD_START = 249;
    Handler handler;
    Runnable getFileThread;
    View view;
    SweetAlertDialog pDialog;
    private static FileFragment fileFragment = new FileFragment();

    public static FileFragment getFragment() {
        return fileFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file, container, false);
        ButterKnife.inject(this, view);
        initToolbar();
        pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("视频文件加载中");
        pDialog.setCancelable(false);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case FILE_LOADED:
                        initList();
                        pDialog.dismissWithAnimation();
                        break;
                    case FILE_LOAD_START:
                        pDialog.show();
                        break;
                }
            }
        };
        getFileThread = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    handler.sendEmptyMessage(FILE_LOAD_START);
                    getFiles();
                    Thread.sleep(200);
                    handler.sendEmptyMessage(FILE_LOADED);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
        new Thread(getFileThread).start();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initList();
    }

    private SwipeMenuCreator smc = new SwipeMenuCreator() {
        @Override
        public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
            int size = getResources().getDimensionPixelSize(R.dimen.item_height);
            SwipeMenuItem deleteItem = new SwipeMenuItem(getActivity())
                    .setImage(R.drawable.delete)
                    .setWidth(size)
                    .setHeight(size)
                    .setBackgroundDrawable(R.drawable.selector_red);
            swipeRightMenu.addMenuItem(deleteItem);
        }
    };

    public void clearFileCache() {
        PublicDate.files.clear();
    }

    public void deleteAllVideo() {
        String videoFilePath = getActivity().getFilesDir().getAbsolutePath() + "/videofiles/";
        File dir = new File(videoFilePath);
        if (! dir.exists()) {
            dir.mkdir();
        }
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                file.delete();
            }
        }
    }

    private void initList() {
        if (PublicDate.files.size() == 0) {
            tips.setVisibility(View.VISIBLE);
        } else {
            tips.setVisibility(View.GONE);
        }

        filesList.setSwipeMenuCreator(smc);
        filesList.setLayoutManager(new LinearLayoutManager(getActivity()));// 布局管理器。
        filesList.setHasFixedSize(true);// 如果Item够简单，高度是确定的，打开FixSize将提高性能。
        filesList.setItemAnimator(new DefaultItemAnimator());// 设置Item默认动画，加也行，不加也行。
        filesList.addItemDecoration(new ListViewDecoration());
        filesList.setSwipeMenuItemClickListener(osmic);

        mFileMenuAdapter = new FileMenuAdapter(PublicDate.files);
        mFileMenuAdapter.setOnItemClickListener(onItemClickListener);
        filesList.setAdapter(mFileMenuAdapter);

    }

    private OnSwipeMenuItemClickListener osmic = new OnSwipeMenuItemClickListener() {
        @Override
        public void onItemClick(Closeable closeable, int adapterPosition, int menuPosition, int direction) {
            closeable.smoothCloseMenu();// 关闭被点击的菜单。
            final int itemPos = adapterPosition;
            switch (menuPosition) {
                /*case 1:

                    File saveFile = files.get(adapterPosition);
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(saveFile));
                    getActivity().sendBroadcast(intent);

                    break;*/
                case 0:
                    SweetAlertDialog dialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("确定删除？")
                            .setContentText("文件删除后无法恢复")
                            .setConfirmText("确定")
                            .setCancelText("取消")
                            .setCancelClickListener(null)
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    FileInfo fileToDel = PublicDate.files.get(itemPos);
                                    File file_del = new File(fileToDel.getAbsolutePath());
                                    if(file_del.delete()) {
                                        PublicDate.files.remove(itemPos);
                                        sDialog.setTitleText("删除成功")
                                                .setContentText(fileToDel.getName() + "已删除")
                                                .setConfirmClickListener(null)
                                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                    }
                                    mFileMenuAdapter.notifyItemRemoved(itemPos);
                                    if(mFileMenuAdapter.getItemCount() == 0) {
                                        tips.setVisibility(View.VISIBLE);
                                    } else {
                                        tips.setVisibility(View.GONE);
                                    }
                                }
                            });
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                    break;
            }
        }
    };

    private OnItemClickListener onItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            String filepath = PublicDate.files.get(position).getAbsolutePath();
            Intent intent = new Intent(getActivity(), RecordPlayActivity.class);
            intent.putExtra("filepath", filepath);
            startActivity(intent);
        }
    };

    private void getFiles() {
        PublicDate.files.clear();
        if (getActivity() == null) {
            return;
        }
        File dir = new File(getActivity().getFilesDir().getAbsolutePath() + "/videofiles/");
        if (! dir.exists()) {
            dir.mkdir();
        }
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".mp4");
            }
        });


        for (File file: files) {

            MediaMetadataRetriever mmr;
            String duration;
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(getActivity(), Uri.fromFile(file));
            duration  = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
            mmr.release();

            PublicDate.files.add(new FileInfo(file.getName(), file.getAbsolutePath(), Long.parseLong(duration) / 1000 ));

        }
        Collections.reverse(PublicDate.files);
    }

    public void initToolbar() {
        ((MainActivity)getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle("");
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                getActivity(), PublicDate.drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        PublicDate.drawer.setDrawerListener(toggle);
        toggle.syncState();
    }

}
