package com.test.gogo.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.NetworkOnMainThreadException;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;


public class MainActivity extends ActionBarActivity {

    String ip = "58.127.223.63";

    int port = 2015;


    static String idrandommaker() {
        Random r = new Random();
        String str = "";

        for (int i = 0; i < 10; i++) {
            char c = (char) (r.nextInt(26) + 65);
            str += c;
        }

        return str;
    }

    String id = idrandommaker();


    ScrollView scrollView;

    TextView textView2;
    EditText editText;
    //Button button;

    public Socket socket; // 연결소켓
    public InputStream is;
    public OutputStream os;
    public DataInputStream dis;
    public DataOutputStream dos;


    @Override
    protected void onCreate(Bundle savedInstanceState) throws NetworkOnMainThreadException {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        final Button button = (Button) findViewById(R.id.button);
        final Button button2 = (Button) findViewById(R.id.button2);
        editText = (EditText) findViewById(R.id.editText);
        textView2 = (TextView) findViewById(R.id.textView2);
        scrollView = (ScrollView) findViewById(R.id.scrView);

        textView2.setMaxLines(100);
        textView2.setVerticalScrollBarEnabled(true);
        textView2.setMovementMethod(new ScrollingMovementMethod());

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (!editText.getText().toString().equals(""))
                {
                    send_Message(editText.getText().toString());
                    textView2.append(id + " : " + editText.getText().toString() + "\n");
                    editText.setText("");

                    /*scrollView.post(new Runnable() {
                        public void run() {
                            // TODO Auto-generated method stub
                            scrollView.scrollTo(0, textView2.getHeight());
                        }
                    });*/
                }
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
               send_Message("passwd4321");

               //나중에 주석 해제할부분
               //button.setEnabled(false);
            }
        });


        try {
            socket = new Socket(ip, port);
            if (socket != null) // socket이 null값이 아닐때 즉! 연결되었을때
            {
                try {
                    is = socket.getInputStream();
                    dis = new DataInputStream(is);

                    os = socket.getOutputStream();
                    dos = new DataOutputStream(os);
                    send_Message(id);
                } catch (IOException e) {
                    textView2.append("스트림 설정 에러!!\n");
                }
            }
        } catch (UnknownHostException e) {
            textView2.append("안노운 호스트 예외 발생");

        } catch (IOException e) {
            textView2.append("소켓 접속 에러!!\n");
        }


        ReplyThread thread = new ReplyThread();
        thread.setDaemon(true);
        thread.start();
        scrollView = (ScrollView) findViewById(R.id.scrView);
        scrollView.setVerticalScrollBarEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void send_Message(String str) { // 서버로 메세지를 보내는 메소드
        try {
            dos.writeUTF(str);
        } catch (IOException e) {
            System.exit(0);
        }
    }


    class ReplyThread extends Thread {
        Handler mHandler = new Handler();

        @Override
        public void run() {

            while (true) try {
                final String msg = dis.readUTF(); // 메세지를 수신한다

                mHandler.post(new Runnable() {
                    public void run() {
                        textView2.append(msg + "\n");
                    }
                });

/*                if (msg.equals("대화상대를 찾았습니다."))
                {
                    // 메시지보내기버튼 활성화
                    mHandler2.sendEmptyMessage((0));
                }*/


            } catch (IOException e) {


                mHandler.post(new Runnable() {
                    public void run() {
                        textView2.append("메세지 수신 에러!!\n");
                    }
                });


                // 서버와 소켓 통신에 문제가 생겼을 경우 소켓을 닫는다
                try {
                    os.close();
                    is.close();
                    dos.close();
                    dis.close();
                    socket.close();
                    break; // 에러 발생하면 while문 종료
                } catch (IOException e1) {
                    e.printStackTrace();
                }

            }

            // while문 끝
        }// run메소드 끝




    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                new AlertDialog.Builder(this).setTitle("종료").setMessage("종료하시겠어요 ?").setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        send_Message("password1234");
                        finish();
                    }
                }).setNegativeButton("아니오", null).show();
                return false;
            default:
                return false;
        }
    }
}


        /*// 서버에 접속
        try {
            socket = new Socket(ip, port);
            if (socket != null) // socket이 null값이 아닐때 즉! 연결되었을때
            {
                // 실직 적인 메소드 연결부분
                try { // 스트림 설정
                    is = socket.getInputStream();
                    dis = new DataInputStream(is);

                    os = socket.getOutputStream();
                    dos = new DataOutputStream(os);

                } catch (IOException e) {
                    tv.append("스트림 설정 에러!!\n");
                }
                send_Message(id); // 정상적으로 연결되면 나의 id를 전송

                Thread th = new Thread(new Runnable() { // 스레드를 돌려서 서버로부터 메세지를 수신

                    @Override
                    public void run() {

                        while (true) {

                            try {
                                String msg = dis.readUTF(); // 메세지를 수신한다
                                tv.append(msg + "\n");
                            } catch (IOException e) {
                                tv.append("메세지 수신 에러!!\n");
                                // 서버와 소켓 통신에 문제가 생겼을 경우 소켓을 닫는다
                                try {
                                    os.close();
                                    is.close();
                                    dos.close();
                                    dis.close();
                                    socket.close();
                                    break; // 에러 발생하면 while문 종료
                                } catch (IOException e1) {

                                }

                            }
                        } // while문 끝

                    }// run메소드 끝
                });

                th.start();
            }
        } catch (UnknownHostException e) {

        } catch (IOException e) {
            tv.append("소켓 접속 에러!!\n");
        }*/

