package com.yougowegoteam.demo;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.yougowegoteam.demo.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;



public class Fallpopup extends Activity {

    private TextToSpeech tts;
    private static PowerManager.WakeLock sCpuWakeLock;
    private String textMsg;
    private Handler handler;
    private String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS};

    private static final int PERMISSIONS_REQUEST_CODE = 1003;

    private static final String PERMISSION_CALL_PHONE = Manifest.permission.CALL_PHONE;
    private static final String PERMISSION_SEND_SMS = Manifest.permission.SEND_SMS;
    private View mLayout;

    NamePhone namePhone = new NamePhone();
    String name1, name2, name3, phone1, phone2, phone3, phoneNum;

    Button button0, button1, button2, button3, confirm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fallpopup);

        mLayout = findViewById(R.id.layout_main);

        handler = new Handler(getMainLooper());
        final Dialog dialog = new Dialog(this);
        // 사이즈조절
        // 1. 디스플레이 화면 사이즈 구하기
        Display dp = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        // 2. 화면 비율 설정
        int width = (int)(dp.getWidth()*1.0);
        int height = (int)(dp.getHeight()*1.0);
        // 3. 현재 화면에 적용
        getWindow().getAttributes().width = width;
        getWindow().getAttributes().height = height;

        // 액티비티 바깥화면이 클릭되어도 종료되지 않게 설정하기
        this.setFinishOnTouchOutside(false);

        // 꺼진 화면을 켜서 팝업을 보여줍니다.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        if (sCpuWakeLock != null) {
            return;
        }
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        sCpuWakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE, "AppName:tag");

        sCpuWakeLock.acquire();

        if (sCpuWakeLock != null) {
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }


        int hasCallPhonePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE);
        int hasSendSMSPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS);


        if (hasSendSMSPermission == PackageManager.PERMISSION_GRANTED &&
                hasCallPhonePermission == PackageManager.PERMISSION_GRANTED) {

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = getIntent();
                    try{
                        double latitude = intent.getExtras().getDouble("lastlat");
                        double longitude = intent.getExtras().getDouble("lastlon");
                        List<String> phoneList = new ArrayList<>();

                        phoneList.add(phone1);
                        phoneList.add(phone2);
                        phoneList.add(phone3);

                        for(String phoneNum: phoneList){

                            if (phoneNum != null &&
                                    ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_DENIED &&
                                    ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_DENIED) {
                                textMsg = "낙상 탐지: " + "http://maps.google.com/?q=" + String.valueOf(latitude) + "," + String.valueOf(longitude);
                                try {
                                    SmsManager sms = SmsManager.getDefault();
                                    sms.sendTextMessage(phoneNum, null, textMsg, null, null);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Log.i("Message", textMsg + "<" + phoneNum + ">");
                            }
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }, 10000);

        }else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            if (shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE) ||
            shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)) {

                Toast.makeText(Fallpopup.this, "낙상 서비스를 위해서 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);

            } else {

                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }


        // 번호와 이름을 버튼에 저장합니다.
        button0 = (Button)findViewById(R.id.응급);
        button1 = (Button)findViewById(R.id.번호설정1);
        button2 = (Button)findViewById(R.id.번호설정2);
        button3 = (Button)findViewById(R.id.번호설정3);
        confirm = findViewById(R.id.confirm);

        SharedPreferences sharedPreferences = getSharedPreferences("popup", MODE_PRIVATE);

        String intname1 = sharedPreferences.getString("name1", "");
        String intname2 = sharedPreferences.getString("name2", "");
        String intname3 = sharedPreferences.getString("name3", "");

        button1.setText(intname1);
        button2.setText(intname2);
        button3.setText(intname3);

        String intphone1 = sharedPreferences.getString("phone1", "");
        String intphone2 = sharedPreferences.getString("phone2", "");
        String intphone3 = sharedPreferences.getString("phone3", "");


        // 임시 저장 기능 SharedPreferences를 사용하여 저장된 값 불러오기

        if(intname1 == "" || intname1 == null) {name1 = intname1; phone1 = intphone1;
            Log.i("Fallpopup spname", "1"); }
        else {name1 = intname1; phone1 = intphone1;
            Log.i("Fallpopup spname", "2"); }

        if(intname2 == "" || intname2 == null) {name2 = intname2; phone2 = intphone2;
            Log.i("Fallpopup spname", "1");}
        else {name2 = intname2; phone2 = intphone2;
            Log.i("Fallpopup spname", "2");}

        if(intname3 == "" || intname3 == null) {name3 = intname3; phone3 = intphone3;}
        else {name3 = intname3; phone3 = intphone3;}

        namePhone.name1 = name1;
        namePhone.name2 = name2;
        namePhone.name3 = name3;
        namePhone.phone1 = phone1;
        namePhone.phone2 = phone2;
        namePhone.phone3 = phone3;


        button0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(hasPermission()){
                    phoneNum = "119";
                    Speech("119에 전화중 입니다.");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    callPhone();
                } else {
                    requestPermission();
                }
            }
        });

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(hasPermission()){
                    if (name1 == "" || phone1 == "") {
                        Speech("번호가 없습니다.");
                        return;
                    }
                    phoneNum = phone1;
                    Speech(name1 + "에게 전화중 입니다.");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    callPhone();
                } else {
                    requestPermission();
                }
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(hasPermission()){
                    if (name2 == "" || phone2 == "") {
                        Speech("번호가 없습니다.");
                        return;
                    }
                    phoneNum = phone2;
                    Speech(name2 + "에게 전화중 입니다.");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    callPhone();
                } else {
                    requestPermission();
                }
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasPermission()){
                    if (name3 == "" || phone3 == "") {
                        Speech("번호가 없습니다.");
                        return;
                    }
                    phoneNum = phone3;
                    Speech(name3 + "에게 전화중 입니다.");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    callPhone();
                } else {
                    requestPermission();
                }
            }
        });

        confirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                handler.removeCallbacksAndMessages(null);
                dialog.dismiss();
                finish();
            }
        });

        dialog.show();

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.KOREAN);
                    tts.setPitch(0.7f);
                    tts.setSpeechRate(1.2f);
                }
            }
        });



    }

    private void Speech(String text){
        String msg = text;
        tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
    }

    public class NamePhone{
        String name1 = "";
        String name2 = "";
        String name3 = "";

        String phone1 = "";
        String phone2 = "";
        String phone3 = "";
    }

    private void callPhone(){
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNum));
        startActivity(intent);
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(PERMISSION_CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(PERMISSION_CALL_PHONE)) {
                Toast.makeText(Fallpopup.this,
                        "낙상 서비스를 위해서 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[] {PERMISSION_SEND_SMS, PERMISSION_CALL_PHONE}, PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if ( check_result ) {
                callPhone();
            }
            else {
                Toast.makeText(Fallpopup.this, "낙상 서비스 권한이 승인되지 않았습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(PERMISSIONS_REQUEST_CODE, permissions, grandResults);
        }
    }

    @Override
    protected void onStop(){
        super.onStop();

        if(tts != null){
            tts.stop();
            tts.shutdown();
        }
    }






}
