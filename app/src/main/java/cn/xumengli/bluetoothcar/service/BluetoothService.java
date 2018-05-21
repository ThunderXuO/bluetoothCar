package cn.xumengli.bluetoothcar.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;


import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;


/**
 * Created by xml on 2018/2/26.
 */

public class BluetoothService extends Service{
    private String dataMac = "";
    private boolean running = false;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice device;
    private BluetoothSocket btsocekt;
    private String getNowAddress;
    private OutputStream os;
    public IBinder onBind(Intent intent){
        return null;
    }

    public void onCreate(){
        registerReceiver(broadcastReceiver,intentFilter());

        System.out.println("onCreate Invoke");
        super.onCreate();
    }
    public int onStartCommand(Intent intent,int flag, int startId){
        String data = intent.getStringExtra("connectAddress");
        getconnect(data);

        return super.onStartCommand(intent,flag,startId);
    }
    //注册广播
    public IntentFilter intentFilter(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        return intentFilter;
    }
    //发送数据,以广播的形式
    private void putData(int state){
        Intent intent = new Intent();
        intent.putExtra("stateInt",state);
        intent.setAction("cn.xumengli.bluetoothcar.service.BluetoothService");
        sendBroadcast(intent);
    }
    private void putConnect(String state){
        Intent intent = new Intent();
        intent.putExtra("`",state);
        intent.setAction("cn.xumengli.bluetoothcar.service.BluetoothService");
        sendBroadcast(intent);
    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ((action).equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
                switch (state){
                    case BluetoothAdapter.STATE_OFF:
                        putData(BluetoothAdapter.STATE_OFF);
                        Log.d("state","蓝牙关闭");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        putData(BluetoothAdapter.STATE_OFF);
                        Log.d("state","蓝牙正在关闭");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        putData(BluetoothAdapter.STATE_ON);
                        Log.d("state","蓝牙开启");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        putData(BluetoothAdapter.STATE_TURNING_ON);
                        Log.d("state","蓝牙正在打开");
                        break;

                }
            }
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,-1);
                switch (state){
                    case BluetoothDevice.BOND_NONE:
                        putData(BluetoothDevice.BOND_NONE);
                        Log.d("state","删除配对");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        putData(BluetoothDevice.BOND_BONDED);
                        Log.d("state","配对成功");
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        putData(BluetoothDevice.BOND_BONDING);
                        Log.d("state","正在配对");
                        break;
                }
            }
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){
                putData(1);
                putConnect(getBluetoothInfo());
                Log.d("state","连接成功");
            }
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
                putData(0);
                putConnect(getBluetoothInfo());
                Log.d("state","断开连接");
            }

        }
    };
    public String getBluetoothInfo(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;
        try{
            Method method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState",(Class[]) null);
            method.setAccessible(true);
            int state = (int) method.invoke(bluetoothAdapter,(Object[]) null);

            if (state == BluetoothAdapter.STATE_CONNECTED){
                Log.i("Id","001");
                Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
                if (devices.size()>0 ||bluetoothAdapter != null){
                    for (BluetoothDevice device:devices){
                        Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected",(Class[]) null);
                        method.setAccessible(true);
                        boolean isConnected = (boolean) isConnectedMethod.invoke(device,(Object[]) null);
                        if (isConnected){
                            getNowAddress = device.getName();
                        }
                    }
                }

            }else if (state == BluetoothAdapter.STATE_DISCONNECTED){
                Log.i("Id","003");
                getNowAddress = "无设备连接";
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return getNowAddress;
    }
    public void onDestroy(){
        System.out.println("onDestroy Invoke");
        super.onDestroy();
    }

    //蓝牙连接
    public void getconnect(final String address){
        bluetoothAdapter =  BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
        try{
            Method clientMethod = bluetoothDevice.getClass().getMethod("createRfcommSocket",new Class[]{int.class});
            btsocekt = (BluetoothSocket) clientMethod.invoke(bluetoothDevice,1);
            btsocekt.connect();

        }catch (NoSuchMethodException e){
            e.printStackTrace();
        }catch (IllegalAccessException e){
            e.printStackTrace();
        }catch (InvocationTargetException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

    }
    private void sendMessage(final BluetoothSocket btsocekt,final String code){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (btsocekt == null){
                        Log.i("state","bluetoothSocket object is null");
                        os = btsocekt.getOutputStream();
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
                try{
                    if (os != null){
                        os.write(code.getBytes());
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
    }

}
