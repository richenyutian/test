package com.example.ticketworkflow.mapper;

import com.example.ticketworkflow.entity.WorkflowTemplateEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface WorkflowTemplateMapper {

    @Insert("""
        INSERT INTO wf_template (
            template_code, name, description, status, version_no,
            process_definition_key, process_definition_id, bpmn_xml, definition_json,
            created_at, updated_at
        ) VALUES (
            #{templateCode}, #{name}, #{description}, #{status}, #{versionNo},
            #{processDefinitionKey}, #{processDefinitionId}, #{bpmnXml}, #{definitionJson},
            #{createdAt}, #{updatedAt}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(WorkflowTemplateEntity entity);

    @Update("""
        UPDATE wf_template
        SET name = #{name},
            description = #{description},
            status = #{status},
            version_no = #{versionNo},
            process_definition_key = #{processDefinitionKey},
            process_definition_id = #{processDefinitionId},
            bpmn_xml = #{bpmnXml},
            definition_json = #{definitionJson},
            updated_at = #{updatedAt}
        WHERE id = #{id}
        """)
    int update(WorkflowTemplateEntity entity);

    @Select("SELECT * FROM wf_template WHERE id = #{id}")
    WorkflowTemplateEntity findById(Long id);

    @Select("SELECT * FROM wf_template WHERE template_code = #{templateCode}")
    WorkflowTemplateEntity findByTemplateCode(String templateCode);

    @Select("SELECT * FROM wf_template ORDER BY updated_at DESC, id DESC")
    List<WorkflowTemplateEntity> findAll();
}
