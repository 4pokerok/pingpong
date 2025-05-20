package com.example.pong;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

public class HelpActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        Button backButton = findViewById(R.id.btn_back);
    }
}