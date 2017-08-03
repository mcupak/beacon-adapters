package com.dnastack.beacon.storage.model;

import com.dnastack.beacon.storage.dao.AdapterConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Response getting adapter config by id
 *
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
@Builder
@Getter
@ToString
public class GetAdapterConfigByIdResponse {

    private final AdapterConfig adapterConfig;

}
