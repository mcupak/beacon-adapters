package com.dnastack.beacon.adapter.variants

import org.ga4gh.methods.*
import org.ga4gh.models.*

/**
 * @author Artem (tema.voskoboynick@gmail.com)
 * @version 1.0
 */
public class TestData {
    def public static final TEST_DATASET = Dataset.newBuilder()
            .setId("test-dataset")
            .build()

    def public static final TEST_CALL_SET_1 = CallSet.newBuilder()
            .setId("test-callset-1")
            .setSampleId("test-bio-sample-1")
            .build()

    def public static final TEST_CALL_SET_2 = CallSet.newBuilder()
            .setId("test-callset-2")
            .setSampleId("test-bio-sample-2")
            .build()

    def public static final TEST_CALL_1 = Call.newBuilder()
            .setCallSetId(TEST_CALL_SET_1.id)
            .setGenotype([1, 2])
            .build()

    def public static final TEST_CALL_2 = Call.newBuilder()
            .setCallSetId(TEST_CALL_SET_2.id)
            .setGenotype([3, 4])
            .build()

    def public static final TEST_VARIANT = Variant.newBuilder()
            .setId("test-variant")
            .setReferenceBases("test-reference-bases")
            .setAlternateBases(["test-alternate-base-1", "test-alternate-base-2", "test-alternate-base-3"])
            .setCalls([TEST_CALL_1, TEST_CALL_2])
            .build()

    def public static final TEST_REFERENCE_SET = ReferenceSet.newBuilder()
            .setId("test-reference-set")
            .setAssemblyId("GRCh37")
            .build()

    def public static final TEST_VARIANT_SET = VariantSet.newBuilder()
            .setId("test-variant-set")
            .setReferenceSetId(TEST_REFERENCE_SET.id)
            .build()

    def public static final SEARCH_DATASET_TEST_RESPONSE = SearchDatasetsResponse.newBuilder()
            .setDatasets([TEST_DATASET])
            .build()

    def public static final SEARCH_VARIANT_SETS_TEST_RESPONSE = SearchVariantSetsResponse.newBuilder()
            .setVariantSets([TEST_VARIANT_SET])
            .build()

    def public static final SEARCH_VARIANTS_TEST_RESPONSE = SearchVariantsResponse.newBuilder()
            .setVariants([TEST_VARIANT])
            .build()

    def public static final SEARCH_DATASET_TEST_REQUEST = SearchDatasetsRequest.newBuilder()
            .build()

    def public static final SEARCH_VARIANT_SETS_TEST_REQUEST = SearchVariantSetsRequest.newBuilder()
            .setDatasetIds([TEST_DATASET.id])
            .build()

    def public static final SEARCH_VARIANTS_TEST_REQUEST = SearchVariantsRequest.newBuilder()
            .setVariantSetIds([TEST_VARIANT_SET.id])
            .setReferenceName("test-reference-name")
            .setStart(100L)
            .setEnd(101L)
            .build()
}
