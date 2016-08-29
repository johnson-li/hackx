package com.huawei.esdk.uc.demo.im;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

import com.huawei.esdk.uc.demo.im.application.data.UCAPIApp;

public abstract class BaseActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        UCAPIApp.getApp().addActivity(this);
        
        initializeData();
        
        initializeComposition();

        initializeTitle();
     
    }

    @Override
    public void onBackPressed()
    {
        // super.onBackPressed();
        UCAPIApp.getApp().popActivity(this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        clearData();
    }
    
    public abstract void initializeData();

    public abstract void initializeComposition();
    
    public abstract void clearData();

	protected void onBack() {
		// TODO Auto-generated method stub
		
	}
	
	protected void initializeTitle()
    {
        View backLayout = findViewById(R.id.btn_back);
        if (null != backLayout)
        {
            backLayout.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    onBack();
                }
            });
        }
    }

}
