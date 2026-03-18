package com.example.ticketsystem.mapper;

import com.example.ticketsystem.entity.TicketTaskEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface TicketTaskMapper {

    @Insert("""
        INSERT INTO ticket_task (
            ticket_id, flow_id, node_id, node_name, node_state, form_id, assignee, action_type,
            status, started_at, completed_at
        ) VALUES (
            #{ticketId}, #{flowId}, #{nodeId}, #{nodeName}, #{nodeState}, #{formId}, #{assignee}, #{actionType},
            #{status}, #{startedAt}, #{completedAt}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TicketTaskEntity entity);

    @Update("""
        UPDATE ticket_task
        SET status = #{status},
            completed_at = #{completedAt}
        WHERE id = #{id}
        """)
    int update(TicketTaskEntity entity);

    @Select("SELECT * FROM ticket_task WHERE id = #{id}")
    TicketTaskEntity findById(Long id);

    @Select("SELECT * FROM ticket_task WHERE assignee = #{assignee} AND status = 'PENDING' ORDER BY started_at DESC, id DESC")
    List<TicketTaskEntity> findPendingByAssignee(String assignee);
}
