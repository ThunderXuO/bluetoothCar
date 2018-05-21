package cn.xumengli.bluetoothcar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import cn.xumengli.bluetoothcar.service.BluetoothService;
public class StartActivity extends Activity {
    private final int SPLASH_DISPLAY_LENGHT = 3000;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
        Intent intent = new Intent(StartActivity.this,BluetoothService.class);
        startService(intent);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(StartActivity.this,MainActivity.class);
                StartActivity.this.startActivity(mainIntent);
                StartActivity.this.finish();

            }
        },SPLASH_DISPLAY_LENGHT);
    }
}
