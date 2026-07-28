package com.jiubuntu.redislab.cacheaside.controller;

import com.jiubuntu.redislab.cacheaside.dto.UserProfile;
import com.jiubuntu.redislab.cacheaside.dto.UserProfileUpdateRequest;
import com.jiubuntu.redislab.cacheaside.repository.FakeUserStore;
import com.jiubuntu.redislab.common.redis.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Cache-Aside", description = "Cache-Aside 패턴 예제 API")
public class UserCacheController {

    private static final Duration CACHE_TTL = Duration.ofSeconds(300);
    private final RedisService redisService;
    private final FakeUserStore fakeUserStore;

    @Operation(summary = "유저 프로필 조회", description = "캐시를 먼저 조회하고, 없으면 DB를 조회한 뒤 캐시에 저장한다. (Cache-Aside Read)")
    @GetMapping(value = "/user/{userId}")
    public UserProfile getUser(
            @PathVariable(value = "userId") Long userId
    ) {
        String cacheKey = cacheKey(userId);

        // redis 에서 캐시 확인
        UserProfile cached = redisService.get(cacheKey, UserProfile.class);
        if (cached != null) {
            log.info("Cache Hit! (userId={})", userId);
            return cached;
        }

        // Cache Miss 시 실제 DB 조회
        log.info("Cache Miss! Fetching from DB (userId={})", userId);
        UserProfile userProfile = fakeUserStore.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));


        // 실제 DB에서 데이터를 가져온다고 가정 (지연 1초)
        simulateSlowDbCall();


        // Redis에 데이터 저장
        redisService.set(cacheKey, userProfile, CACHE_TTL);
        return userProfile;
    }

    @Operation(summary = "유저 프로필 수정", description = "DB 데이터를 수정한 뒤, 관련 캐시를 삭제한다. (Cache Invalidation)")
    @PutMapping(value = "/user/{userId}")
    public UserProfile updateUser(
            @PathVariable(value = "userId") Long userId,
            @RequestBody UserProfileUpdateRequest request
    ) {
        // 1. 실제 DB 데이터 업데이트
        UserProfile updated = fakeUserStore.update(userId, request)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // 2. 기존 캐시 삭제 (Cache Invalidation)
        // 다음 조회 시 Cache Miss가 발생해 DB의 최신 데이터를 다시 캐싱하게 만든다.
        redisService.delete(cacheKey(userId));
        log.info("Cache Invalidated! (userId={})", userId);

        return updated;
    }




    private String cacheKey(Long userId) {
        return "user:profile:" + userId;
    }

    private void simulateSlowDbCall() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while simulating DB latency", e);
        }
    }
}
