package cn.edu.scu.carrecorder.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.CamcorderProfile;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

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
import cn.edu.scu.carrecorder.classes.Contactor;
import cn.edu.scu.carrecorder.fragment.ContactFragment;
import cn.edu.scu.carrecorder.fragment.FileFragment;
import cn.edu.scu.carrecorder.fragment.MonitorFragment;
import cn.edu.scu.carrecorder.fragment.RecordFragment;
import cn.edu.scu.carrecorder.fragment.SettingFragment;
import cn.edu.scu.carrecorder.util.PublicDate;
import cn.edu.scu.carrecorder.util.VideoSize;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Toolbar toolbar;
    DrawerLayout drawer;
    Fragment currFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        VideoSize.getOptimalSize(screenWidth, screenHeight);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        PublicDate.drawer = drawer;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_record);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        Fragment homeFragment = RecordFragment.getFragment();
        transaction.add(R.id.content, homeFragment, "Record");
        currFrag = homeFragment;
        transaction.commit();


    }

    @Override
    protected void onResume() {
        super.onResume();
        ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0).invalidate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadContacts();
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
                                FileFragment.getFragment().onResume();
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
            case R.id.add_contactor:
                startActivityForResult(new Intent(
                        Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), 0);
                break;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void deleteAllContact() {
        PublicDate.contactors.clear();
        saveContacts(PublicDate.contactors);
        ContactFragment.getFragment().onResume();
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
                saveContacts(PublicDate.contactors);
                ContactFragment.getFragment().onResume();
            }
        }
    }

    public void saveContacts(List<Contactor> contactors) {
        try {
            FileOutputStream fos = this.openFileOutput("Contacts", Context.MODE_PRIVATE);
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

    public void loadContacts() {
        PublicDate.contactors = new ArrayList<>();
        try {
            FileInputStream fis = this.openFileInput("Contacts");
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

    private void deleteAllVideo() {
        String videoFilePath = getFilesDir().getAbsolutePath();
        File dir = new File(videoFilePath);
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                file.delete();
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
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment = null;

        if (id == R.id.nav_record) {
            fragment = fragmentManager.findFragmentByTag("Record");
            if (fragment == null) {
                fragment = RecordFragment.getFragment();
                transaction = transaction.add(R.id.content, fragment, "Record");
            }
        } else if (id == R.id.nav_file) {
            fragment = fragmentManager.findFragmentByTag("File");
            if (fragment == null) {
                fragment = FileFragment.getFragment();
                transaction = transaction.add(R.id.content, fragment, "File");
            } else {
                fragment.onResume();
            }
        } else if (id == R.id.nav_friends) {
            fragment = fragmentManager.findFragmentByTag("Contacts");
            if (fragment == null) {
                fragment = ContactFragment.getFragment();
                transaction = transaction.add(R.id.content, fragment, "Contacts");
            }
        } /*else if (id == R.id.nav_camera) {
            fragment = fragmentManager.findFragmentByTag("Camera");
            if (fragment == null) {
                fragment = MonitorFragment.getFragment();
                transaction = transaction.add(R.id.content, fragment, "Camera");
            }
        }*/ else if (id == R.id.nav_setting) {
            fragment = fragmentManager.findFragmentByTag("Setting");
            if (fragment == null) {
                fragment = SettingFragment.getFragment();
                transaction = transaction.add(R.id.content, fragment, "Setting");
            }
        }

        //transaction = transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);

        if (currFrag != fragment) {
            transaction.hide(currFrag).show(fragment).commit();
            currFrag.onPause();
            currFrag = fragment;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
