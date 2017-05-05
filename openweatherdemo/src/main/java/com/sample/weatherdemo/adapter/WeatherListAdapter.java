package com.sample.weatherdemo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.sample.weatherdemo.R;
import com.sample.weatherdemo.ui.view.WeatherBaseView;
import com.sample.weatherdemo.weather.plugin.bean.AQI;
import com.sample.weatherdemo.weather.plugin.bean.WeatherInfo;
import com.sample.weatherdemo.weather.plugin.spider.WeatherSpider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class WeatherListAdapter extends BaseAdapter {
	private final HashMap<Integer, WeatherBaseView> mWeatherBaseViews = new HashMap<Integer, WeatherBaseView>();
	public static final int FORECAST_TYPE = 0;
	public static final int WEATHER_DETAILS_TYPE = 1;
	public static final int AQI_TYPE = 2;
	public static final int INDEX_TYPE = 3;


	private LayoutInflater mLayoutInflater;
	private List<Integer> mTypes;
	private WeatherInfo mWeatherInfo;
	private int ShowType = 0;//0:全部天气数据； 1:空气质量；2:一周天气

	public WeatherListAdapter(Context context) {
		mTypes = new ArrayList<Integer>();
		mLayoutInflater = LayoutInflater.from(context);
	}

	public void initViews() {
		if(!mWeatherBaseViews.isEmpty())
			return;

		WeatherBaseView convertView = (WeatherBaseView) mLayoutInflater
				.inflate(R.layout.weather_forecast, null);
		mWeatherBaseViews.put(FORECAST_TYPE, convertView);
		convertView = (WeatherBaseView) mLayoutInflater.inflate(
				R.layout.weather_details, null);
		mWeatherBaseViews.put(WEATHER_DETAILS_TYPE, convertView);
		convertView = (WeatherBaseView) mLayoutInflater.inflate(
				R.layout.weather_aqi, null);
		mWeatherBaseViews.put(AQI_TYPE, convertView);
		convertView = (WeatherBaseView) mLayoutInflater.inflate(
				R.layout.weather_index, null);
		mWeatherBaseViews.put(INDEX_TYPE, convertView);


	}

	public void setWeather(WeatherInfo weatherInfo) {
		if (WeatherSpider.isEmpty(weatherInfo))
			return;
		mWeatherInfo = weatherInfo;
		AQI aqi = weatherInfo.getAqi();
		mTypes.clear();
		mTypes.add(FORECAST_TYPE);
		mTypes.add(WEATHER_DETAILS_TYPE);
		if (!WeatherSpider.isEmpty(aqi) && aqi.getAqi() >= 0)
			mTypes.add(AQI_TYPE);
		mTypes.add(INDEX_TYPE);

		notifyDataSetChanged();
	}

	public void setShowType(int value){
		this.ShowType = value;
	}

	@Override
	public int getCount() {
		if(ShowType != 0) {
			return 1;
		}else {
			return mTypes.size();
		}
	}

	@Override
	public Object getItem(int position) {
		return mTypes.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getViewTypeCount() {
		if (mTypes.size() < 1)
			return 1;
		return mTypes.size();

	}

	@Override
	public int getItemViewType(int position) {
		if (position < mTypes.size())
			return mTypes.get(position);
		return super.getItemViewType(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		int itemType = getItemViewType(position);

		if(ShowType == 1){
			itemType = AQI_TYPE;
		}else if(ShowType == 2){
			itemType = FORECAST_TYPE;
		}else if(ShowType == 4){
			itemType = INDEX_TYPE;
		}
		if (convertView == null
				|| !convertView.getTag().equals(
						R.drawable.ic_launcher + itemType) ) {
			final WeatherBaseView weakFragment = mWeatherBaseViews
					.get(itemType);
			if (weakFragment != null ) {
				Log.i("Tag", "getView..." + "weakFragment = " + weakFragment);
				convertView = weakFragment;
			} else {
				switch (itemType) {
				case FORECAST_TYPE:
					convertView = (WeatherBaseView) mLayoutInflater.inflate(
							R.layout.weather_forecast, parent, false);
					break;
				case WEATHER_DETAILS_TYPE:
					convertView = (WeatherBaseView) mLayoutInflater.inflate(
							R.layout.weather_details, parent, false);
					break;
				case AQI_TYPE:
					convertView = (WeatherBaseView) mLayoutInflater.inflate(
							R.layout.weather_aqi, parent, false);
					break;
				case INDEX_TYPE:
					convertView = (WeatherBaseView) mLayoutInflater.inflate(
							R.layout.weather_index, parent, false);

					break;
				default:
					break;
				}
				mWeatherBaseViews.put(itemType,
						
								(WeatherBaseView) convertView);
			}
			convertView.setTag(R.drawable.ic_launcher + itemType);
		}

		if (convertView instanceof WeatherBaseView
				&& !WeatherSpider.isEmpty(mWeatherInfo)) {
			WeatherBaseView baseView = (WeatherBaseView) convertView;
			baseView.setWeatherInfo(mWeatherInfo);
			return baseView;
		}
		return convertView;
	}
}
