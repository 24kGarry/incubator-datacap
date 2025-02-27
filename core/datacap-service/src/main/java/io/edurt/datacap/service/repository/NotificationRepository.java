package io.edurt.datacap.service.repository;

import io.edurt.datacap.service.entity.NotificationEntity;
import io.edurt.datacap.service.enums.EntityType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationRepository
        extends BaseRepository<NotificationEntity, Long>
{
    @Modifying
    @Transactional
    @Query("UPDATE NotificationEntity n SET n.entityExists = :exists WHERE n.entityCode = :entityCode AND n.entityType = :entityType")
    void updateEntityExistsByEntityCodeAndType(
            @Param("entityCode") String entityCode,
            @Param("entityType") EntityType entityType,
            @Param("exists") Boolean exists);
}
