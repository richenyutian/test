package com.example.ticketsystem.controller;

import com.example.ticketsystem.api.ApiModels;
import com.example.ticketsystem.common.ApiResponse;
import com.example.ticketsystem.service.DefinitionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/forms")
public class FormController {

    private final DefinitionService definitionService;

    public FormController(DefinitionService definitionService) {
        this.definitionService = definitionService;
    }

    @GetMapping
    public ApiResponse<List<ApiModels.FormView>> list() {
        return ApiResponse.ok(definitionService.listForms());
    }

    @PostMapping
    public ApiResponse<ApiModels.FormView> save(@Valid @RequestBody ApiModels.FormSaveRequest request) {
        return ApiResponse.ok(definitionService.saveForm(request));
    }
}
