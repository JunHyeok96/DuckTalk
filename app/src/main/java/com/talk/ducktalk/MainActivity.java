package com.talk.ducktalk;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.talk.ducktalk.R;

import com.talk.ducktalk.fragment.AccontFragment;
import com.talk.ducktalk.fragment.ChatFragment;
import com.talk.ducktalk.fragment.PeopleFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.talk.ducktalk.model.UserModel;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private FrameLayout splashLayout;
    private FrameLayout frameLayout;
    private BottomNavigationView bottomNavigationView;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    private static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        splashLayout = (FrameLayout) findViewById(R.id.splash_frame);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.mainactivity_bottomnavigationview);
        frameLayout = (FrameLayout) findViewById(R.id.mainactivity_frame);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //전체화면만들기
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.default_config);
        mFirebaseRemoteConfig.fetch(0)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                        } else {
                        }
                        displayMessage();
                    }
                });
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 100:
                        dismissSplash();
                        break;
                    case 101:
                        showSplash();
                        break;
                }
                super.handleMessage(msg);
            }
        };
        handler.sendEmptyMessageDelayed(101, 0);
        handler.sendEmptyMessageDelayed(100, 1000);
        getSupportFragmentManager().beginTransaction().replace(R.id.mainactivity_frame, new PeopleFragment()).commit();
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_people:
                        getSupportFragmentManager().beginTransaction().replace(R.id.mainactivity_frame, new PeopleFragment()).commit();
                        return true;
                    case R.id.action_chat:
                        getSupportFragmentManager().beginTransaction().replace(R.id.mainactivity_frame, new ChatFragment()).commit();
                        return true;
                    case R.id.action_account:
                        getSupportFragmentManager().beginTransaction().replace(R.id.mainactivity_frame, new AccontFragment()).commit();
                        return true;

                }
                return false;
            }
        });
        passPushTokenToServer();
    }

    private void showSplash() {
        splashLayout.setVisibility(View.VISIBLE);
        frameLayout.setVisibility(View.GONE);
        bottomNavigationView.setVisibility(View.GONE);

    }

    private void dismissSplash() {
        splashLayout.setVisibility(View.GONE);
        frameLayout.setVisibility(View.VISIBLE);
        bottomNavigationView.setVisibility(View.VISIBLE);
    }

    void displayMessage() {
        String splash_background = mFirebaseRemoteConfig.getString("splash_background");
        boolean caps = mFirebaseRemoteConfig.getBoolean("splash_message_caps");
        String splash_message = mFirebaseRemoteConfig.getString("splash_message");

        if (caps) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(splash_message).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setCancelable(false);
            builder.create().show();
        } else {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            PeopleFragment mainFragment = (PeopleFragment)getSupportFragmentManager().findFragmentById(R.id.mainactivity_frame);
            mainFragment.setImageView_profile();
        }
        if (requestCode == 102) {
                ChatFragment chatFragment = (ChatFragment)getSupportFragmentManager().findFragmentById(R.id.mainactivity_frame);
                chatFragment.setChatroom();
            }


        }

        void passPushTokenToServer () {
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    Map<String, Object> map = new HashMap<>();
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    String deviceToken = instanceIdResult.getToken();
                    map.put("pushToken", deviceToken);
                    FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(map);
                }
            });
        }
    }

