package com.example.samsung.indianpoker1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }
    public void login_click(View v){
        Intent intent_login = new Intent(getApplicationContext(), Main.class);
        startActivity(intent_login);
    }
}
