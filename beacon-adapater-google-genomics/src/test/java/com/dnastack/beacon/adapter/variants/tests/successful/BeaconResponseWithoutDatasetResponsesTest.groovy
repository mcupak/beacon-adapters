package com.dnastack.beacon.adapter.variants.tests.successful

import com.dnastack.beacon.adapter.variants.BaseTest
import com.dnastack.beacon.adater.variants.VariantsBeaconAdapter
import org.ga4gh.beacon.BeaconAlleleRequest
import org.ga4gh.beacon.BeaconAlleleResponse

import static com.dnastack.beacon.adapter.variants.TestData.*
import static org.assertj.core.api.Assertions.assertThat

/**
 * @author Artem (tema.voskoboynick@gmail.com)
 * @version 1.0
 */
class BeaconResponseWithoutDatasetResponsesTest extends BaseTest {

    /**
     * Test the response doesn't contain individual dataset responses,
     * when the flag includeDatasetResponses is set to false in the request.
     */
    @Override
    void doTest() {
        def referenceName = getSearchVariantsRequest().referenceName
        def start = getSearchVariantsRequest().start
        def referenceBases = getTestVariant().referenceBases
        def alternateBases = getTestVariant().getAlternateBases(0)
        def assemblyId = getTestReferenceSet().assemblyId
        def datasetIds = null
        def includeDatasetResponses = false

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
        BeaconAlleleResponse getMethodResponse = ADAPTER.getBeaconAlleleResponse(
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
        BeaconAlleleResponse postMethodResponse = ADAPTER.getBeaconAlleleResponse(request);
        checkAssertions(postMethodResponse, request)
    }

    private void checkAssertions(BeaconAlleleResponse response, BeaconAlleleRequest request) {
        assertThat(response.alleleRequest).isEqualTo(request)
        assertThat(response.beaconId).isEqualTo(BaseTest.ADAPTER.getBeacon().getId())
        assertThat(response.datasetAlleleResponses).isNull()
        assertThat(response.error).isNull()
        assertThat(response.exists).isTrue()
    }
}
