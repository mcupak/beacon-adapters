package com.dnastack.beacon.adater.variants.client.ga4gh.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
@Builder
@Getter
public class VariantSet {

    private String id;

    private String datasetId;

    private String referenceSetId;

    private List<VariantSetMetadata> metadata;

}
