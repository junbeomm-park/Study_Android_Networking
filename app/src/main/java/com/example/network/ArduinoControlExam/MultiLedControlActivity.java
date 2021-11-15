package com.example.network.ArduinoControlExam;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.network.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MultiLedControlActivity extends AppCompatActivity {
    BufferedReader serverIn; // 서버에서 보내오는 메시지 읽기 위한 스트림
    PrintWriter serverOut; // 서버로 메시지를 보내기 위한 스트림
    Socket server;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiled_control);
        new LedThread().start();
    }
    //버튼을 누를때 호출되는 메소드
    public void send_msg(View view){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String message = "";

                if(view.getId()==R.id.led_on1){
                    message = "led_on1";
                }else if(view.getId()==R.id.led_off1) {
                    message = "led_off1";
                }

                else if(view.getId()==R.id.led_on2){
                    message = "led_on2";
                }else if(view.getId()==R.id.led_off2){
                    message = "led_off2";
                }

                else if(view.getId()==R.id.led_on3){
                    message = "led_on3";
                }else if(view.getId()==R.id.led_off3){
                    message = "led_off3";
                }
                serverOut.println(message); //서버로 메시지 보내기

            }
        }).start();

    }
    class LedThread extends Thread{
        public void run() {
            try {
                server = new Socket("192.168.0.15",12345);
                if(server!=null) {
                    io_init();
                }
                //서버가 보내오는 메시지를 읽기 위한 쓰레드
                Thread t1 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(true){
                            String msg;
                            try {
                                msg = serverIn.readLine();
                                Log.d("network","서버로부터 수신된 메시지>>>"+msg);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                t1.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        void io_init(){
            try{
            serverIn = new BufferedReader(
                        new InputStreamReader(server.getInputStream()));
            serverOut = new PrintWriter(server.getOutputStream(),true);

            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}