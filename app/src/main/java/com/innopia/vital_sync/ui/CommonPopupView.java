package com.innopia.vital_sync.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

public class CommonPopupView{

    private AlertDialog popupDialog;

    public CommonPopupView(Context context, View view){
        popupDialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();
    }

    public void show(){
        if(popupDialog == null || popupDialog.isShowing()){
            return;
        }
        new Handler(Looper.getMainLooper()).post(() -> popupDialog.show());
    }

    public boolean isShowing(){
        if(popupDialog != null){
            return popupDialog.isShowing();
        }
        return false;
    }

    public void dismiss(){
        if(popupDialog != null){
            popupDialog.dismiss();
        }
    }
}
