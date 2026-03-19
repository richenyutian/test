package com.acme.support.ticket.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 处理组实体。
 */
@Data
@TableName("biz_handle_group")
public class HandleGroupEntity {

    @TableId(type = IdType.AUTO)
    private Long handleGroupId;
    private String groupCode;
    private String groupName;
    private String groupType;
    private Long leaderUserId;
    private String leaderName;
    private String status;
    private String remark;
    private Integer deletedFlag;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
