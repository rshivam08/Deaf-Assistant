package com.example.cardano.deafassistant;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cardano.deafassistant.Emergency_Signal_Recognition.ClassificationActivity;
import com.example.cardano.deafassistant.Sign_language_translation.SignActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView mEmergencyBtn = findViewById(R.id.emergency_btn);
        TextView mCommunicationBtn = findViewById(R.id.communication_btn);

        mEmergencyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ClassificationActivity.class));

            }
        });

        mCommunicationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Hello!!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, SignActivity.class));


            }
        });

    }

}
