package com.sample.weatherdemo.imain;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.sample.weatherdemo.MyApplication;
import com.sample.weatherdemo.common.util.T;
import com.sample.weatherdemo.component.MessageConst;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ai.olami.aiCloudService.sdk.engin.OlamiVoiceRecognizer;
import ai.olami.aiCloudService.sdk.interfaces.IOlamiVoiceRecognizerListener;


/**
 * Created by FarinaZhang on 2017/4/21.
 * MainService
 * <p/>
 * 用于语音识别
 */

public class VoiceSdkService extends Service {

    private String TAG = "VoiceSdkService";
    private Handler mInComingHandler;
    private VoiceSdkComAssist mVoiceSdkComAssist;
    private OlamiVoiceRecognizer mViaVoiceRecognizer;
    private IOlamiVoiceRecognizerListener mViaVoiceRecognizerListener;


    @Override
    public void onCreate() {
        initInComingHandler();
        initCommunicationAssist();
        initViaVoiceRecognizerListener();
        init();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    //设置语音识别相关初始化参数
    public void init() {
        mViaVoiceRecognizer = new OlamiVoiceRecognizer(VoiceSdkService.this);
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(this.getBaseContext().TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        mViaVoiceRecognizer.init(imei);

        mViaVoiceRecognizer.setListener(mViaVoiceRecognizerListener);
        mViaVoiceRecognizer.setLocalization(OlamiVoiceRecognizer.LANGUAGE_SIMPLIFIED_CHINESE);
        mViaVoiceRecognizer.setAuthorization("0fabfbd3db2546a0b48628a4354801f8", "asr", "0a714958eef844869175f9507a074c17", "nli");
        mViaVoiceRecognizer.setVADTailTimeout(3000);

        mViaVoiceRecognizer.setLatitudeAndLongitude(31.155364678184498, 121.34882432933009);
    }

    private void initInComingHandler() {
        mInComingHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MessageConst.CLIENT_ACTION_START_RECORED:
                        if (mViaVoiceRecognizer != null)
                            mViaVoiceRecognizer.start();
                        break;
                    case MessageConst.CLIENT_ACTION_STOP_RECORED:
                        if (mViaVoiceRecognizer != null)
                            mViaVoiceRecognizer.stop();
                        break;
                    case MessageConst.CLIENT_ACTION_CANCEL_RECORED:
                        if (mViaVoiceRecognizer != null)
                            mViaVoiceRecognizer.cancel();
                        break;
                    case MessageConst.CLIENT_ACTION_SENT_TEXT:
                        if (mViaVoiceRecognizer != null)
                            mViaVoiceRecognizer.sendText(msg.obj.toString());
                        break;
                }
            }
        };
    }

    private void initViaVoiceRecognizerListener() {
        mViaVoiceRecognizerListener = new olamiVoiceRecognizerListener();
    }

    private class olamiVoiceRecognizerListener implements IOlamiVoiceRecognizerListener {

        @Override
        public void onError(int errCode) {
            Log.d(TAG, "onError ");
            // TODO Auto-generated method stub

        }

        @Override
        public void onEndOfSpeech() {
            // TODO Auto-generated method stub
            Log.d(TAG, "onEndOfSpeech ");
            sendMessageToActivity(MessageConst.CLENT_END_SPEECH_RECORDING, 0, 0, null, null);
        }

        @Override
        public void onBeginningOfSpeech() {
            // TODO Auto-generated method stub
            Log.d(TAG, "onBeginningOfSpeech ");
            sendMessageToActivity(MessageConst.CLENT_START_SPEECH_RECORDING, 0, 0, null, null);
        }

        @Override
        public void onResult(String result, int type) {
            Log.d(TAG, "onResult ,result=" + result);
            //{"data":{"asr":{"result":"上海的天气","speech_status":0,"final":true,"status":0},"nli":[{"desc_obj":{"status":0},"semantic":[{"app":"sample","input":"上海的天气","slots":[{"name":"city","value":"上海"}],"modifier":["chacitytianqi"],"customer":"58df54a484ae11f0bb7b488b"}],"type":"sample"}]},"status":"ok"}
            getValidData(result);
        }

        @Override
        public void onCancel() {
            Log.d(TAG, "onCancel ");
            // TODO Auto-generated method stub

        }

        @Override
        public void onUpdateVolume(int volume) {
            Log.d(TAG, "onUpdateVolume ");
            // TODO Auto-generated method stub

        }

    }

    private void initCommunicationAssist() {
        mVoiceSdkComAssist = new VoiceSdkComAssist();
        MyApplication.getInstance().setActivityToServiceListener(mVoiceSdkComAssist);
    }


    private void sendMessageToActivity(int what, int arg1, int arg2, Bundle data, Object obj) {
        if (MyApplication.getInstance().getServiceToActivityListener() != null)
            MyApplication.getInstance().getServiceToActivityListener().callBack(what, arg1, arg2, data, obj);
    }

    private class VoiceSdkComAssist implements CommunicationAssist {

        @Override
        public void callBack(int what, int arg1, int arg2, Bundle data, Object obj) {
            Message msg = Message.obtain(null, what);
            msg.arg1 = arg1;
            msg.arg2 = arg2;
            if (data != null)
                msg.setData(data);
            if (obj != null)
                msg.obj = obj;
            mInComingHandler.sendMessage(msg);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mViaVoiceRecognizer != null)
            mViaVoiceRecognizer.destroy();
    }

    //解析语音结果数据
    private void getValidData(String ValidData) {
        //{"data":{"asr":{"result":"上海的天气","speech_status":0,"final":true,"status":0},"nli":[{"desc_obj":{"status":0},"semantic":[{"app":"sample","input":"上海的天气","slots":[{"name":"city","value":"上海"}],"modifier":["chacitytianqi"],"customer":"58df54a484ae11f0bb7b488b"}],"type":"sample"}]},"status":"ok"}
        //{"data":{"asr":{"result":"空气质量","speech_status":0,"final":true,"status":0},"nli":[{"desc_obj":{"result":"对不起，你说的我还不懂，能换个说法吗？","status":"1003"},"type":"ds"}]},"status":"ok"}
        JSONObject jTemp;
        JSONArray jDatanli;
        JSONArray jDataSemantic;
        String jDataModifier;
        JSONArray jDataSlots;

        try {
            jTemp = new JSONObject(ValidData);

            if (jTemp.getString("status").equals("ok")) {
                //服务器返回结果正确
                jDatanli = jTemp.getJSONObject("data").getJSONArray("nli");

                JSONObject jDesObj = jDatanli.getJSONObject(0).getJSONObject("desc_obj");
                if (jDesObj.getInt("status") == 0 && jDatanli.getJSONObject(0).has("semantic")) {

                    jDataSemantic = jDatanli.getJSONObject(0).getJSONArray("semantic");
                    //"slots":[{"name":"city","value":"上海"}],"modifier":["chacitytianqi"]

                    jDataModifier = jDataSemantic.getJSONObject(0).getJSONArray("modifier").getString(0);

                    Log.d(TAG, " SPEAK result jDataModifier : " + jDataModifier);

                    if (jDataModifier.equals("chacitytianqi")) { //天气

                        jDataSlots = jDataSemantic.getJSONObject(0).getJSONArray("slots");
                        Log.d(TAG, " SPEAK result jDataSlots : " + jDataSlots.toString());

                        String city = "";
                        String riqi = "";

                        for(int i=0;i<jDataSlots.length();i++){
                            JSONObject jDataSlot =(JSONObject) jDataSlots.get(i);
                            String type = jDataSlot.getString("name");
                            String value = jDataSlot.getString("value");


                            if(type.equals("city")){
                                city = value;
                            }else if(type.equals("riqitype")){
                                riqi = value;
                            }
                        }

                        int type =0;

                        if(riqi.length()>0){

                            int temp =0;

                            if(riqi.equals("今天")){
                                temp=1;
                            }

                            if( 0 == temp){
                                sendMessageToActivity(MessageConst.SERVER_ACTION_RETURN_RESULT, 2, 0, null, city); //一周
                            }else {
                                sendMessageToActivity(MessageConst.SERVER_ACTION_RETURN_RESULT, 3, 0, null, city);// 今天
                            }
                        }else{
                            sendMessageToActivity(MessageConst.SERVER_ACTION_RETURN_RESULT, 0, type, null, city); //全部
                        }

                    } else if (jDataModifier.equals("kqzhiliang")) {//空气质量
                        String name = "";

                        JSONArray jsobjSlots = jDataSemantic.getJSONObject(0).getJSONArray("slots");
                        if (jsobjSlots.length() > 0) {
                            jDataSlots = jDataSemantic.getJSONObject(0).getJSONArray("slots");
                            String type = jDataSlots.getJSONObject(0).getString("name");
                            String value = jDataSlots.getJSONObject(0).getString("value");
                            Log.d(TAG, " SPEAK result jDataSlots : " + jDataSlots.toString());
                            // 根据城市名从数据库中获取城市Id
                            sendMessageToActivity(MessageConst.SERVER_ACTION_RETURN_RESULT, 1, 0, null, value);
                        } else {
                            sendMessageToActivity(MessageConst.SERVER_ACTION_RETURN_RESULT, 1, 0, null, null); //空气质量
                        }
                    } else {
                        //不是天气相关
                        T.showShort(VoiceSdkService.this, "您所说的暂不支持，请换个说法");
                    }
                } else {
                    T.showShort(VoiceSdkService.this, "您所说的暂不支持，请换个说法");
                }

            } else {
                //服务器返回出错
                T.showShort(VoiceSdkService.this, "服务器返回出错");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


}
