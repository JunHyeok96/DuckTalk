package com.talk.ducktalk;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.talk.ducktalk.R;

import com.talk.ducktalk.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

public class SignupActivity extends AppCompatActivity {
    private static final int PICK_FROM_ALBUM = 10;
    private EditText email;
    private EditText password;
    private EditText password2;
    private EditText name;
    private Button signup;
    private ImageView profile;
    private Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        profile = (ImageView)findViewById(R.id.singupActivity_imageview_profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);
            }
        });
        email = (EditText)findViewById(R.id.singupActivity_edittext_email);
        password =(EditText)findViewById(R.id.singupActivity_edittext_password);
        password2 =(EditText)findViewById(R.id.singupActivity_edittext_password2);
        name =(EditText)findViewById(R.id.singupActivity_edittext_name);
        signup = (Button)findViewById(R.id.signActivity_button_signup);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(email.getText().toString().length()==0 ||password.getText().toString().length()==0
                        ||name.getText().toString().length()==0||password2.getText().toString().length()==0 ){
                    Toast.makeText(getApplicationContext(),"정보를 올바르게 입력해주세요!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!password.getText().toString().equals(password2.getText().toString())){
                    Toast.makeText(getApplicationContext(),"비밀번호가 서로 다릅니다!",Toast.LENGTH_SHORT).show();
                    password.setText("");
                    password2.setText("");
                    return;
                }
                if(password.getText().toString().length()<6 || password2.getText().toString().length()<6) {
                    Toast.makeText(getBaseContext(), "패스워드는 6자리 이상입니다!", Toast.LENGTH_SHORT).show();
                    password.setText("");
                    password2.setText("");
                    return;
                }
                if(imageUri ==null){
                    imageUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.basic_user_icon);
                }
                FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                            try{
                                final String uid =task.getResult().getUser().getUid();
                                FirebaseStorage.getInstance().getReference().child("userImages").child(uid).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        @SuppressWarnings("VisibleForTests")
                                        Task<Uri> uriTask = FirebaseStorage.getInstance().getReference().child("userImages").child(uid).getDownloadUrl();
                                        while(!uriTask.isSuccessful());
                                        Uri downloadUrl = uriTask.getResult();
                                        String imageUrl = String.valueOf(downloadUrl);
                                        UserModel userModel = new UserModel();
                                        userModel.userName = name.getText().toString();
                                        userModel.profileImageUrl = imageUrl;
                                        userModel.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel);
                                        if(task.isSuccessful()){
                                            Toast.makeText(getBaseContext() , "회원가입 완료!", Toast.LENGTH_SHORT).show();
                                            FirebaseAuth.getInstance().signOut();
                                            finish();
                                        }else {
                                            Toast.makeText(getBaseContext(), "회원가입 실패!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }catch(Exception e){
                                Toast.makeText(getBaseContext(), "이미 존재하는 이메일입니다!", Toast.LENGTH_SHORT).show();
                                email.setText("");}
                            }
                        });
            }
        });

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK){
            profile.setImageURI(data.getData()); //가운데 뷰를 바꿈
            imageUri = data.getData(); // 이미지 경로 원본
        }
}
}
