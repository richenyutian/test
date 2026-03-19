package com.acme.support.ticket.system.mapper;

import com.acme.support.ticket.system.entity.SysMenuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单 Mapper。
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenuEntity> {

    @Select("""
            select distinct m.*
            from sys_menu m
            inner join sys_role_menu rm on rm.menu_id = m.menu_id
            inner join sys_user_role ur on ur.role_id = rm.role_id
            where ur.user_id = #{userId}
              and m.deleted_flag = 0
              and m.enabled_flag = 1
            order by m.sort_order asc, m.menu_id asc
            """)
    List<SysMenuEntity> findMenusByUserId(@Param("userId") Long userId);
}
