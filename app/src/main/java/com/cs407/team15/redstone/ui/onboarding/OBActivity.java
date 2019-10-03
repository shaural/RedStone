package com.cs407.team15.redstone.ui.onboarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import com.cs407.team15.redstone.R;
import com.cs407.team15.redstone.ui.authentication.LoginActivity;

public class OBActivity extends AppCompatActivity {

    ViewPager viewPager;
    SlideAdaptor slideAdaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        final String PREFS_NAME = "PrefsFile";
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        //for testing
        //settings.edit().putBoolean("first_time", true).apply();
        if (settings.getBoolean("first_time", true)) {
            //the app is being launched for first time, do something
            setContentView(R.layout.activity_onboarding);

            viewPager = findViewById(R.id.viewPager_id);
            slideAdaptor = new SlideAdaptor(this);
            viewPager.setAdapter(slideAdaptor);

            // record the fact that the app has been started at least once
            settings.edit().putBoolean("first_time", false).apply();
        }
        else{
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    public void signUpClicked(View view){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
