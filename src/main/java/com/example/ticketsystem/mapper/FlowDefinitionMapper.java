package com.example.ticketsystem.mapper;

import com.example.ticketsystem.entity.FlowDefinitionEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface FlowDefinitionMapper {

    @Insert("""
        INSERT INTO flow_definition (
            flow_code, name, description, status, start_node_id, graph_json, created_at, updated_at
        ) VALUES (
            #{flowCode}, #{name}, #{description}, #{status}, #{startNodeId}, #{graphJson}, #{createdAt}, #{updatedAt}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FlowDefinitionEntity entity);

    @Update("""
        UPDATE flow_definition
        SET flow_code = #{flowCode},
            name = #{name},
            description = #{description},
            status = #{status},
            start_node_id = #{startNodeId},
            graph_json = #{graphJson},
            updated_at = #{updatedAt}
        WHERE id = #{id}
        """)
    int update(FlowDefinitionEntity entity);

    @Select("SELECT * FROM flow_definition WHERE id = #{id}")
    FlowDefinitionEntity findById(Long id);

    @Select("SELECT * FROM flow_definition WHERE flow_code = #{flowCode}")
    FlowDefinitionEntity findByFlowCode(String flowCode);

    @Select("SELECT * FROM flow_definition ORDER BY updated_at DESC, id DESC")
    List<FlowDefinitionEntity> findAll();
}
