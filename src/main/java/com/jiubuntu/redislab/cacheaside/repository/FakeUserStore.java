package com.jiubuntu.redislab.cacheaside.repository;

import com.jiubuntu.redislab.cacheaside.dto.UserProfile;
import com.jiubuntu.redislab.cacheaside.dto.UserProfileUpdateRequest;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class FakeUserStore {

    private final Map<Long, UserProfile> fakeDb = new HashMap<>();

    public FakeUserStore() {
        fakeDb.put(1L, new UserProfile("Kim", "kim@example.com", "Gold"));
        fakeDb.put(2L, new UserProfile("Lee", "lee@example.com", "Silver"));
    }

    public Optional<UserProfile> findById(Long userId) {
        return Optional.ofNullable(fakeDb.get(userId));
    }

    public Optional<UserProfile> update(Long userId, UserProfileUpdateRequest request) {
        UserProfile existing = fakeDb.get(userId);
        if (existing == null) {
            return Optional.empty();
        }

        existing.setName(request.getName());
        existing.setEmail(request.getEmail());
        existing.setTier(request.getTier());
        return Optional.of(existing);
    }
}
