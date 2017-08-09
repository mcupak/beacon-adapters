package com.dnastack.beacon.adater.variants.client.ga4gh.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.ga4gh.beacon.Beacon;

/**
 * Request for create ga4gh client
 *
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
@Builder
@Getter
public class Ga4ghClientRequest {
    
    private final String apiKey;

    @NonNull
    private final Beacon beacon;

}
