package com.example.lll.smstest;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private TextView sender;
    private TextView content;
    private IntentFilter receiverFilter;
    private MessageReceiver messageReceiver;
    private EditText to;
    private EditText msgInput;
    private Button send;
    private IntentFilter sendFilter;
    private SendStatusReceiver sendStatusReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sender = (TextView) findViewById(R.id.sender);
        content = (TextView) findViewById(R.id.content);
        receiverFilter = new IntentFilter();
        receiverFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        receiverFilter.setPriority(2147483647);
        messageReceiver = new MessageReceiver();
        registerReceiver(messageReceiver,receiverFilter);
        to = (EditText) findViewById(R.id.to);
        msgInput = (EditText) findViewById(R.id.msg_input);
        send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SmsManager smsManager = SmsManager.getDefault();
                Intent sentIntent = new Intent("SENT_SMS_ACTION");
                PendingIntent  pi = PendingIntent.getBroadcast(
                        MainActivity.this,0,sentIntent,0);
                smsManager.sendTextMessage(to.getText().toString(),null,
                        msgInput.getText().toString(),pi,null);
            }
        });
        sendFilter = new IntentFilter();
        sendFilter.addAction("SENT_SMS_ACTION");
        sendStatusReceiver = new SendStatusReceiver();
        registerReceiver(sendStatusReceiver,sendFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(messageReceiver);
        unregisterReceiver(sendStatusReceiver);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class MessageReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context,Intent intent){
            Bundle bundle = intent.getExtras();
            Object[] pdus = (Object[]) bundle.get("pdus");
            SmsMessage[] messages = new SmsMessage[pdus.length];
            for (int i = 0;i < messages.length;i++){
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                String address = messages[0].getOriginatingAddress();
                String fullMessage = "";
                for (SmsMessage message:messages){
                    fullMessage = message.getMessageBody();
                }
                sender.setText(address);
                content.setText(fullMessage);
                abortBroadcast();
            }

        }
    }

    class SendStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context,Intent intent){
            if (getResultCode() == RESULT_OK) {
                Toast.makeText(context,"Message Sent",Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(context,"Send failed",Toast.LENGTH_LONG).show();
            }
        }
    }
}
