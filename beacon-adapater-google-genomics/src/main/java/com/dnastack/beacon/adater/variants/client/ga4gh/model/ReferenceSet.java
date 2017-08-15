package com.dnastack.beacon.adater.variants.client.ga4gh.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
@Builder
@Getter
public class ReferenceSet {

    private String id;
    private List<String> referenceIds;
    private String md5checksum;
    private Integer ncbiTaxonId;
    private String description;
    private String assemblyId;
    private String sourceURI;
    private List<String> sourceAccessions;
    private boolean isDerived;
    
}
