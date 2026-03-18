package com.example.ticketsystem.mapper;

import com.example.ticketsystem.entity.TicketInstanceEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface TicketInstanceMapper {

    @Insert("""
        INSERT INTO ticket_instance (
            ticket_no, flow_id, flow_name, title, applicant, current_node_id, current_node_name,
            status, business_data_json, created_at, updated_at, finished_at
        ) VALUES (
            #{ticketNo}, #{flowId}, #{flowName}, #{title}, #{applicant}, #{currentNodeId}, #{currentNodeName},
            #{status}, #{businessDataJson}, #{createdAt}, #{updatedAt}, #{finishedAt}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TicketInstanceEntity entity);

    @Update("""
        UPDATE ticket_instance
        SET current_node_id = #{currentNodeId},
            current_node_name = #{currentNodeName},
            status = #{status},
            business_data_json = #{businessDataJson},
            updated_at = #{updatedAt},
            finished_at = #{finishedAt}
        WHERE id = #{id}
        """)
    int update(TicketInstanceEntity entity);

    @Select("SELECT * FROM ticket_instance WHERE id = #{id}")
    TicketInstanceEntity findById(Long id);

    @Select("""
        <script>
        SELECT * FROM ticket_instance
        WHERE 1 = 1
        <if test="keyword != null and keyword != ''">
            AND (
                LOWER(ticket_no) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                OR LOWER(title) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                OR LOWER(applicant) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                OR LOWER(flow_name) LIKE CONCAT('%', LOWER(#{keyword}), '%')
            )
        </if>
        <if test="status != null and status != ''">
            AND status = #{status}
        </if>
        ORDER BY updated_at DESC, id DESC
        </script>
        """)
    List<TicketInstanceEntity> search(@Param("keyword") String keyword, @Param("status") String status);
}
