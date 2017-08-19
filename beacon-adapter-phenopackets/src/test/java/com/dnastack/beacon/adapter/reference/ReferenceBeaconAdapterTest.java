package com.dnastack.beacon.adapter.reference;

import com.dnastack.beacon.adapter.api.BeaconAdapter;
import com.dnastack.beacon.adapter.reference.ReferenceBeaconAdapter;
import com.dnastack.beacon.exceptions.BeaconException;
import com.dnastack.beacon.utils.AdapterConfig;
import com.dnastack.beacon.utils.ConfigValue;
import com.google.common.collect.ImmutableList;
import org.ga4gh.beacon.BeaconAlleleRequest;
import org.ga4gh.beacon.BeaconAlleleResponse;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
public class ReferenceBeaconAdapterTest {

    private final static String BEACON_FILE = "test_beacon.json";
    private final static String PHENO_PACKET_FILE = "test_pheno_packet.json";
    private final static AdapterConfig adapterConfig = createConfig();

    private static AdapterConfig createConfig() {
        ClassLoader cl = ReferenceBeaconAdapter.class.getClassLoader();
        try {
            String beaconPath = cl.getResource(BEACON_FILE).toURI().getPath();
            String phenoPacketPath = cl.getResource(PHENO_PACKET_FILE).toURI().getPath();

            List<ConfigValue> values = ImmutableList.of(
                    ConfigValue.builder().name("beaconJsonFile").value(beaconPath).build(),
                    ConfigValue.builder().name("phenoPacketFile").value(phenoPacketPath).build()
            );

            return AdapterConfig.builder()
                    .name("phenoPacket_test_beacon")
                    .adapterClass(AdapterConfig.class.getCanonicalName())
                    .configValues(values)
                    .build();
        } catch (URISyntaxException e) {
            throw new NullPointerException(e.getMessage());
        }
    }

    @Test
    public void testInitAdapter() throws BeaconException {
        ReferenceBeaconAdapter adapter = new ReferenceBeaconAdapter();
        adapter.initAdapter(adapterConfig);
        assertThat(adapter.getBeacon()).isNotNull();
        assertThat(adapter.getPhenopacketClient()).isNotNull();
    }

    @Test
    public void testAdapterMustBeInitialized() {
        BeaconAdapter adapter = new ReferenceBeaconAdapter();
        assertThatThrownBy(adapter::getBeacon).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> adapter.getBeaconAlleleResponse(null,
                null,
                null,
                null,
                null,
                null,
                null)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> adapter.getBeaconAlleleResponse(null)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_returnExistVariant_when_beaconAlleleRequest() throws BeaconException {
        ReferenceBeaconAdapter adapter = new ReferenceBeaconAdapter();
        adapter.initAdapter(adapterConfig);

        BeaconAlleleResponse beaconAlleleResponse = adapter.getBeaconAlleleResponse(BeaconAlleleRequest.newBuilder()
                .setAssemblyId("GRCh37")
                .setStart(10571)
                .setAlternateBases("AC")
                .setReferenceBases("A")
                .setReferenceName("refName")
                .setDatasetIds(Collections.emptyList())
                .setIncludeDatasetResponses(Boolean.TRUE)
                .build());

        assertThat(beaconAlleleResponse.getExists()).isTrue();
        assertThat(beaconAlleleResponse.getBeaconId()).isEqualTo("sample-beacon");
        assertThat(beaconAlleleResponse.getDatasetAlleleResponses()).isNotEmpty();
        assertThat(beaconAlleleResponse.getDatasetAlleleResponses().get(0).getExists()).isTrue();
        assertThat(beaconAlleleResponse.getDatasetAlleleResponses().get(0).getVariantCount()).isEqualTo(1);
    }

    @Test
    public void should_returnExistVariant_when_params() throws BeaconException {
        ReferenceBeaconAdapter adapter = new ReferenceBeaconAdapter();
        adapter.initAdapter(adapterConfig);

        BeaconAlleleResponse beaconAlleleResponse = adapter.getBeaconAlleleResponse("refName", 10571L, "A", "AC", "GRCh37", Collections.emptyList(), true);

        assertThat(beaconAlleleResponse.getExists()).isTrue();
        assertThat(beaconAlleleResponse.getBeaconId()).isEqualTo("sample-beacon");
        assertThat(beaconAlleleResponse.getDatasetAlleleResponses()).isNotEmpty();
        assertThat(beaconAlleleResponse.getDatasetAlleleResponses().get(0).getExists()).isTrue();
        assertThat(beaconAlleleResponse.getDatasetAlleleResponses().get(0).getVariantCount()).isEqualTo(1);
    }

}
