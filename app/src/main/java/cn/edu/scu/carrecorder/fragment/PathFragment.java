package cn.edu.scu.carrecorder.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.view.ContextThemeWrapper;
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

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.edu.scu.carrecorder.R;
import cn.edu.scu.carrecorder.activity.MainActivity;
import cn.edu.scu.carrecorder.activity.PathShowActivity;
import cn.edu.scu.carrecorder.activity.RecordPlayActivity;
import cn.edu.scu.carrecorder.adapter.FileMenuAdapter;
import cn.edu.scu.carrecorder.adapter.PathMenuAdapter;
import cn.edu.scu.carrecorder.classes.LatLonPoint;
import cn.edu.scu.carrecorder.classes.WheelPath;
import cn.edu.scu.carrecorder.customview.ListViewDecoration;
import cn.edu.scu.carrecorder.listener.OnItemClickListener;
import cn.edu.scu.carrecorder.util.PublicDate;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class PathFragment extends Fragment {
    @InjectView(R.id.pathlist)
    SwipeMenuRecyclerView pathlist;

    @InjectView(R.id.toolbar_path)
    Toolbar toolbar;
    @InjectView(R.id.tips_path)
    TextView tips;

    List<WheelPath> paths = PublicDate.paths;
    List<String> names;
    PathMenuAdapter mPathMenuAdapter;

    private static PathFragment pathFragment = new PathFragment();

    public static PathFragment getFragment() {
        return pathFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_path, container, false);
        ButterKnife.inject(this, view);
        testPath();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initToolbar();
        loadPaths();
        initList();
    }

    private void testPath() {
        List<WheelPath> paths = new ArrayList<>();

        Random rd = new Random();
        for (int i=0;i < 5;i ++) {
            ArrayList<LatLonPoint> points = new ArrayList<>();
            for (int j=0;j < 5; j ++) {
                points.add(new LatLonPoint(rd.nextDouble(),rd.nextDouble()));
            }
            paths.add(new WheelPath("Test" + i, points));
        }

        saveWheelPath(paths);
    }

    public void saveWheelPath(List<WheelPath> paths) {
        try {
            FileOutputStream fos = getActivity().openFileOutput("WheelPath", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            for (WheelPath path: paths) {
                oos.writeObject(path);
            }
            oos.writeObject(null);
            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    private void initList() {
        pathlist.setSwipeMenuCreator(smc);
        pathlist.setLayoutManager(new LinearLayoutManager(getActivity()));// 布局管理器。
        pathlist.setHasFixedSize(true);// 如果Item够简单，高度是确定的，打开FixSize将提高性能。
        pathlist.setItemAnimator(new DefaultItemAnimator());// 设置Item默认动画，加也行，不加也行。
        pathlist.addItemDecoration(new ListViewDecoration());
        pathlist.setSwipeMenuItemClickListener(osmic);

        names = getNames();
        mPathMenuAdapter = new PathMenuAdapter(names);
        mPathMenuAdapter.setOnItemClickListener(onItemClickListener);
        pathlist.setAdapter(mPathMenuAdapter);

    }

    private void loadPaths() {
        paths.clear();
        FileInputStream fis;
        ObjectInputStream ois = null;
        try {
            fis = getActivity().openFileInput("WheelPath");
            ois = new ObjectInputStream(fis);
            WheelPath temp = (WheelPath) ois.readObject();
            while (temp != null) {
                paths.add(temp);
                temp = (WheelPath) ois.readObject();
            }
            if (paths.size() == 0) {
                tips.setVisibility(View.VISIBLE);
            } else {
                tips.setVisibility(View.GONE);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private List<String> getNames() {
        ArrayList<String> names = new ArrayList<>();
        for (WheelPath path: paths) {
            names.add(path.getName());
        }
        return names;

    }

    private OnSwipeMenuItemClickListener osmic = new OnSwipeMenuItemClickListener() {
        @Override
        public void onItemClick(Closeable closeable, int adapterPosition, int menuPosition, int direction) {
            closeable.smoothCloseMenu();// 关闭被点击的菜单。
            final int itemPos = adapterPosition;
            switch (menuPosition) {
                case 0:
                    SweetAlertDialog dialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("确定删除？")
                            .setContentText("轨迹删除后无法恢复")
                            .setConfirmText("确定")
                            .setCancelText("取消")
                            .setCancelClickListener(null)
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.setTitleText("删除成功")
                                            .setContentText(paths.get(itemPos).getName() + "已删除")
                                            .setConfirmClickListener(null)
                                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                    names.remove(itemPos);
                                    paths.remove(itemPos);
                                    saveWheelPath(paths);

                                    mPathMenuAdapter.notifyItemRemoved(itemPos);
                                    if(mPathMenuAdapter.getItemCount() == 0) {
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
            Intent intent = new Intent(getActivity(), PathShowActivity.class);
            intent.putExtra("PathName",paths.get(position).getName());
            startActivity(intent);
        }
    };

    public void initToolbar() {
        ((MainActivity)getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle("");
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                getActivity(), PublicDate.drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        PublicDate.drawer.setDrawerListener(toggle);
        toggle.syncState();
    }

}
