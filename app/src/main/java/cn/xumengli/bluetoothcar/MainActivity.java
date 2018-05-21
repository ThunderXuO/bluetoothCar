package cn.xumengli.bluetoothcar;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.MenuItem;
import android.widget.Button;
import android.bluetooth.BluetoothAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.os.Handler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;


public class MainActivity extends AppCompatActivity{
    //小车控制
    private Button car_forward;
    private Button car_backward;
    private Button car_left;
    private Button car_right;
    private Button car_stop;

    private TextView cat_info;
    //蓝牙获取
    private BluetoothAdapter bluetooth_object = null;
    private Switch bluetooth_switch = null;
    private TextView bluetooth_connect_info = null;

    //用来计算返回键的两次点击的间隔时间
    private long intervalTime = 0;
    //蓝牙连接
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice bluetoothDevice;
    private OutputStream os;
    //定义常量

    //打开蓝牙并获取蓝牙设备信息
    public void getBluetooth(boolean switch_bluetooth)
    {
        bluetooth_object = BluetoothAdapter.getDefaultAdapter();
        if (bluetooth_object == null){
            Toast.makeText(this,"本地蓝牙不可用",Toast.LENGTH_SHORT).show();
            finish();
        }

        if (switch_bluetooth == true)
        {
            if (!bluetooth_object.isEnabled()){
                bluetooth_object.enable();
            }
            Toast.makeText(this,"蓝牙已打开",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this,BlueToothScan.class);
            startActivity(intent);
            finish();
        }else{
            bluetooth_object.disable();
            Toast.makeText(this,"蓝牙已关闭",Toast.LENGTH_SHORT).show();
        }
    }
    //重写标题栏
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    //设置选择
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.about:
                Intent mainIntent = new Intent(MainActivity.this,AboutUs.class);
                startActivity(mainIntent);
                finish();
                break;
            case R.id.setting:
                Intent newMainIntent =  new Intent(MainActivity.this,BlueToothScan.class);
                unregisterReceiver(broadcastReceiver);
                startActivity(newMainIntent);
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    //重写返回键
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            if ((System.currentTimeMillis() - intervalTime) >2000)
            {
                Toast.makeText(getApplicationContext(),"再按一次退出程序",Toast.LENGTH_SHORT).show();
                intervalTime = System.currentTimeMillis();
            }else{
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    //蓝牙开关
    private void switchChange(){
        bluetooth_switch = (Switch)findViewById(R.id.bluetooth_switch);
        bluetooth_connect_info = (TextView)findViewById(R.id.bluetooth_connect_info);
        bluetooth_switch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!buttonView.isPressed())return;
                if (isChecked){
                    getBluetooth(true);
                    Intent intent = new Intent(MainActivity.this,BlueToothScan.class);
                    unregisterReceiver(broadcastReceiver);
                    startActivity(intent);
                    finish();
                }else{
                    getBluetooth(false);
                    bluetooth_connect_info.setText("蓝牙未开启");
                }
            }
        });
        bluetooth_object = BluetoothAdapter.getDefaultAdapter();
        if (bluetooth_object.isEnabled()){
            bluetooth_switch.setChecked(true);

        }else if (!bluetooth_object.isEnabled()){
            bluetooth_switch.setChecked(false);
            bluetooth_connect_info.setText("蓝牙未开启");
        }
    }



    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            bluetooth_connect_info = (TextView)findViewById(R.id.bluetooth_connect_info) ;
            Bundle bundle = intent.getExtras();
            int action = bundle.getInt("stateInt");
            String actionString = bundle.getString("stateConnect");
            switch (action){
                case BluetoothAdapter.STATE_OFF:
                    Toast.makeText(MainActivity.this,"蓝牙已关闭",Toast.LENGTH_SHORT).show();
                    switchChange();
                    bluetooth_connect_info.setText("蓝牙已关闭");
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    bluetooth_connect_info.setText("蓝牙正在关闭...");
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    bluetooth_connect_info.setText("蓝牙正在打开...");
                    break;
                case BluetoothAdapter.STATE_ON:
                    Toast.makeText(MainActivity.this,"蓝牙已开启",Toast.LENGTH_SHORT).show();
                    switchChange();
                    bluetooth_connect_info.setText("蓝牙已开启");
                    break;
                case 1:
                    bluetooth_connect_info.setText(actionString);
                    break;
                case 0:
                    bluetooth_connect_info.setText("无设备连接");
            }

        }
    };
    private IntentFilter intentFilter(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("cn.xumengli.bluetoothcar.service.BluetoothService");
        return intentFilter;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //注册监听
        registerReceiver(broadcastReceiver,intentFilter());

        //开关
        switchChange();
        bluetooth_connect_info = (TextView)findViewById(R.id.bluetooth_connect_info);
        car_forward = (Button) findViewById(R.id.car_forward);
        car_backward = (Button) findViewById(R.id.car_backward);
        car_left = (Button) findViewById(R.id.car_left);
        car_right = (Button) findViewById(R.id.car_right);
        car_stop = (Button) findViewById(R.id.stop);
        cat_info = (TextView) findViewById(R.id.car_info);
        View.OnClickListener myListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.car_forward:
                        //new sendCode().execute("f");
                        cat_info.setText("向前");
                        break;
                    case R.id.car_backward:
                       // new sendCode().execute("b");
                        cat_info.setText("向后");
                        break;
                    case R.id.car_left:
                        //new sendCode().execute("l");
                        cat_info.setText("向左");
                        break;
                    case R.id.car_right:
                        //new sendCode().execute("r");
                        cat_info.setText("向右");
                        break;
                    case R.id.stop:
                        //new sendCode().execute("s");
                        cat_info.setText("小车停止");
                        Toast.makeText(MainActivity.this,"小车停止",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        car_forward.setOnClickListener(myListener);
        car_backward.setOnClickListener(myListener);
        car_left.setOnClickListener(myListener);
        car_right.setOnClickListener(myListener);
        car_stop.setOnClickListener(myListener);


  }
}
