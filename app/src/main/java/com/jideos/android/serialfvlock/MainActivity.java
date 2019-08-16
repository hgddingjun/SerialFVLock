package com.jideos.android.serialfvlock;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.jideos.android.serialfvlock.CommandPackage.CMD_DEL_ALL;
import static com.jideos.android.serialfvlock.CommandPackage.CMD_DEL_MODEL;
import static com.jideos.android.serialfvlock.CommandPackage.CMD_DEVICE_INIT;
import static com.jideos.android.serialfvlock.CommandPackage.CMD_DEV_CLOSE;
import static com.jideos.android.serialfvlock.CommandPackage.CMD_DEV_OPEN;
import static com.jideos.android.serialfvlock.CommandPackage.CMD_GET_BUTTON_STATE;
import static com.jideos.android.serialfvlock.CommandPackage.CMD_GET_ID;
import static com.jideos.android.serialfvlock.CommandPackage.CMD_GET_NEW_ID;
import static com.jideos.android.serialfvlock.CommandPackage.CMD_GET_SYS_INFO;
import static com.jideos.android.serialfvlock.CommandPackage.CMD_GET_USER_INFO;
import static com.jideos.android.serialfvlock.CommandPackage.CMD_IDENTY_USER;
import static com.jideos.android.serialfvlock.CommandPackage.CMD_READ_START;
import static com.jideos.android.serialfvlock.CommandPackage.CMD_READ_UNIQUE_ID;
import static com.jideos.android.serialfvlock.CommandPackage.CMD_ROLL_STEP;
import static com.jideos.android.serialfvlock.CommandPackage.CMD_ROLL_STOP;
import static com.jideos.android.serialfvlock.CommandPackage.CMD_SET_BAUD_RATE;
import static com.jideos.android.serialfvlock.CommandPackage.CMD_SET_DEVICEID;
import static com.jideos.android.serialfvlock.CommandPackage.CMD_SET_TIMEOUT;
import static com.jideos.android.serialfvlock.CommandPackage.CMD_WRITE_START;

public class MainActivity extends AppCompatActivity {
    private Spinner    spn_serial_port;
    private Spinner    spn_serial_rate;
    private Button     btn_serial_open;
    private Button     btn_serial_close;
    private Button     btn_device_open;
    private Button     btn_device_close;
    private Button     btn_device_roll;
    private Button     btn_device_identify;
    private Spinner    spn_command_cmd;
    private EditText   edt_command_para;
    private Button     btn_command_send;
    private TextView   txv_status_prompt1;
    private TextView   txv_status_prompt2;
    private TextView   txv_logcat_prompt3;
    private ScrollView scv_logcat_scroll;

    private FingerVein mFingerVein;
    private String mPath;
    private int mRate;

    private boolean isOpened = false;
    private Handler mHandler;

    private boolean mRollStarted = false;
    private RollThread mRollThread;
    private int mRollStep = -1;

    private boolean mIdentifyStarted = false;
    private IdentifyThread mIdentifyThread;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFingerVein = new FingerVein();
        mFingerVein.setCallback(new ICallback() {
            @Override
            public void onReceive(byte[] buf) {
                receive(buf);
            }
        });

        spn_serial_port = findViewById(R.id.serial_port);
        SpinnerAdapter portAdapter = ArrayAdapter.createFromResource(this, R.array.serial_port_spinner_values, android.R.layout.simple_spinner_dropdown_item);
        spn_serial_port.setAdapter(portAdapter);
        spn_serial_port.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] ports = getResources().getStringArray(R.array.serial_port_spinner_values);
                mPath = ports[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spn_serial_port.setSelection(4,true);

        spn_serial_rate = findViewById(R.id.serial_rate);
        SpinnerAdapter rateAdapter = ArrayAdapter.createFromResource(this, R.array.serial_rate_spinner_values, android.R.layout.simple_spinner_dropdown_item);
        spn_serial_rate.setAdapter(rateAdapter);
        spn_serial_rate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] rates = getResources().getStringArray(R.array.serial_rate_spinner_values);
                mRate = Integer.parseInt(rates[i].split("\\(")[1].split("\\)")[0]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spn_serial_rate.setSelection(16,true);

        btn_serial_open = findViewById(R.id.serial_open);
        btn_serial_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isOpened) {
                    try {
                        mFingerVein.open(mPath, mRate);
                        isOpened = true;
                        prompt(3, "打开串口成功");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        btn_serial_close = findViewById(R.id.serial_close);
        btn_serial_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOpened) {
                    try {
                        mFingerVein.close();
                        isOpened = false;
                        prompt(3, "关闭串口成功");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        btn_device_open = findViewById(R.id.device_open);
        btn_device_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send(CMD_DEV_OPEN, null);
            }
        });

        btn_device_close = findViewById(R.id.device_close);
        btn_device_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send(CMD_DEV_CLOSE, null);
            }
        });

        btn_device_roll = findViewById(R.id.device_roll);
        btn_device_roll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mRollStarted) {
                    mRollThread = new RollThread();
                    mRollThread.start();
                    mRollStarted = true;
                    btn_device_roll.setTextColor(Color.GREEN);
                } else {
                    mRollStarted = false;
                    btn_device_roll.setTextColor(Color.BLACK);
                }
            }
        });

        btn_device_identify = findViewById(R.id.device_identify);
        btn_device_identify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mIdentifyStarted) {
                    mIdentifyThread = new IdentifyThread();
                    mIdentifyThread.start();
                    mIdentifyStarted = true;
                    btn_device_identify.setTextColor(Color.GREEN);
                } else {
                    mIdentifyStarted = false;
                    btn_device_identify.setTextColor(Color.BLACK);
                }
            }
        });

        spn_command_cmd = findViewById(R.id.command_cmd);
        SpinnerAdapter cmdAdapter = ArrayAdapter.createFromResource(this, R.array.command_cmd_spinner_values, android.R.layout.simple_spinner_dropdown_item);
        spn_command_cmd.setAdapter(cmdAdapter);
        spn_command_cmd.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        edt_command_para = findViewById(R.id.command_para);

        btn_command_send = findViewById(R.id.command_send);
        btn_command_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (spn_command_cmd.getSelectedItemPosition()) {
                    case 0: {
                        int id = Integer.parseInt(edt_command_para.getText().toString());
                        byte[] param = {(byte) (id & 0xFF)};
                        send(CMD_SET_DEVICEID, param);
                    }
                    break;
                    case 1: {
                        int rate = Integer.parseInt(edt_command_para.getText().toString());
                        byte[] param = {(byte) (rate & 0xFF)};
                        send(CMD_SET_BAUD_RATE, param);
                    }
                    break;
                    case 2: {
                        int timeout = Integer.parseInt(edt_command_para.getText().toString());
                        byte[] param = {(byte) (timeout & 0xFF)};
                        send(CMD_SET_TIMEOUT, param);
                    }
                    break;
                    case 3: {
                        send(CMD_READ_UNIQUE_ID, null);
                    }
                    break;
                    case 4: {
                        send(CMD_GET_BUTTON_STATE, null);
                    }
                    break;
                    case 5: {
                        int id = Integer.parseInt(edt_command_para.getText().toString());
                        byte[] param = {(byte) (id & 0xFF), (byte) ((id >> 8) & 0xFF), (byte) ((id >> 16) & 0xFF), (byte) ((id >> 24) & 0xFF)};
                        send(CMD_DEL_MODEL, param);
                    }
                    break;
                    case 6: {
                        send(CMD_DEL_ALL, null);
                    }
                    break;
                    case 7: {
                        send(CMD_GET_NEW_ID, null);
                    }
                    break;
                    case 8: {
                        send(CMD_GET_USER_INFO, null);
                    }
                    break;
                    case 9: {
                        int id = Integer.parseInt(edt_command_para.getText().toString());
                        byte[] param = {(byte) (id & 0xFF), (byte) ((id >> 8) & 0xFF), (byte) ((id >> 16) & 0xFF), (byte) ((id >> 24) & 0xFF)};
                        send(CMD_GET_ID, param);
                    }
                    break;
                    case 10: {
                        send(CMD_DEVICE_INIT, null);
                    }
                    break;
                    case 11: {
                        send(CMD_GET_SYS_INFO, null);
                    }
                    break;
                    case 12: {
                        int id = Integer.parseInt(edt_command_para.getText().toString());
                        byte[] param = {0x01, 0x18, 0x0D, (byte) (id & 0xFF), (byte) ((id >> 8) & 0xFF), (byte) ((id >> 16) & 0xFF), (byte) ((id >> 24) & 0xFF)};
                        send(CMD_WRITE_START, param);
                    }
                    break;
                    case 13: {
                        int id = Integer.parseInt(edt_command_para.getText().toString());
                        byte[] param = {0x01, 0x18, 0x0D, (byte) (id & 0xFF), (byte) ((id >> 8) & 0xFF), (byte) ((id >> 16) & 0xFF), (byte) ((id >> 24) & 0xFF)};
                        send(CMD_READ_START, param);
                    }
                }
            }
        });

        txv_status_prompt1 = findViewById(R.id.status_prompt1);
        txv_status_prompt2 = findViewById(R.id.status_prompt2);
        txv_logcat_prompt3 = findViewById(R.id.logcat_prompt3);

        scv_logcat_scroll = findViewById(R.id.logcat_scroll);

        mHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        txv_status_prompt1.setText((CharSequence) msg.obj);
                        break;
                    case 2:
                        txv_status_prompt2.setText((CharSequence) msg.obj);
                        break;
                    case 3:
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ", Locale.CHINA);
                        txv_logcat_prompt3.append(format.format(new Date(System.currentTimeMillis())) + msg.obj + "\n");
                        this.post(new Runnable() {
                            @Override
                            public void run() {
                                scv_logcat_scroll.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });
                        break;
                }
            }
        };
    }

    private CommandMap mCommandMap;

    private void send(byte command, byte[] param) {
        CommandPackage commandPackage = new CommandPackage(command, param);
        mCommandMap = commandPackage.getCommandMap();
        mFingerVein.send(commandPackage.toBytes());
    }

    private void receive(byte[] buf) {
        ResponsePackage responsePackage = new ResponsePackage(buf);
        if (mCommandMap.getSequence() == responsePackage.getSequence()) {
            switch (mCommandMap.getCommand()) {
                case CMD_DEV_OPEN: {
                    int result = responsePackage.getResult();
                    switch (result) {
                        case 0x00:
                            prompt(3, "打开设备成功");
                            break;
                        case 0x01:
                            prompt(3, "打开设备失败");
                            break;
                    }
                }
                break;
                case CMD_DEV_CLOSE: {
                    int result = responsePackage.getResult();
                    switch (result) {
                        case 0x00:
                            prompt(3, "关闭设备成功");
                            break;
                        case 0x01:
                            prompt(3, "关闭设备失败");
                            break;
                    }
                }
                break;
                case CMD_SET_DEVICEID: {
                    int result = responsePackage.getResult();
                    if (result == 0x00) {
                        prompt(3, "设置设备编号成功");
                    } else {
                        prompt(3, "设置设备编号失败");
                    }
                }
                break;
                case CMD_SET_BAUD_RATE: {
                    int result = responsePackage.getResult();
                    if (result == 0x00) {
                        prompt(3, "设置波特率成功");
                    } else {
                        prompt(3, "设置波特率失败");
                    }
                }
                break;
                case CMD_SET_TIMEOUT: {
                    int result = responsePackage.getResult();
                    if (result == 0x00) {
                        prompt(3, "设置延时成功");
                    } else {
                        prompt(3, "设置延时失败");
                    }
                }
                break;
                case CMD_READ_UNIQUE_ID: {
                    int result = responsePackage.getResult();
                    if (result == 0x00) {
                        StringBuffer id = new StringBuffer();
                        for (int i = 10; i <= 25; i++)
                            id.append(Integer.toHexString(responsePackage.getByte(i) & 0xFF));
                        prompt(3, "唯一ID：" + id.toString());
                    } else {
                        prompt(3, "获取唯一ID失败");
                    }
                }
                break;
                case CMD_GET_BUTTON_STATE: {
                    int result = responsePackage.getResult();
                    if (result == 0x00) {
                        int state = responsePackage.getByte(10);
                        switch (state) {
                            case 0:
                                prompt(2, "弹起");
                                break;
                            case 1:
                                prompt(2, "按下");
                                break;
                        }
                        if (mRollStarted) {
                            mRollThread.setFingerState(state);
                        }
                        if (mIdentifyStarted) {
                            mIdentifyThread.setFingerState(state);
                        }
                    }
                }
                break;
                case CMD_DEL_MODEL: {
                    int result = responsePackage.getResult();
                    if (result == 0x00) {
                        prompt(3, "删除用户" + Integer.parseInt(edt_command_para.getText().toString()) + "成功");
                    } else {
                        prompt(3, "删除用户" + Integer.parseInt(edt_command_para.getText().toString()) + "失败");
                    }
                }
                break;
                case CMD_DEL_ALL: {
                    int result = responsePackage.getResult();
                    if (result == 0x00) {
                        prompt(3, "清除所有用户成功");
                    } else {
                        prompt(3, "清除所有用户失败");
                    }
                }
                break;
                case CMD_GET_NEW_ID: {
                    int result = responsePackage.getResult();
                    if (result == 0x00) {
                        prompt(3, "id = " + ((responsePackage.getByte(10) & 0xFF) + ((responsePackage.getByte(11) & 0xFF) << 8) + ((responsePackage.getByte(12) & 0xFF) << 16) + ((responsePackage.getByte(13) & 0xFF) << 24)));
                    }
                }
                break;
                case CMD_GET_USER_INFO: {
                    int result = responsePackage.getResult();
                    if (result == 0x00) {
                        prompt(3, "注册用户数" + ((responsePackage.getByte(10) & 0xFF) + ((responsePackage.getByte(11) & 0xFF) << 8) + ((responsePackage.getByte(12) & 0xFF) << 16) + ((responsePackage.getByte(13) & 0xFF) << 24)));
                    }
                }
                break;
                case CMD_GET_ID: {
                    int result = responsePackage.getResult();
                    if (result == 0x00) {
                        prompt(3, "用户存在，注册用户模板数" + (responsePackage.getByte(10) & 0xFF));
                    } else if (result == 0xFF) {
                        prompt(3, "用户不存在");
                    }
                }
                break;
                case CMD_DEVICE_INIT: {
                    int result = responsePackage.getResult();
                    if (result == 0x00) {
                        prompt(3, "恢复出厂设备成功");
                    } else {
                        prompt(3, "恢复出厂设备失败");
                    }
                }
                break;
                case CMD_GET_SYS_INFO: {
                    if (responsePackage.getResult() == 0x00) {
                        prompt(3, "版本：" + (responsePackage.getByte(10) & 0xFF) + "." + (responsePackage.getByte(11) & 0xFF));
                        prompt(3, "设备编号：" + (responsePackage.getByte(12) & 0xFF));
                        prompt(3, "波特率：" + getResources().getStringArray(R.array.serial_rate_spinner_values)[(responsePackage.getByte(13) & 0xFF)].split("\\(")[1].split("\\)")[0]);
                        prompt(3, "安全等级：" + (((responsePackage.getByte(15) & 0xFF) << 8) + (responsePackage.getByte(14) & 0xFF)));
                        prompt(3, "超时时间：" + (responsePackage.getByte(16) & 0xFF));
                        prompt(3, "允许重复建模：" + ((responsePackage.getByte(17) & 0xFF) == 0 ? "是" : "否"));
                        prompt(3, "允许同一手指检测：" + ((responsePackage.getByte(18) & 0xFF) == 1 ? "是" : "否"));
                        prompt(3, "模板载入时发生错误的个数：" + (responsePackage.getByte(20) & 0xFF));
                    }
                }
                break;
                case CMD_ROLL_STEP: {
                        prompt(3, "建模步骤" + mRollStep + ":" + (responsePackage.getResult() == 0x00 ? "ok" : "fail"));
                }
                case CMD_ROLL_STOP: {
                    if (responsePackage.getResult() == 0x00) {
                        int id = (responsePackage.getByte(10) & 0xFF) + ((responsePackage.getByte(11) & 0xFF) << 8) + ((responsePackage.getByte(12) & 0xFF) << 16) + ((responsePackage.getByte(13) & 0xFF) << 24);
                        if (id != 0)
                            prompt(3, "建模完成id=" + id);
                    }
                }
                break;
                case CMD_IDENTY_USER: {
                    int result = responsePackage.getResult();
                    if (result == 0x00) {
                        int id = ((responsePackage.getByte(13) & 0xFF) << 24) | ((responsePackage.getByte(12) & 0xFF) << 16) | ((responsePackage.getByte(11) & 0xFF) << 8) | (responsePackage.getByte(10) & 0xFF);
                        prompt(3, "认证成功 id = " + id);
                    } else {
                        prompt(3, "认证失败");
                    }
                }
                break;
            }
        }
    }

    private void prompt(int id, String string) {
        Message message = mHandler.obtainMessage(id, string);
        mHandler.sendMessage(message);
    }

    private class RollThread extends Thread {
        private static final byte UP   = 0x00;
        private static final byte DOWN = 0x01;

        private int mFingerState = UP;

        int getFingerState() {
            return mFingerState;
        }

        void setFingerState(int state) {
            mFingerState = state;
        }

        @Override
        public void run() {
            while (true) {
                mRollStep = 0;
                prompt(3, "建模步骤" + mRollStep);
                while (getFingerState() != DOWN && mRollStarted) {
                    send(CMD_GET_BUTTON_STATE, null);
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!mRollStarted)
                    break;
                {
                    byte[] param = {0x00};
                    send(CMD_ROLL_STEP, param);
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                while (getFingerState() != UP && mRollStarted) {
                    send(CMD_GET_BUTTON_STATE, null);
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                mRollStep = 1;
                prompt(3, "建模步骤" + mRollStep);
                while (getFingerState() != DOWN && mRollStarted) {
                    send(CMD_GET_BUTTON_STATE, null);
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!mRollStarted)
                    break;
                {
                    byte[] param = {0x01};
                    send(CMD_ROLL_STEP, param);
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                while (getFingerState() != UP && mRollStarted) {
                    send(CMD_GET_BUTTON_STATE, null);
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                mRollStep = 2;
                prompt(3, "建模步骤" + mRollStep);
                while (getFingerState() != DOWN && mRollStarted) {
                    send(CMD_GET_BUTTON_STATE, null);
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!mRollStarted)
                    break;
                {
                    byte[] param = {0x02};
                    send(CMD_ROLL_STEP, param);
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                while (getFingerState() != UP && mRollStarted) {
                    send(CMD_GET_BUTTON_STATE, null);
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                {
                    send(CMD_ROLL_STOP, null);
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class IdentifyThread extends Thread {
        private static final byte UP   = 0x00;
        private static final byte DOWN = 0x01;

        private int mFingerState = UP;

        int getFingerState() {
            return mFingerState;
        }

        void setFingerState(int state) {
            mFingerState = state;
        }

        @Override
        public void run() {
            while (true) {
                while (getFingerState() != DOWN && mIdentifyStarted) {
                    send(CMD_GET_BUTTON_STATE, null);
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!mIdentifyStarted)
                    break;
                {
                    send(CMD_IDENTY_USER, null);
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                while (getFingerState() != UP && mIdentifyStarted) {
                    send(CMD_GET_BUTTON_STATE, null);
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
