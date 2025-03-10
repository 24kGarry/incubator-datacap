package io.edurt.datacap.service.event;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.edurt.datacap.service.entity.BaseEntity;
import io.edurt.datacap.service.entity.UserEntity;
import io.edurt.datacap.service.enums.EntityType;
import io.edurt.datacap.service.enums.NotificationType;
import lombok.Getter;

@Getter
@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class NotificationEvent
{
    private final BaseEntity data;
    private final UserEntity user;
    private final String title;
    private final String content;
    private final EntityType entityType;
    private final NotificationType type;
    private final String[] channels;

    public NotificationEvent(
            BaseEntity data,
            UserEntity user,
            String title,
            String content,
            EntityType entityType,
            NotificationType type,
            String[] channels
    )
    {
        this.data = data;
        this.user = user;
        this.title = title;
        this.content = content;
        this.entityType = entityType;
        this.type = type;
        this.channels = channels;
    }
}
