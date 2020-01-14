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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.talk.ducktalk.MainActivity;
import com.talk.ducktalk.PeopleProfile;
import com.talk.ducktalk.R;

import com.talk.ducktalk.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PeopleFragment extends Fragment {
    MainActivity activity;
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


    ImageView imageView_profile;
    TextView textView_id;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            final View view = inflater.inflate(R.layout.fragment_people, container, false);
            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.peoplefragment_recyclerview);
            recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
            recyclerView.setAdapter(new PeopleFragmentRecyclerViewAdapter());
            final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            imageView_profile = (ImageView) view.findViewById(R.id.fragement_People_imageview);
            textView_id = (TextView) view.findViewById(R.id.fragement_People_textview);
            LinearLayout myProfile = (LinearLayout) view.findViewById(R.id.my_profile);
            FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        UserModel userModel = snapshot.getValue(UserModel.class);
                        if(isAdded()){
                        if (userModel.uid.equals(myUid)) {
                            Glide.with  //이미지와 사진 추가
                                    (getContext())
                                    .load(userModel.profileImageUrl)
                                    .apply(new RequestOptions().circleCrop())
                                    .into(imageView_profile);
                            textView_id.setText(userModel.userName);
                            return;
                        }
                    }}
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            myProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getView().getContext(), PeopleProfile.class);
                    intent.putExtra("destinationUid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(getView().getContext(), R.anim.fromright, R.anim.toleft);
                    activity.startActivityForResult(intent, 101, activityOptions.toBundle());
                }
            });

            return view;

    }

    class PeopleFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        List<UserModel> userModels;

        public PeopleFragmentRecyclerViewAdapter() {
            userModels = new ArrayList<>();
            final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userModels.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        UserModel userModel = snapshot.getValue(UserModel.class);
                        if (userModel.uid.equals(myUid)) {
                            continue;
                        } else {
                            userModels.add(userModel);
                        }
                    }
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
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_friend, viewGroup, false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int i) {
            Glide.with  //이미지와 사진 추가
                    (viewHolder.itemView.getContext())
                    .load(userModels.get(i).profileImageUrl)
                    .apply(new RequestOptions().circleCrop())
                    .into(((CustomViewHolder) viewHolder).imageView);
            ((CustomViewHolder) viewHolder).textView.setText(userModels.get(i).userName);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getView().getContext(), PeopleProfile.class);
                    intent.putExtra("destinationUid", userModels.get(i).uid);
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(getView().getContext(), R.anim.fromright, R.anim.toleft);
                    activity.startActivity(intent, activityOptions.toBundle());
                }
            });
        }

        @Override
        public int getItemCount() {
            return userModels.size();
        }
    }

    private class CustomViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView;

        public CustomViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.frienditem_imageview);
            textView = (TextView) view.findViewById(R.id.frienditem_textview);
        }
    }

    public void setImageView_profile(){
        FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    if(isAdded()){
                    if (userModel.uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        Glide.with  //이미지와 사진 추가
                                (getContext())
                                .load(userModel.profileImageUrl)
                                .apply(new RequestOptions().circleCrop())
                                .into(imageView_profile);
                    }}
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
