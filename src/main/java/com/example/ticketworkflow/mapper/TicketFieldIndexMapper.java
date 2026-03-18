package com.example.ticketworkflow.mapper;

import com.example.ticketworkflow.entity.TicketFieldIndexEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TicketFieldIndexMapper {

    @Delete("DELETE FROM wf_ticket_field_index WHERE instance_id = #{instanceId}")
    int deleteByInstanceId(Long instanceId);

    @Insert("""
        INSERT INTO wf_ticket_field_index (
            instance_id, field_key, field_label, field_value, updated_at
        ) VALUES (
            #{instanceId}, #{fieldKey}, #{fieldLabel}, #{fieldValue}, #{updatedAt}
        )
        """)
    int insert(TicketFieldIndexEntity entity);

    @Select("""
        SELECT DISTINCT instance_id
        FROM wf_ticket_field_index
        WHERE field_key = #{fieldKey}
          AND LOWER(field_value) LIKE CONCAT('%', LOWER(#{fieldValue}), '%')
        ORDER BY instance_id DESC
        """)
    List<Long> findInstanceIdsByField(@Param("fieldKey") String fieldKey, @Param("fieldValue") String fieldValue);

    @Select("SELECT * FROM wf_ticket_field_index WHERE instance_id = #{instanceId} ORDER BY field_key ASC")
    List<TicketFieldIndexEntity> findByInstanceId(Long instanceId);
}
