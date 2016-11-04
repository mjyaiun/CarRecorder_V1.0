package cn.edu.scu.carrecorder.fragment;

import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
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

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.edu.scu.carrecorder.R;
import cn.edu.scu.carrecorder.activity.JumpActivity;
import cn.edu.scu.carrecorder.activity.MainActivity;
import cn.edu.scu.carrecorder.adapter.ContactorMenuAdapter;
import cn.edu.scu.carrecorder.adapter.FileMenuAdapter;
import cn.edu.scu.carrecorder.classes.Contactor;
import cn.edu.scu.carrecorder.customview.ListViewDecoration;
import cn.edu.scu.carrecorder.listener.OnItemClickListener;
import cn.edu.scu.carrecorder.util.PublicDate;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class ContactFragment extends Fragment {

    @InjectView(R.id.toolbar_contact)
    Toolbar toolbar;
    @InjectView(R.id.contactlist)
    SwipeMenuRecyclerView contactList;
    @InjectView(R.id.tips_contact)
    TextView tips;
    ContactorMenuAdapter mMenuAdapter;

    boolean flag = true;

    private static ContactFragment contactFragment = new ContactFragment();

    public static ContactFragment getFragment() {
        return contactFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        ButterKnife.inject(this, view);
        initToolbar();
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
    private void initList() {

        contactList.setSwipeMenuCreator(smc);
        contactList.setLayoutManager(new LinearLayoutManager(getActivity()));// 布局管理器。
        contactList.setHasFixedSize(true);// 如果Item够简单，高度是确定的，打开FixSize将提高性能。
        contactList.setItemAnimator(new DefaultItemAnimator());// 设置Item默认动画，加也行，不加也行。
        contactList.addItemDecoration(new ListViewDecoration());
        contactList.setSwipeMenuItemClickListener(osmic);
        if (PublicDate.contactors.size() == 0) {
            tips.setVisibility(View.VISIBLE);
        } else {
            tips.setVisibility(View.GONE);
        }
        mMenuAdapter = new ContactorMenuAdapter(PublicDate.contactors.size() == 0? null:PublicDate.contactors);
        mMenuAdapter.setOnItemClickListener(onItemClickListener);
        contactList.setAdapter(mMenuAdapter);

    }

    private OnSwipeMenuItemClickListener osmic = new OnSwipeMenuItemClickListener() {
        @Override
        public void onItemClick(Closeable closeable, int adapterPosition, final int menuPosition, int direction) {
            closeable.smoothCloseMenu();// 关闭被点击的菜单。
            final int itemPos = adapterPosition;
            switch (menuPosition) {
                case 0:
                    final String name = PublicDate.contactors.get(itemPos).getName();
                    SweetAlertDialog dialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("确定删除？")
                            .setContentText("删除联系人" + name)
                            .setConfirmText("确定")
                            .setCancelText("取消")
                            .setCancelClickListener(null)
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    PublicDate.contactors.remove(menuPosition);
                                    ((MainActivity)getActivity()).saveContacts(PublicDate.contactors);
                                    sDialog.setTitleText("删除成功")
                                            .setContentText(name + "已删除")
                                            .setConfirmClickListener(null)
                                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                    mMenuAdapter.notifyItemRemoved(itemPos);
                                    if(mMenuAdapter.getItemCount() == 0) {
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
        String number;
        @Override
        public void onItemClick(int position) {
            number = PublicDate.contactors.get(position).getPhoneNumber();
            SweetAlertDialog dialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText("确认拨打")
                    .setContentText(number)
                    .setConfirmText("确定")
                    .setCancelText("取消")
                    .setCancelClickListener(null)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            Intent intent = new Intent(getActivity(), JumpActivity.class);
                            intent.putExtra("Number", number);
                            startActivity(intent);
                        }
                    });
            dialog.show();
        }
    };

    private void initToolbar() {
        ((MainActivity)getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle("");
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                getActivity(), PublicDate.drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        PublicDate.drawer.setDrawerListener(toggle);
        toggle.syncState();
    }
}
