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

        /*
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        */

        TextView Btn1 = findViewById(R.id.btn1);
        TextView Btn2 = findViewById(R.id.btn2);

        Btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ClassificationActivity.class));

            }
        });

        Btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Hello!!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, SignActivity.class));


            }
        });

    }

}
