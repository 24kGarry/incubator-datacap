package io.edurt.datacap.service.aspect;

import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.edurt.datacap.common.response.CommonResponse;
import io.edurt.datacap.notify.NotifyService;
import io.edurt.datacap.notify.model.NotifyRequest;
import io.edurt.datacap.plugin.PluginManager;
import io.edurt.datacap.service.annotation.SendNotification;
import io.edurt.datacap.service.entity.BaseEntity;
import io.edurt.datacap.service.entity.NotificationEntity;
import io.edurt.datacap.service.entity.UserEntity;
import io.edurt.datacap.service.enums.NotificationType;
import io.edurt.datacap.service.repository.NotificationRepository;
import io.edurt.datacap.service.repository.UserRepository;
import io.edurt.datacap.service.security.UserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@SuppressFBWarnings(value = {"EI_EXPOSE_REP2"})
public class NotificationAspect
{
    private final NotificationRepository repository;
    private final UserRepository userRepository;
    private final PluginManager pluginManager;

    @AfterReturning(pointcut = "@annotation(sendNotification)", returning = "result")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendNotification(JoinPoint joinPoint, SendNotification sendNotification, Object result)
    {
        try {
            UserEntity loginUser = UserDetailsService.getUser();

            // 处理返回结果并发送默认通知
            processResultAndSendDefaultNotification(result, sendNotification);

            // 发送用户配置的通知
            sendCustomNotifications(loginUser, sendNotification);
        }
        catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage(), e);
        }
    }

    private void processResultAndSendDefaultNotification(Object result, SendNotification sendNotification)
    {
        if (result instanceof CommonResponse) {
            @SuppressWarnings("unchecked")
            CommonResponse<BaseEntity> response = (CommonResponse<BaseEntity>) result;
            sendDefaultNotification(sendNotification, response.getData());
        }
    }

    private void sendCustomNotifications(UserEntity loginUser, SendNotification sendNotification)
    {
        userRepository.findByCode(loginUser.getCode())
                .ifPresent(user -> Optional.ofNullable(user.getNotificationTypes())
                        .orElse(Lists.newArrayList())
                        .stream()
                        .filter(Objects::nonNull)
                        .forEach(type -> sendNotificationViaPlugin(type, sendNotification)));
    }

    private void sendNotificationViaPlugin(String type, SendNotification sendNotification)
    {
        pluginManager.getPlugin(type)
                .flatMap(plugin -> Optional.ofNullable(plugin.getService(NotifyService.class)))
                .ifPresent(notifyService -> {
                    NotifyRequest request = createNotifyRequest(sendNotification);
                    notifyService.send(request);
                });
    }

    private NotifyRequest createNotifyRequest(SendNotification sendNotification)
    {
        NotifyRequest request = new NotifyRequest();
        request.setTitle(sendNotification.title());
        request.setContent(sendNotification.content());
        return request;
    }

    private void sendDefaultNotification(SendNotification sendNotification, Object configure)
    {
        boolean isDelete = sendNotification.type().equals(NotificationType.DELETED);

        // 处理字符串类型的配置对象
        if (configure instanceof String) {
            configure = processStringConfigure((String) configure);
        }

        // 构建通知实体
        NotificationEntity entity = buildNotificationEntity(sendNotification, configure, isDelete);

        // 根据动态类型设置通知类型
        if (sendNotification.type().equals(NotificationType.DYNAMIC) && configure != null) {
            entity.setType(((BaseEntity) configure).getId() == null ?
                    NotificationType.CREATED.name() :
                    NotificationType.UPDATED.name());
        }

        repository.save(entity);
    }

    private BaseEntity processStringConfigure(String configureCode)
    {
        BaseEntity baseEntity = BaseEntity.builder()
                .code(configureCode)
                .build();

        // 尝试获取已通知内容是否包含当前相关信息
        repository.findFirstByEntityCode(configureCode)
                .ifPresentOrElse(
                        entity -> baseEntity.setName(entity.getEntityName()),
                        () -> baseEntity.setName(configureCode)
                );

        return baseEntity;
    }

    private NotificationEntity buildNotificationEntity(SendNotification sendNotification,
            Object configure,
            boolean isDelete)
    {
        boolean isStringConfigure = (configure instanceof String);

        return NotificationEntity.builder()
                .content(sendNotification.content())
                .user(UserDetailsService.getUser())
                .type(sendNotification.type().name())
                .name(sendNotification.title())
                .entityCode(configure == null ? null : ((BaseEntity) configure).getCode())
                .entityName(configure == null ? null : ((BaseEntity) configure).getName())
                .entityType(sendNotification.entityType())
                .entityExists(!(isDelete || isStringConfigure))
                .isRead(false)
                .build();
    }
}
