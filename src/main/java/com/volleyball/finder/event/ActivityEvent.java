package com.volleyball.finder.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ActivityEvent extends ApplicationEvent {
    private final Long activityId;
    private final Long userId;
    private final String title;
    private final String content;

    public ActivityEvent(Object source, Long activityId, Long userId, String title, String content) {
        super(source);
        this.activityId = activityId;
        this.userId = userId;
        this.title = title;
        this.content = content;
    }
} 