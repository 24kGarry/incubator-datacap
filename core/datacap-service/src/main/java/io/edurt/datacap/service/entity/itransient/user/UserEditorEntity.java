package io.edurt.datacap.service.entity.itransient.user;

import com.fasterxml.jackson.annotation.JsonView;
import io.edurt.datacap.common.view.EntityView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserEditorEntity
{
    @JsonView(value = {EntityView.UserView.class, EntityView.AdminView.class})
    private Integer fontSize = 12;

    @JsonView(value = {EntityView.UserView.class, EntityView.AdminView.class})
    private String theme = "chrome";
}
