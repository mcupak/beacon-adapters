package com.dnastack.beacon.adapter.reference;

import com.dnastack.beacon.adapter.api.BeaconAdapter;
import com.dnastack.beacon.adapter.reference.client.phenopackets.PhenopacketClient;
import com.dnastack.beacon.exceptions.BeaconException;
import com.dnastack.beacon.utils.AdapterConfig;
import com.dnastack.beacon.utils.ConfigValue;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.BooleanUtils;
import org.ga4gh.beacon.Beacon;
import org.ga4gh.beacon.BeaconAlleleRequest;
import org.ga4gh.beacon.BeaconAlleleResponse;
import org.ga4gh.beacon.BeaconDatasetAlleleResponse;
import org.phenopackets.api.PhenoPacket;
import org.phenopackets.api.io.JsonReader;
import org.phenopackets.api.model.entity.Variant;

import javax.enterprise.context.Dependent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
@Dependent
public class ReferenceBeaconAdapter implements BeaconAdapter {

    private PhenopacketClient phenopacketClient;
    private Beacon beacon;

    private void initPhenopacketClient(AdapterConfig adapterConfig) {
        List<ConfigValue> configValues = adapterConfig.getConfigValues();
        PhenoPacket phenoPacket = null;

        for (ConfigValue configValue : configValues) {
            switch (configValue.getName()) {
                case "beaconJsonFile":
                    beacon = readBeaconJsonFile(configValue.getValue());
                    break;
                case "beaconJson":
                    beacon = readBeaconJson(configValue.getValue());
                    break;
                case "phenoPacketFile":
                    phenoPacket = readPhenoPacketFile(configValue.getValue());
                    break;
                case "phenoPacket":
                    phenoPacket = readPhenoPacket(configValue.getValue());
                    break;
            }
        }

        if (phenoPacket == null) {
            throw new RuntimeException(
                    "Missing required parameter: phenoPacket. Please add the appropriate configuration parameter then retry");
        }

        if (beacon == null) {
            throw new RuntimeException(
                    "Missing required parameter: beaconJson. Please add the appropriate configuration parameter then retry");
        }

        phenopacketClient = new PhenopacketClient(phenoPacket);
    }

    private PhenoPacket readPhenoPacketFile(String path) {
        try {
            return JsonReader.readFile(path);
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + e);
        }
    }

    private PhenoPacket readPhenoPacket(String value) {
        try {
            return JsonReader.readInputStream(new ByteArrayInputStream(value.getBytes()));
        } catch (IOException e) {
            throw new RuntimeException("Error reading string: " + value);
        }
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

    private void checkAdapterInit() {
        if (phenopacketClient == null) {
            throw new IllegalStateException("Phenopacket adapter has not been initialized");
        }
    }

    @Override
    public void initAdapter(AdapterConfig adapterConfig) {
        initPhenopacketClient(adapterConfig);
    }

    @Override
    public BeaconAlleleResponse getBeaconAlleleResponse(BeaconAlleleRequest request) throws BeaconException {
        checkAdapterInit();

        List<Variant> variants = phenopacketClient.getVariants();

        long count = variants.stream().filter(filterVariants(request)).count();

        BeaconDatasetAlleleResponse datasetAlleleResponse = BeaconDatasetAlleleResponse.newBuilder()
                .setVariantCount(count)
                .setExists(count != 0)
                .setDatasetId("")
                .build();

        BeaconAlleleResponse.Builder builder = BeaconAlleleResponse.newBuilder()
                .setAlleleRequest(request)
                .setBeaconId(getBeacon().getId())
                .setError(null)
                .setExists(datasetAlleleResponse.getExists());

        if (BooleanUtils.isTrue(request.getIncludeDatasetResponses())) {
            builder.setDatasetAlleleResponses(ImmutableList.of(datasetAlleleResponse));
        }

        return builder.build();
    }

    private Predicate<Variant> filterVariants(BeaconAlleleRequest request) {
        return variant -> Objects.equals(request.getStart(), (long) variant.getStartPosition())
                && Objects.equals(request.getStart() + 1, (long) variant.getEndPosition())
                && variant.getAltBases().equalsIgnoreCase(request.getAlternateBases())
                && variant.getRefBases().equalsIgnoreCase(request.getReferenceBases())
                && variant.getAssembly().equalsIgnoreCase(request.getAssemblyId());
    }

    @Override
    public BeaconAlleleResponse getBeaconAlleleResponse(String referenceName, Long start, String referenceBases,
                                                        String alternateBases, String assemblyId, List<String> datasetIds,
                                                        Boolean includeDatasetResponses) throws BeaconException {
        checkAdapterInit();

        BeaconAlleleRequest request = BeaconAlleleRequest.newBuilder()
                .setReferenceName(referenceName)
                .setStart(start)
                .setReferenceBases(referenceBases)
                .setAlternateBases(alternateBases)
                .setAssemblyId(assemblyId)
                .setDatasetIds(datasetIds)
                .setIncludeDatasetResponses(includeDatasetResponses)
                .build();

        return getBeaconAlleleResponse(request);
    }

    @Override
    public Beacon getBeacon() throws BeaconException {
        if (beacon == null) {
            throw new IllegalStateException("Beacon not initialized");
        }
        return beacon;
    }

    public PhenopacketClient getPhenopacketClient() {
        return phenopacketClient;
    }

}
