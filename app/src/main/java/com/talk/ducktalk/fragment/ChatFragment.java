package com.talk.ducktalk.fragment;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.talk.ducktalk.MainActivity;
import com.talk.ducktalk.R;
import com.talk.ducktalk.chat.MessageActivity;
import com.talk.ducktalk.model.ChatModel;
import com.talk.ducktalk.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

public class ChatFragment extends Fragment {
    MainActivity activity;
    RecyclerView recyclerView;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity)getActivity();

    }
    @Override
    public void onDetach() {
        super.onDetach();
        activity=null;
    }

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container,false);

            recyclerView = view.findViewById(R.id.chatfragment_recyclerView);
            recyclerView.setAdapter(new ChatRecyclerViewAdapter());
            recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
            FirebaseDatabase.getInstance().getReference().child("chatrooms").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    recyclerView.setAdapter(new ChatRecyclerViewAdapter());
                    recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

        return view;
    }
    class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private List<ChatModel> chatModels = new ArrayList<ChatModel>();
        private List<ChatModel> timesort = new ArrayList<ChatModel>();

        private String uid;
        private ArrayList<String> destinationUsers = new ArrayList<String>();
        public ChatRecyclerViewAdapter() {
            uid= FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    chatModels.clear();
                    timesort.clear();
                    for(DataSnapshot item : dataSnapshot.getChildren()){
                        timesort.add(item.getValue(ChatModel.class));
                    }
                    Collections.sort(timesort, new Comparator<ChatModel>() {
                        @Override
                        public int compare(ChatModel o1, ChatModel o2) {
                            if(o1.lastTime-o2.lastTime >0){
                                return 1;
                            }else if(o1.lastTime-o2.lastTime <0){
                                return -1;
                            }
                            else
                                return 0;
                        }
                    });
                    Collections.reverse(timesort);
                    chatModels = timesort;
                    notifyDataSetChanged();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat, viewGroup ,false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int i) {
            final CustomViewHolder customViewHolder = (CustomViewHolder)viewHolder;
            String destinationUid = null;
            //일일이 챗방에 있는 유저 체크
            for(String user: chatModels.get(i).users.keySet()){
                if(!user.equals(uid)){
                    destinationUid = user;
                    destinationUsers.add(destinationUid);
                }
            }
            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserModel userModel = dataSnapshot.getValue(UserModel.class);
                    if(isAdded()) {
                        Glide.with(customViewHolder.itemView.getContext())
                                .load(userModel.profileImageUrl)
                                .apply(new RequestOptions().circleCrop())
                                .into(customViewHolder.imageView);

                        customViewHolder.textView1_title.setText(userModel.userName);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            //메세지를 내림차순으로 정렬후 마지막 메세지의 키값을 가져옴
            Map<String, ChatModel.Comment> commentMap = new TreeMap<>(Collections.reverseOrder());
            commentMap.putAll(chatModels.get(i).comments);
            String lastMessageKey =(String)commentMap.keySet().toArray()[0];
            customViewHolder.textView2_lastMessage.setText(chatModels.get(i).comments.get(lastMessageKey).meassge);

            customViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent =new Intent(v.getContext(), MessageActivity.class);
                    intent.putExtra("destinationUid", destinationUsers.get(i));
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(v.getContext(), R.anim.fromright,R.anim.toleft);
                    activity.startActivityForResult(intent,102, activityOptions.toBundle());
                }
            });
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            long unixTime = (long)chatModels.get(i).comments.get(lastMessageKey).timestamp;
            Date date =new Date(unixTime);
            customViewHolder.textView_time.setText(simpleDateFormat.format(date));
        }

        @Override
        public int getItemCount() {
            return chatModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textView1_title;
            public  TextView textView2_lastMessage;
            public  TextView textView_time;
            public CustomViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.chatitem_imageView);
                textView1_title = view.findViewById(R.id.chatitem_textView_title);
                textView2_lastMessage = view.findViewById(R.id.chatitem_textView_lastMessage);
                textView_time = view.findViewById(R.id.chatitem_textView_timestamp);
            }
        }
    }
    public void setChatroom(){
        FirebaseDatabase.getInstance().getReference().child("chatrooms").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recyclerView.setAdapter(new ChatRecyclerViewAdapter());
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
