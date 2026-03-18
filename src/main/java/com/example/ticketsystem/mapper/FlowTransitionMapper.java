package com.example.ticketsystem.mapper;

import com.example.ticketsystem.entity.FlowTransitionEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FlowTransitionMapper {

    @Insert("""
        INSERT INTO flow_transition (
            flow_id, from_node_id, to_node_id, transition_name, sort_no
        ) VALUES (
            #{flowId}, #{fromNodeId}, #{toNodeId}, #{transitionName}, #{sortNo}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FlowTransitionEntity entity);

    @Delete("DELETE FROM flow_transition WHERE flow_id = #{flowId}")
    int deleteByFlowId(Long flowId);

    @Select("SELECT * FROM flow_transition WHERE flow_id = #{flowId} ORDER BY sort_no ASC, id ASC")
    List<FlowTransitionEntity> findByFlowId(Long flowId);

    @Select("SELECT * FROM flow_transition WHERE flow_id = #{flowId} AND from_node_id = #{fromNodeId} ORDER BY sort_no ASC, id ASC")
    List<FlowTransitionEntity> findByFlowIdAndFromNodeId(@Param("flowId") Long flowId, @Param("fromNodeId") Long fromNodeId);
}
