package com.example.mhpractice.features.user.service;

import com.example.mhpractice.features.user.service.result.UserProfileResult;

public interface UserService {

    public UserProfileResult getUserProfile(String email);

    public void updateUserProfile(String email, String name);

}
