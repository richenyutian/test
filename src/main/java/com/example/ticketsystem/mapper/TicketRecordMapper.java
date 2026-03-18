package com.example.ticketsystem.mapper;

import com.example.ticketsystem.entity.TicketRecordEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TicketRecordMapper {

    @Insert("""
        INSERT INTO ticket_record (
            ticket_id, node_id, node_name, node_state, assignee, operator, action_type,
            comment, selected_next_node_id, selected_next_node_name, submitted_data_json, merged_data_json, created_at
        ) VALUES (
            #{ticketId}, #{nodeId}, #{nodeName}, #{nodeState}, #{assignee}, #{operator}, #{actionType},
            #{comment}, #{selectedNextNodeId}, #{selectedNextNodeName}, #{submittedDataJson}, #{mergedDataJson}, #{createdAt}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TicketRecordEntity entity);

    @Select("SELECT * FROM ticket_record WHERE ticket_id = #{ticketId} ORDER BY created_at ASC, id ASC")
    List<TicketRecordEntity> findByTicketId(Long ticketId);
}
