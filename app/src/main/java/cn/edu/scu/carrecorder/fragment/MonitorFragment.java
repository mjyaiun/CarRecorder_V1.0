package cn.edu.scu.carrecorder.fragment;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.edu.scu.carrecorder.R;
import cn.edu.scu.carrecorder.activity.MainActivity;
import cn.edu.scu.carrecorder.util.PublicDate;

public class MonitorFragment extends Fragment {
    private static MonitorFragment monitorFragment = new MonitorFragment();

    public static MonitorFragment getFragment() {
        return monitorFragment;
    }

    @InjectView(R.id.toolbar_monitor)
    Toolbar toolbar;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monitor, container, false);
        ButterKnife.inject(this, view);
        initView();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.main, menu);
    }

    private void initView() {
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
