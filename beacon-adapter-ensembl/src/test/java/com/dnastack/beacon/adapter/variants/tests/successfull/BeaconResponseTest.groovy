package com.dnastack.beacon.adapter.variants.tests.successfull

import com.dnastack.beacon.adapter.variants.TestData
import com.dnastack.beacon.adapter.variants.BaseTest
import org.ga4gh.beacon.BeaconAlleleRequest
import org.ga4gh.beacon.BeaconAlleleResponse

import static org.assertj.core.api.Assertions.assertThat

/**
 * @author Artem (tema.voskoboynick@gmail.com)
 * @version 1.0
 */
class BeaconResponseTest extends BaseTest {

    @Override
    void doTest() {
        def referenceName = TestData.SEARCH_VARIANTS_TEST_REQUEST.referenceName
        def start = TestData.SEARCH_VARIANTS_TEST_REQUEST.start
        def referenceBases = TestData.TEST_VARIANT.referenceBases
        def alternateBases = TestData.TEST_VARIANT.getAlternateBases(0)
        def assemblyId = TestData.TEST_REFERENCE_SET.assemblyId
        def datasetIds = null
        def includeDatasetResponses = true

        BeaconAlleleRequest request = BeaconAlleleRequest.newBuilder()
                .setReferenceName(referenceName)
                .setStart(start)
                .setReferenceBases(referenceBases)
                .setAlternateBases(alternateBases)
                .setAssemblyId(assemblyId)
                .setDatasetIds(datasetIds)
                .setIncludeDatasetResponses(includeDatasetResponses)
                .build();

        testPostMethod(request)
        testGetMethod(request)
    }

    private void testGetMethod(BeaconAlleleRequest request) {
        BeaconAlleleResponse getMethodResponse = BaseTest.ADAPTER.getBeaconAlleleResponse(
                request.getReferenceName(),
                request.getStart(),
                request.getReferenceBases(),
                request.getAlternateBases(),
                request.getAssemblyId(),
                request.getDatasetIds(),
                request.getIncludeDatasetResponses());
        checkAssertions(getMethodResponse, request)
    }

    private void testPostMethod(BeaconAlleleRequest request) {
        BeaconAlleleResponse postMethodResponse = BaseTest.ADAPTER.getBeaconAlleleResponse(request);
        checkAssertions(postMethodResponse, request)
    }

    private void checkAssertions(BeaconAlleleResponse response, BeaconAlleleRequest request) {
        assertThat(response.alleleRequest).isEqualTo(request)
        assertThat(response.beaconId).isEqualTo(BaseTest.ADAPTER.getBeacon().getId())
        assertThat(response.datasetAlleleResponses).hasSize(1)
        assertThat(response.error).isNull()
        assertThat(response.exists).isTrue()

        def datasetResponse = response.datasetAlleleResponses.get(0)
        assertThat(datasetResponse.error).isNull()
        assertThat(datasetResponse.exists).isTrue()
        assertThat(datasetResponse.callCount).isEqualTo(TestData.TEST_VARIANT.callsCount)
        assertThat(datasetResponse.datasetId).isEqualTo(TestData.TEST_DATASET.id)
        assertThat(datasetResponse.frequency).isEqualTo(0.25d) // 4 total genotypes, only 1 matches (see test calls).
        assertThat(datasetResponse.sampleCount).isEqualTo(2) // 2 call sets with 2 distinct bio samples
        assertThat(datasetResponse.variantCount).isEqualTo(1) // 1 test variant
    }
}
