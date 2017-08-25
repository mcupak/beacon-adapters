package com.dnastack.beacon.adater.variants;

import com.dnastack.beacon.adapter.api.BeaconAdapter;
import com.dnastack.beacon.adater.variants.client.ga4gh.Ga4ghClient;
import com.dnastack.beacon.adater.variants.client.ga4gh.exceptions.Ga4ghClientException;
import com.dnastack.beacon.adater.variants.client.ga4gh.model.*;
import com.dnastack.beacon.exceptions.BeaconAlleleRequestException;
import com.dnastack.beacon.exceptions.BeaconException;
import com.dnastack.beacon.utils.AdapterConfig;
import com.dnastack.beacon.utils.ConfigValue;
import com.dnastack.beacon.utils.Reason;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ga4gh.beacon.*;

import javax.enterprise.context.Dependent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Exposes Ga4gh variants as a Beacon.
 * Queries the underlying Ga4gh client and assembles Beacon responses.
 *
 * @author Artem (tema.voskoboynick@gmail.com)
 * @author Miro Cupak (mirocupak@gmail.com)
 * @version 1.0
 */
@Dependent
public class VariantsBeaconAdapter implements BeaconAdapter {

    private static final Set<Set<String>> ASSEMBLY_ALIASES = ImmutableSet.of(ImmutableSet.of("grch38", "hg38", "hg20"),
            ImmutableSet.of("grch37", "hg37", "hg19"),
            ImmutableSet.of("ncbi36", "hg18"),
            ImmutableSet.of("ncbi35", "hg17"),
            ImmutableSet.of("ncbi34", "hg16"));

    private Ga4ghClient ga4ghClient;

    /**
     * Copy of the the Java 8 function, but can throw {@link BeaconAlleleRequestException}.
     */
    @FunctionalInterface
    public interface FunctionThrowingAlleleRequestException<T, R> {

        R apply(T t) throws BeaconAlleleRequestException;
    }

    /**
     * Copy of the the Java 8 predicate, but can throw {@link BeaconAlleleRequestException}.
     */
    @FunctionalInterface
    public interface PredicateThrowingAlleleRequestException<T> {

        boolean test(T t) throws BeaconAlleleRequestException;
    }

    private static boolean containsCaseInsensitive(Collection<String> container, String element) {
        return container.stream().anyMatch(s -> s.equalsIgnoreCase(element));
    }

    private void initGa4ghClient(AdapterConfig adapterConfig) {
        List<ConfigValue> configValues = adapterConfig.getConfigValues();
        Beacon beacon = null;
        String apiKey = null;
        String projectId = null;

        for (ConfigValue configValue : configValues) {
            switch (configValue.getName()) {
                case "beaconJsonFile":
                    beacon = readBeaconJsonFile(configValue.getValue());
                    break;
                case "beaconJson":
                    beacon = readBeaconJson(configValue.getValue());
                    break;
                case "apiKey":
                    apiKey = configValue.getValue();
                    break;
                case "projectId":
                    projectId = configValue.getValue();
                    break;

            }
        }

        if (beacon == null) {
            throw new RuntimeException(
                    "Missing required parameter: beaconJson. Please add the appropriate configuration parameter then retry");
        }

        ga4ghClient = new Ga4ghClient(Ga4ghClientRequest.builder()
                .beacon(beacon)
                .apiKey(apiKey)
                .projectId(projectId)
                .build());
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

    private BeaconAlleleRequest createRequest(String referenceName, Long start, String referenceBases, String alternateBases, String assemblyId, List<String> datasetIds, Boolean includeDatasetResponses) {
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

    private BeaconDatasetAlleleResponse getDatasetResponse(String referenceName, long start, String referenceBases, String alternateBases, String assemblyId, String datasetId) throws BeaconAlleleRequestException {
        List<VariantSet> variantSets = getVariantSetsToSearch(datasetId, assemblyId);

        List<Variant> variants = map(variantSets,
                variantSet -> loadVariants(datasetId,
                        variantSet.getId(),
                        referenceName,
                        start)).stream()
                .flatMap(Collection::stream)
                .filter(variant -> basesMatchVariant(variant,
                        referenceBases,
                        alternateBases))
                .collect(Collectors.toList());

        Double frequency = calculateFrequency(alternateBases, variants);
        Long sampleCount = countSamples(datasetId, variants);
        Long callsCount = variants.stream().map(Variant::getCalls).mapToLong(List::size).sum();
        long variantCount = variants.size();
        boolean exists = CollectionUtils.isNotEmpty(variants);

        return BeaconDatasetAlleleResponse.newBuilder()
                .setDatasetId(datasetId)
                .setFrequency(frequency)
                .setCallCount(callsCount)
                .setVariantCount(variantCount)
                .setSampleCount(sampleCount)
                .setExists(exists)
                .build();
    }

    private Long countSamples(String datasetId, List<Variant> variants) throws BeaconAlleleRequestException {
        List<String> callSetIds = variants.stream()
                .flatMap(variant -> variant.getCalls().stream())
                .map(Call::getCallSetId)
                .map(CharSequence::toString)
                .collect(Collectors.toList());

        List<CallSet> callSets = new ArrayList<>();

        for (String callSetId : callSetIds) {
            callSets.add(loadCallSet(datasetId, callSetId));
        }

        return callSets.stream().map(CallSet::getSampleId).distinct().count();
    }

    private Double calculateFrequency(String alternateBases, List<Variant> variants) {
        Long matchingGenotypesCount = variants.stream()
                .mapToLong(variant -> calculateMatchingGenotypesCount(variant,
                        alternateBases))
                .sum();

        Long totalGenotypesCount = variants.stream()
                .flatMap(variant -> variant.getCalls().stream())
                .map(Call::getGenotype)
                .mapToLong(List::size)
                .sum();

        return (totalGenotypesCount == 0) ? null : ((double) matchingGenotypesCount / totalGenotypesCount);
    }

    private long calculateMatchingGenotypesCount(Variant variant, String alternateBases) {
        int requestedGenotype = variant.getAlternateBases().stream().map(CharSequence::toString)
                .collect(Collectors.toList()).indexOf(alternateBases) + 1;

        return variant.getCalls()
                .stream()
                .map(Call::getGenotype)
                .flatMap(Collection::stream)
                .filter(integer -> integer.equals(requestedGenotype))
                .count();
    }

    private List<com.dnastack.beacon.adater.variants.client.ga4gh.model.VariantSet> getVariantSetsToSearch(String datasetId, String assemblyId) throws BeaconAlleleRequestException {
        List<com.dnastack.beacon.adater.variants.client.ga4gh.model.VariantSet> variantSets = loadVariantSets(datasetId);
        filter(variantSets, variantSet -> referencesetMatchesVariantset(datasetId, variantSet, assemblyId));
        return variantSets;
    }

    private boolean referencesetMatchesVariantset(String datasetId, com.dnastack.beacon.adater.variants.client.ga4gh.model.VariantSet variantSet, String assemblyId) throws BeaconAlleleRequestException {
        com.dnastack.beacon.adater.variants.client.ga4gh.model.ReferenceSet referenceSet = loadReferenceSet(datasetId, variantSet.getReferenceSetId().toString());

        Optional<Set<String>> assemblySynonyms = ASSEMBLY_ALIASES.stream()
                .filter(bag -> containsCaseInsensitive(bag,
                        assemblyId))
                .findAny();

        return assemblySynonyms.isPresent() ? containsCaseInsensitive(assemblySynonyms.get(),
                referenceSet.getAssemblyId().toString()) : false;
    }

    private boolean basesMatchVariant(Variant variant, String referenceBases, String alternateBases) {
        return StringUtils.equals(variant.getReferenceBases(), referenceBases)
                && variant.getAlternateBases().stream().map(CharSequence::toString).collect(Collectors.toList())
                    .contains(alternateBases);
    }

    private List<String> getDatasetIdsToSearch(List<String> requestedDatasetIds) throws BeaconAlleleRequestException {
        return CollectionUtils.isNotEmpty(requestedDatasetIds) ? requestedDatasetIds : loadAllDatasetIds();
    }

    private List<String> loadAllDatasetIds() throws BeaconAlleleRequestException {
        return loadAllDatasets().stream().map(Dataset::getId).map(CharSequence::toString).collect(Collectors.toList());
    }

    private List<Dataset> loadAllDatasets() throws BeaconAlleleRequestException {
        try {
            return ga4ghClient.searchDatasets();
        } catch (Ga4ghClientException e) {
            BeaconAlleleRequestException alleleRequestException = new BeaconAlleleRequestException(
                    "Couldn't load all datasets.",
                    Reason.CONN_ERR,
                    null);
            alleleRequestException.initCause(e);
            throw alleleRequestException;
        }
    }

    private List<com.dnastack.beacon.adater.variants.client.ga4gh.model.Variant> loadVariants(String datasetId, String variantSetId, String referenceName, long start) throws BeaconAlleleRequestException {
        try {
            return ga4ghClient.searchVariants(datasetId, variantSetId, referenceName, start);
        } catch (Ga4ghClientException e) {
            BeaconAlleleRequestException alleleRequestException = new BeaconAlleleRequestException(
                    "Couldn't load reference set with id %s.",
                    Reason.CONN_ERR,
                    null);
            alleleRequestException.initCause(e);
            throw alleleRequestException;
        }
    }

    private List<com.dnastack.beacon.adater.variants.client.ga4gh.model.VariantSet> loadVariantSets(String datasetId) throws BeaconAlleleRequestException {
        try {
            return ga4ghClient.searchVariantSets(datasetId);
        } catch (Ga4ghClientException e) {
            BeaconAlleleRequestException alleleRequestException = new BeaconAlleleRequestException(String.format(
                    "Couldn't load all variant sets for dataset id %s.",
                    datasetId), Reason.CONN_ERR, null);
            alleleRequestException.initCause(e);
            throw alleleRequestException;
        }
    }

    private com.dnastack.beacon.adater.variants.client.ga4gh.model.ReferenceSet loadReferenceSet(String datasetId, String referenceSetId) throws BeaconAlleleRequestException {
        try {
            return ga4ghClient.loadReferenceSet(datasetId, referenceSetId);
        } catch (Ga4ghClientException e) {
            BeaconAlleleRequestException alleleRequestException = new BeaconAlleleRequestException(
                    "Couldn't load reference set with id %s.",
                    Reason.CONN_ERR,
                    null);
            alleleRequestException.initCause(e);
            throw alleleRequestException;
        }
    }

    private com.dnastack.beacon.adater.variants.client.ga4gh.model.CallSet loadCallSet(String datasetId, String callSetId) throws BeaconAlleleRequestException {
        try {
            return ga4ghClient.loadCallSet(datasetId, callSetId);
        } catch (Ga4ghClientException e) {
            BeaconAlleleRequestException alleleRequestException = new BeaconAlleleRequestException(
                    "Couldn't load call set with id %s.",
                    Reason.CONN_ERR,
                    null);
            alleleRequestException.initCause(e);
            throw alleleRequestException;
        }
    }

    /**
     * Works the same way as the Java 8 filter API map method, but can throw {@link BeaconAlleleRequestException}.
     */
    private <T> void filter(Collection<T> collection, PredicateThrowingAlleleRequestException<? super T> filter) throws BeaconAlleleRequestException {
        Iterator<T> it = collection.iterator();

        while (it.hasNext()) {
            T item = it.next();

            if (!filter.test(item)) {
                it.remove();
            }
        }
    }

    private void checkAdapterInit() {
        if (ga4ghClient == null || ga4ghClient.getBeacon() == null) {
            throw new IllegalStateException("VariantsBeaconAdapter adapter has not been initialized");
        }
    }

    /**
     * Works the same way as the Java 8 stream API map method, but can throw {@link BeaconAlleleRequestException}.
     */
    private <T, R> List<R> map(List<T> list, FunctionThrowingAlleleRequestException<? super T, ? extends R> mapper) throws BeaconAlleleRequestException {
        List<R> result = new ArrayList<>();

        for (T item : list) {
            R mapped = mapper.apply(item);
            result.add(mapped);
        }

        return result;
    }

    @Override
    public void initAdapter(AdapterConfig adapterConfig) {
        initGa4ghClient(adapterConfig);
    }

    @Override
    public Beacon getBeacon() throws BeaconException {
        checkAdapterInit();
        return ga4ghClient.getBeacon();
    }

    @Override
    public BeaconAlleleResponse getBeaconAlleleResponse(BeaconAlleleRequest request) throws BeaconException {
        checkAdapterInit();

        try {
            List<String> datasetIdsToSearch = getDatasetIdsToSearch(request.getDatasetIds());

            for (String dataset : datasetIdsToSearch) {
                if (!ga4ghClient.isExistDataset(dataset)) {
                    throw new BeaconException(dataset);
                }
            }

            List<BeaconDatasetAlleleResponse> datasetResponses = map(datasetIdsToSearch,
                    datasetId -> getDatasetResponse(request.getReferenceName(),
                            request.getStart(),
                            request.getReferenceBases(),
                            request.getAlternateBases(),
                            request.getAssemblyId(),
                            datasetId));

            List<BeaconDatasetAlleleResponse> returnedDatasetResponses = BooleanUtils.isTrue(request.getIncludeDatasetResponses())
                    ? datasetResponses
                    : null;

            BeaconError anyError = datasetResponses.stream()
                    .map(BeaconDatasetAlleleResponse::getError)
                    .filter(Objects::nonNull)
                    .findAny()
                    .orElse(null);

            Boolean exists = anyError != null
                    ? null
                    : datasetResponses.stream().anyMatch(BeaconDatasetAlleleResponse::getExists);

            return BeaconAlleleResponse.newBuilder()
                    .setAlleleRequest(request)
                    .setDatasetAlleleResponses(returnedDatasetResponses)
                    .setBeaconId(getBeacon().getId())
                    .setError(anyError)
                    .setExists(exists)
                    .build();

        } catch (BeaconAlleleRequestException e) {
            e.setRequest(request);
            throw e;
        }
    }

    @Override
    public BeaconAlleleResponse getBeaconAlleleResponse(String referenceName, Long start, String referenceBases, String alternateBases, String assemblyId, List<String> datasetIds, Boolean includeDatasetResponses) throws BeaconException {
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

}
