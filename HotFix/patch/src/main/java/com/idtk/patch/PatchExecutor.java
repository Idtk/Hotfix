package com.idtk.patch;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.idtk.base.ChangeFix;
import com.idtk.base.IPatchesLoader;
import com.idtk.base.PatchClassInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;

/**
 * Created by Idtk on 2017/9/20.
 */

public class PatchExecutor{
//    private final static String PATCH_PATH = Environment.getExternalStorageDirectory().getPath()+ File.separator+"hotfix"+File.separator +  "patch_dex.jar";

    private Context context;
    private String patchPath;

    public PatchExecutor(Context context,String patchPath) {
        super();
        this.context = context.getApplicationContext();
        this.patchPath = patchPath;
    }

    public void copy(String srcPath,String dstPath) throws IOException {
        File src=new File(srcPath);
        if(!src.exists()){
            throw new RuntimeException("source patch does not exist ");
        }
        File dst=new File(dstPath);
        if(!dst.getParentFile().exists()){
            dst.getParentFile().mkdirs();
        }
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    public void loader() {
        try {
            copy(patchPath,context.getCacheDir()+ File.separator+"hotfix"+File.separator + "patch.jar");
        }catch (IOException e){
            Log.d("TAG","复制失败");
            e.printStackTrace();
        }

        Log.d("path",patchPath+":"+context.getCacheDir().getAbsolutePath()+":"+context.getCacheDir().getPath()+":"+context.getCacheDir()+ File.separator+"hotfix"+File.separator + "patch.jar");
        DexClassLoader dexClassLoader = new DexClassLoader(context.getCacheDir()+ File.separator+"hotfix"+File.separator + "patch.jar"
                , context.getCacheDir().getAbsolutePath(), null, PatchExecutor.class.getClassLoader());
        try {
            Class patchLoaderClass = dexClassLoader.loadClass("com.idtk.hotfix.PatchesLoaderImpl");
            IPatchesLoader patchesLoader = (IPatchesLoader) patchLoaderClass.newInstance();
            for (PatchClassInfo patchClassInfo : patchesLoader.getPatchedClasses()) {
                Class patchClass = dexClassLoader.loadClass(patchClassInfo.getPatchClassName());
                Class patchedClass = dexClassLoader.loadClass(patchClassInfo.getPatchedClassName());
                Object newField = patchClass.newInstance();
                Field changeField = patchedClass.getDeclaredField("$changeFix");
                changeField.setAccessible(true);
                changeField.set(null,newField);
            }
            Toast.makeText(context,"load patch", Toast.LENGTH_SHORT).show();
            Log.d("TAG","加载成功");
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchFieldException e) {
            e.printStackTrace();
            Log.d("TAG","加载失败");
        }
    }
}
