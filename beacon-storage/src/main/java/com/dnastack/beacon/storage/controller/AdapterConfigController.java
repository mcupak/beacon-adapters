package com.dnastack.beacon.storage.controller;

import com.dnastack.beacon.storage.model.*;
import com.dnastack.beacon.storage.service.AdapterConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Controller for adapter config
 *
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
@RestController
@RequestMapping(
        value = "api/adapterConfig",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.POST)
public class AdapterConfigController {

    @Autowired
    private AdapterConfigService adapterConfigService;

    @RequestMapping("getById")
    public GetAdapterConfigByIdResponse getById(@RequestBody @Valid GetAdapterConfigByIdRequest request) {
        return GetAdapterConfigByIdResponse.builder()
                .adapterConfig(adapterConfigService.getAdapterConfigById(request.getId()))
                .build();
    }

    @RequestMapping("save")
    public SaveAdapterConfigResponse save(@RequestBody @Valid SaveAdapterConfigRequest request) {
        return SaveAdapterConfigResponse.builder()
                .id(adapterConfigService.saveAdapterConfig(request.getAdapterConfig()))
                .build();
    }

    @RequestMapping("findAll")
    public GetAllAdapterConfigsResponse findAll() {
        return GetAllAdapterConfigsResponse.builder()
                .adapterConfigs(adapterConfigService.findAll())
                .build();
    }

}
