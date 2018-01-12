package mqtt.client.demo;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import layout.AboutUsFragment;
import layout.DeviceListFragment;
import layout.MqttLoginFragment;
import layout.MqttService;
import layout.SettingFragment;
import layout.ShareFragment;

import static android.R.attr.port;
import static mqtt.client.demo.R.id.login_edit_account;
import static mqtt.client.demo.R.id.login_edit_deviceid;
import static mqtt.client.demo.R.id.login_edit_pwd;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{
    // Toast对象
    private static Toast toast = null;

    /**
     * 显示Toast
     */
    public static void showText(Context context, String text) {
        if (toast == null) {
            toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        }
        toast.setText(text);
        toast.show();
    }
    private Integer nCount=0;
    private  NotificationManager manager; //通知控制类
    private void sendNotification(String title,String text) {
        //if (nCount>=1)return;
        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pintent = PendingIntent.getActivity(this,0,intent,0);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.head_portrait);//设置图标
        builder.setTicker("hello");//设置手机状态栏提示
        builder.setWhen(System.currentTimeMillis());//时间
        builder.setContentTitle(title);//标题
        builder.setContentText(text);//通知内容
        builder.setContentIntent(pintent);//点击后的意图
        builder.setDefaults(Notification.DEFAULT_ALL);//给通知设置震动，声音，和提示灯三种效果，不过要记得申请权限
        Notification notification = builder.build(); //4.1版本以上用这种方法
        //builder.getNotification();   //4.1版本以下用这种方法
        manager.notify(nCount,notification);
        nCount++;
    }
    public MqttService.MqttMessageCallBack MqttMessageCallback=new MqttService.MqttMessageCallBack() {

        @Override
        public void sendMqttMessageDataToActivity(String topic,byte[] message,Integer qos,Boolean retain) {
            // 这里的 data 参数便是从 Service 传递过来的数据
             TextView text=(TextView)findViewById(R.id.DeviceMessage);
            String str=new String(message);
            System.out.println("topic "+topic+" message "+str);

            sendNotification(topic,str);

            //showText(getApplicationContext(),"topic "+topic+" message "+str);
            //Toast.makeText(getApplicationContext(), "topic "+topic+" message "+str, Toast.LENGTH_SHORT).show();
        }
    };
    public MqttService.MqttMessageCallBack GetMqttMessageCallback(){
        return this.MqttMessageCallback;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        manager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        initFristFragment();


    }

    protected void initFristFragment(){
        String UserName ;
        String PassWd;
        String DeviceId;
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.PREFS), Context.MODE_PRIVATE);
        DeviceId	= sharedPref.getString(getString(R.string.DEVICEID), "");
        UserName = sharedPref.getString(getString(R.string.USERNAME), "");
        PassWd	= sharedPref.getString(getString(R.string.PASSWORD), "");

        if(UserName==""){//没有找到配置项，显示登录页面
            MqttLoginFragment fragment = new MqttLoginFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.main_layout, fragment).commit();
        }else{//有找到配置项，显示设备列表页面
/*
            MqttLoginFragment MqttLoginfragment = new MqttLoginFragment();
            String hostName,port, deviceId, userName, passwd;
            hostName="iot.iotman.club";
            port="1883";
            deviceId=DeviceId;
            userName=UserName;
            passwd=PassWd;
            MqttLoginfragment.mqtt_connect(hostName,port,deviceId,userName,passwd);
*/
            DeviceListFragment fragment = new DeviceListFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.main_layout, fragment).commit();
        }
    }
    //监听按键事件
    private long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
                return true;
            } else {
                if((System.currentTimeMillis()-exitTime) > 800){
                    Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    exitTime = System.currentTimeMillis();
                } else {
                    stopService(new Intent(MainActivity.this,MqttService.class));
                    finish();
                    System.exit(0);
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_login) {
            // Handle the login action
            MqttLoginFragment fragment = new MqttLoginFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment).commit();
        } else if (id == R.id.nav_device_list) {

            DeviceListFragment fragment = new DeviceListFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment).commit();
        //} else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_setting) {
            SettingFragment fragment = new SettingFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment).commit();
        } else if (id == R.id.nav_share) {
            ShareFragment fragment = new ShareFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment).commit();
        } else if (id == R.id.nav_about_us) {
            AboutUsFragment fragment = new AboutUsFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
