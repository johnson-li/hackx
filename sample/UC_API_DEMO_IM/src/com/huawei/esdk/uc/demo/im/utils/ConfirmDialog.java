package com.huawei.esdk.uc.demo.im.utils;

import com.huawei.esdk.uc.demo.im.R;
import com.huawei.esdk.uc.demo.im.widget.BaseDialog;

import android.content.Context;

public class ConfirmDialog extends BaseDialog
{
    public ConfirmDialog(Context context, String message)
    {
        super(context);
        setContentView(R.layout.dialog_confirm_title);
        setMessage(message);
        setCanceledOnTouchOutside(false);
        setLeftButtonListener(null);
        setRightButtonListener(null);
    }

    public ConfirmDialog(Context context, int resId)
    {
        this(context, context.getString(resId));
    }

}
