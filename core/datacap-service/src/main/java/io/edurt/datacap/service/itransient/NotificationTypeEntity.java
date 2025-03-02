package io.edurt.datacap.service.itransient;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonView;
import io.edurt.datacap.common.view.EntityView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTypeEntity
{
    @JsonView(value = {EntityView.UserView.class, EntityView.AdminView.class})
    private String service;

    @JsonView(value = {EntityView.UserView.class, EntityView.AdminView.class})
    private Set<String> types;
}
