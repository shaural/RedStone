package com.cs407.team15.redstone.ui.home;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.cs407.team15.redstone.R;
import com.cs407.team15.redstone.model.Notices;

public class NoticeActivity extends AppCompatActivity {

    Notices notice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);

        if (getIntent().hasExtra("selected_notice")){
            notice = getIntent().getParcelableExtra("selected_notice");

            Context context = this.getApplicationContext();
            CharSequence text = "Title: " + notice.getTitle() + "\nWriter: " + notice.getWriter();
            int duration = Toast.LENGTH_LONG;
            Toast.makeText(context, text, duration).show();
        }
    }
}
