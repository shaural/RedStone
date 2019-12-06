package com.cs407.team15.redstone.ui.publicboard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cs407.team15.redstone.MainActivity;
import com.cs407.team15.redstone.R;
import com.cs407.team15.redstone.ui.post.PostActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PublicBoardActivity extends AppCompatActivity {
    private String TAG = getClass().toString();
    private Context mContext;

    private ViewPager viewPager;
    private TabLayout mTabLayout;
    private PbTabsPagerAdapter tabsPagerAdapter;
    private TextView pb_title;
    private String location;

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_board);

        mContext = getApplicationContext();

        Toolbar toolbar = (Toolbar) findViewById(R.id.pb_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        pb_title = findViewById(R.id.pb_title);
        Intent intent = getIntent();
        location = intent.getStringExtra("area");

        tabsPagerAdapter = new PbTabsPagerAdapter(mContext, getSupportFragmentManager(), location);

        viewPager = findViewById(R.id.pb_view_pager);
        viewPager.setAdapter(tabsPagerAdapter);

        mTabLayout = findViewById(R.id.pb_tabs);
        mTabLayout.setupWithViewPager(viewPager);

        fab = findViewById(R.id.pb_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, PostActivity.class);
                intent.putExtra("area", location);
                intent.putExtra("path", "public");
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Title
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("PUBLIC_BOARD");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(location).exists()){
                    Log.e(TAG, dataSnapshot.child(location).getValue().toString());
                    pb_title.setText(dataSnapshot.child(location).getValue().toString());
                } else{

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
