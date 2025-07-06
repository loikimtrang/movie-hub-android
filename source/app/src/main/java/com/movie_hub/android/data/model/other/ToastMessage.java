package com.movie_hub.android.data.model.other;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.movie_hub.android.R;

import es.dmoral.toasty.Toasty;
import lombok.Data;

@Data
public class ToastMessage {
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_SUCCESS = 1;
    public static final int TYPE_ERROR = 2;
    public static final int TYPE_WARNING = 3;

    private int type;
    private String message;

    public ToastMessage(int type, String message) {
        this.type = type;
        this.message = message;
    }

    public void showMessage(Context context){
        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(Looper.getMainLooper()).post(() -> showMessage(context));
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.layout_toast, null);

        ImageView icon = layout.findViewById(R.id.icon);
        TextView text = layout.findViewById(R.id.mgs);

        text.setText(message);
        Toast toast = new Toast(context);

        switch (type){
            case TYPE_NORMAL:
                icon.setImageResource(R.drawable.ic_bell);
                layout.setBackgroundResource(R.color.bg_toast_normal);
                break;
            case TYPE_SUCCESS:
                Toasty.success(context, message).show();
                break;
            case TYPE_WARNING:
                icon.setImageResource(R.drawable.ic_warning);
                layout.setBackgroundResource(R.color.bg_toast_warning);
                break;
            case TYPE_ERROR:
                Toasty.error(context,message).show();
                break;
            default:
                break;
        }
        toast.setView(layout);
        toast.setDuration(Toast.LENGTH_SHORT);
        int bottomOffset = context.getResources().getDimensionPixelSize(R.dimen._70sdp);
        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, bottomOffset);
        toast.show();
    }
}
