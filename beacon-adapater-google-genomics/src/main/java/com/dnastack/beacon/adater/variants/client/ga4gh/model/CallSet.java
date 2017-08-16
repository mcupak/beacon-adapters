package com.dnastack.beacon.adater.variants.client.ga4gh.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
@Builder
@Getter
public class CallSet {

    private String id;
    private String name;
    private String sampleId;
    private List<String> variantSetIds;
    private Long created;
    private Long updated;
    private Map<String, List<String>> info;
    
}
