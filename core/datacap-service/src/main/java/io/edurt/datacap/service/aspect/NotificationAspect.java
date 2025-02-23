package io.edurt.datacap.service.aspect;

import com.google.common.collect.Lists;
import io.edurt.datacap.common.response.CommonResponse;
import io.edurt.datacap.notify.NotifyService;
import io.edurt.datacap.notify.model.NotifyRequest;
import io.edurt.datacap.plugin.PluginManager;
import io.edurt.datacap.service.annotation.SendNotification;
import io.edurt.datacap.service.entity.BaseEntity;
import io.edurt.datacap.service.entity.NotificationEntity;
import io.edurt.datacap.service.entity.UserEntity;
import io.edurt.datacap.service.repository.NotificationRepository;
import io.edurt.datacap.service.repository.UserRepository;
import io.edurt.datacap.service.security.UserDetailsService;
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
public class NotificationAspect
{
    private final NotificationRepository repository;
    private final UserRepository userRepository;
    private final PluginManager pluginManager;

    public NotificationAspect(NotificationRepository repository, UserRepository userRepository, PluginManager pluginManager)
    {
        this.repository = repository;
        this.userRepository = userRepository;
        this.pluginManager = pluginManager;
    }

    @AfterReturning(pointcut = "@annotation(sendNotification)", returning = "result")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendNotification(JoinPoint joinPoint, SendNotification sendNotification, Object result)
    {
        try {
            UserEntity loginUser = UserDetailsService.getUser();

            if (result instanceof CommonResponse) {
                @SuppressWarnings("unchecked")
                CommonResponse<BaseEntity> response = (CommonResponse<BaseEntity>) result;
                sendDefaultNotification(sendNotification, response.getData());
            }

            userRepository.findByCode(loginUser.getCode())
                    .ifPresent(value -> Optional.ofNullable(value.getNotificationTypes())
                            .orElse(Lists.newArrayList())
                            .stream()
                            .filter(Objects::nonNull)
                            .forEach(type -> pluginManager.getPlugin(type).flatMap(plugin -> Optional.ofNullable(plugin.getService(NotifyService.class)))
                                    .ifPresent(notifyService -> {
                                        NotifyRequest request = new NotifyRequest();
                                        request.setTitle(sendNotification.title());
                                        request.setContent(sendNotification.content());
                                        notifyService.send(request);
                                    })));
        }
        catch (Exception e) {
            log.error("Failed to send notification", e);
        }
    }

    private void sendDefaultNotification(SendNotification sendNotification, Object configure)
    {
        NotificationEntity entity = NotificationEntity.builder()
                .content(sendNotification.content())
                .user(UserDetailsService.getUser())
                .type(sendNotification.type().name())
                .name(sendNotification.title())
                .original(configure)
                .isRead(false)
                .build();
        repository.save(entity);
    }
}
