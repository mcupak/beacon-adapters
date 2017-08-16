package com.dnastack.beacon.adater.variants.client.ga4gh.model;

import lombok.Builder;
import lombok.Getter;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
@Builder
@Getter
public class SearchVariantSetsResponse extends RecordBase {

    @JsonProperty("variantSets")
    @Builder.Default
    private List<VariantSet> variantSets = new ArrayList<>();

    @JsonProperty("nextPageToken")
    private String nextPageToken;

}
