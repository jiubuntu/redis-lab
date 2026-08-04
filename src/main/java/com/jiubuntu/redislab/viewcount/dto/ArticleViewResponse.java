package com.jiubuntu.redislab.viewcount.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArticleViewResponse {
    private Long articleId;
    private long totalViews;
}
