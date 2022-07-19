package net.rekall.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;

public class AppFilePath {



    public static String Wallet_DIR;


    public static void init(Context context) {

        Wallet_DIR = context.getFilesDir().getAbsolutePath() + "/wallet";
    }

    /**
     *
     *
     * @param context
     * @return
     */
    public static File getExternalFilesDir(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            File path = context.getExternalFilesDir(null);

            if (path != null) {
                return path;
            }
        }
        final String filesDir = "/Android/data/" + context.getPackageName() + "/files/";
        return new File(Environment.getExternalStorageDirectory().getPath() + filesDir);
    }

    /**
     *
     *
     * @param context
     * @return
     */
    public static File getExternalCacheDir(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            File path = context.getExternalCacheDir();
            if (path != null) {
                return path;
            }
        }
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }


    public static File getImagePath(Context context) {
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }


    public static String getExternalPrivatePath(String name) {
        String namedir = "/" + name + "/";
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {

            return Environment.getExternalStorageDirectory().getPath() + namedir;
        } else {
            return null;
//            return new File(DATA_ROOT_DIR_OUTER, name).getPath();
        }
    }
}
