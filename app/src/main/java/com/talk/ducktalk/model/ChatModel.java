package com.talk.ducktalk.model;

import java.util.HashMap;
import java.util.Map;

public class ChatModel {

    public Map<String, Boolean> users = new HashMap<>(); //채팅방 유저들
    public Map<String, Comment> comments = new HashMap<>();// 채팅방 내용
    public long lastTime;
    public static class Comment {
        public String uid;
        public String meassge;
        public Object timestamp;
    }

}
