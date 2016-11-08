package cn.edu.scu.carrecorder.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.edu.scu.carrecorder.R;
import cn.edu.scu.carrecorder.activity.MainActivity;
import cn.edu.scu.carrecorder.adapter.ContactorMenuAdapter;
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
    private static final int CONTACT_LOADED = 441;
    private static final int CONTACT_LOAD_START = 918;
    Handler handler;
    Runnable getContactsThread;
    View view;
    SweetAlertDialog pDialog;
    private static ContactFragment contactFragment = new ContactFragment();

    public static ContactFragment getFragment() {
        return contactFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_contact, container, false);
        ButterKnife.inject(this, view);

        initToolbar();
        pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("联系人加载中");
        pDialog.setCancelable(false);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CONTACT_LOADED:
                        initList();
                        pDialog.dismissWithAnimation();
                        break;
                    case CONTACT_LOAD_START:
                        pDialog.show();
                        break;
                }
            }
        };
        getContactsThread = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(300);
                    handler.sendEmptyMessage(CONTACT_LOAD_START);
                    loadContacts();
                    Thread.sleep(200);
                    handler.sendEmptyMessage(CONTACT_LOADED);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(getContactsThread).start();

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        initList();
    }

    @Override
    public void onDetach() {
        super.onDetach();
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

    public void loadContacts() {
        PublicDate.contactors.clear();
        try {
            if (getActivity() == null) {
                return;
            }
            FileInputStream fis = getActivity().openFileInput("Contacts");
            ObjectInputStream ois = new ObjectInputStream(fis);
            Contactor contact = (Contactor) ois.readObject();
            while(contact != null) {
                PublicDate.contactors.add(contact);
                contact = (Contactor) ois.readObject();
            }
            ois.close();
            fis.close();

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

    private void initList() {
        if (PublicDate.contactors.size() == 0) {
            tips.setVisibility(View.VISIBLE);
        } else {
            tips.setVisibility(View.GONE);
        }

        contactList.setSwipeMenuCreator(smc);
        contactList.setLayoutManager(new LinearLayoutManager(getActivity()));// 布局管理器。
        contactList.setHasFixedSize(true);// 如果Item够简单，高度是确定的，打开FixSize将提高性能。
        contactList.setItemAnimator(new DefaultItemAnimator());// 设置Item默认动画，加也行，不加也行。
        contactList.addItemDecoration(new ListViewDecoration());
        contactList.setSwipeMenuItemClickListener(osmic);

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
                                    saveContacts(PublicDate.contactors);
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

    public void saveContacts(List<Contactor> contactors) {
        try {
            FileOutputStream fos = getActivity().openFileOutput("Contacts", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            for (Contactor contact: contactors) {
                oos.writeObject(contact);
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

    private OnItemClickListener onItemClickListener = new OnItemClickListener() {
        String number;
        @Override
        public void onItemClick(int position) {
            number = PublicDate.contactors.get(position).getPhoneNumber();
            new SweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText("确认拨打")
                    .setContentText(number)
                    .setConfirmText("确定")
                    .setCancelText("取消")
                    .setCancelClickListener(null)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
                            startActivity(intent);
                        }
                    }).show();

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
