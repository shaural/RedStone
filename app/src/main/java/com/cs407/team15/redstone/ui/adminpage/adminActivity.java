package com.cs407.team15.redstone.ui.adminpage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.cs407.team15.redstone.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

public class adminActivity extends AppCompatActivity {
    private Context mContext;

    private ViewPager viewPager;
    private TabsPagerAdapter tabsPagerAdapter;
    private TabLayout mTabLayout;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        mContext = getApplicationContext();

        tabsPagerAdapter = new TabsPagerAdapter(this, getSupportFragmentManager());

        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(tabsPagerAdapter);

        mTabLayout = findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(viewPager);

        fab = findViewById(R.id.fab_admin);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PostActivity.class);
                startActivity(intent);
            }
        });
    }

}
