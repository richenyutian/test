package com.example.ticketworkflow.mapper;

import com.example.ticketworkflow.entity.TicketRecordEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TicketRecordMapper {

    @Insert("""
        INSERT INTO wf_ticket_record (
            instance_id, process_instance_id, task_id, task_definition_key, task_name,
            assignee, operator, action, comment, submitted_form_data, merged_form_data, created_at
        ) VALUES (
            #{instanceId}, #{processInstanceId}, #{taskId}, #{taskDefinitionKey}, #{taskName},
            #{assignee}, #{operator}, #{action}, #{comment}, #{submittedFormData}, #{mergedFormData}, #{createdAt}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TicketRecordEntity entity);

    @Select("SELECT * FROM wf_ticket_record WHERE instance_id = #{instanceId} ORDER BY created_at ASC, id ASC")
    List<TicketRecordEntity> findByInstanceId(Long instanceId);
}
