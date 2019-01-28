package com.weilan.mynotify;

public class User {

    private static User instance;

    public static User instance() {
        if (instance == null) {
            instance = new User();
        }
        return instance;
    }

    public String merchant_id;

}
