package io.edurt.datacap.service.entity;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonView;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.edurt.datacap.common.view.EntityView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Any;
import org.hibernate.annotations.AnyMetaDef;
import org.hibernate.annotations.MetaValue;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(value = AuditingEntityListener.class)
@Table(name = "datacap_notification")
@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EQ_OVERRIDING_EQUALS_NOT_SYMMETRIC"})
public class NotificationEntity
        extends BaseEntity
{
    @Column(nullable = false)
    @JsonView(value = {EntityView.UserView.class, EntityView.AdminView.class})
    private String content;

    @Column(name = "type")
    @JsonView(value = {EntityView.UserView.class, EntityView.AdminView.class})
    private String type;

    @Column(name = "is_read", nullable = false, columnDefinition = "boolean default false")
    @JsonView(value = {EntityView.UserView.class, EntityView.AdminView.class})
    private Boolean isRead = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonView(value = {EntityView.UserView.class, EntityView.AdminView.class})
    private UserEntity user;

    @Column(name = "original_type", nullable = false, insertable = false, updatable = false)
    @JsonView(value = {EntityView.UserView.class, EntityView.AdminView.class})
    private String originalType;

    @Any(metaColumn = @Column(name = "original_type"))
    @AnyMetaDef(
            idType = "long",
            metaType = "string",
            metaValues = {
                    @MetaValue(targetEntity = DataSetEntity.class, value = "DATASET"),
            }
    )
    @JoinColumn(name = "original_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonView(value = {EntityView.UserView.class, EntityView.AdminView.class})
    @JsonIncludeProperties(value = {"code", "name"})
    private Object original;
}
