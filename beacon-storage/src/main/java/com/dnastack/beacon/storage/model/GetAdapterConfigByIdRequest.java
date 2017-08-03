package com.dnastack.beacon.storage.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Request for get adapter config by id
 *
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
@Builder
@Getter
@ToString
public class GetAdapterConfigByIdRequest {

    @NonNull
    private final String id;

}
