package io.edurt.datacap.service.annotation;

import io.edurt.datacap.service.enums.EntityType;
import io.edurt.datacap.service.enums.NotificationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SendNotification
{
    String title() default "";

    String content() default "";

    EntityType entityType() default EntityType.DATASET;

    NotificationType type() default NotificationType.CREATED;

    String[] channels() default {};
}
