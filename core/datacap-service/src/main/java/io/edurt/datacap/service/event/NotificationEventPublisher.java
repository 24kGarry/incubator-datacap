package io.edurt.datacap.service.event;

import io.edurt.datacap.service.entity.BaseEntity;
import io.edurt.datacap.service.entity.UserEntity;
import io.edurt.datacap.service.enums.EntityType;
import io.edurt.datacap.service.enums.NotificationType;
import io.edurt.datacap.service.security.UserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationEventPublisher
{
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 发布通知事件
     *
     * @param data 相关数据实体
     * @param title 通知标题
     * @param content 通知内容
     * @param entityType 实体类型
     * @param type 通知类型
     * @param channels 通知渠道
     */
    public void publishNotificationEvent(
            BaseEntity data,
            String title,
            String content,
            EntityType entityType,
            NotificationType type,
            String[] channels
    )
    {
        // 获取当前用户
        UserEntity user = UserDetailsService.getUser();

        // 发布事件
        NotificationEvent event = new NotificationEvent(
                data,
                user,
                title,
                content,
                entityType,
                type,
                channels
        );
        eventPublisher.publishEvent(event);
    }
}
