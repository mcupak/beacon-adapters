package com.dnastack.beacon.adapter.variants;

import com.dnastack.beacon.adapter.api.BeaconAdapter;
import com.dnastack.beacon.adapter.variants.client.fhir.FhirClient;
import com.dnastack.beacon.exceptions.BeaconException;
import com.dnastack.beacon.utils.AdapterConfig;
import com.dnastack.beacon.utils.ConfigValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.ga4gh.beacon.Beacon;
import org.ga4gh.beacon.BeaconAlleleRequest;
import org.ga4gh.beacon.BeaconAlleleResponse;
import org.ga4gh.beacon.BeaconDatasetAlleleResponse;
import org.hl7.fhir.dstu3.model.Sequence;

import javax.enterprise.context.Dependent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

/**
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
@Dependent
public class VariantsBeaconAdapter implements BeaconAdapter {

    private Beacon beacon;
    private FhirClient fhirClient;

    private void checkAdapterInit() {
        if (fhirClient == null) {
            throw new IllegalStateException("VariantsBeaconAdapter adapter has not been initialized");
        }
    }

    private void initGa4ghClient(AdapterConfig adapterConfig) {
        List<ConfigValue> configValues = adapterConfig.getConfigValues();
        String url = null;

        for (ConfigValue configValue : configValues) {
            switch (configValue.getName()) {
                case "beaconJsonFile":
                    beacon = readBeaconJsonFile(configValue.getValue());
                    break;
                case "beaconJson":
                    beacon = readBeaconJson(configValue.getValue());
                    break;
                case "url":
                    url = configValue.getValue();
                    break;
            }
        }

        if (beacon == null) {
            throw new RuntimeException(
                    "Missing required parameter: beaconJson. Please add the appropriate configuration parameter then retry");
        }

        fhirClient = new FhirClient(url);
    }

    private Beacon readBeaconJsonFile(String filename) {
        File beaconJsonFile = new File(filename);
        if (!beaconJsonFile.exists()) {
            throw new RuntimeException("BeaconJson file does not exist");
        }
        try {

            String beaconJson = new String(Files.readAllBytes(beaconJsonFile.toPath()));
            return readBeaconJson(beaconJson);

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Beacon readBeaconJson(String json) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, Beacon.class);
    }

    private BeaconAlleleRequest createRequest(String referenceName, Long start, String referenceBases, String alternateBases,
                                              String assemblyId, List<String> datasetIds, Boolean includeDatasetResponses) {
        return BeaconAlleleRequest.newBuilder()
                .setReferenceName(referenceName)
                .setStart(start)
                .setReferenceBases(referenceBases)
                .setAlternateBases(alternateBases)
                .setAssemblyId(assemblyId)
                .setDatasetIds(datasetIds)
                .setIncludeDatasetResponses(includeDatasetResponses)
                .build();
    }

    @Override
    public void initAdapter(AdapterConfig adapterConfig) {
        initGa4ghClient(adapterConfig);
    }

    @Override
    public BeaconAlleleResponse getBeaconAlleleResponse(BeaconAlleleRequest request) throws BeaconException {
        checkAdapterInit();

        List<Sequence.SequenceVariantComponent> variants = fhirClient.getVariants(request.getStart(), request.getStart() + 1,
                request.getReferenceBases(), request.getAlternateBases());

        return BeaconAlleleResponse.newBuilder()
                .setBeaconId(beacon.getId())
                .setError(null)
                .setAlleleRequest(request)
                .setExists(!variants.isEmpty())
                .setDatasetAlleleResponses(Collections.singletonList(BeaconDatasetAlleleResponse.newBuilder()
                        .setExists(!variants.isEmpty())
                        .build()))
                .build();
    }

    @Override
    public BeaconAlleleResponse getBeaconAlleleResponse(String referenceName, Long start, String referenceBases,
                                                        String alternateBases, String assemblyId, List<String> datasetIds,
                                                        Boolean includeDatasetResponses) throws BeaconException {
        checkAdapterInit();
        BeaconAlleleRequest request = createRequest(referenceName,
                start,
                referenceBases,
                alternateBases,
                assemblyId,
                datasetIds,
                includeDatasetResponses);
        return getBeaconAlleleResponse(request);
    }

    @Override
    public Beacon getBeacon() throws BeaconException {
        return beacon;
    }

}
