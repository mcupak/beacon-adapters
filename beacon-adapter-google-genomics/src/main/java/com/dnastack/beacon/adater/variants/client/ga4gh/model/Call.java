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
public class Call {

    private String callSetId;
    private String callSetName;
    private String variantId;

    @Builder.Default
    private List<Integer> genotype = new ArrayList<>();

    private Object phaseset;

    @Builder.Default
    private List<Double> genotypeLikelihood = new ArrayList<>();

    @Builder.Default
    private Map<String, List<String>> info = new HashMap<>();

}
