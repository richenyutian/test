package com.acme.support.ticket.system.mapper;

import com.acme.support.ticket.system.entity.SysUserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * 系统用户 Mapper。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUserEntity> {

    @Select("""
            select *
            from sys_user
            where username = #{username}
              and deleted_flag = 0
            limit 1
            """)
    Optional<SysUserEntity> findByUsername(@Param("username") String username);

    @Select("""
            select distinct u.*
            from sys_user u
            inner join sys_user_role ur on ur.user_id = u.user_id
            inner join sys_role r on r.role_id = ur.role_id
            where r.role_code in (${roleCodes})
              and u.deleted_flag = 0
              and u.status = 'ENABLED'
            """)
    List<SysUserEntity> findUsersByRoleCodes(@Param("roleCodes") String roleCodes);
}
