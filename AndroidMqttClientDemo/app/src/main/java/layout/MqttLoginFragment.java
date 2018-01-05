package layout;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import mqtt.client.demo.MainActivity;
import mqtt.client.demo.R;

import static android.content.Context.BIND_AUTO_CREATE;

public class MqttLoginFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mqtt_login, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Button btn_login = (Button) getActivity().findViewById(R.id.login_btn_login);
        btn_login.setOnClickListener(this);
    }


    protected void test(){
        EditText login_edit_deviceid = (EditText) getActivity().findViewById(R.id.login_edit_deviceid);//xiaohui
        EditText login_edit_account = (EditText) getActivity().findViewById(R.id.login_edit_account);//xiaohuihui
        EditText login_edit_pwd = (EditText) getActivity().findViewById(R.id.login_edit_pwd);//xiaohuihui
        CheckBox Login_Remember = (CheckBox) getActivity().findViewById(R.id.Login_Remember);
        login_edit_deviceid.setText("xiaohui");
        login_edit_account.setText("xiaohuihui");
        login_edit_pwd.setText("xiaohuihui");
        Login_Remember.setChecked(true);
    }
    public void onClick(View view) {
        String hostName,port, deviceId, userName, passwd;
        switch (view.getId()){
            case R.id.login_btn_login:
                //topic iotman/1512226921
                test();
                EditText login_edit_deviceid = (EditText) getActivity().findViewById(R.id.login_edit_deviceid);//xiaohui
                EditText login_edit_account = (EditText) getActivity().findViewById(R.id.login_edit_account);//xiaohuihui
                EditText login_edit_pwd = (EditText) getActivity().findViewById(R.id.login_edit_pwd);//xiaohuihui
                CheckBox Login_Remember = (CheckBox) getActivity().findViewById(R.id.Login_Remember);
                if (login_edit_deviceid.getText().toString().length() > 0 &&
                        login_edit_account.getText().toString().length() > 0 &&
                        login_edit_pwd.getText().toString().length() > 0 ) {
                            if (Login_Remember.isChecked()){//记住密码被选中
                                final SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.PREFS), Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString(getString(R.string.DEVICEID), login_edit_deviceid.getText().toString());
                                editor.putString(getString(R.string.USERNAME), login_edit_account.getText().toString());
                                editor.putString(getString(R.string.PASSWORD), login_edit_pwd.getText().toString());
                                editor.commit();
                            }
                    hostName="iot.iotman.club";
                    port="1883";
                    deviceId=login_edit_deviceid.getText().toString();
                    userName=login_edit_account.getText().toString();
                    passwd=login_edit_pwd.getText().toString();
                    mqtt_connect(hostName,port,deviceId,userName,passwd);
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), "请输入账户信息", Toast.LENGTH_SHORT).show();
                }

                break;
            default:
                break;
        }
    }
    private MqttService.Binder binder = null;
    private  String OverallHostName, OverallPort, OverallDeviceId, OverallUserName, OverallPasswd;
    // 服务连接时，调用该方法，其中 IBinder 对象，就是上面 onBind() 方法中返回的对象
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            //return super.onServiceDisconnected(name);
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (MqttService.Binder) service;
            Toast.makeText(getActivity().getApplicationContext(), "start1", Toast.LENGTH_SHORT).show();
            if (binder != null) {
                Toast.makeText(getActivity().getApplicationContext(), "start", Toast.LENGTH_SHORT).show();
                binder.GetService().setMqttMessageCallBack(((MainActivity)getActivity()).GetMqttMessageCallback());
                binder.ServiceMqttConnect(OverallHostName,OverallPort,OverallDeviceId,OverallUserName,OverallPasswd,30,30,true);
                binder.ServiceMqttSubcribe("iotman/1512226921",2);
                DeviceListFragment fragment = new DeviceListFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment).commit();
            }
        }
    };
    public void mqtt_connect(String HostName,String Port,String DeviceId,String UserName,String Passwd){
        //Intent i = new Intent(getActivity(), MqttService.class);
        //i.putExtra(HostName,HostName);
        //i.putExtra(Port,Port);
        //i.putExtra(DeviceId,DeviceId);
        //i.putExtra(UserName,UserName);
        //i.putExtra(Passwd,UserName);
        //getActivity().startService(i);
        OverallHostName=HostName;
        OverallPort=Port;
        OverallDeviceId=DeviceId;
        OverallUserName=UserName;
        OverallPasswd=Passwd;
        Toast.makeText(getActivity().getApplicationContext(), "start0", Toast.LENGTH_SHORT).show();
        // 新建一个 Intent
        Intent intent = new Intent(getActivity(), MqttService.class);
        // 开启服务
        getActivity().bindService(intent,connection,BIND_AUTO_CREATE);
        //stopService(new Intent(getActivity(), MqttService.class));
    }

}
