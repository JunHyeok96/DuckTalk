package com.talk.ducktalk;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    public  static SQLiteDatabase database;
    private Button login;
    private Button signup;
    private EditText id;
    private EditText password;
    private FirebaseAuth firebaseAuth; //로그인
    private FirebaseAuth.AuthStateListener authStateListener; //로그인 정보가 맞으면 다음으로 이동
    private CheckBox checkBox;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        openDatabase("DB");
        DatabaseHelper databaseHelper = new DatabaseHelper(this,"DB", null,4);
        database = databaseHelper.getWritableDatabase();
        databaseHelper.onCreate(database);
        firebaseAuth = FirebaseAuth.getInstance();
        if(selectData("체크박스정보")==0){
            firebaseAuth.signOut();
        }
        login = (Button)findViewById(R.id.loginActivity_button_login);
        signup = (Button)findViewById(R.id.loginActivity_button_singup);
        checkBox =(CheckBox)findViewById(R.id.checkBox);
        checkBox.setChecked(true);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.fromright,R.anim.toleft);
                startActivity(new Intent(LoginActivity.this,SignupActivity.class) ,activityOptions.toBundle());
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginEvent();
            }
        });
        id = (EditText)findViewById(R.id.loginActivity_edittext_id);
        password= (EditText)findViewById(R.id.loginActivity_edittext_password);
        //로그인 인터페이스 리스너
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){   //유저가있을때
                    //로그인
                    Intent intent =new Intent(LoginActivity.this , MainActivity.class);
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.fromright,R.anim.toleft);
                    startActivity(intent,activityOptions.toBundle());
                    finish();
                }else{
                    //로그아웃
                }
            }
        };

    }

    void loginEvent() {
        firebaseAuth.signInWithEmailAndPassword(id.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    //로그인 실패한 부분
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show(); //실패 메세지
                }else{
                    if (checkBox.isChecked()){
                        insertData(1);
                    }else{
                        insertData(0);
                    }
                }

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }
    class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String tableName= "체크박스정보";
            String sql = "CREATE TABLE IF NOT EXISTS " +tableName +
                    "(_id integer PRIMARY KEY autoincrement, checkbox integer)";
            db.execSQL(sql);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
    public void openDatabase(String databaseName){
        DatabaseHelper helper = new DatabaseHelper(this, databaseName, null, 4);
        database = helper.getWritableDatabase();
    }
    public int selectData(String tableName){
        int checkbox = 0;
        if(database!=null){
            String sql ="select checkbox from "+tableName;
            Cursor cursor = database.rawQuery(sql, null);
            if(cursor.getCount()!=0) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToNext();
                }
                checkbox = cursor.getInt(0);
            }

            cursor.close();
        }
            return checkbox;
    }
    public static void insertData(int checkbox){
        if(database != null){
            String sql = "insert into 체크박스정보(checkbox) values(?)";
            Object[] params = {checkbox};
            database.execSQL(sql,params);
        }else{
        }
    }
}