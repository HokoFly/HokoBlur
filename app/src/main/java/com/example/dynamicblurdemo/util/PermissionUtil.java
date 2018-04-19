package com.example.dynamicblurdemo.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

/**
 * Created by yuxfzju on 16/8/29.
 */
public class PermissionUtil {

    private static final int REQUEST_CODE = 1001;

    public static void checkPermission(String permission, final Activity activity) {

            boolean hasPermission = ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
            if (!hasPermission) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showMessageOKCancel("You need to allow the permission: " + permission, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                            }
                        }
                    }, activity);
                    return;
                }

                ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                return;
            }
    }

    private static void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener, Context context) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}
