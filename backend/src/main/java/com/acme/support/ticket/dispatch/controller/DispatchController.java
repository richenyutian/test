package com.acme.support.ticket.dispatch.controller;

import com.acme.support.ticket.common.api.ApiResponse;
import com.acme.support.ticket.dispatch.dto.DispatchResponses;
import com.acme.support.ticket.mock.MockTicketDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分派中心接口。
 */
@Tag(name = "分派中心")
@RestController
@RequestMapping("/v1/dispatch")
public class DispatchController {

    private final MockTicketDataService mockTicketDataService;

    public DispatchController(MockTicketDataService mockTicketDataService) {
        this.mockTicketDataService = mockTicketDataService;
    }

    @Operation(summary = "获取分派中心数据")
    @GetMapping("/board")
    public ApiResponse<DispatchResponses.DispatchBoardResponse> getDispatchBoard() {
        return ApiResponse.success(mockTicketDataService.getDispatchBoard());
    }
}
