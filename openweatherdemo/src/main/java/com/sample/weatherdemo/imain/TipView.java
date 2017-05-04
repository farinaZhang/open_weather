package com.sample.weatherdemo.imain;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.sample.weatherdemo.R;

import butterknife.ButterKnife;

/**
 * Created by FarinaZhang on 2017/4/21.
 * 提示画面
 */
public class TipView extends LinearLayout{
    private Context mContext;

    public TipView(Context context) {
        this(context,null);
    }

    public TipView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TipView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mContext = context;

        View curview = LayoutInflater.from(mContext).inflate(R.layout.layout_tip_view, null);
        this.addView(curview);
        ButterKnife.inject(this, curview);
    }
}
