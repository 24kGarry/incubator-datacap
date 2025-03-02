package io.edurt.datacap.service.converter;

import javax.persistence.AttributeConverter;

import io.edurt.datacap.common.utils.JsonUtils;
import io.edurt.datacap.service.itransient.NotificationTypeEntity;
import org.apache.commons.lang3.StringUtils;

public class NotificationTypeConverter
        implements AttributeConverter<NotificationTypeEntity, String>
{
    @Override
    public String convertToDatabaseColumn(NotificationTypeEntity entity)
    {
        return JsonUtils.toJSON(entity);
    }

    @Override
    public NotificationTypeEntity convertToEntityAttribute(String s)
    {
        if (StringUtils.isEmpty(s)) {
            return null;
        }
        else {
            return JsonUtils.toObject(s, NotificationTypeEntity.class);
        }
    }
}
