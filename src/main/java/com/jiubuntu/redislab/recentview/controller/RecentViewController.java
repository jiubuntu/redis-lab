package com.jiubuntu.redislab.recentview.controller;

import com.jiubuntu.redislab.recentview.dto.RecentViewResponse;
import com.jiubuntu.redislab.common.redis.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Recent-View", description = "Redis List를 활용한 최근 본 상품 목록 예제 API (고정 크기 큐 사용)")
public class RecentViewController {

    private static final int MAX_RECENT_VIEWS = 5;
    private final RedisService redisService;

    @Operation(summary = "상품 조회(최근 본 상품 갱신)", description = "LREM으로 중복을 제거한 뒤, LPUSH + LTRIM으로 최근 N개만 유지한다. (Fixed-size Queue)")
    @PostMapping(value = "/products/{productId}/view")
    public void viewProduct(
            @PathVariable(value = "productId") Long productId,
            @RequestParam(value = "userId", defaultValue = "1") Long userId
    ) {
        String key = recentViewsKey(userId);

        // 1. 기존 리스트에 동일한 상품이 있다면 제거 (중복 방지 및 최신순 유지)
        redisService.remove(key, 0, productId);

        // 2. 최신 상품을 맨 앞에 추가
        redisService.leftPush(key, productId);

        // 3. 최근 N개만 남기고 자르기 (고정 크기 유지)
        redisService.trim(key, 0, MAX_RECENT_VIEWS - 1);

        log.info("Recent view updated (userId={}, productId={})", userId, productId);
    }

    @Operation(summary = "최근 본 상품 목록 조회", description = "고정 크기로 유지되는 최근 본 상품 목록을 최신순으로 조회한다.")
    @GetMapping(value = "/users/{userId}/recent-views")
    public RecentViewResponse getRecentViews(
            @PathVariable(value = "userId") Long userId
    ) {
        return new RecentViewResponse(redisService.range(recentViewsKey(userId), 0, -1));
    }

    private String recentViewsKey(Long userId) {
        return "user:" + userId + ":recent_views";
    }
}
