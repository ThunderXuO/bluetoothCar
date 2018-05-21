package cn.xumengli.bluetoothcar;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.bluetooth.BluetoothAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import cn.xumengli.bluetoothcar.service.BluetoothService;

public class BlueToothScan extends Activity{
    //返回
    private Button back;

    private BluetoothAdapter bluetoothAdapter;

    private BluetoothDevice bluetoothDevice;
    private ListView bondedListView;

    private TextView bondedTextView;

    private final String[] selectFunction = {"连接","取消配对"};
    private final String[] selectFunction1 = {"取消连接","取消配对"};

    private String setFun;

    private ProgressDialog progressDialog;
    //蓝牙连接
    private List<Map<String,String>> bluetoothSearch = new ArrayList<Map<String,String>>();
    private ListView bluetoothShow;
    private SimpleAdapter newSimpleAdapter;
    private String nowAddress;
    private String listViewConnect;

    //搜索按钮
    private Button bluetoothScan;
    //重写返回键
    public boolean onKeyDown(int KeyCode, KeyEvent event){
        if(KeyCode == KeyEvent.KEYCODE_BACK){
            Intent intent = new Intent(BlueToothScan.this,MainActivity.class);
            unregisterReceiver(broadcastReceiver);
            startActivity(intent);
            finish();
        }
        return super.onKeyDown(KeyCode,event);
    }
    //注册监听
    public IntentFilter makeFilter(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction("cn.xumengli.bluetoothcar.service.BluetoothService");
        return filter;
    }
    //实现搜索动画，开始搜索
    private void progressBar(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("提示");
        progressDialog.setMessage("正在搜索附近的蓝牙...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.cancel();
                bluetoothAdapter.cancelDiscovery();
                Toast.makeText(BlueToothScan.this,"已取消",Toast.LENGTH_SHORT).show();
            }
        });
        progressDialog.show();
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String  action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
            {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
                switch (state){
                    case BluetoothAdapter.STATE_OFF:
                        Toast.makeText(BlueToothScan.this,"蓝牙未开启",Toast.LENGTH_SHORT).show();
                        Log.d("state","蓝牙关闭");
                        getBondedDevices();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d("state","蓝牙开启");
                        getBondedDevices();
                }
            }
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
                int stateOther = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,-1);
                switch (stateOther){
                    case BluetoothDevice.BOND_NONE:
                        Log.d("state","删除配对");
                        getBondedDevices();
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.d("state","配对成功");
                        getBondedDevices();
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Log.d("state","正在配对");
                        getBondedDevices();
                        break;
                }

            }
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                Log.i("state","扫描");
                BluetoothDevice findBluetoothDevices = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (findBluetoothDevices.getBondState() != BluetoothDevice.BOND_BONDED){
                    Map<String,String> item = new HashMap<>();
                    item.put("name",findBluetoothDevices.getName());
                    item.put("address",findBluetoothDevices.getAddress());
                    for (String key : item.keySet()){
                        if (!item.get(key).equals(findBluetoothDevices.getAddress())){
                            bluetoothSearch.add(item);
                            newSimpleAdapter.notifyDataSetChanged();
                        }
                    }

                }
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                Log.i("state","扫描完毕");
                Toast.makeText(BlueToothScan.this,"搜索完毕",Toast.LENGTH_SHORT).show();
            }
        }
    };
    //搜索
    private void bluetoothScan(){
        if (bluetoothAdapter.isEnabled()){
            bluetoothAdapter.startDiscovery();
            progressBar();
        }else{
            Toast.makeText(BlueToothScan.this,"蓝牙未开启",Toast.LENGTH_SHORT).show();
        }
    }
    //配对询问
    private void booleanPair(final String Address){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("是否配对");
        builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                makeBond(Address);
            }
        });
        builder.create().show();
    }
    //删除配对
    private void boolDelPair(final String address){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("是否删除配对");
        builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delBond(address);
            }
        });
        builder.create().show();
    }
    //获取当前连接
    private String getConnected(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Class<BluetoothAdapter>bluetoothAdapterClass = BluetoothAdapter.class;
        try{
            Method method = bluetoothAdapterClass.getDeclaredMethod("getConnectedState",(Class[]) null);
            method.setAccessible(true);
            int state = (int) method.invoke(bluetoothAdapter,(Object[])null);

            if (state == BluetoothAdapter.STATE_CONNECTED){
                Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
                for (BluetoothDevice device : devices){
                    Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected",(Class[])null);
                    method.setAccessible(true);
                    boolean isConnected = (Boolean)isConnectedMethod.invoke(device,(Object[])null);

                    if (isConnected){
                        Toast.makeText(BlueToothScan.this,device.getAddress()+device.getName(),Toast.LENGTH_SHORT);
                        nowAddress = device.getAddress();
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return nowAddress;
    }
    private boolean isConnected(String address){
        if (address.equals(getConnected()))return true;
        else return false;
    }
    //获取已配对的蓝牙设备信息
    public void getBondedDevices(){
        bondedListView = (ListView) findViewById(R.id.bonded_bluetooth);
        bondedTextView = (TextView) findViewById(R.id.bonded_bluetooth_empty);
        List<Map<String,String>> bondedDevicesList = new ArrayList<>( );
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bondedDevicesList.clear();
        Set <BluetoothDevice> pairDevices = bluetoothAdapter.getBondedDevices();
        if (bluetoothAdapter != null) {
            if (pairDevices.size() > 0){
                for(BluetoothDevice deivce : pairDevices){

                    if (isConnected(deivce.getAddress())){
                        listViewConnect = "已连接";
                    }else{
                        listViewConnect = "";
                    }
                    Map<String,String> item = new HashMap<>();
                    item.put("name",deivce.getName());
                    item.put("address",deivce.getAddress());
                    item.put("isconnected",listViewConnect);
                    bondedDevicesList.add(item);
                }

            }
        }
        SimpleAdapter adapter = new SimpleAdapter(this,bondedDevicesList,R.layout.list_view_bluetooth,new String[]{"name","address","isconnected"},new int[]{R.id.list_item_1,R.id.list_item_2,R.id.list_item_3});
        bondedListView.setAdapter(adapter);
    }





    //配对选项
    private void setDialog(final String address,final String name){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择操作");
        if (!isConnected(address)){
            builder.setItems(selectFunction, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setFun = selectFunction[which];
                    switch (setFun){
                        case "连接":
                            Intent intent = new Intent(BlueToothScan.this,BluetoothService.class);
                            intent.putExtra("connectAddress",address);
                            startService(intent);
                            break;
                        case "取消配对":
                            boolDelPair(address);
                            break;
                    }
                }
            });
        }else{
            builder.setItems(selectFunction1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setFun = selectFunction1[which];
                    switch (setFun){
                        case "取消连接":
                            Toast.makeText(BlueToothScan.this,address,Toast.LENGTH_SHORT).show();
                            break;
                        case "取消配对":
                            boolDelPair(address);
                            break;
                    }
                }
            });
        }
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                bluetoothAdapter.cancelDiscovery();
            }
        });
        builder.create().show();
    }
    //创建配对
    private void makeBond(String address){
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
        if (bluetoothDevice != null){
            Boolean returnBond = null;
            try {
                returnBond = ClsUtils.createBond(BluetoothDevice.class,bluetoothDevice);
            }catch (Exception e){
                e.printStackTrace();
            }
            if (returnBond){
                Toast.makeText(BlueToothScan.this,"配对成功",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(BlueToothScan.this,"配对失败",Toast.LENGTH_SHORT).show();
            }
        }
    }
    //删除配对
    private void delBond(String address){
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
        if (bluetoothDevice != null){
            Boolean returnBond = null;
            try {
                returnBond = ClsUtils.removeBond(BluetoothDevice.class,bluetoothDevice);
            }catch (Exception e){
                e.printStackTrace();
            }
            if (returnBond){
                Toast.makeText(BlueToothScan.this,"删除配对成功",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(BlueToothScan.this,"删除配对失败",Toast.LENGTH_SHORT).show();
            }
        }
    }
    protected void onCreate(Bundle savedInstanceStat){
        super.onCreate(savedInstanceStat);
        setContentView(R.layout.bluetoothscan_all);
        //设置返回
        back = (Button)findViewById(R.id.backward);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BlueToothScan.this,MainActivity.class);
                startActivity(intent);
                unregisterReceiver(broadcastReceiver);
                finish();
            }
        });
        //注册事件
        registerReceiver(broadcastReceiver,makeFilter());
        getBondedDevices();

        //蓝牙配对列表点击事件
        bondedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String,String> map = (Map<String,String>)parent.getItemAtPosition(position);
                setDialog(map.get("address"),map.get("name"));
            }
        });
        //蓝牙搜索
        bluetoothScan = (Button) findViewById(R.id.begin_search);
        bluetoothScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothSearch.clear();
                bluetoothScan();
                Toast.makeText(BlueToothScan.this,"正在搜索附近设备...",Toast.LENGTH_SHORT).show();
            }
        });
        bluetoothShow = (ListView)findViewById(R.id.avalible_bluetooth);

        newSimpleAdapter = new SimpleAdapter(BlueToothScan.this,bluetoothSearch,android.R.layout.simple_list_item_2,new String[]{"name","address"},new int[]{android.R.id.text1,android.R.id.text2});
        bluetoothShow.setAdapter(newSimpleAdapter);

        //蓝牙搜索列表点击事件

        bluetoothShow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String,String> map = (Map<String,String>)parent.getItemAtPosition(position);
                String s = map.get("address");
                if (bluetoothAdapter.isDiscovering()){
                    bluetoothAdapter.cancelDiscovery();
                }
                booleanPair(s);

            }
        });

    }
}
