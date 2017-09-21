package com.idtk.hotfix;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.idtk.base.ChangeFix;
import com.idtk.patch.PatchExecutor;

import java.io.File;


/**
 * Created by mzc on 2017/9/18.
 */

public class MainActivityPatch extends AppCompatActivity implements ChangeFix{

    private final static String PATCH_PATH = Environment.getExternalStorageDirectory().getPath()+ File.separator+"hotfix"+File.separator +  "patch_dex.jar";

    @Override
    public Object accessDispatch(boolean isStatic, String nameDesc, Object[] paramsArray) {
        Log.d("tag","dispatch:"+nameDesc);
        switch (nameDesc){
//            case "onCreate.(Landroid/os/Bundle;)V":
//                onCreate((MainActivity)paramsArray[0],(Bundle)paramsArray[1]);
//                break;
            case "access$000.(Lcom/idtk/hotfix/MainActivity;)V":
//            case "logHot.()V":
                logHot((MainActivity)paramsArray[0]);
                break;
//            case "access$100.(Lcom/idtk/hotfix/MainActivity;)V":
//            case "logLoad.()V":
//                logLoad((MainActivity)paramsArray[0]);
//                break;
            default:
                throw new RuntimeException("patch not find the Method");
        }
        return null;
    }

    @Override
    public boolean isSupport(String nameDesc) {
        Log.d("tag","isSupport"+":"+nameDesc);
        return "access$000.(Lcom/idtk/hotfix/MainActivity;)V".contains(nameDesc);
//        return "logHot.()V:logLoad.()V:access$000.(Lcom/idtk/hotfix/MainActivity;)V:access$100.(Lcom/idtk/hotfix/MainActivity;)V:".contains(nameDesc);
    }


    protected void onCreate(MainActivity activity,Bundle savedInstanceState) {
        activity.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button hot = (Button) findViewById(R.id.hot_fix);
        Button load = (Button) findViewById(R.id.load_patch);
        hot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logHot(activity);
            }
        });
        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logLoad(activity);
            }
        });
    }

    private void logHot(MainActivity activity){
        Log.d("tag","instant hot");
        Toast.makeText(activity,"fix success",Toast.LENGTH_SHORT).show();
    }

    private void logLoad(MainActivity activity){
        Log.d("tag","instant load");
        new PatchExecutor(activity.getApplicationContext(),PATCH_PATH).loader();
    }
}
