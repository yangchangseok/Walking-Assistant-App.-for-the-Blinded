package com.yougowegoteam.demo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.yougowegoteam.demo.R;

import java.util.Locale;

public class Setting extends Activity {

    static boolean fallingButtonState;
    static boolean nearButtonState;
    private boolean vibeButtonState;
    private boolean soundButtonState;
    private TextToSpeech tts;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


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

        final Switch fallingButton = (Switch) findViewById(R.id.낙상);
        final Switch nearButton = (Switch) findViewById(R.id.근접);
        final Switch vibeButton = (Switch) findViewById(R.id.진동);
        final Switch soundButton = (Switch) findViewById(R.id.소리);


        SharedPreferences buttonSaver = getSharedPreferences("sFile",MODE_PRIVATE);

        fallingButtonState = buttonSaver.getBoolean("falling", true);
        nearButtonState = buttonSaver.getBoolean("near", true);
        vibeButtonState = buttonSaver.getBoolean("vibe", false);
        soundButtonState = buttonSaver.getBoolean("sound", true);

        fallingButton.setChecked(fallingButtonState);
        nearButton.setChecked(nearButtonState);
        vibeButton.setChecked(vibeButtonState);
        soundButton.setChecked(soundButtonState);

        fallingButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    Intent fallingIntent = new Intent(getApplicationContext(), fallingDetector.class);
                    startService(fallingIntent);
                    speak("낙상 감지를 킵니다.");
                    fallingButtonState = true;

                } else {
                    Intent fallingIntent = new Intent(getApplicationContext(), fallingDetector.class);
                    stopService(fallingIntent);
                    speak("낙상 감지를 끕니다");
                    fallingButtonState = false;
                }
            }
        });

        nearButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    Intent GPSIntent = new Intent(getApplicationContext(), GPSService.class);
                    startService(GPSIntent);
                    speak("근접 알림을 켭니다.");
                    nearButtonState = true;
                } else {
                    Intent GPSIntent = new Intent(getApplicationContext(), GPSService.class);
                    stopService(GPSIntent);
                    speak("근접 알림을 끕니다.");
                    nearButtonState = false;
                }
            }
        });

        vibeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    speak("진동을 켭니다.");
                    vibeButtonState = true;
                } else {
                    speak("진동을 끕니다.");
                    vibeButtonState = false;
                }
            }
        });

        soundButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    speak("소리를 켭니다.");
                    soundButtonState = true;
                } else {
                    speak("소리를 끕니다.");
                    soundButtonState = false;
                }
            }
        });

    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        SharedPreferences buttonSaver = getSharedPreferences("sFile",MODE_PRIVATE);
        SharedPreferences.Editor editor = buttonSaver.edit();

        editor.putBoolean("falling", fallingButtonState);
        editor.putBoolean("near", nearButtonState);
        editor.putBoolean("vibe", vibeButtonState);
        editor.putBoolean("sound", soundButtonState);

        editor.apply();

        if(tts != null){
            tts.stop();
            tts.shutdown();
            tts = null;
        }


    }

    void speak(String s){
        tts.speak(s,TextToSpeech.QUEUE_FLUSH,null);
    }


}
