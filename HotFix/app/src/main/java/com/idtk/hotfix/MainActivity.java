package com.idtk.hotfix;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.idtk.patch.PatchExecutor;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private final static String PATCH_PATH = Environment.getExternalStorageDirectory().getPath()+ File.separator+"hotfix"+File.separator +  "patch_dex.jar";
    private final static int READ_WRITE_SD_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkReadAndWriteSDPermission();
        Button hot = (Button) findViewById(R.id.hot_fix);
        Button load = (Button) findViewById(R.id.load_patch);
        hot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logHot();
            }
        });
        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logLoad();
            }
        });
    }

    private String test(String msg, int num) {
        return msg + num + "patch";
    }

    private void logHot(){
        Log.d("tag","instant hot");
        Toast.makeText(this,"exist bug",Toast.LENGTH_SHORT).show();
    }

    private void logLoad(){
        new PatchExecutor(getApplicationContext(),PATCH_PATH).loader();
        Log.d("tag","instant load");
    }

    public void checkReadAndWriteSDPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, READ_WRITE_SD_CODE);
            } else {
                Log.d("TAG","已有权限");
            }
        } else {
            Log.d("TAG","已有权限");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case READ_WRITE_SD_CODE:
                Log.d("TAG","已有权限");
                break;
        }
    }
}
