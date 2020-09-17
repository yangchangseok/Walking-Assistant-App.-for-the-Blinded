package com.yougowegoteam.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean bEnter = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, true);

       // if(bEnter ){
         //   tts.speak("귀하의 현재위치로부터 10m 반경에 볼라드가 있습니다.", TextToSpeech.QUEUE_FLUSH,null,null);
        //}else{
         //   tts.speak("볼라드 진입구역에서 벗어나셨습니다.",TextToSpeech.QUEUE_FLUSH,null,null);
        }
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
       // throw new UnsupportedOperationException("Not yet implemented");
    }


