package com.dnastack.beacon.adapter.variants.tests.error

import com.dnastack.beacon.adapter.variants.BaseTest
import com.dnastack.beacon.exceptions.BeaconException
import org.ga4gh.beacon.BeaconAlleleRequest

import static com.dnastack.beacon.adapter.variants.TestData.*
import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown

/**
 * @author Artem (tema.voskoboynick@gmail.com)
 * @author Miro Cupak (mirocupak@gmail.com)
 * @version 1.0
 */
class BeaconResponseUnknownDatasetTest extends BaseTest {

    /**
     * Test that requesting unknown datasets causes {@link BeaconException}.
     */
    @Override
    void doTest() {
        def referenceName = getSearchVariantsRequest().referenceName
        def start = getSearchVariantsRequest().start
        def referenceBases = getTestVariant().referenceBases
        def alternateBases = getTestVariant().getAlternateBases().get(0)
        def assemblyId = getTestReferenceSet().assemblyId
        def datasetIds = ["unknown-dataset"]
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
        try {
            ADAPTER.getBeaconAlleleResponse(
                    request.getReferenceName(),
                    request.getStart(),
                    request.getReferenceBases(),
                    request.getAlternateBases(),
                    request.getAssemblyId(),
                    request.getDatasetIds(),
                    request.getIncludeDatasetResponses());
            failBecauseExceptionWasNotThrown(BeaconException.class);
        } catch (BeaconException e) {
            def unknownDatasetId = request.getDatasetIds().get(0)
            assertThat(e).hasMessageContaining(unknownDatasetId);
        }
    }

    private void testPostMethod(BeaconAlleleRequest request) {
        try {
            ADAPTER.getBeaconAlleleResponse(request);
            failBecauseExceptionWasNotThrown(BeaconException.class);
        } catch (BeaconException e) {
            def unknownDatasetId = request.getDatasetIds().get(0)
            assertThat(e).hasMessageContaining(unknownDatasetId);
        }
    }
}