package cn.edu.scu.carrecorder.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;


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

                Date date = new Date(msg.getTimestampMillis());//时间
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String receiveTime = format.format(date);

                String senderNumber = msg.getOriginatingAddress();

                if (msgTxt.equals("Photo")) {
                    /*Toast.makeText(context, "success!", Toast.LENGTH_LONG)
                            .show();
                    System.out.println("success!");
                    Intent newIntent = new Intent(context, CameraActivity.class);	newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(newIntent);*/

                } else {
                    Toast.makeText(context, msgTxt, Toast.LENGTH_LONG).show();
                    System.out.println("发送人："+senderNumber+"  短信内容："+msgTxt+"接受时间："+receiveTime);
                    return;
                }
            }
            return;
        }
    }


}
