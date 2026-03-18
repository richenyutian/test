package com.example.ticketsystem.mapper;

import com.example.ticketsystem.entity.NodeDefinitionEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NodeDefinitionMapper {

    @Insert("""
        INSERT INTO node_definition (
            node_code, name, node_state, form_id, assignee, action_type, description, created_at, updated_at
        ) VALUES (
            #{nodeCode}, #{name}, #{nodeState}, #{formId}, #{assignee}, #{actionType}, #{description}, #{createdAt}, #{updatedAt}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(NodeDefinitionEntity entity);

    @Update("""
        UPDATE node_definition
        SET node_code = #{nodeCode},
            name = #{name},
            node_state = #{nodeState},
            form_id = #{formId},
            assignee = #{assignee},
            action_type = #{actionType},
            description = #{description},
            updated_at = #{updatedAt}
        WHERE id = #{id}
        """)
    int update(NodeDefinitionEntity entity);

    @Select("SELECT * FROM node_definition WHERE id = #{id}")
    NodeDefinitionEntity findById(Long id);

    @Select("SELECT * FROM node_definition WHERE node_code = #{nodeCode}")
    NodeDefinitionEntity findByNodeCode(String nodeCode);

    @Select("""
        <script>
        SELECT * FROM node_definition
        <if test="ids != null and ids.size() > 0">
            WHERE id IN
            <foreach collection="ids" item="id" open="(" separator="," close=")">
                #{id}
            </foreach>
        </if>
        ORDER BY updated_at DESC, id DESC
        </script>
        """)
    List<NodeDefinitionEntity> findByIds(@Param("ids") List<Long> ids);

    @Select("SELECT * FROM node_definition ORDER BY updated_at DESC, id DESC")
    List<NodeDefinitionEntity> findAll();
}
