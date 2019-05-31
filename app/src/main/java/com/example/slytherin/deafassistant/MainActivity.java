package com.example.slytherin.deafassistant;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private MediaRecorder myRecorder;  //used for recording
    private String outputFile = null;
    String nameOfFolder="Deaf Assistant";
    ProgressDialog dialog;
    private static final String BASE_URL = "http://192.168.0.102:5000/fun";
    int pd;
    CountDownTimer t;
    String nameOfFile;
    Button mVibrateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn=(Button)findViewById(R.id.recordBtn);
        mVibrateBtn = findViewById(R.id.vibrate);

        mVibrateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                vibrate();
            }
        });


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd=5000;
                record();
                /*t = new CountDownTimer(Long.MAX_VALUE, 3000) {
                    @Override
                    public void onTick(long l) {
                        record();
                    }

                    @Override
                    public void onFinish() {

                    }
                }.start();*/

            }
        });
    }


    public void record(){
        String filePath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+nameOfFolder;
        String currentDateAndTime=getCurrentDateAndTime();
        File dir=new File(filePath);
        if(!dir.exists()){
            dir.mkdirs();
        }

        outputFile = filePath+"/"+nameOfFile+"_"+currentDateAndTime+".mp3";

        myRecorder = new MediaRecorder();
        myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);  // setting the audio source as MIC
        myRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);  //set audio format 3gpp
        myRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC); //setting audio encoder
        myRecorder.setOutputFile(outputFile);   // sets the output path.

        // record part  -- START'S THE RECORDING
        try {
            Toast.makeText(this, "recording started", Toast.LENGTH_SHORT).show();
            myRecorder.prepare();
            myRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // prepare() fails
            e.printStackTrace();
        }


        //STOPS the record after 10 sec.
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //STOP
                try {
                    myRecorder.stop();
                    myRecorder.release();
                    Toast.makeText(getApplicationContext(), "Saved"+outputFile, Toast.LENGTH_SHORT).show();
                    upload();
                    //uploadFile2(Uri.fromFile(new File(outputFile)));
                    myRecorder = null;
                   /* AlarmManager alarmManager=(AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),30000,
                            pendingIntent);*/

                } catch (IllegalStateException e) {
                    // it is called before start()
                    e.printStackTrace();
                } catch (RuntimeException e) {
                    // no valid audio/video data has been received
                    e.printStackTrace();
                }
            }
        }, pd);
    }
    public String getCurrentDateAndTime(){
        Calendar c=Calendar.getInstance();
        SimpleDateFormat df= new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String formattedDate=df.format(c.getTime());
        return formattedDate;
    }
    private void upload(){
        if (outputFile != null) {
            dialog = ProgressDialog.show(MainActivity.this, "", "Uploading File...", true);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        //creating new thread to handle Http Operations
                        uploadFile(outputFile);

                    } catch (OutOfMemoryError e) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Insufficient Memory!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        dialog.dismiss();
                    }

                }
            }).start();
        } else {
            Toast.makeText(MainActivity.this, "Please choose a File First", Toast.LENGTH_SHORT).show();
        }

    }




    public int uploadFile(String outputFile) {

        int serverResponseCode = 0;
        String serverResponse = " ";
        OutputStream os;

        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";


        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = new File(outputFile);


        String[] parts = outputFile.split("/");
        final String fileName = parts[parts.length - 1];

        if (!selectedFile.isFile()) {
            dialog.dismiss();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
            return 0;
        } else {
            try {
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL("http://192.168.0.102:5000/fun");
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);//Allow Inputs
                connection.setDoOutput(true);//Allow Outputs
                connection.setUseCaches(false);//Don't use a cached Copy
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty(
                        "Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("file",outputFile);

                //creating new dataoutputstream
                dataOutputStream = new DataOutputStream(connection.getOutputStream());

                //writing bytes to data outputstream
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""
                        + outputFile + "\"" + lineEnd);

                dataOutputStream.writeBytes(lineEnd);

                //returns no. of bytes present in fileInputStream
                bytesAvailable = fileInputStream.available();
                //selecting the buffer size as minimum of available bytes or 1 MB
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];

                //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);


                //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                while (bytesRead > 0) {

                    try {

                        //write the bytes read from inputstream
                        dataOutputStream.write(buffer, 0, bufferSize);
                    } catch (OutOfMemoryError e) {
                        Toast.makeText(MainActivity.this, "Insufficient Memory!", Toast.LENGTH_SHORT).show();
                    }
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                try{

                    serverResponse = processResponse(connection, serverResponse);


                    serverResponseCode = connection.getResponseCode();
                }catch (OutOfMemoryError e){
                    Toast.makeText(MainActivity.this, "Memory Insufficient!", Toast.LENGTH_SHORT).show();
                }
                String serverResponseMessage = connection.getResponseMessage();

                Log.i("YEAHHH", "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);
                Log.i("YEAHHH_Response", "Server Response: " + serverResponse);




                //response code of 200 indicates the server status OK
                if (serverResponseCode == 200) {
                    final String finalServerResponse = serverResponse;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Done!! "+ finalServerResponse, Toast.LENGTH_SHORT).show();

                            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                            long[] pattern = {0, 1000, 500, 2000, 500, 3000};

                            // Vibrate for 500 milliseconds
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                v.vibrate(VibrationEffect.createOneShot(1000,  VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                //deprecated in API 26
                                v.vibrate(pattern, -1);
                            }

                        }
                    });
                }

                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();



            } catch (FileNotFoundException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "File Not Found", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "URL Error!", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (final IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            dialog.dismiss();
            return serverResponseCode;
        }

    }

    private static String processResponse(HttpURLConnection conn, String responseFromServer) {
        DataInputStream inStream;
        try {
            inStream = new DataInputStream(conn.getInputStream());
            String str;

            while ((str = inStream.readLine()) != null) {
                responseFromServer = str;
            }
            inStream.close();

        } catch (IOException ioex) {
            Log.e("Debug", "error: " + ioex.getMessage(), ioex);
        }
        return responseFromServer;
    }

    public void vibrate(){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        long[] pattern = {0, 1000, 500, 2000, 500, 3000};

        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(1000,  VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(pattern, -1);
        }
    }


}
