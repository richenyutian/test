package com.acme.support.ticket.system.mapper;

import com.acme.support.ticket.system.entity.SysUserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}
