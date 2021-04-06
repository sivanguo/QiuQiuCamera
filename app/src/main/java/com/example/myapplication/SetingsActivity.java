package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;

public class SetingsActivity extends AppCompatActivity {


    boolean wmFlag;
    private ImageButton back;
    private Switch wm_switch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setings);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("flag");
        wmFlag = bundle.getBoolean("WmFlag");
//        System.out.println(wmFlag+"-------------------------------------------------------------------------------");

        wm_switch = findViewById(R.id.wm_switch);
        back = findViewById(R.id.toBack);

        wm_switch.setChecked(wmFlag);

        wm_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    wmFlag=true;
                }else {
                    wmFlag=false;
                }
//                System.out.println("------------------------------------------"+wmFlag+"");
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(SetingsActivity.this, MainActivity.class);
                Bundle bundle1 = new Bundle();
                bundle1.putBoolean("WmFlag1",wmFlag);
                intent1.putExtra("flag1", bundle1);
                startActivity(intent1);
            }
        });
    }

}