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
public class VariantSetMetadata {

    private String key;
    private String value;
    private String id;
    private String type;
    private String number;
    private String description;
    private Map<String, List<String>> info;

}
