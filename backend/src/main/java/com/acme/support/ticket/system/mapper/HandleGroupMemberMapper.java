package com.acme.support.ticket.system.mapper;

import com.acme.support.ticket.system.entity.HandleGroupMemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 处理组成员 Mapper。
 */
@Mapper
public interface HandleGroupMemberMapper extends BaseMapper<HandleGroupMemberEntity> {

    @Select("""
            select *
            from biz_handle_group_member
            where user_id = #{userId}
            order by primary_group_flag desc, group_member_id asc
            """)
    List<HandleGroupMemberEntity> findByUserId(@Param("userId") Long userId);
}
