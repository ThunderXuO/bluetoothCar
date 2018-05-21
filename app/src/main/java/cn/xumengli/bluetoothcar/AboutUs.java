package cn.xumengli.bluetoothcar;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.AdapterView;
import android.widget.Button;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AboutUs extends Activity implements View.OnTouchListener {
    private Button backWard;
    private String[] name = {"APP名称","当前版本","软件开发者","开发者邮箱","开发者网站","Github地址"};
    private String[] address = {"蓝牙汽车","1.0","magbone","xuhongyu@xumengli.cn","https://magbone.in","暂无"};

    private ListView listView;
    private List<Map<String,String>> list = new ArrayList<>();
    private ProgressDialog progressDialog;
    //重写返回键
    public boolean onKeyDown(int KeyCode, KeyEvent event){
        if(KeyCode == KeyEvent.KEYCODE_BACK){
            Intent intent = new Intent(AboutUs.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onKeyDown(KeyCode,event);
    }
    public boolean onTouch(View v , MotionEvent event){
        Toast.makeText(this,"touch event",Toast.LENGTH_SHORT).show();
        return false;
    }
    //跳转网页
    public void jumpToWeb(String url){
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
        startActivity(intent);
    }
    //跳转邮箱
    public void jumpToMail(String mail){
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse(mail));
        startActivity(intent);
    }
    public void getUpdate(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("提示");
        progressDialog.setMessage("正在获取可用的更新...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.cancel();
            }
        });
        progressDialog.show();
    }
    protected void onCreate(Bundle savedInstanceStat){
        super.onCreate(savedInstanceStat);
        setContentView(R.layout.about_us_all);

        backWard = (Button)findViewById(R.id.backward);
        backWard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AboutUs.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        listView = (ListView)findViewById(R.id.developer_info);
        for (int i = 0;i < name.length;i++){
            Map<String,String> map = new HashMap<>();
            map.put("name",name[i]);
            map.put("address",address[i]);
            list.add(map);
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(this,list,R.layout.about_us_listview,new String[]{"name","address"},new int[]{R.id.about_text1,R.id.about_text2});
        listView.setAdapter(simpleAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String,String>  map = (Map<String,String>)parent.getItemAtPosition(position);
                String s = map.get("name");
                switch (s){
                    case "开发者网站":
                        jumpToWeb("https://magbone.in");
                        break;
                    case "开发者邮箱":
                        jumpToMail("mailto:xuhongyu@xumengli.cn");
                        break;
                    case "当前版本":
                        getUpdate();
                        break;
                }
            }
        });

    }

}
