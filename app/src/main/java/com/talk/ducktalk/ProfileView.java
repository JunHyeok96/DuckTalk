package com.talk.ducktalk;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.talk.ducktalk.R;

import com.talk.ducktalk.model.UserModel;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileView extends AppCompatActivity {
    private  String destinationUid;
    private PhotoView profile_imageView;
    private Button exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);
        destinationUid = getIntent().getStringExtra("destinationUid"); //채팅을 당하는 아이디
        exit= (Button) findViewById(R.id.profile_view_button);
        profile_imageView=(PhotoView) findViewById(R.id.profile_view_imageView);
        FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    if (userModel.uid.equals(destinationUid)) {
                        Glide.with  //이미지와 사진 추가
                                (getApplicationContext())
                                .load(userModel.profileImageUrl)
                                .apply(new RequestOptions())
                                .into(profile_imageView);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
