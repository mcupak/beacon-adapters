package com.dnastack.beacon.adater.variants.client.ga4gh.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
@Builder
@Getter
public class SearchVariantsRequest extends RecordBase {

    private List<String> variantSetIds;
    private String variantName;
    private List<String> callSetIds;
    private String referenceName;
    private String referenceId;
    private long start;
    private long end;

}
