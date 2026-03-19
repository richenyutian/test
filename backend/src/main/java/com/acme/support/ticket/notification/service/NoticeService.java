package com.acme.support.ticket.notification.service;

import com.acme.support.ticket.common.exception.BusinessException;
import com.acme.support.ticket.notification.dto.NotificationResponses;
import com.acme.support.ticket.notification.entity.NoticeEntity;
import com.acme.support.ticket.notification.mapper.NoticeMapper;
import com.acme.support.ticket.security.LoginUserPrincipal;
import com.acme.support.ticket.security.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 站内消息服务。
 */
@Service
public class NoticeService {

    private final NoticeMapper noticeMapper;

    public NoticeService(NoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }

    public void createNotice(Long receiverUserId, String title, String content, String noticeType, String businessType, Long businessId) {
        NoticeEntity entity = new NoticeEntity();
        entity.setReceiverUserId(receiverUserId);
        entity.setNoticeTitle(title);
        entity.setNoticeContent(content);
        entity.setNoticeType(noticeType);
        entity.setBusinessType(businessType);
        entity.setBusinessId(businessId);
        entity.setReadFlag(0);
        entity.setDeletedFlag(0);
        entity.setCreatedAt(LocalDateTime.now());
        noticeMapper.insert(entity);
    }

    public NotificationResponses.NotificationCenterResponse listMine() {
        LoginUserPrincipal principal = requireLoginUser();

        List<NoticeEntity> notices = noticeMapper.selectList(new LambdaQueryWrapper<NoticeEntity>()
                .eq(NoticeEntity::getReceiverUserId, principal.userId())
                .eq(NoticeEntity::getDeletedFlag, 0)
                .orderByDesc(NoticeEntity::getNoticeId));

        long unreadCount = notices.stream().filter(item -> Integer.valueOf(0).equals(item.getReadFlag())).count();
        List<NotificationResponses.NotificationMessageResponse> messages = notices.stream()
                .map(item -> new NotificationResponses.NotificationMessageResponse(
                        item.getNoticeId(),
                        item.getNoticeTitle(),
                        item.getNoticeContent(),
                        item.getNoticeType(),
                        item.getBusinessType(),
                        item.getBusinessId(),
                        Integer.valueOf(1).equals(item.getReadFlag()),
                        item.getReadTime(),
                        item.getCreatedAt()
                ))
                .toList();

        return new NotificationResponses.NotificationCenterResponse(unreadCount, messages);
    }

    @Transactional
    public void markRead(Long noticeId) {
        LoginUserPrincipal principal = requireLoginUser();

        NoticeEntity entity = noticeMapper.selectById(noticeId);
        if (entity == null || !entity.getReceiverUserId().equals(principal.userId())) {
            throw new BusinessException("消息不存在");
        }

        noticeMapper.update(
                null,
                new LambdaUpdateWrapper<NoticeEntity>()
                        .eq(NoticeEntity::getNoticeId, noticeId)
                        .set(NoticeEntity::getReadFlag, 1)
                        .set(NoticeEntity::getReadTime, LocalDateTime.now())
        );
    }

    private LoginUserPrincipal requireLoginUser() {
        LoginUserPrincipal principal = SecurityUtils.getLoginUser();
        if (principal == null) {
            throw new BusinessException("未获取到当前登录用户");
        }
        return principal;
    }
}
