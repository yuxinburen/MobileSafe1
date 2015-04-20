package com.davie.mobilesafe;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.davie.mobilesafe.ui.SettingItemView;

/**
 * User: davie
 * Date: 15-4-20
 */
public class SettingActivity extends Activity {

    private SettingItemView siv_update;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        siv_update = (SettingItemView) findViewById(R.id.siv_update);
        siv_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }
}