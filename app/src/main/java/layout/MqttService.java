package layout;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.util.Locale;

import static android.content.ContentValues.TAG;

public class MqttService extends Service {
    public MqttService() {
    }
    public void onCreate() {

        super.onCreate();
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        // Android 官方提供的 Binder 对象，不过为了实现 Service 和 Activity 互相通信，之后我们要自己重新实现一个类
        return new Binder();
    }
    public String data;
    public MqttClient MqttClientObj;
    MqttConnectOptions MqttOpts;
    private MemoryPersistence MqttMemStore;
    private Handler MqttServerThreadConnHandler;
    private MqttMessageCallBack MqttMessagecallback = null;
    public class Binder extends android.os.Binder {
        public void setServerInfo(String d) {
            // data 为 Service 类的属性，在内部类中可以很方便的访问到外部的属性
            data = d;
        }
        // MQTT是否连接成功
        private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

            @Override
            public void onSuccess(IMqttToken arg0) {
                Log.i(TAG, "连接成功 ");

                try {
                    // 订阅myTopic话题
                    MqttClientObj.subscribe("iotman/1512226921",1);
                } catch (MqttException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(IMqttToken arg0, Throwable arg1) {
                arg1.printStackTrace();
                // 连接失败，重连
            }
        };

        // MQTT监听并且接受消息
        private MqttCallback mqttCallback = new MqttCallback() {

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //String str1 = new String(message.getPayload());
                //String str2 = topic + ";qos:" + message.getQos() + ";retained:" + message.isRetained();
                MqttMessagecallback.sendMqttMessageDataToActivity(topic,message.getPayload(),message.getQos(),message.isRetained());

                //Log.i(TAG, "messageArrived:" + str1);
                //Log.i(TAG, str2);
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken arg0) {

            }
            @Override
            public void connectionLost(Throwable arg0) {
                // 失去连接，重连
            }
        };
        public void ServiceMqttConnect(String HostName,String Port,String DeviceId,String UserName,String Passwd,Integer ConnectionTimeout,Integer KeepAliveInterval,Boolean MqttCleanSession){
            String MqttUrlFormat;
            if (Port.equals("1883")) {
                MqttUrlFormat = "tcp://%s:%s";
            } else {
                MqttUrlFormat = "ssl://%s:%s";
            }
            String MqttUrl = String.format(Locale.US, MqttUrlFormat, HostName, Port);
            try {
                ProviderInstaller.installIfNeeded(getApplicationContext());//更新你的Security Provider来对抗SSL漏洞利用
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }

            String MqttThreadName="MqttServerThread";
            HandlerThread MqttThread = new HandlerThread(MqttThreadName);
            MqttThread.start();

            MqttServerThreadConnHandler = new Handler(MqttThread.getLooper());
            MqttDefaultFilePersistence MqttDataStore = new MqttDefaultFilePersistence(getCacheDir().getAbsolutePath());

            MqttOpts = new MqttConnectOptions();
            // 设置超时时间，单位：秒
            MqttOpts.setConnectionTimeout(ConnectionTimeout);
            // 心跳包发送间隔，单位：秒
            MqttOpts.setKeepAliveInterval(KeepAliveInterval);

            MqttOpts.setCleanSession(MqttCleanSession);

            if (UserName != null && !UserName.isEmpty()) {
                MqttOpts.setUserName(UserName);
            }

            if (Passwd != null && !Passwd.isEmpty()) {
                MqttOpts.setPassword(Passwd.toCharArray());
            }

            AlarmManager MqttAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            ConnectivityManager MqttConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

            try {
                if(MqttDataStore != null) {
                    MqttClientObj = new MqttClient(MqttUrl,DeviceId,MqttDataStore);
                } else {
                    MqttClientObj = new MqttClient(MqttUrl,DeviceId,MqttMemStore);
                }
            } catch(MqttException e) {
                e.printStackTrace();
            }
            try {
                if (MqttClientObj != null && MqttClientObj.isConnected())
                    return;
                MqttClientObj.connect(MqttOpts);
                System.out.println("Connected");
                MqttClientObj.setCallback(mqttCallback);
                //connected

                MqttStarted = true;
                //startKeepAlives();
                MqttIsReconnecting = false;
            }catch (Exception e) {
                    e.printStackTrace();
                    //forceReconnect();
            }
        }
        Boolean MqttStarted;
        Boolean MqttIsReconnecting;
        String SubTopic;
        Integer SubQos;
        public void ServiceMqttSubcribe(String Topic,Integer Qos){
            SubTopic=Topic;
            SubQos=Qos;
            MqttServerThreadConnHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (MqttClientObj == null && !MqttClientObj.isConnected())
                            return;

                        String[] topics = SubTopic.split(",");

                        for (String topic : topics) {
                            MqttClientObj.subscribe(topic,SubQos);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        //forceReconnect();
                    }
                }
            });
        }
        public boolean isConnected() {
            if (MqttStarted && MqttClientObj != null && !MqttClientObj.isConnected()) {}

            if (MqttClientObj != null) {
                return (MqttStarted && MqttClientObj.isConnected()) ? true : false;
            }

            return false;
        }
        public MqttService GetService() {
            return MqttService.this;
        }

    }
    public static interface MqttMessageCallBack {
        void sendMqttMessageDataToActivity(String topic,byte[] message,Integer qos,Boolean retain);
    }

    public void setMqttMessageCallBack(MqttMessageCallBack callBack) {
        this.MqttMessagecallback = callBack;
    }

    public MqttMessageCallBack getMqttMessageCallBack() {
        return MqttMessagecallback;
    }
}
