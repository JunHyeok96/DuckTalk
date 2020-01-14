package com.talk.ducktalk;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.talk.ducktalk.R;

import com.talk.ducktalk.chat.MessageActivity;
import com.talk.ducktalk.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

public class PeopleProfile extends AppCompatActivity {
    private static final int PICK_FROM_ALBUM = 10;
    private  String destinationUid;
    private TextView id;
    private Button profile;
    private Button chat;
    private Button profileEdit;
    private ImageView profile_imageView;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_profile);
        destinationUid = getIntent().getStringExtra("destinationUid"); //채팅을 당하는 아이디
        chat= (Button) findViewById(R.id.people_profile_chat);
        profile=(Button) findViewById(R.id.people_profile_profile);
        profile_imageView=(ImageView)findViewById(R.id.people_profile_image);
        profileEdit = (Button)findViewById(R.id.people_profile_profileedit);
        LinearLayout profile_edit = (LinearLayout)findViewById(R.id.people_profile_profileedit_layout);
        LinearLayout profile_chat = (LinearLayout)findViewById(R.id.people_profile_chat_layout);

        id = (TextView)findViewById(R.id.people_profile_id);
        if(destinationUid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            profile_chat.setVisibility(View.GONE);
            profile_edit.setVisibility(View.VISIBLE);
        }else{
            profile_edit.setVisibility(View.VISIBLE);
            profile_edit.setVisibility(View.GONE);
        }
        FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
         @Override
         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                 UserModel userModel = snapshot.getValue(UserModel.class);
                 if (userModel.uid.equals(destinationUid)) {
                     Glide.with  //이미지와 사진 추가
                             (getApplicationContext())
                             .load(userModel.profileImageUrl)
                             .apply(new RequestOptions().circleCrop())
                             .into(profile_imageView);
                     id.setText(userModel.userName);
                 }
             }
         }
          @Override
         public void onCancelled(@NonNull DatabaseError databaseError) {

          }
          });

        profileEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);
}});
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext() , MessageActivity.class);
                intent.putExtra("destinationUid", destinationUid);
                ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(getApplicationContext() ,R.anim.fromright,R.anim.toleft);
                startActivity(intent,activityOptions.toBundle());
                finish();
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProfileView.class);
                intent.putExtra("destinationUid", destinationUid);
                startActivity(intent);
                finish();
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK){
            imageUri = data.getData(); // 이미지 경로 원본
            FirebaseStorage.getInstance().getReference().child("userImages").child(destinationUid).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    @SuppressWarnings("VisibleForTests")
                    Task<Uri> uriTask = FirebaseStorage.getInstance().getReference().child("userImages").child(destinationUid).getDownloadUrl();
                    while(!uriTask.isSuccessful());
                    Uri downloadUrl = uriTask.getResult();
                    String imageUrl = String.valueOf(downloadUrl);
                    UserModel userModel = new UserModel();
                    userModel.userName = id.getText().toString();
                    userModel.profileImageUrl = imageUrl;
                    userModel.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).setValue(userModel);
                }});
            Toast.makeText(getApplicationContext(),"프로필 변경완료.", Toast.LENGTH_SHORT).show();
            finish();
        }
}
}
