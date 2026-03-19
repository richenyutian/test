package com.acme.support.ticket.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 系统用户实体。
 */
@Data
@TableName("sys_user")
public class SysUserEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    private String userId;
    private String username;
    private String passwordHash;
    private String displayName;
    private String email;
    private String phone;
    private String departmentCode;
    private String departmentName;
    private String teamCode;
    private String teamName;
    private String status;
    private Integer deleted;
}
