package com.acme.support.ticket.system.mapper;

import com.acme.support.ticket.system.entity.SysRoleEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色 Mapper。
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRoleEntity> {

    @Select("""
            select r.*
            from sys_role r
            inner join sys_user_role ur on ur.role_id = r.role_id
            where ur.user_id = #{userId}
            order by r.role_id
            """)
    List<SysRoleEntity> findRolesByUserId(@Param("userId") Long userId);
}
