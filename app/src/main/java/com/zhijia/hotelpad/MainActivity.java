package com.zhijia.hotelpad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.zsoft.signala.ConnectionState;
import com.zsoft.signala.hubs.HubConnection;
import com.zsoft.signala.hubs.HubInvocationMessage;
import com.zsoft.signala.hubs.HubInvokeCallback;
import com.zsoft.signala.hubs.IHubProxy;
import com.zsoft.signala.transport.StateBase;
import com.zsoft.signala.transport.longpolling.DisconnectedState;
import com.zsoft.signala.transport.longpolling.LongPollingTransport;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends Activity {

    public static Context mainActivity ;
    private final static String TAG = "lcm";
    ImageView imageView;
    TextView qrcodemsg;
    Button logButton;
    Button btnOut;
    TextView txtroom_num;
    TextView txtguest_name;
    TextView txtguest_id;
    TextView txtTip;


    private HubConnection conn;

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

        imageView = findViewById(R.id.qrcodeimg);
        qrcodemsg = findViewById(R.id.qrcodemsg);
        txtguest_id = findViewById(R.id.txtguest_id);
        txtguest_name = findViewById(R.id.txtguest_name);
        txtroom_num = findViewById(R.id.txtroom_num);
        logButton = findViewById(R.id.logButton);
        btnOut = findViewById(R.id.btnOut);
        txtTip = findViewById(R.id.txtTip);

        qrcodemsg.setText("等待客人入住");

        logButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                startMainActivity();
            }
        });

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

        try {
            if(conn.getCurrentState().getState() == ConnectionState.Disconnected){
                String token = ZhijiaPreferenceUtil.getAccessToken(MainActivity.this);
                conn.addHeader("Authorization",token);
                conn.CreateHubProxy("messagehub");
            }
            conn.Start();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    private void startMainActivity() {

        initConn();
        //判断本地是否登陆了,没登陆去扫码
        conn.SetNewState(new DisconnectedState(conn));

        beginConnect();
    }

    private void initConn(){
         conn = new HubConnection(ZhiJiaUrl.HUB_URL, this, new LongPollingTransport()) {
            @Override
            public void OnError(Exception exception) {
                Log.d(TAG, "OnError=" + exception.getMessage());
            }
            @Override
            public void OnMessage(String message) {
                Log.d(TAG, "message=" + message);
                try {
                    JSONObject jo = new JSONObject(message);
                    HubInvocationMessage him = new HubInvocationMessage(jo);
                    String eventName = him.getArgs().getString(0);

                    switch (eventName)
                    {
                        case "updateHotelQR":
                            qrcodemsg.setText("");
                            imageView.setVisibility(View.VISIBLE);

                            JSONObject data = him.getArgs().getJSONObject(1).getJSONObject("data");
                            Log.i(TAG, "OnMessage: "+data.toString());

                            String Qrcode = data.getString("Qrcode") == null?"meideerweima":data.getString("Qrcode");
                            String room_num = data.getString("room_num");
                            String guest_name = data.getString("guest_name");
                            String guest_identification = data.getString("guest_identification");

                            Bitmap qrBitmap = generateBitmap(Qrcode, 600, 600);
                            imageView.setImageBitmap(qrBitmap);
                            txtguest_id.setText("客人身份信息:"+guest_identification);
                            txtguest_name.setText("客人姓名:"+guest_name);
                            txtroom_num.setText("入住房号:"+room_num);
                            txtTip.setText("请使用微信扫码");

                            break;
                        case "closeHotelQR":
                            imageView.setVisibility(View.GONE);
                            txtTip.setText("");
                            txtguest_id.setText("");
                            txtguest_name.setText("");
                            txtroom_num.setText("");
                            qrcodemsg.setText("等待客人入住");
                            break;
                        default:
                            Log.i(TAG, "wodeeventName: "+message);
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void OnStateChanged(StateBase oldState, StateBase newState) {

                if(newState.getState() == ConnectionState.Connected){
                    //连接上
                    qrcodemsg.setText("");
                }else if(newState.getState() == ConnectionState.Disconnected){
                    qrcodemsg.setText("网络断开,请刷新");
                }

                Log.d(TAG, "OnStateChanged=" + oldState.getState() + " -> " + newState.getState());
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (conn != null)
            conn.Stop();
    }

    private Bitmap generateBitmap(String content, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, String> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        try {
            BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (encode.get(j, i)) {
                        pixels[i * width + j] = 0x00000000;
                    } else {
                        pixels[i * width + j] = 0xffffffff;
                    }
                }
            }
            return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }
}
