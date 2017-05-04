package com.sample.weatherdemo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.sample.weatherdemo.android.volley.RequestQueue;
import com.sample.weatherdemo.android.volley.toolbox.Volley;
import com.sample.weatherdemo.common.util.SystemUtils;
import com.sample.weatherdemo.imain.CommunicationAssist;

public class MyApplication extends Application{
	 private static MyApplication myApplication;
	   private static CommunicationAssist mActivityToServiceListener;
	   private static CommunicationAssist mServiceToActivityListener;
	   public static Activity assitActivity;
	   
	   public static MyApplication getInstance()
	   {
		   return myApplication;
	   }
	   
	   
	   public Context getContext(){
		   return getApplicationContext();
	   } 
	   
	   public void setActivityToServiceListener(CommunicationAssist listener)
	   {
		   mActivityToServiceListener = listener;
	   }
	   
	   public void setServiceToActivityListener(CommunicationAssist listener)
	   {
		   mServiceToActivityListener = listener;
	   }
	   
	   public  CommunicationAssist getActivityToServiceListener()
	   {
		   return mActivityToServiceListener;
	   }
	   public  CommunicationAssist getServiceToActivityListener()
	   {
		   return mServiceToActivityListener;
	   }


	private static RequestQueue mVolleyRequestQueue;

	public static synchronized RequestQueue getVolleyRequestQueue() {
		if (mVolleyRequestQueue == null)
			mVolleyRequestQueue = Volley.newRequestQueue(myApplication);
		return mVolleyRequestQueue;
	}
	   
	   @Override
		public void onCreate() {
		   super.onCreate();
		   myApplication = this;
		   SystemUtils.copyDB(this);// 程序第一次运行将数据库copy过去
	    }
}
