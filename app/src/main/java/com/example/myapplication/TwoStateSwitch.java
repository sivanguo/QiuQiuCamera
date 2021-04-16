package com.example.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TwoStateSwitch extends FrameLayout {

    private ImageView ivCheckOn,ivCheckOff;//两种状态的ImageView
    private SwitchStatusCheckListener switchStatusCheckListener;
    private boolean isCheck;

    public TwoStateSwitch(@NonNull Context context) {
        super(context,null);
    }

    public TwoStateSwitch(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.switch_status,this);
        ivCheckOn=findViewById(R.id.status_on);
        ivCheckOff=findViewById(R.id.status_off);

        ivCheckOn.setOnClickListener(new ClickListener());
        ivCheckOff.setOnClickListener(new ClickListener());

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TwoStateSwitch);
        int imageOn = typedArray.getResourceId(R.styleable.TwoStateSwitch_imageOn, -1);
        int imageOff = typedArray.getResourceId(R.styleable.TwoStateSwitch_imageOff, -1);
        setOnImage(imageOn);
        setOffImage(imageOff);

        typedArray.recycle();
        setCheckOff();//默认显示
    }

    private void setOffImage(int imageOff) {
        ivCheckOff.setImageResource(imageOff);
    }

    private void setOnImage(int imageOn) {
        ivCheckOn.setImageResource(imageOn);
    }

    public void setSwitchStatusCheckListener(SwitchStatusCheckListener switchStatusCheckListener){
        this.switchStatusCheckListener=switchStatusCheckListener;
    }
    public interface SwitchStatusCheckListener{
        void ViewCheckOnMethod(int viewId);
        void ViewCheckOffMethod(int viewId);
    }

    private class ClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.status_on:
                    setCheckOff();
                    switchStatusCheckListener.ViewCheckOffMethod(getId());
                    break;
                case R.id.status_off:
                    setCheckOn();
                    switchStatusCheckListener.ViewCheckOnMethod(getId());
                    break;
            }
        }
    }

    private void setCheckOn() {
        isCheck=true;
        ivCheckOff.setVisibility(GONE);
        ivCheckOn.setVisibility(VISIBLE);
    }

    private void setCheckOff() {
        isCheck=false;
        ivCheckOn.setVisibility(GONE);
        ivCheckOff.setVisibility(VISIBLE);
    }

}
