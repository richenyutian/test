package com.acme.support.ticket.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 处理组成员关联实体。
 */
@Data
@TableName("biz_handle_group_member")
public class HandleGroupMemberEntity {

    @TableId(type = IdType.AUTO)
    private Long groupMemberId;
    private Long handleGroupId;
    private Long userId;
    private Integer leaderFlag;
    private Integer primaryGroupFlag;
    private LocalDateTime createdAt;
}
