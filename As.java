package com.yougowegoteam.demo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.yougowegoteam.demo.R;

import java.util.Locale;


public class As  extends Mainpage {

    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_as);

        final EditText name1 = (EditText) findViewById(R.id.name2);
        final EditText name2 = (EditText) findViewById(R.id.name3);
        final EditText name3 = (EditText) findViewById(R.id.name4);

        final EditText phone1 = (EditText) findViewById(R.id.번호설정1);
        final EditText phone2 = (EditText) findViewById(R.id.번호설정2);
        final EditText phone3 = (EditText) findViewById(R.id.번호설정3);

        SharedPreferences sharedPreferences = getSharedPreferences("popup", MODE_PRIVATE);

        String tmpName1 = sharedPreferences.getString("name1", "");
        String tmpName2 = sharedPreferences.getString("name2", "");
        String tmpName3 = sharedPreferences.getString("name3", "");

        String tmpPhone1 = sharedPreferences.getString("phone1", "");
        String tmpPhone2 = sharedPreferences.getString("phone2", "");
        String tmpPhone3 = sharedPreferences.getString("phone3", "");

        name1.setText(tmpName1);
        name2.setText(tmpName2);
        name3.setText(tmpName3);

        phone1.setText(tmpPhone1);
        phone2.setText(tmpPhone2);
        phone3.setText(tmpPhone3);

        Button button1 = findViewById(R.id.complete);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Speech("입력이 완료되었습니다.");
                String setName1 = name1.getText().toString();
                String setName2 = name2.getText().toString();
                String setName3 = name3.getText().toString();

                String setPhone1 = phone1.getText().toString();
                String setPhone2 = phone2.getText().toString();
                String setPhone3 = phone3.getText().toString();

                SharedPreferences.Editor editor = getSharedPreferences("popup", MODE_PRIVATE).edit();
                editor.putString("name1", setName1);
                editor.putString("name2", setName2);
                editor.putString("name3", setName3);

                editor.putString("phone1", setPhone1);
                editor.putString("phone2", setPhone2);
                editor.putString("phone3", setPhone3);
                editor.apply();

                onBackPressed();

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
    public void onDestroy(){
        super.onDestroy();
        if(tts != null){
            tts.stop();
            tts.shutdown();
            tts = null;
        }

    }
}
