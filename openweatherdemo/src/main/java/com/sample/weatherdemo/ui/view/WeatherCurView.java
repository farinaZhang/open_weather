package com.sample.weatherdemo.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sample.weatherdemo.R;
import com.sample.weatherdemo.common.util.WeatherIconUtils;
import com.sample.weatherdemo.weather.plugin.bean.Forecast;
import com.sample.weatherdemo.weather.plugin.bean.RealTime;
import com.sample.weatherdemo.weather.plugin.bean.WeatherInfo;

public class WeatherCurView extends WeatherBaseView {
	// 当前天气的View
	private ImageView mCurWeatherIV;
	private TextView mCurWeatherTV;
	private TextView mCurHighTempTV;
	private TextView mCurLowTempTV;
	private TextView mCurFeelsTempTV;
	private TextView mCurWeatherCopyTV;


	public WeatherCurView(Context c) {
		this(c, null);
	}

	public WeatherCurView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WeatherCurView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		// 空气质量
		mCurWeatherIV = (ImageView) findViewById(R.id.main_icon);
		mCurWeatherTV = (TextView) findViewById(R.id.weather_description);
		mCurHighTempTV = (TextView) findViewById(R.id.temp_high);
		mCurLowTempTV = (TextView) findViewById(R.id.temp_low);
		mCurFeelsTempTV = (TextView) findViewById(R.id.temperature);
		mCurWeatherCopyTV = (TextView) findViewById(R.id.copyright);
		mCurWeatherCopyTV.setVisibility(View.GONE);
	}


	@Override
	public void setWeatherInfo(WeatherInfo weatherInfo) {
		RealTime realTime = weatherInfo.getRealTime();
		Forecast forecast = weatherInfo.getForecast();
		int type = realTime.getAnimation_type();

		if (forecast == null || forecast.getType(1) < 0)
			return;
		mCurWeatherIV.setImageResource(WeatherIconUtils.getWeatherIcon(type));
		mCurWeatherTV.setText(realTime.getWeather_name());
		mCurFeelsTempTV.setText(realTime.getTemp() + "");
		mCurHighTempTV.setText(forecast.getTmpHigh(1) + "°");
		mCurLowTempTV.setText(forecast.getTmpLow(1) + "°");
	}
}
