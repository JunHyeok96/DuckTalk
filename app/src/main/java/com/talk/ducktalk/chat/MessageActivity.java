package com.talk.ducktalk.chat;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.talk.ducktalk.R;
import com.talk.ducktalk.model.ChatModel;
import com.talk.ducktalk.model.NotificationModel;
import com.talk.ducktalk.model.UserModel;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MessageActivity extends AppCompatActivity {
    private UserModel destinationUserModel;
    private String destinationUid;
    private Button button;
    private Button exitbutton;
    private String myName;
    private EditText editText;
    private TextView textView;
    private String uid;
    private String chatRoomUid;
    private RecyclerView recyclerView;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); //채팅을 요구하는 아이디 즉 단말기가 로그인된 UID
        destinationUid = getIntent().getStringExtra("destinationUid"); //채팅을 당하는 아이디
        button = (Button)findViewById(R.id.messageActivity_button);
        editText = (EditText)findViewById(R.id.messageActivity_editText);
        recyclerView = (RecyclerView)findViewById(R.id.messageActivity_recyclerView);
        textView = (TextView)findViewById(R.id.messageActivity_destname);
        FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel mymodel = new UserModel();
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    mymodel = item.getValue(UserModel.class);
                    if(mymodel.uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        myName = mymodel.userName;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel userModel = dataSnapshot.getValue(UserModel.class);
                textView.setText(userModel.userName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        exitbutton = (Button)findViewById(R.id.messageActivity_button_exit);
        exitbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editText.getText().length()==0){
                    return;
                }
                ChatModel chatModel =new ChatModel();
                chatModel.users.put(uid,true);
                chatModel.users.put(destinationUid, true);
                final ChatModel.Comment comment =new ChatModel.Comment();
                comment.uid =uid;
                comment.meassge = editText.getText().toString();
                comment.timestamp = ServerValue.TIMESTAMP;
                if(chatRoomUid==null) {
                    button.setEnabled(false);
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            checkChatRoom();
                        }
                    });
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment);
                            sendGcm();
                        }
                    }, 1000 );
                }else{
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment);
                    sendGcm();
                }
                editText.setText("");
            }

        });

        checkChatRoom();

    }
    void sendGcm(){
        Gson gson = new Gson();
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = destinationUserModel.pushToken;
        notificationModel.notification.title = myName;
        notificationModel.notification.text =  editText.getText().toString();
        notificationModel.data.title = myName;
        notificationModel.data.message = editText.getText().toString();

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=uf8"),gson.toJson(notificationModel));
        Request request = new Request.Builder()
                .header("Content-Type","application/json")
                .addHeader("Authorization","key=AIzaSyBlnYSpeUIozlPrO_ULgLroFbgvUumfs7Q")
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

            }
        });
        Map<String, Object> map = new HashMap<>();
        map.put("lastTime", ServerValue.TIMESTAMP);
        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).updateChildren(map);
    }

    void checkChatRoom(){
        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot item :dataSnapshot.getChildren()){
                    ChatModel chatModel = item.getValue(ChatModel.class);
                    if(chatModel.users.containsKey(destinationUid)){
                            chatRoomUid = item.getKey();
                            button.setEnabled(true);
                            recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
                            recyclerView.setAdapter(new RecyclerViewAdapter());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<ChatModel.Comment> comments;
        public RecyclerViewAdapter() {
            comments = new ArrayList<>();
            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        destinationUserModel = dataSnapshot.getValue(UserModel.class);
                        getMessageList();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
        void getMessageList(){
            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    comments.clear();

                    for(DataSnapshot item : dataSnapshot.getChildren()){
                        comments.add(item.getValue(ChatModel.Comment.class));
                    }
                    //메세지갱신
                    notifyDataSetChanged();
                    recyclerView.scrollToPosition(comments.size() -1 );
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_meassge,viewGroup,false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            MessageViewHolder messageViewHolder = ((MessageViewHolder) viewHolder);

            //내가보낸 메세지
            if (comments.get(i).uid.equals(uid)){
                messageViewHolder.textView_name.setText("");
                messageViewHolder.textView_message.setText(comments.get(i).meassge);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.right);
                messageViewHolder.linearLayout_destination.setVisibility(View.INVISIBLE);
                messageViewHolder.textView_message.setTextSize(12);
                messageViewHolder.linearLayout_main.setGravity(Gravity.RIGHT);
            }else{ //상대방이 보낸 메세지
                Glide.with(viewHolder.itemView.getContext()).
                        load(destinationUserModel.profileImageUrl).
                        apply(new RequestOptions().circleCrop())
                        .into(messageViewHolder.imageView_profile);
                messageViewHolder.textView_name.setText(destinationUserModel.userName);
                messageViewHolder.linearLayout_destination.setVisibility(View.VISIBLE);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.left);
                messageViewHolder.textView_message.setText(comments.get(i).meassge);
                messageViewHolder.textView_message.setTextSize(12);
                messageViewHolder.linearLayout_main.setGravity(Gravity.LEFT);
            }
            messageViewHolder.textView_message.setText(comments.get(i).meassge);
            long unixTime = (long)comments.get(i).timestamp;
            Date date =new Date(unixTime);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String time = simpleDateFormat.format(date);
            messageViewHolder.textView_time.setText(time);
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class MessageViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_message;
            public TextView textView_name;
            public TextView textView_time;
            public ImageView imageView_profile;
            public LinearLayout linearLayout_destination;
            public LinearLayout linearLayout_main;
            public MessageViewHolder(View view) {
                super(view);
                textView_message = view.findViewById(R.id.messageItem_textView_message);
                textView_name = view.findViewById(R.id.messageItem_textView_name);
                textView_time = view.findViewById(R.id.messageItem_textView_timestamp);
                imageView_profile = view.findViewById(R.id.messageItem_imageView_profile);
                linearLayout_destination = view.findViewById(R.id.messageItem_linearlayout_destination);
                linearLayout_main = view.findViewById(R.id.messageItem_linearlayout_main);
            }
        }
    }

    @Override
    public void onBackPressed() {
       // super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.fromleft,R.anim.toright);
    }
}
