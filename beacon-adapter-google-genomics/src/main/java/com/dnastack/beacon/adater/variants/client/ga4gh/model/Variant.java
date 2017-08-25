package com.dnastack.beacon.adater.variants.client.ga4gh.model;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
@Builder
@Getter
public class Variant {

    private String id;
    private String variantSetId;

    @Builder.Default
    private List<String> names = new ArrayList<>();

    private Long created;
    private Long updated;
    private String referenceName;
    private Long start;
    private Long end;
    private String referenceBases;

    @Builder.Default
    private List<String> alternateBases = new ArrayList<>();

    @Builder.Default
    private List<String> alleleIds = new ArrayList<>();

    @Builder.Default
    private Map<String, List<String>> info = new HashMap<>();

    @Builder.Default
    private List<Call> calls = new ArrayList<>();

}
