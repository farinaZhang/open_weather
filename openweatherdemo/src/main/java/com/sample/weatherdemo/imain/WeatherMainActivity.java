package com.sample.weatherdemo.imain;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sample.weatherdemo.MyApplication;
import com.sample.weatherdemo.R;
import com.sample.weatherdemo.adapter.WeatherListAdapter;
import com.sample.weatherdemo.common.util.T;
import com.sample.weatherdemo.common.util.TimeUtils;
import com.sample.weatherdemo.common.util.WeatherIconUtils;
import com.sample.weatherdemo.component.MessageConst;
import com.sample.weatherdemo.db.CityProvider;
import com.sample.weatherdemo.db.CityProvider.CityConstants;
import com.sample.weatherdemo.fragment.ITaskManager;
import com.sample.weatherdemo.fragment.TaskException;
import com.sample.weatherdemo.fragment.TaskManager;
import com.sample.weatherdemo.fragment.WorkTask;
import com.sample.weatherdemo.weather.plugin.bean.Forecast;
import com.sample.weatherdemo.weather.plugin.bean.RealTime;
import com.sample.weatherdemo.weather.plugin.bean.WeatherInfo;
import com.sample.weatherdemo.weather.plugin.spider.WeatherSpider;

import java.io.Serializable;

/**
 * Created by FarinaZhang on 2017/4/21.
 * MainActivity  主画面
 */


public class WeatherMainActivity extends Activity implements ITaskManager {
    private Handler mHandler;
    private Handler mInComingHandler;
    private ActivityComAssist mActivityComAssist;

    private TipView mTipView;
    private ListView mListView;
    private ImageView mBackImage;
    private WeatherListAdapter mWeatherAdapter;
    private FrameLayout mCurWeatherView;

    private TextView mInput;
    // 当前天气的View
    private ImageView mCurWeatherIV;
    private TextView mCurWeatherTV;
    private TextView mCurHighTempTV;
    private TextView mCurLowTempTV;
    private TextView mCurFeelsTempTV;
    private TextView mCurWeatherCopyTV;

    private Button mBtnStart;  //开始录音按键，完成录音自动发送给服务器（网络）获取数据；
    private String postID = "101020100";
    private final String DEFAULT_CITY = "上海";
    private String CityName = DEFAULT_CITY;
    private ContentResolver mContentResolver;
    private int ShowType = 0;//0:全部天气数据； 1:空气质量 ,2: 一周天气,3:今天 ,4: 天气指数


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        taskManager = new TaskManager();
        if (savedInstanceState != null)
            taskManager.restore(savedInstanceState);

        setContentView(R.layout.activity_weather);
        initHandler();
        initInComingHandler();
        initCommunicationAssist();
        initView();


        Intent intent = new Intent();
        intent.setClass(WeatherMainActivity.this, VoiceSdkService.class);
        startService(intent);

    }

    private void initView() {
        mInput = (TextView) findViewById(R.id.input_text);
        mBtnStart = (Button) findViewById(R.id.btn_start);

        mBackImage = (ImageView) findViewById(R.id.weather_background);

        mTipView = (TipView) findViewById(R.id.tip_view);
        mListView = (ListView) findViewById(R.id.drag_list);
        mCurWeatherView = (FrameLayout) findViewById(R.id.current);
        initCurWearther();

        mTipView.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        mCurWeatherView.setVisibility(View.GONE);

        mWeatherAdapter = new WeatherListAdapter(WeatherMainActivity.this);
        mListView.setAdapter(mWeatherAdapter);

        mBtnStart.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                sendMessageToService(MessageConst.CLIENT_ACTION_START_RECORED, 0, 0, null, null);
            }
        });


    }

    private void initCurWearther() {
        mCurWeatherIV = (ImageView) mCurWeatherView.findViewById(R.id.main_icon);
        mCurWeatherTV = (TextView) mCurWeatherView.findViewById(R.id.weather_description);
        mCurHighTempTV = (TextView) mCurWeatherView.findViewById(R.id.temp_high);
        mCurLowTempTV = (TextView) mCurWeatherView.findViewById(R.id.temp_low);
        mCurFeelsTempTV = (TextView) mCurWeatherView.findViewById(R.id.temperature);
        mCurWeatherCopyTV = (TextView) mCurWeatherView.findViewById(R.id.copyright);
    }

    //activity 的普通handler
    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MessageConst.CLENT_SHOW_ERROR: {
                        T.showShort(WeatherMainActivity.this, "刷新失败...请再试一次");
                        break;
                    }
                }
            }
        };
    }


    //service  语音识别的handler
    private void initInComingHandler() {
        mInComingHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MessageConst.CLENT_START_SPEECH_RECORDING:
                        mBtnStart.setText("设别中");
                        break;
                    case MessageConst.CLENT_END_SPEECH_RECORDING:
                        mBtnStart.setText("开始");
                        break;

                    case MessageConst.CLENT_SHOW_INPUT: {
                        mInput.setText((String) msg.obj);
                        break;
                    }
                    case MessageConst.SERVER_ACTION_RETURN_RESULT: {
                        if (msg.arg1 == 0) {
                            String temp = (String) msg.obj;
                            if (temp.equals("今天")) {
                                ShowType = 3;
                            } else if (temp.equals("明天") || temp.equals("后天") || temp.equals("一周")) {
                                ShowType = 2;
                            } else {
                                ShowType = msg.arg1;
                                if (msg.obj != null) {
                                    CityName = (String) msg.obj;
                                }
                            }
                        } else {
                            ShowType = msg.arg1;
                            if (msg.obj != null) {
                                CityName = (String) msg.obj;
                            }
                        }

                        Log.d("TAG", "SERVER_ACTION_RETURN_RESULT name : " + CityName);
                        mContentResolver = WeatherMainActivity.this.getContentResolver();
                        Cursor c = mContentResolver.query(CityProvider.CITY_CONTENT_URI,
                                new String[]{CityProvider.CityConstants.POST_ID}, CityProvider.CityConstants.NAME
                                        + "=?", new String[]{CityName}, null);
                        if (c != null && c.moveToNext()) {
                            postID = c.getString(c.getColumnIndex(CityProvider.CityConstants.POST_ID));
                            new WeatherTask(postID).execute(true);
                        } else {
                            T.showShort(WeatherMainActivity.this, "城市不存在，刷新失败...");
                            CityName = DEFAULT_CITY; //当城市名不存在时，还原城市名，默认上海
                        }

                        break;
                    }
                }
            }
        };
    }

    private void initCommunicationAssist() {
        mActivityComAssist = new ActivityComAssist();
        MyApplication.getInstance().setServiceToActivityListener(mActivityComAssist);
    }

    private void sendMessageToService(int what, int arg1, int arg2, Bundle data, Object obj) {
        if (MyApplication.getInstance().getActivityToServiceListener() != null)
            MyApplication.getInstance().getActivityToServiceListener().callBack(what, arg1, arg2, data, obj);
    }

    private class ActivityComAssist implements CommunicationAssist {

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
        Intent intent = new Intent();
        intent.setClass(WeatherMainActivity.this, VoiceSdkService.class);
        stopService(intent);

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    class WeatherTask extends WorkTask<Boolean, Void, WeatherInfo> {
        public WeatherTask(String postID) {
            super(postID, WeatherMainActivity.this);
        }

        @Override
        protected void onPrepare() {
            super.onPrepare();
            taskStateChanged(ABaseTaskState.prepare, null);
        }

        @Override
        public WeatherInfo workInBackground(Boolean... params)
                throws TaskException {

            mWeatherAdapter.initViews();
            WeatherInfo weatherInfo = null;

            try {
                weatherInfo = WeatherSpider.getWeatherInfo(postID);
                weatherInfo.setCityName(CityName);
            } catch (TaskException e) {
                mHandler.sendEmptyMessage(MessageConst.CLENT_SHOW_ERROR);
            }
            return weatherInfo;
        }

        @Override
        protected void onSuccess(WeatherInfo result) {
            super.onSuccess(result);

            if (result == null) {
                mHandler.sendEmptyMessage(MessageConst.CLENT_SHOW_ERROR);
            } else {
                if (3 == ShowType) {
                    mTipView.setVisibility(View.GONE);
                    mListView.setVisibility(View.GONE);
                    mCurWeatherView.setVisibility(View.VISIBLE);

                    RealTime realTime = result.getRealTime();
                    Forecast forecast = result.getForecast();

                    int type = realTime.getAnimation_type();

                    mCurWeatherIV.setImageResource(WeatherIconUtils.getWeatherIcon(type));
                    mCurWeatherTV.setText(realTime.getWeather_name());
                    mCurFeelsTempTV.setText(realTime.getTemp() + "");
                    mCurHighTempTV.setText(forecast.getTmpHigh(1) + "°");
                    mCurLowTempTV.setText(forecast.getTmpLow(1) + "°");
                    mCurWeatherCopyTV.setText(TimeUtils.getDay(getPubTime(
                            getPostID(CityName))) + "发布");

                } else {
                    mTipView.setVisibility(View.GONE);
                    mListView.setVisibility(View.VISIBLE);
                    mCurWeatherView.setVisibility(View.GONE);

                    mWeatherAdapter.setShowType(ShowType);
                    mWeatherAdapter.setWeather(result);
                }
            }
            taskStateChanged(ABaseTaskState.success, null);
        }

        @Override
        protected void onFailure(TaskException exception) {
            super.onFailure(exception);
            taskStateChanged(ABaseTaskState.falid, exception.getMessage());
        }

        @Override
        protected void onFinished() {
            super.onFinished();
            taskStateChanged(ABaseTaskState.finished, null);
        }
    }

    private long getPubTime(String postID) {
        Cursor c = mContentResolver.query(CityProvider.TMPCITY_CONTENT_URI,
                new String[]{CityConstants.PUB_TIME}, CityConstants.POST_ID
                        + "=?", new String[]{postID}, null);

        long time = 0L;
        if (c.moveToFirst())
            time = c.getLong(c.getColumnIndex(CityConstants.PUB_TIME));
        return time;
    }

    private String getPostID(String name) {
        String postIDtemp = "";
        mContentResolver = WeatherMainActivity.this.getContentResolver();
        Cursor c = mContentResolver.query(CityProvider.CITY_CONTENT_URI,
                new String[]{CityProvider.CityConstants.POST_ID}, CityProvider.CityConstants.NAME
                        + "=?", new String[]{CityName}, null);
        if (c != null && c.moveToNext()) {
            postIDtemp = c.getString(c.getColumnIndex(CityProvider.CityConstants.POST_ID));
        }
        return postIDtemp;
    }


    protected void taskStateChanged(ABaseTaskState state, Serializable tag) {
        // 开始Task
        if (state == ABaseTaskState.prepare) {

        }
        // Task成功
        else if (state == ABaseTaskState.success) {

        }
        // 取消Task
        else if (state == ABaseTaskState.canceled) {

        }
        // Task失败
        else if (state == ABaseTaskState.falid) {
            mHandler.sendEmptyMessage(MessageConst.CLENT_SHOW_ERROR);
        }
        // Task结束
        else if (state == ABaseTaskState.finished) {


        }
    }

    private TaskManager taskManager;// 管理线程

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (taskManager != null) {
            taskManager.save(outState);
        }
    }

    protected ITaskManager getTaskManager() {
        return taskManager;
    }

    @Override
    public void addTask(WorkTask task) {
        taskManager.addTask(task);
    }

    @Override
    public void removeTask(String taskId, boolean cancelIfRunning) {
        taskManager.removeTask(taskId, cancelIfRunning);
    }

    @Override
    public void removeAllTask(boolean cancelIfRunning) {
        taskManager.removeAllTask(cancelIfRunning);
    }

    @Override
    public int getTaskCount(String taskId) {
        return taskManager.getTaskCount(taskId);
    }

    protected enum ABaseTaskState {
        none, prepare, falid, success, finished, canceled
    }


}
