package com.talk.ducktalk.fragment;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.talk.ducktalk.LoginActivity;

import com.talk.ducktalk.R;
import com.google.firebase.auth.FirebaseAuth;

public class AccontFragment extends Fragment {
    Activity activity;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accont, container,false);
        if(isAdded()) {
            Button button = (Button) view.findViewById(R.id.fragement_Account_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getView().getContext(), LoginActivity.class);
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(v.getContext(), R.anim.fromright, R.anim.toleft);
                    LoginActivity.insertData(0);
                    startActivity(intent, activityOptions.toBundle());
                    activity.finish();

                }
            });
        }
        return view;
    }
}
