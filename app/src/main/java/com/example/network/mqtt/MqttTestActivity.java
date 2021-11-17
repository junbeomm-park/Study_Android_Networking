package com.example.network.mqtt;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.network.R;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;




public class MqttTestActivity extends AppCompatActivity {
    private MqttAndroidClient mqttAndroidClient;
    String SERVER_URI =  "tcp://192.168.0.221:1883";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_control);
        // 2번째 파라메터 : 브로커의 ip 주소 , 3번째 파라메터 : client 의 id를 지정함 여기서는 paho 의 자동으로 id를 만들어주는것
        mqttAndroidClient =
                new MqttAndroidClient(this, "tcp://192.168.0.221" + ":1883",
                        MqttClient.generateClientId());
        //mqtttoken 이라는것을 만들어 connect option을 달아줌
        IMqttToken token = null;
        try {
            token = mqttAndroidClient.connect(getMqttConnectionOption());
        } catch (MqttException e) {
            e.printStackTrace();
        }

        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                mqttAndroidClient.setBufferOpts(getDisconnectedBufferOptions());    //연결에 성공한경우
                Log.e("Connect_success", "Success");
                try {
                    mqttAndroidClient.subscribe("led", 0);   //연결에 성공하면 jmlee 라는 토픽으로 subscribe함
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {   //연결에 실패한경우
                Log.e("connect_fail", "Failure " + exception.toString());
            }
        });
        mqttAndroidClient.setCallback(new MqttCallback() {  //클라이언트의 콜백을 처리하는부분
          @Override
           public void connectionLost(Throwable cause) {
          }
            //모든 메시지가 올때 Callback method
          @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if (topic.equals("jmlee")){     //topic 별로 분기처리하여 작업을 수행할수도있음
                    String msg = new String(message.getPayload());

                    Log.e("led", msg);

                }

            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
    }
    public void  send_msg(final View view) {
        String message = "";
        if (view.getId() == R.id.led_on) {
            message = "led_on";
        } else {
            message = "led_off";
        }
        try {
            publish("led",message,0);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public void publish(String topic,String payload, int qos) throws MqttException {
        if(mqttAndroidClient.isConnected() == false) {
            mqttAndroidClient.connect();
        }
        MqttMessage message = new  MqttMessage();
        message.setPayload(payload.getBytes());
        message.setQos(qos);
        mqttAndroidClient.publish(topic, message, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.i("mymqtt", "publish succeed! ");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.i("mymqtt", "publish failed!");
            }
        });
    }
    private DisconnectedBufferOptions getDisconnectedBufferOptions() {
        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(true);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
        return disconnectedBufferOptions;
    }



    private MqttConnectOptions getMqttConnectionOption() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
       mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setWill("aaa", "I am going offline".getBytes(), 1, true);
        return mqttConnectOptions;
    }
}