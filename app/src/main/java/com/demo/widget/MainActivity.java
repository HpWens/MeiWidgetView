package com.demo.widget;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
    }

    public void onMei(View view) {
        startActivity(new Intent(this, com.demo.widget.meis.MainActivity.class));
    }

    public void onGuHong(View view) {
        startActivity(new Intent(this, com.demo.widget.guhong.MainActivity.class));
    }
}
