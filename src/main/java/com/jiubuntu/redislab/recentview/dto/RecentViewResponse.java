package com.jiubuntu.redislab.recentview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RecentViewResponse {
    private List<Object> recentViews;
}
