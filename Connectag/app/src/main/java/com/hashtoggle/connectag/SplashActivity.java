package com.hashtoggle.connectag;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by MIPUNG on 2018. 7. 25..
 */
public class SplashActivity extends Activity {
    protected void onCreate(Bundle savedInstaneState)
    {
        super.onCreate(savedInstaneState);

        try
        {
            Thread.sleep(3000); //3초 대기
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        startActivity(new Intent(this, MainActivity.class));

        finish();
    }
}
