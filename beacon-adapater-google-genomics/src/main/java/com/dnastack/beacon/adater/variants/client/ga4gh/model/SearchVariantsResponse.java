package com.dnastack.beacon.adater.variants.client.ga4gh.model;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
@Builder
@Getter
public class SearchVariantsResponse extends RecordBase {

    @Builder.Default
    private List<Variant> variants = new ArrayList<>();

    private String nextPageToken;

}
