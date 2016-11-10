package cn.edu.scu.carrecorder.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.provider.ContactsContract;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.avos.avoscloud.AVOSCloud;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import cn.edu.scu.carrecorder.R;
import cn.edu.scu.carrecorder.broadcastreceiver.SMSBroadcastReceiver;
import cn.edu.scu.carrecorder.classes.Contactor;
import cn.edu.scu.carrecorder.fragment.ContactFragment;
import cn.edu.scu.carrecorder.fragment.FileFragment;
import cn.edu.scu.carrecorder.fragment.MonitorFragment;
import cn.edu.scu.carrecorder.fragment.PathFragment;
import cn.edu.scu.carrecorder.fragment.RecordFragment;
import cn.edu.scu.carrecorder.fragment.SettingFragment;
import cn.edu.scu.carrecorder.util.PublicDate;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Toolbar toolbar;
    DrawerLayout drawer;

    public Fragment getCurrFrag() {
        return currFrag;
    }

    Fragment currFrag;
    Fragment preFrag;
    SMSBroadcastReceiver receiver;
    FragmentTransaction transaction;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AVOSCloud.initialize(this, "5kA0NMpLnNNA4C8gTHVIzo6S-gzGzoHsz", "VqJscjQvTweuuJllTLKCCfms");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        PublicDate.drawer = drawer;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_record);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        receiver = new SMSBroadcastReceiver();
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(1000);
        registerReceiver(receiver, filter);

        if (savedInstanceState != null) {
            SharedPreferences sp = getPreferences(MODE_PRIVATE);
            String tag = sp.getString("CurrFrag", null);
            if (tag != null) {
                if (tag.equals("Record")) {

                    fragmentManager.beginTransaction().add(R.id.content, RecordFragment.getFragment(), tag).commit();
                    currFrag = RecordFragment.getFragment();
                    return;
                }
                Fragment fragment = fragmentManager.findFragmentByTag(tag);
                currFrag = fragment;
                fragmentManager.beginTransaction().show(fragment).commit();
            } else {
                if (currFrag == null) {
                    Fragment homeFragment = RecordFragment.getFragment();
                    transaction.add(R.id.content, homeFragment, "Record");
                    currFrag = homeFragment;
                }
            }
            return;
        }

        if (currFrag == null) {
            Fragment homeFragment = RecordFragment.getFragment();
            transaction.add(R.id.content, homeFragment, "Record");
            currFrag = homeFragment;
        } else {
            transaction.show(currFrag);
        }
        transaction.commit();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (currFrag instanceof RecordFragment) {
            currFrag.onDestroyView();
            currFrag = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currFrag != null) {
            SharedPreferences sp = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("CurrFrag", currFrag.getTag());
            editor.commit();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void openDrawer() {
        drawer.openDrawer(GravityCompat.START);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clearVideo:
                SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
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
                break;
            case R.id.clear_contactors:
                SweetAlertDialog dialog1 = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("确定清空？")
                        .setContentText("联系人清空后需要重新导入")
                        .setConfirmText("确定")
                        .setCancelText("取消")
                        .setCancelClickListener(null)
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {

                                deleteAllContact();

                                sDialog.setTitleText("清空成功")
                                        .setConfirmClickListener(null)
                                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                            }
                        });
                dialog1.setCanceledOnTouchOutside(false);
                dialog1.show();
                break;
            case R.id.clearPath:
                SweetAlertDialog dialog2 = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("确定清空？")
                        .setContentText("记录清空后无法恢复")
                        .setConfirmText("确定")
                        .setCancelText("取消")
                        .setCancelClickListener(null)
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {

                                deleteAllPath();

                                sDialog.setTitleText("清空成功")
                                        .setConfirmClickListener(null)
                                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                            }
                        });
                dialog2.setCanceledOnTouchOutside(false);
                dialog2.show();
                break;
            case R.id.add_contactor:
                startActivityForResult(new Intent(
                        Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), 0);
                break;
        }
        return true;
    }

    private void deleteAllVideo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileFragment.getFragment().deleteAllVideo();
            }
        }).start();
        FileFragment.getFragment().clearFileCache();
        FileFragment.getFragment().onResume();
    }

    private void deleteAllPath() {
        PublicDate.paths.clear();
        PathFragment.getFragment().onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                PathFragment.getFragment().saveWheelPath(PublicDate.paths);
            }
        }).start();
    }

    private void deleteAllContact() {
        PublicDate.contactors.clear();
        ContactFragment.getFragment().onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContactFragment.getFragment().saveContacts(PublicDate.contactors);
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            ContentResolver reContentResolverol = getContentResolver();
            Uri contactData = data.getData();
            Cursor cursor = managedQuery(contactData, null, null, null, null);
            cursor.moveToFirst();
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            Cursor phone = reContentResolverol.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                    null,
                    null);
            boolean alreadyAdded = false;
            while (phone.moveToNext()) {
                String usernumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                Contactor newContact = new Contactor(name, usernumber);
                int index = PublicDate.contactors.indexOf(newContact);
                if (index != -1) {
                    alreadyAdded = true;
                    break;
                }
                PublicDate.contactors.add(newContact);
            }
            if (alreadyAdded) {
                SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE);
                dialog.setTitleText("联系人已添加")
                        .setContentText("联系人" + name + "已添加到列表")
                        .setConfirmText("确定")
                        .setConfirmClickListener(null)
                        .show();
            } else {
                ContactFragment.getFragment().onResume();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ContactFragment.getFragment().saveContacts(PublicDate.contactors);
                    }
                }).start();
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (currFrag instanceof FileFragment) {
            getMenuInflater().inflate(R.menu.file, menu);
        } else if (currFrag instanceof ContactFragment) {
            getMenuInflater().inflate(R.menu.contact, menu);
        } else if (currFrag instanceof PathFragment) {
            getMenuInflater().inflate(R.menu.path, menu);
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        FragmentManager fragmentManager = getFragmentManager();
        transaction = fragmentManager.beginTransaction();
        Fragment fragment = null;

        if (id == R.id.nav_record) {
            fragment = RecordFragment.getFragment();
            transaction.add(R.id.content, fragment, "Record");
        } else if (id == R.id.nav_file) {

            fragment = FileFragment.getFragment();
            transaction.add(R.id.content, fragment, "File");

        } else if (id == R.id.nav_path) {

            fragment = PathFragment.getFragment();
            transaction.add(R.id.content, fragment, "Path");

        } else if (id == R.id.nav_friends) {

            fragment = ContactFragment.getFragment();
            transaction.add(R.id.content, fragment, "Contact");

        } else if (id == R.id.nav_camera) {

            fragment = MonitorFragment.getFragment();
            transaction.add(R.id.content, fragment, "Monitor");

        } else if (id == R.id.nav_setting) {

            fragment = SettingFragment.getFragment();
            transaction.add(R.id.content, fragment, "Setting");
        }
        if (currFrag != fragment) {
            transaction.commit();
            changeToFragment(fragment, false);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void changeToFragment(Fragment newFrag, boolean isMonitor) {
        transaction = getFragmentManager().beginTransaction();
        if (isMonitor) {
            transaction.add(R.id.content, newFrag, "Record");
            navigationView.setCheckedItem(R.id.nav_record);
        }
        if (currFrag instanceof RecordFragment) {
            currFrag.onPause();
        }
        preFrag = currFrag;

        transaction.hide(currFrag).show(newFrag).commit();
        currFrag = newFrag;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.remove(preFrag).commit();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
