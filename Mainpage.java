package com.yougowegoteam.demo;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.yougowegoteam.demo.R;

import java.util.Locale;

public class Mainpage extends Activity {

    private View mLayout;
    private final int My_PERMISSION_REQUEST_CALL_PHONE = 1001;
    private TextToSpeech tts;

    private String [] REQUIRED_PERMISSIONS = {Manifest.permission.CALL_PHONE,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.SEND_SMS};
    int PERMISSIONS_REQUEST_CODE = 1004;

    static boolean fallingButtonState;
    static boolean nearButtonState;
    private boolean vibeButtonState;
    private boolean soundButtonState;

    Intent foregroundGPSServiceInetent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainpage);

        mLayout = findViewById(R.id.layout_main);

        // 절전모드 해제: 2~5분 마다 서비스가 다시 실행되는 문제 해결
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
        boolean isWhiteListing = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            isWhiteListing = pm.isIgnoringBatteryOptimizations(getApplicationContext().getPackageName());
        }
        if (!isWhiteListing) {
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
            startActivity(intent);
        }

        // 권한 확인, 요청
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CALL_PHONE,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS
        }, My_PERMISSION_REQUEST_CALL_PHONE);

        // 버튼을 누르면 다른 페이지로 이동
        ImageButton buttonCamera = (ImageButton) findViewById(R.id.카메라);
        ImageButton buttonAs = (ImageButton) findViewById(R.id.사후조치);
        ImageButton buttonBollardMap = (ImageButton) findViewById(R.id.bollardMap);
        ImageButton buttonSetting = (ImageButton) findViewById(R.id.setting);

        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Speech("볼라드 감지를 시작합니다.");

                int hasCallPhonePermission = ContextCompat.checkSelfPermission(Mainpage.this,
                        Manifest.permission.CALL_PHONE);
                int hasSendSMSPermission = ContextCompat.checkSelfPermission(Mainpage.this,
                        Manifest.permission.SEND_SMS);
                int hasFineLocationPermission = ContextCompat.checkSelfPermission(Mainpage.this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(Mainpage.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);
                int hasCameraPermission = ContextCompat.checkSelfPermission(Mainpage.this,
                        Manifest.permission.CAMERA);
                int hasWriteExternalStorage = ContextCompat.checkSelfPermission(Mainpage.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);


                SharedPreferences buttonSaver = getSharedPreferences("sFile",MODE_PRIVATE);

                fallingButtonState = buttonSaver.getBoolean("falling", true);
                nearButtonState = buttonSaver.getBoolean("near", true);

                if (hasSendSMSPermission == PackageManager.PERMISSION_GRANTED &&
                        hasCallPhonePermission == PackageManager.PERMISSION_GRANTED &&
                        hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                        hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED &&
                        hasCameraPermission == PackageManager.PERMISSION_GRANTED &&
                        hasWriteExternalStorage == PackageManager.PERMISSION_GRANTED) {

                    if(fallingButtonState) {
                        Log.d("MainPage", "fallingDetector called");
                        Intent fallingIntent = new Intent(getApplicationContext(), fallingDetector.class);
                        startService(fallingIntent);
                    }

                    if(nearButtonState) {
                        Log.d("MainPage", "nearDetector called");
                        Intent GPSServiceIntent = new Intent(getApplicationContext(), GPSService.class);
                        startService(GPSServiceIntent);
                    }

                    Intent bollardDetector =  new Intent(getApplicationContext(), DetectorActivity.class);
                    startService(bollardDetector);

                    Intent intent = new Intent(getApplicationContext(), DetectorActivity.class);
                    startActivity(intent);

                }else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

                    if (ActivityCompat.shouldShowRequestPermissionRationale(Mainpage.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(Mainpage.this, Manifest.permission.CAMERA) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(Mainpage.this, Manifest.permission.SEND_SMS) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(Mainpage.this, Manifest.permission.CALL_PHONE) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(Mainpage.this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(Mainpage.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {

                        // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.

                        Toast.makeText(Mainpage.this, "이 앱을 실행하려면 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(Mainpage.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);

                    } else {
                        ActivityCompat.requestPermissions(Mainpage.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                    }
                }

            }
        });

        buttonAs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Speech("사후조치 전화번호 입력을 하십시오.");
                Intent intent = new Intent(getApplicationContext(), As.class);
                startActivity(intent);
            }
        });

        buttonBollardMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Speech("볼라드 지도로 이동합니다.");
                Intent bollardMap = new Intent(getApplicationContext(), BollardMap.class);
                startActivity(bollardMap);
            }
        });

        buttonSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Speech("시스템 설정으로 이동합니다.");
                Intent settingIntent = new Intent(getApplicationContext(), Setting.class);
                startActivity(settingIntent);
            }
        });



        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.KOREAN);
                    tts.setPitch(0.7f);
                    tts.setSpeechRate(1.4f);
                }
            }
        });

    }

    private void Speech(String text){
        String string = text;
        tts.speak(string, TextToSpeech.QUEUE_FLUSH, null);
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

                SharedPreferences buttonSaver = getSharedPreferences("sFile",MODE_PRIVATE);

                fallingButtonState = buttonSaver.getBoolean("falling", true);
                nearButtonState = buttonSaver.getBoolean("near", true);

                if(fallingButtonState) {
                    Log.d("MainPage", "fallingDetector called");
                    Intent fallingIntent = new Intent(getApplicationContext(), fallingDetector.class);
                    startService(fallingIntent);
                }

                if(nearButtonState) {
                    Log.d("MainPage", "nearDetector called");
                    Intent GPSServiceIntent = new Intent(getApplicationContext(), GPSService.class);
                    startService(GPSServiceIntent);
                }

                Intent bollardDetector =  new Intent(getApplicationContext(), DetectorActivity.class);
                startService(bollardDetector);

                Intent intent = new Intent(getApplicationContext(), DetectorActivity.class);
                startActivity(intent);
            }
            else {
                Toast.makeText(Mainpage.this, "권한이 승인되지 않았습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(PERMISSIONS_REQUEST_CODE, permissions, grandResults);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Mainpage", "onDestroy() called");
        // TTS 객체가 남아있다면 실행을 중지하고 메모리에서 제거한다.

        if (null != foregroundGPSServiceInetent) {
            stopService(foregroundGPSServiceInetent);
            foregroundGPSServiceInetent = null;
        }


        if(tts != null){
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }
}
