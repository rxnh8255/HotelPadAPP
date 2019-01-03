package com.zhijia.hotelpad;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.zhijiaiot.app.signalr4j.ConnectionState;
import com.zhijiaiot.app.signalr4j.LogLevel;
import com.zhijiaiot.app.signalr4j.Logger;
import com.zhijiaiot.app.signalr4j.StateChangedCallback;
import com.zhijiaiot.app.signalr4j.hubs.HubConnection;
import com.zhijiaiot.app.signalr4j.hubs.HubProxy;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.zhijia.hotelpad.ZhiJiaUrl.HUB_URL;
import static com.zhijia.hotelpad.ZhijiaPreferenceUtil.HUB_NAME;


public class MainActivity extends AppCompatActivity {

    public static Context mainActivity ;
    private final static String TAG = "lcm";
    Button btnOut;

    QrPopDialog dialog;

    private HubProxy proxy;
    private HubConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mainActivity = this;
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //隐藏状态栏
        //定义全屏参数
        int flag= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);

        setContentView(R.layout.activity_main);

        btnOut = findViewById(R.id.btnOut);

        btnOut.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                ZhijiaPreferenceUtil.setAccessToken(MainActivity.this,"");
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        startMainActivity();

    }


    private void beginConnect() {

        if(connection != null){
            connection.stop();
            connection = null;
        }else{
            proxy = null;
            connection = null;

            connection = new HubConnection(HUB_URL, "", true, new Logger() {
                @Override
                public void log(String message, LogLevel level) {
                    //Log.i(TAG, "log: "+message);
                }
            } );

            proxy = connection.createHubProxy(HUB_NAME);
            proxy.subscribe(new Object(){
                public void onevent(String eventName, JsonElement arg2) {

                    if(!"online".equals(eventName)){
                        Log.i(TAG, "eventName: "+eventName+"\r\nmsg:"+arg2.toString());
                    }
                    try {
                        final JSONObject data = new JSONObject(arg2.toString());
                        //解析传回的数据
                        if ("updateHotelQR".equals(eventName)) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //qrcodemsg.setText("");
                                    //imageView.setVisibility(View.VISIBLE);

                                    //JSONObject data = him.getArgs().getJSONObject(1).getJSONObject("data");
                                    Log.i(TAG, "OnMessage: "+data.toString());
                                    JSONObject guestInfo = null;
                                    try {
                                        guestInfo = data.getJSONObject("data");
                                        String Qrcode = guestInfo.getString("Qrcode") == null?"meideerweima":guestInfo.getString("Qrcode");
                                        String room_num = guestInfo.getString("room_num");
                                        String guest_name = guestInfo.getString("guest_name");
                                        String guest_identification = guestInfo.getString("guest_identification");

                                        showQr(Qrcode,room_num,guest_name);

                                        //Bitmap qrBitmap = generateBitmap(Qrcode, 600, 600);
                                        //imageView.setImageBitmap(qrBitmap);
//                                        txtguest_id.setText("客人身份信息:"+guest_identification);
//                                        txtguest_name.setText("客人姓名:"+guest_name);
//                                        txtroom_num.setText("入住房号:"+room_num);
//                                        txtTip.setText("请使用微信扫码");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });

                        }else if("closeHotelQR".equals(eventName)){

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.hide();
                                }
                            });
//                            imageView.setVisibility(View.GONE);
//                            txtTip.setText("");
//                            txtguest_id.setText("");
//                            txtguest_name.setText("");
//                            txtroom_num.setText("");
//                            qrcodemsg.setText("等待客人入住");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    //multiResult.listResult.add(1);
                }
            });

            if("".equals(ZhijiaPreferenceUtil.getAccessToken(MainActivity.this)) && connection.getHeaders().containsKey("Authorization")){
                connection.getHeaders().remove("Authorization");
            } else if(!connection.getHeaders().containsKey("Authorization") || "".equals(connection.getHeaders().get("Authorization")))
                connection.addHeader("Authorization",ZhijiaPreferenceUtil.getAccessToken(MainActivity.this));


            connection.stateChanged(new StateChangedCallback(){

                @Override
                public void stateChanged(ConnectionState oldState, ConnectionState newState) {
                    Log.i(TAG, "ChangeState:"+oldState+" ==> "+newState);
                    if(newState == ConnectionState.Disconnected ||newState == ConnectionState.Reconnecting){
                        beginConnect();
                    }
                }
            });

            connection.start().isDone();
        }
    }

    private void showQr(String qrcode,String roomName,String tel){

        final QrPopDialog.Builder dialogBuild = new QrPopDialog.Builder(MainActivity.this);
        if(dialog == null) {
            dialog = dialogBuild.create(R.layout.dialog_share);
            //dialog.setCanceledOnTouchOutside(true);// 点击外部区域关闭
        }
        dialog.show();
        WindowManager.LayoutParams params =
                dialog.getWindow().getAttributes();
        params.width = 1040;
        params.height = 640;
        dialog.getWindow().setAttributes(params);

        TextView txtroom = dialog.findViewById(R.id.txtroom_num);
        txtroom.setText("房间号："+roomName);

        TextView txttel = dialog.findViewById(R.id.txt_tel);
        txttel.setText("客人姓名："+tel);
        //String qrurl = "https://zhijiaiot.com/h5/GenerateQr?str=https%3A%2F%2Fzhijiaiot.com%2Fapp.html%23wf-tvbox1%2C"+deviceId;
        ImageView iv = dialog.findViewById(R.id.img_qrcode);
        //mImageLoader.loadImage(qrurl,iv,true);
        Bitmap qrBitmap = generateBitmap(qrcode, 300, 300);
        iv.setImageBitmap(qrBitmap);
        iv.setVisibility(View.VISIBLE);

        dialog.findViewById(R.id.img_loading1).setVisibility(View.GONE);

    }

    private void startMainActivity() {

        //initConn();
        //判断本地是否登陆了,没登陆去扫码
        beginConnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private Bitmap generateBitmap(String content, int width, int height) {

        //Base64.decode(content,Base64.DEFAULT);
        Bitmap bitmap=null;
        try {
            byte[]bitmapArray;
            bitmapArray=Base64.decode(content, Base64.DEFAULT);
            //bitmapArray=Base64.decode("iVBORw0KGgoAAAANSUhEUgAAABoAAAAaCAMAAACelLz8AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAA3hpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuNi1jMTM4IDc5LjE1OTgyNCwgMjAxNi8wOS8xNC0wMTowOTowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD0ieG1wLmRpZDozZTMxOTRjYy0wMjE2LTQ1YjgtOTRkOS00OWM4MDdjMzk3MmYiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6OUJFRUYzN0E4M0U0MTFFN0ExMjRBOTJCQUZCMUFCMDgiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6OUJFRUYzNzk4M0U0MTFFN0ExMjRBOTJCQUZCMUFCMDgiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENDIDIwMTcgKE1hY2ludG9zaCkiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDo2NDRjYTRiNS02OGQ1LTQ4ZDMtYTYwNi1jZmU0ZTRlODNkNzUiIHN0UmVmOmRvY3VtZW50SUQ9InhtcC5kaWQ6M2UzMTk0Y2MtMDIxNi00NWI4LTk0ZDktNDljODA3YzM5NzJmIi8+IDwvcmRmOkRlc2NyaXB0aW9uPiA8L3JkZjpSREY+IDwveDp4bXBtZXRhPiA8P3hwYWNrZXQgZW5kPSJyIj8+nC+EaQAAAMZQTFRFUNLV////UdLVVNPW1PT1dtzeYdbZ+P39pujq3/f3ed3fl+Tmfd3g0/T0luTmft7gd9zeUtPVleTl+v7+rerrfN3grurset3fXtbYeNzf5Pj4YNbZ5fj58vz8U9PW8/z8a9nbuu3vue3ulOPl/f7/8fv8WtTX+f3+jOHj/v//7/v7XNXYnebncNrdZtja4/j4Y9fZ4ff42fX2sOvsoOfo3Pb39f397Pr64Pf4tuzuvO7vV9TXZdfa5vn5wu/wyfHy4vj4////Ov1i4gAAAEJ0Uk5T//////////////////////////////////////////////////////////////////////////////////////8ARBYQAAAAAPBJREFUeNqEkud2wjAMRj/ZiRMgCXuvttDF7J604Pd/KUgdJ04I9P6xju6xjyQLMmLQKgvORbk10Bmow2ojpm0Zql5iMGClulbjCjJUxko5No6wnT9VQw61UFksTzHroIrIpShRwAkKcDOZ5SxQgQsvbS6u6U1FTQgVbF54eKzeaXqnMgJcBWv6rQIPP3Q/ie5zrbZEl6zzSLRArKIHv32iz1eipCoRl3HzRAeek4KaSfHzD6IrYzKu0XJn91VNtXxmUKfG64efEuSp4L+vlMMjZw/12txm18Yxl62bmK6f2kMpew2vPxr1vUZPZ/YCDAAf0iaEu71SwgAAAABJRU5ErkJggg==", Base64.DEFAULT);
            bitmap= BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;

    }
}
