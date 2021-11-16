package com.example.network.arduino;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.network.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class LedControlActivity extends AppCompatActivity {
    BufferedReader serverIn; // 서버에서 보내오는 메시지 읽기 위한 스트림
    PrintWriter serverOut; // 서버로 메시지를 보내기 위한 스트림
    Socket server;
    Handler handler;
    TextView view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_control);
        view = findViewById(R.id.showdata);
        new LedThread().start();
        handler = new Handler(Looper.myLooper());
    }
    //버튼을 누를때 호출되는 메소드
    public void send_msg(View view){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String message = "";
                if(view.getId()==R.id.led_on){
                    message = "led_on";
                }else if(view.getId()==R.id.led_off){
                    message = "led_off";
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
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        view.append(msg+"\n");
                                    }
                                });
                                Log.d("network","서버로부터 수신된 메시지>>>"+msg);

                            } catch (IOException e) {
                                //서버에서 연결이 끊어지는 경우 사용자는 자원을 반납
                                try {
                                    serverIn.close();
                                    serverOut.close();
                                    server.close();

                                } catch (IOException ioException) {
                                    ioException.printStackTrace();
                                }
                                break; //서버에서 연결이 끊어지거나 오류가 발생되면 무한루프를 빠져나와 쓰레드를 종료할 수 있도록 처리
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