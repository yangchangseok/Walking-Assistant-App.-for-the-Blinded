package com.yougowegoteam.demo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.annotation.Nullable;

public class fallingDetector extends Service implements SensorEventListener{
    protected double latitude = 0.0, longitude = 0.0; //GPS를 통해 측정된 위치 값을 다른 액티비티로 넘겨주기 위한 변수
    Handler handler = new Handler(Looper.getMainLooper()); // 메인 쓰레드
    private Handler mPeriodicEventHandler = new Handler(); // TIME OUT 쓰레드
    private final int PERIODIC_EVENT_TIMEOUT = 3000; // TIME OUT 세팅

    String TAG = "fallingDetector";

    //서비스에서 작동될 쓰레드 시간 관리 타이머
    private Timer fuseTimer = new Timer();
    private int sendCount = 0;
    private char sentRecently = 'N';
    //자이로 센서의 각속도
    private float[] gyro = new float[3];
    //계산된 각속도
    private float degreeFloat;
    private float degreeFloat2;
    //자이로 센서 데이터의 회전 행렬
    private float[] gyroMatrix = new float[9];
    //자이로 행렬으로부터의 방위각
    private float[] gyroOrientation = new float[3];
    //자기장 벡터
    private float[] magnet = new float[3];
    //가속도계 벡터
    private float[] accel = new float[3];
    //가속도계와 자기장으로부터의 방위각
    private float[] accMagOrientation = new float[3];
    //3가지 센서를 합친 것의 방위각
    private float[] fusedOrientation = new float[3];
    //가속도계와 자기장센서의 기준 회전 행렬
    private float[] rotationMatrix = new float[9];
    //센서 값의 변화 크기를 비교하기 위한 변수
    public static final float EPSILON = 0.000000001f;
    //타이머의 쓰레드의 간격 설정용 변수
    public static final int TIME_CONSTANT = 30;
    //중력 가속도값
    public static final float FILTER_COEFFICIENT = 0.98f;
    //나노s -> s
    private static final float NS2S = 1.0f / 1000000000.0f;
    //시간으로 적분하기 위한 변수
    private float timestamp;
    //재실행을 위한 변수
    private boolean initState = true;
    //안드로이드 센서를 사용하기 위한 변수
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private Sensor senProximity;
    private SensorEvent mSensorEvent;
    //메시지 전송 이벤트 처리를 위한 Runnable 임시 객체 구현
    private Runnable doPeriodicTask = new Runnable() {
        public void run() {
            sentRecently = 'N';
        }
    };


    private TextToSpeech tts;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("falling", "onCreate");

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


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
        mPeriodicEventHandler.removeCallbacks(doPeriodicTask);
        senSensorManager.unregisterListener(this);
        sendCount = 0;

        if(tts != null){
            tts.stop();
            tts.shutdown();
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        //서비스가 시작되면 센서를 사용하기 위한 객체 할당.
        Log.d("fallingDetector", "onStartCommand() called");

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //간혹 낮은 버전의 안드로이드 기종은 가속도계 센서가 사용 불가한 경우가 있으니, 확인 작업을 한다.
        if(senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!=null) {
            initListeners();
            //타이머 시작.
            fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(), 1000, TIME_CONSTANT);
        }else {
            speak("낙상 센서를 동작할 수 없습니다.");
        }

        return START_STICKY;
    }

    //센서 초기화.
    public void initListeners() {
        senSensorManager.registerListener(this,
                senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);

        senSensorManager.registerListener(this,
                senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_FASTEST);

        senSensorManager.registerListener(this,
                senSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    //SensorEventListener 인터페이스의 속한 메서드를 오버라이딩해서 구현 한다.
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                // 새로운 가속도계 데이터를 가속도계 배열에 복사
                // 새로운 방위각 계산
                System.arraycopy(sensorEvent.values, 0, accel, 0, 3);
                calculateAccMagOrientation();
                break;
            case Sensor.TYPE_GYROSCOPE:
                // 자이로 데이터 처리
                gyroFunction(sensorEvent);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                //새로운 자기장 데이터를 배열에 복사
                System.arraycopy(sensorEvent.values, 0, magnet, 0, 3);
                break;
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void calculateAccMagOrientation() {
        if(SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)){
            SensorManager.getOrientation(rotationMatrix, accMagOrientation);
        }
    }

    private void getRotationVectorFromGyro(float[] gyroValues,float[] deltaRotationVector, float timeFactor) {
        float[] normValues = new float[3];

        //샘플의 각속도를 계산한다.
        float omegaMagnitude =
                (float) Math.sqrt(gyroValues[0] * gyroValues[0] +
                        gyroValues[1] * gyroValues[1] +
                        gyroValues[2] * gyroValues[2]);

        //축을 얻기에 충분히 큰 경우, 회전 벡터를 표준화
        if (omegaMagnitude > EPSILON) {
            normValues[0] = gyroValues[0] / omegaMagnitude;
            normValues[1] = gyroValues[1] / omegaMagnitude;
            normValues[2] = gyroValues[2] / omegaMagnitude;
        }


               /*timestep에 의해 이 축을 중심으로 각속도와 통합한다.
               이 샘플에서 시간 경과에 따른 델타 값의 회전변환을 얻으려면 델타 회전의 축각 표현의 변환이 필요하다.
               즉, 회전 행렬로 변환하기 전에 쿼터니언으로 변환 */
        float thetaOverTwo = omegaMagnitude * timeFactor;
        float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
        deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
        deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
        deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
        deltaRotationVector[3] = cosThetaOverTwo;
    }

    private void gyroFunction(SensorEvent event) {
        //첫 번째 가속도계 / 자기장 방향이 획득 될 때까지 시작하지 않음.
        if (accMagOrientation == null)
            return;

        //자이로 회전 배열 값을 초기화한다.
        if (initState) {
            float[] initMatrix = new float[9];
            initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
            float[] test = new float[3];
            SensorManager.getOrientation(initMatrix, test);
            gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
            initState = false;
        }

        //새 자이로 값을 자이로 배열에 복사한다.
        //원래의 자이로 데이터를 회전 벡터로 변환한다.
        float[] deltaVector = new float[4];
        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            System.arraycopy(event.values, 0, gyro, 0, 3);
            getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f);
        }

        //측정 완료이 완료되면, 다음 시간 간격을 위해 현재 시간을 설정한다.
        timestamp = event.timestamp;

        //회전 벡터를 회전 행렬로 변환한다.
        float[] deltaMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);
        //회전 벡터를 회전 행렬로 변환한다.
        gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);
        //회전 행렬에서 자이로 스코프 기반 방향을 얻는다.
        SensorManager.getOrientation(gyroMatrix, gyroOrientation);
    }

    //회전행렬을 구하기 위한 메서드
    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];

        float sinX = (float) Math.sin(o[1]);
        float cosX = (float) Math.cos(o[1]);
        float sinY = (float) Math.sin(o[2]);
        float cosY = (float) Math.cos(o[2]);
        float sinZ = (float) Math.sin(o[0]);
        float cosZ = (float) Math.cos(o[0]);

        //x 축 (피치)에 대한 회전배열
        xM[0] = 1.0f;
        xM[1] = 0.0f;
        xM[2] = 0.0f;
        xM[3] = 0.0f;
        xM[4] = cosX;
        xM[5] = sinX;
        xM[6] = 0.0f;
        xM[7] = -sinX;
        xM[8] = cosX;

        //y 축 (롤)에 대한 회전배열
        yM[0] = cosY;
        yM[1] = 0.0f;
        yM[2] = sinY;
        yM[3] = 0.0f;
        yM[4] = 1.0f;
        yM[5] = 0.0f;
        yM[6] = -sinY;
        yM[7] = 0.0f;
        yM[8] = cosY;

        //z 축에 대한 회전 (방위각)배열
        zM[0] = cosZ;
        zM[1] = sinZ;
        zM[2] = 0.0f;
        zM[3] = -sinZ;
        zM[4] = cosZ;
        zM[5] = 0.0f;
        zM[6] = 0.0f;
        zM[7] = 0.0f;
        zM[8] = 1.0f;

        //회전 순서는 y, x, z (롤, 피치, yaw)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);

        return resultMatrix;

    }

    //회전행렬의 곱을 계산하기 위한 메서드.
    private float[] matrixMultiplication(float[] A, float[] B) {

        float[] result = new float[9];
        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

        return result;
    }


    //지속적으로 센서 값의 변화를 계산할 쓰레드 세팅.
    class calculateFusedOrientationTask extends TimerTask {
        public void run() {

            float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;
            //세가지 센서의 최종 측정값을 필터링.
            fusedOrientation[0] =
                    FILTER_COEFFICIENT * gyroOrientation[0]
                            + oneMinusCoeff * accMagOrientation[0];

            fusedOrientation[1] =
                    FILTER_COEFFICIENT * gyroOrientation[1]
                            + oneMinusCoeff * accMagOrientation[1];

            fusedOrientation[2] =
                    FILTER_COEFFICIENT * gyroOrientation[2]
                            + oneMinusCoeff * accMagOrientation[2];

            //**********가속도계 센서 값의 변화 측정**********
            double SMV = Math.sqrt(accel[0] * accel[0] + accel[1] * accel[1] + accel[2] * accel[2]);
            //여기선 35로 되어있지만 값이 25 정도만 되어도 충분히 회전 변화를 측정 가능하다.
            if (SMV > 60 ) {
                GPSService gpsService = new GPSService();
                if (gpsService.getLocation() != null && gpsService.getLongitude() != 0) {
                    if (sentRecently == 'N') {
                        Log.d("Accelerometer vector:", "" + SMV);
                        //라디안 값을 각도로 표현.
                        degreeFloat = (float) (fusedOrientation[1] * 180 / Math.PI);
                        degreeFloat2 = (float) (fusedOrientation[2] * 180 / Math.PI);
                        if (degreeFloat < 0)
                            degreeFloat = degreeFloat * -1;
                        if (degreeFloat2 < 0)
                            degreeFloat2 = degreeFloat2 * -1;
                        if (degreeFloat > 30 || degreeFloat2 > 30) {
                            Log.d("Degree1:", "" + degreeFloat);
                            Log.d("Degree2:", "" + degreeFloat2);
                            //이 곳에 넘어짐 감지시 필요헌 코드 작성.

                            speak("낙상이 감지되었습니다");

                            //새로운 화면을 띄우기 위한 액션 처리.
                            Intent intent = new Intent(getApplicationContext(), Fallpopup.class);
                            intent.putExtra("lastlat", gpsService.getLatitude());
                            intent.putExtra("lastlon", gpsService.getLongitude());
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //센서값 변화는 측정. but, 회전 축이  x-z, y-z 간의 회전 변화가 아니므로 다른 액션을 발생한다.
                                    //speak("조심하세요.");
                                    Log.d("Send!", "센서 값 변화!!!!! " + sendCount);
                                }
                            });
                            sendCount++;
                        }
                        sentRecently = 'Y';
                        //쓰레드를 지연시킨다.
                        mPeriodicEventHandler.postDelayed(doPeriodicTask, PERIODIC_EVENT_TIMEOUT);
                    }
                }
            }
            //측정 값을 다시 덮어쓴다.
            gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
            System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);
        }
    }

    void speak(String s){
        tts.speak(s,TextToSpeech.QUEUE_FLUSH,null);
    }

}