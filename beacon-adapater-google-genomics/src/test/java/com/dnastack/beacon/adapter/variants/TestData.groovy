package com.dnastack.beacon.adapter.variants

import org.ga4gh.methods.*
import org.ga4gh.models.*

/**
 * @author Artem (tema.voskoboynick@gmail.com)
 * @version 1.0
 */
public class TestData {

    static Dataset getTestDataset() {
        Dataset dataset = new Dataset()
        dataset.setId("test-dataset")
        return dataset
    }

    static CallSet getTestCallSet1() {
        CallSet callSet = new CallSet()
        callSet.setId("test-callset-1")
        callSet.setSampleId("test-bio-sample-1")
        callSet.setVariantSetIds([getTestVariantSet().id])
        callSet.setInfo(Collections.emptyMap())

        return callSet
    }

    static CallSet getTestCallSet2() {
        CallSet callSet = new CallSet()
        callSet.setId("test-callset-2")
        callSet.setSampleId("test-bio-sample-2")
        callSet.setVariantSetIds([getTestVariantSet().id])
        callSet.setInfo(Collections.emptyMap())

        return callSet
    }

    static Call getTestCall1() {
        Call call = new Call()
        call.setCallSetId(getTestCallSet1().id)
        call.setGenotype([1, 2])
        call.setGenotypeLikelihood([])
        call.setInfo(Collections.emptyMap())

        return call
    }

    static Call getTestCall2() {
        Call call = new Call()
        call.setCallSetId(getTestCallSet2().id)
        call.setGenotype([3, 4])
        call.setGenotypeLikelihood([])
        call.setInfo(Collections.emptyMap())

        return call
    }

    static Variant getTestVariant() {
        Variant variant = new Variant()
        variant.setId("test-variant")
        variant.setReferenceBases("test-reference-bases")
        variant.setAlternateBases(["test-alternate-base-1", "test-alternate-base-2", "test-alternate-base-3"])
        variant.setCalls([getTestCall1(), getTestCall2()])
        variant.setVariantSetId(getTestVariantSet().id)
        variant.setNames([])
        variant.setInfo(Collections.emptyMap())

        return variant
    }

    static ReferenceSet getTestReferenceSet() {
        ReferenceSet referenceSet = new ReferenceSet()
        referenceSet.setId("test-reference-set")
        referenceSet.setAssemblyId("GRCh37")
        referenceSet.setMd5checksum("")
        referenceSet.setSourceAccessions([])

        return referenceSet
    }

    static VariantSet getTestVariantSet() {
        VariantSet variantSet = new VariantSet()
        variantSet.setId("test-variant-set")
        variantSet.setReferenceSetId(getTestReferenceSet().id)
        variantSet.setDatasetId(getTestDataset().id)
        variantSet.setMetadata([])

        return variantSet
    }

    static SearchDatasetsResponse getSearchDatasetsResponse() {
        SearchDatasetsResponse searchDatasetsResponse = new SearchDatasetsResponse()
        searchDatasetsResponse.setDatasets([getTestDataset()])

        return searchDatasetsResponse
    }

    static SearchVariantSetsResponse getSearchVariantSetsResponse() {
        SearchVariantSetsResponse searchVariantSetsResponse = new SearchVariantSetsResponse()
        searchVariantSetsResponse.setVariantSets([getTestVariantSet()])

        return searchVariantSetsResponse
    }

    static SearchVariantsResponse getSearchVariantsResponse() {
        SearchVariantsResponse searchVariantsResponse = new SearchVariantsResponse()
        searchVariantsResponse.setVariants([getTestVariant()])

        return searchVariantsResponse
    }

    static SearchDatasetsRequest getSearchDatasetsRequest() {
        def request = new SearchDatasetsRequest()
        request.setPageToken("")

        return request
    }

    static SearchVariantSetsRequest getSearchVariantSetsRequest() {
        SearchVariantSetsRequest searchVariantSetsRequest = new SearchVariantSetsRequest()
        searchVariantSetsRequest.setDatasetIds([getTestDataset().id])
        searchVariantSetsRequest.setPageToken("")

        return searchVariantSetsRequest
    }

    static SearchVariantsRequest getSearchVariantsRequest() {
        SearchVariantsRequest searchVariantsRequest = new SearchVariantsRequest()
        searchVariantsRequest.setVariantSetIds([getTestVariantSet().id])
        searchVariantsRequest.setReferenceName("test-reference-name")
        searchVariantsRequest.setStart(100L)
        searchVariantsRequest.setEnd(101L)
        searchVariantsRequest.setPageToken("")

        return searchVariantsRequest
    }

}
