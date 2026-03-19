package com.acme.support.ticket.ticket.service;

import com.acme.support.ticket.ticket.entity.TicketEntity;
import com.acme.support.ticket.ticket.mapper.TicketMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 工单编号生成服务。
 */
@Service
public class TicketNoGeneratorService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final TicketMapper ticketMapper;

    public TicketNoGeneratorService(TicketMapper ticketMapper) {
        this.ticketMapper = ticketMapper;
    }

    public String nextTicketNo() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = LocalDateTime.of(today, LocalTime.MAX);

        Long count = ticketMapper.selectCount(new LambdaQueryWrapper<TicketEntity>()
                .ge(TicketEntity::getCreatedAt, start)
                .le(TicketEntity::getCreatedAt, end));

        long seq = (count == null ? 0L : count) + 1L;
        return "WO" + today.format(DATE_FORMATTER) + String.format("%04d", seq);
    }
}
