package cn.edu.scu.carrecorder.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import cn.edu.scu.carrecorder.activity.MainActivity;
import cn.edu.scu.carrecorder.fragment.RecordFragment;


/**
 * Created by zjh on 2016/9/18.
 */
public class SMSBroadcastReceiver extends BroadcastReceiver {
    private static final int CAMERA_WITH_DATA = 3023;
    public void onReceive(Context context, Intent intent) {
        SmsMessage msg = null;
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdusObj = (Object[]) bundle.get("pdus");
            for (Object p : pdusObj) {
                msg= SmsMessage.createFromPdu((byte[]) p);

                String msgTxt =msg.getMessageBody();//得到消息的内容
                String number = msg.getOriginatingAddress();

                if (msgTxt.equals("Photo")) {
                    MainActivity activity = (MainActivity)context;
                    if (activity.getCurrFrag() instanceof RecordFragment) {
                        RecordFragment fragment = (RecordFragment) activity.getCurrFrag();
                        fragment.takePicture(number);
                    } else {
                        activity.changeToFragment(RecordFragment.getFragment());
                        RecordFragment.getFragment().takePicture(number);
                    }
                }
            }
            return;
        }
    }


}
