package io.edurt.datacap.service.aspect;

import io.edurt.datacap.notify.NotifyService;
import io.edurt.datacap.notify.model.NotifyRequest;
import io.edurt.datacap.plugin.PluginManager;
import io.edurt.datacap.service.annotation.SendNotification;
import io.edurt.datacap.service.entity.UserEntity;
import io.edurt.datacap.service.repository.NotificationRepository;
import io.edurt.datacap.service.repository.UserRepository;
import io.edurt.datacap.service.security.UserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class NotificationAspect
{
    private final NotificationRepository repository;
    private final UserRepository userRepository;
    private final SpelExpressionParser expressionParser;
    private final PluginManager pluginManager;

    public NotificationAspect(NotificationRepository repository, UserRepository userRepository, PluginManager pluginManager)
    {
        this.repository = repository;
        this.userRepository = userRepository;
        this.pluginManager = pluginManager;
        this.expressionParser = new SpelExpressionParser();
    }

    @AfterReturning(pointcut = "@annotation(sendNotification)", returning = "result")
    public void sendNotification(JoinPoint joinPoint, SendNotification sendNotification, Object result)
    {
        try {
            // 获取方法参数
            Object[] args = joinPoint.getArgs();

            // 创建 SpEL 上下文
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariable("args", args);
            context.setVariable("result", result);

            // 解析标题和内容中的表达式
            String title = expressionParser.parseExpression(sendNotification.title())
                    .getValue(context, String.class);
            String content = expressionParser.parseExpression(sendNotification.content())
                    .getValue(context, String.class);

            UserEntity loginUser = UserDetailsService.getUser();
            userRepository.findByCode(loginUser.getCode())
                    .ifPresent(value -> value.getNotificationTypes().forEach(type -> pluginManager.getPlugin(type)
                            .ifPresent(plugin -> {
                                NotifyService notifyService = plugin.getService(NotifyService.class);

                                NotifyRequest request = new NotifyRequest();
                                request.setTitle(title);
                                request.setContent(content);
                                notifyService.send(request);
                            })
                    ));
        }
        catch (Exception e) {
            log.error("Failed to send notification", e);
        }
    }
}
