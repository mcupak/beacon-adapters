package com.dnastack.beacon.storage.model;

import com.dnastack.beacon.storage.dao.AdapterConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Response getting all adapter config
 *
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
@Builder
@Getter
@ToString
public class GetAllAdapterConfigsResponse {

    @Builder.Default
    private List<AdapterConfig> adapterConfigs = new ArrayList<>();

}
