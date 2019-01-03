package com.zhijia.hotelpad;


import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by rxnh8 on 2018/7/18.
 */

public class QrPopDialog extends Dialog {
    public QrPopDialog(Context context) {
        super(context);
    }

    public QrPopDialog(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        private Context context;

        public Builder(Context context) {
            this.context = context;
        }

        public QrPopDialog create(int layoutId) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final QrPopDialog dialog = new QrPopDialog(context, R.style.Dialog);
            View layout = inflater.inflate(layoutId, null);
            dialog.addContentView(layout, new WindowManager.LayoutParams(
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    , android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
            dialog.setContentView(layout);

            return dialog;
        }
    }
}
