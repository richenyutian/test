package com.acme.support.ticket.notification.mapper;

import com.acme.support.ticket.notification.entity.NoticeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 站内消息 Mapper。
 */
@Mapper
public interface NoticeMapper extends BaseMapper<NoticeEntity> {
}
