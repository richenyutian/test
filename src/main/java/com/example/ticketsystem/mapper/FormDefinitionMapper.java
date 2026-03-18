package com.example.ticketsystem.mapper;

import com.example.ticketsystem.entity.FormDefinitionEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface FormDefinitionMapper {

    @Insert("""
        INSERT INTO form_definition (
            form_code, name, description, status, schema_json, created_at, updated_at
        ) VALUES (
            #{formCode}, #{name}, #{description}, #{status}, #{schemaJson}, #{createdAt}, #{updatedAt}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FormDefinitionEntity entity);

    @Update("""
        UPDATE form_definition
        SET form_code = #{formCode},
            name = #{name},
            description = #{description},
            status = #{status},
            schema_json = #{schemaJson},
            updated_at = #{updatedAt}
        WHERE id = #{id}
        """)
    int update(FormDefinitionEntity entity);

    @Select("SELECT * FROM form_definition WHERE id = #{id}")
    FormDefinitionEntity findById(Long id);

    @Select("SELECT * FROM form_definition WHERE form_code = #{formCode}")
    FormDefinitionEntity findByFormCode(String formCode);

    @Select("SELECT * FROM form_definition ORDER BY updated_at DESC, id DESC")
    List<FormDefinitionEntity> findAll();
}
