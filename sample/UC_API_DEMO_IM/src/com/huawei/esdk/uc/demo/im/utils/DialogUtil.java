package com.huawei.esdk.uc.demo.im.utils;

import com.huawei.common.res.LocContext;
import com.huawei.esdk.uc.demo.im.R;
import com.huawei.esdk.uc.demo.im.application.data.UCAPIApp;
import com.huawei.module.um.UmConstant;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class DialogUtil 
{
	public static boolean showDataLimitTip(Context context,long fileSize,OnClickListener onClickListener)
    {
        boolean hasWifi = DeviceUtil.isWifiConnect();
        if (hasWifi)
        {
            return false;
        }

        // 非wifi情况下提示size大小1M.
        if (fileSize > UmConstant.DOWNLOAD_PROMPT_SIZE)
        {
            ConfirmDialog dialog = new ConfirmDialog(context,
                    R.string.file_too_large_tip);
            dialog.setRightText(R.string.res_continue);
            dialog.setLeftText(R.string.btn_cancel);

            dialog.setRightButtonListener(onClickListener);
            dialog.show();
            return true;
        }
        return false;
    }
	
	/**
     * Show toast
     * @param context Context
     * @param message Message to show
     */
    public static void showToast(Context context, String message)
    {
        if (message == null)
        {
            return;
        }

        LayoutInflater inflater = (LayoutInflater) LocContext.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Toast toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        View contentView = inflater.inflate(R.layout.dialog_toast, null);
        toast.setView(contentView);
        toast.setGravity(Gravity.BOTTOM, 0, 185);
        TextView textView = (TextView) contentView.findViewById(R.id.dialog_message);
        textView.setText(message);
        toast.show();
    }
}
