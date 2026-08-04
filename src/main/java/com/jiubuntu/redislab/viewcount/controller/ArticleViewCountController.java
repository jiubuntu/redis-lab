package com.jiubuntu.redislab.viewcount.controller;

import com.jiubuntu.redislab.viewcount.dto.ArticleViewResponse;
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
@Tag(name = "View-Count", description = "SET + INCR로 유저별 중복 조회를 방지하는 원자적 조회수 카운터 예제 API")
public class ArticleViewCountController {

    private static final int DB_SYNC_INTERVAL = 100;
    private final RedisService redisService;

    @Operation(summary = "게시글 조회수 증가", description = "SET(SADD)으로 유저별 조회 이력을 관리해, 같은 유저가 다시 조회해도 중복 집계되지 않도록 한다. 새로운 유저일 때만 INCR로 원자적으로 증가시킨다.")
    @PostMapping(value = "/articles/{articleId}/view")
    public ArticleViewResponse increaseViewCount(
            @PathVariable(value = "articleId") Long articleId,
            @RequestParam(value = "userId", defaultValue = "1") Long userId
    ) {
        // SADD: 이미 조회한 적 있는 유저면 0, 처음 조회하는 유저면 1을 반환
        Long added = redisService.addToSet(viewersKey(articleId), userId);

        if (added == null || added == 0) {
            // 이미 조회한 유저 → 조회수를 올리지 않고 현재 값만 반환
            return new ArticleViewResponse(articleId, redisService.getCounter(viewKey(articleId)));
        }

        // 새로운 유저일 때만 INCR 을 이용하여 원자적으로 값 증가
        Long currentViews = redisService.increment(viewKey(articleId));

        // 매 요청마다 DB에 쓰지 않고, 일정 단위에 도달했을 때만 동기화한다고 가정 (Write-Back)
        if (currentViews % DB_SYNC_INTERVAL == 0) {
            log.info("[Backup] Article {} reached {} views. Syncing to DB", articleId, currentViews);
        }

        return new ArticleViewResponse(articleId, currentViews);
    }

    @Operation(summary = "게시글 통계 조회", description = "조회수를 조회한다. 값이 없으면 0으로 반환한다.")
    @GetMapping(value = "/articles/{articleId}/stats")
    public ArticleViewResponse getArticleStats(
            @PathVariable(value = "articleId") Long articleId
    ) {
        long views = redisService.getCounter(viewKey(articleId));
        return new ArticleViewResponse(articleId, views);
    }

    private String viewKey(Long articleId) {
        return "article:" + articleId + ":views";
    }

    private String viewersKey(Long articleId) {
        return "article:" + articleId + ":viewers";
    }
}
