package com.example.ticketworkflow.mapper;

import com.example.ticketworkflow.entity.TicketInstanceEntity;
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
        INSERT INTO wf_ticket_instance (
            business_key, template_id, template_code, template_name, process_definition_id,
            process_instance_id, title, initiator, current_node_key, current_node_name,
            status, merged_form_data, searchable_text, started_at, ended_at, updated_at
        ) VALUES (
            #{businessKey}, #{templateId}, #{templateCode}, #{templateName}, #{processDefinitionId},
            #{processInstanceId}, #{title}, #{initiator}, #{currentNodeKey}, #{currentNodeName},
            #{status}, #{mergedFormData}, #{searchableText}, #{startedAt}, #{endedAt}, #{updatedAt}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TicketInstanceEntity entity);

    @Update("""
        UPDATE wf_ticket_instance
        SET current_node_key = #{currentNodeKey},
            current_node_name = #{currentNodeName},
            status = #{status},
            merged_form_data = #{mergedFormData},
            searchable_text = #{searchableText},
            ended_at = #{endedAt},
            updated_at = #{updatedAt}
        WHERE id = #{id}
        """)
    int update(TicketInstanceEntity entity);

    @Select("SELECT * FROM wf_ticket_instance WHERE id = #{id}")
    TicketInstanceEntity findById(Long id);

    @Select("SELECT * FROM wf_ticket_instance WHERE process_instance_id = #{processInstanceId}")
    TicketInstanceEntity findByProcessInstanceId(String processInstanceId);

    @Select("""
        <script>
        SELECT * FROM wf_ticket_instance
        WHERE 1 = 1
        <if test="keyword != null and keyword != ''">
            AND (
                LOWER(title) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                OR LOWER(searchable_text) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                OR LOWER(business_key) LIKE CONCAT('%', LOWER(#{keyword}), '%')
            )
        </if>
        <if test="initiator != null and initiator != ''">
            AND initiator = #{initiator}
        </if>
        <if test="status != null and status != ''">
            AND status = #{status}
        </if>
        <if test="instanceIds != null">
            AND id IN
            <foreach collection="instanceIds" item="id" open="(" separator="," close=")">
                #{id}
            </foreach>
        </if>
        ORDER BY updated_at DESC, id DESC
        </script>
        """)
    List<TicketInstanceEntity> search(@Param("keyword") String keyword,
                                      @Param("initiator") String initiator,
                                      @Param("status") String status,
                                      @Param("instanceIds") List<Long> instanceIds);
}
