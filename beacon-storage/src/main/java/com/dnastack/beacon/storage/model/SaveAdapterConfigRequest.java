package com.dnastack.beacon.storage.model;

import com.dnastack.beacon.storage.dao.AdapterConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Request for save adapter config
 *
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
@Builder
@Getter
@ToString
public class SaveAdapterConfigRequest {

    private final AdapterConfig adapterConfig;

}
