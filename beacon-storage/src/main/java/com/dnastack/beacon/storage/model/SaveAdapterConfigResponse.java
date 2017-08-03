package com.dnastack.beacon.storage.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Response saving adapter config
 *
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
@Builder
@Getter
@ToString
public class SaveAdapterConfigResponse {

    private final String id;

}
