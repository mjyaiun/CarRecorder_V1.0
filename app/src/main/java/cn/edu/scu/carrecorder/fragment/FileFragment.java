package cn.edu.scu.carrecorder.fragment;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
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

    List<File> files = new ArrayList<File>();
    FileMenuAdapter mFileMenuAdapter;
    List<String> titles;
    List<Integer> durations;

    private static FileFragment fileFragment = new FileFragment();

    public static FileFragment getFragment() {
        return fileFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initToolbar();
        getFiles();
        initList();
    }

    public void notifyDataChange() {
        for (int i=0; i<titles.size();i ++) {
            mFileMenuAdapter.notifyItemRemoved(0);
        }
    }

    private SwipeMenuCreator smc = new SwipeMenuCreator() {
        @Override
        public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
            int size = getResources().getDimensionPixelSize(R.dimen.item_height);
            /*SwipeMenuItem saveItem = new SwipeMenuItem(getActivity())
                    .setImage(R.drawable.save)
                    .setWidth(size)
                    .setHeight(size)
                    .setBackgroundDrawable(R.drawable.selector_red);
            swipeRightMenu.addMenuItem(saveItem);*/
            SwipeMenuItem deleteItem = new SwipeMenuItem(getActivity())
                    .setImage(R.drawable.delete)
                    .setWidth(size)
                    .setHeight(size)
                    .setBackgroundDrawable(R.drawable.selector_red);
            swipeRightMenu.addMenuItem(deleteItem);
        }
    };
    private void initList() {
        filesList.setSwipeMenuCreator(smc);
        filesList.setLayoutManager(new LinearLayoutManager(getActivity()));// 布局管理器。
        filesList.setHasFixedSize(true);// 如果Item够简单，高度是确定的，打开FixSize将提高性能。
        filesList.setItemAnimator(new DefaultItemAnimator());// 设置Item默认动画，加也行，不加也行。
        filesList.addItemDecoration(new ListViewDecoration());
        filesList.setSwipeMenuItemClickListener(osmic);

        titles = getTitles();
        durations = getDurations();
        mFileMenuAdapter = new FileMenuAdapter(titles, durations);
        mFileMenuAdapter.setOnItemClickListener(onItemClickListener);
        filesList.setAdapter(mFileMenuAdapter);

    }

    private OnSwipeMenuItemClickListener osmic = new OnSwipeMenuItemClickListener() {
        @Override
        public void onItemClick(Closeable closeable, int adapterPosition, int menuPosition, int direction) {
            closeable.smoothCloseMenu();// 关闭被点击的菜单。
            final int itemPos = adapterPosition;
            switch (menuPosition) {
                case 1:

                    File saveFile = files.get(adapterPosition);
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(saveFile));
                    getActivity().sendBroadcast(intent);

                    break;
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
                                    if(files.get(itemPos).delete()) {
                                        File file_del = files.remove(itemPos);
                                        titles.remove(itemPos);
                                        sDialog.setTitleText("删除成功")
                                                .setContentText(file_del.getName() + "已删除")
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
            String filepath = files.get(position).getAbsolutePath();
            Intent intent = new Intent(getActivity(), RecordPlayActivity.class);
            intent.putExtra("filepath", filepath);
            startActivity(intent);
        }
    };

    private void getFiles() {
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File dir = getActivity().getFilesDir();
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".mp4");
            }
        });
        this.files.clear();
        for (File file: files) {
            this.files.add(file);
        }
        Collections.reverse(this.files);
    }

    private List<Integer> getDurations() {
        MediaMetadataRetriever mmr;
        List<Integer> durations = new ArrayList<>();
        String duration;
        for (File file: files) {
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(getActivity(), Uri.fromFile(file));
            duration  = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
            durations.add((int)(Math.ceil(Integer.parseInt(duration) / 1000)));
            mmr.release();
        }
        return durations;
    }

    private List<String> getTitles() {
        if (files.size() == 0) {
            tips.setVisibility(View.VISIBLE);
            return null;
        }
        tips.setVisibility(View.GONE);
        List<String> titles = new ArrayList<>();
        for (File file: files) {
            titles.add(file.getName());
        }
        return titles;
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
