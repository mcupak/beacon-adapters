package com.dnastack.beacon.adapter.variants;

import com.dnastack.beacon.adapter.api.BeaconAdapter;
import com.dnastack.beacon.adapter.variants.client.ga4gh.Ga4ghClient;
import com.dnastack.beacon.adapter.variants.client.ga4gh.exceptions.Ga4ghClientException;
import com.dnastack.beacon.exceptions.BeaconAlleleRequestException;
import com.dnastack.beacon.exceptions.BeaconException;
import com.dnastack.beacon.utils.AdapterConfig;
import com.dnastack.beacon.utils.ConfigValue;
import com.dnastack.beacon.utils.Reason;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.ListValue;
import ga4gh.Metadata;
import ga4gh.References;
import ga4gh.Variants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ga4gh.beacon.*;

import javax.enterprise.context.Dependent;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
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

    private BeaconDatasetAlleleResponse getDatasetResponse(String referenceName, long start, String referenceBases, String alternateBases, String assemblyId, String datasetId) throws BeaconAlleleRequestException {
        List<Variants.VariantSet> variantSets = getVariantSetsToSearch(datasetId, assemblyId);

        List<Variants.Variant> variants = map(variantSets,
                variantSet -> loadVariants(variantSet.getId(), referenceName, start))
                .stream()
                .flatMap(Collection::stream)
                .filter(variant -> basesMatchVariant(variant,
                        referenceBases,
                        alternateBases))
                .collect(Collectors.toList());

        Double frequency = calculateFrequency(alternateBases, variants);
        Long sampleCount = countSamples(variants);
        Long callsCount = variants.stream().mapToLong(Variants.Variant::getCallsCount).sum();
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

    private Long countSamples(List<Variants.Variant> variants) throws BeaconAlleleRequestException {
        List<String> callSetIds = variants.stream()
                .flatMap(variant -> variant.getCallsList().stream())
                .map(Variants.Call::getCallSetId)
                .collect(Collectors.toList());

        List<Variants.CallSet> callSets = new ArrayList<>();

        for (String callSetId : callSetIds) {
            callSets.add(loadCallSet(callSetId));
        }

        return callSets.stream().map(Variants.CallSet::getBiosampleId).distinct().count();
    }

    private Double calculateFrequency(String alternateBases, List<Variants.Variant> variants) {
        Long matchingGenotypesCount = variants.stream()
                .mapToLong(variant -> calculateMatchingGenotypesCount(variant,
                        alternateBases))
                .sum();

        Long totalGenotypesCount = variants.stream()
                .flatMap(variant -> variant.getCallsList().stream())
                .map(Variants.Call::getGenotype)
                .mapToLong(ListValue::getValuesCount)
                .sum();

        return (totalGenotypesCount == 0) ? null : ((double) matchingGenotypesCount / totalGenotypesCount);
    }

    private long calculateMatchingGenotypesCount(Variants.Variant variant, String alternateBases) {
        int requestedGenotype = variant.getAlternateBasesList().indexOf(alternateBases) + 1;

        return variant.getCallsList()
                .stream()
                .map(Variants.Call::getGenotype)
                .flatMap(listValue -> listValue.getValuesList().stream())
                .filter(genotype -> genotype.getNumberValue() == (double) requestedGenotype)
                .count();
    }

    private List<Variants.VariantSet> getVariantSetsToSearch(String datasetId, String assemblyId) throws BeaconAlleleRequestException {
        List<Variants.VariantSet> variantSets = loadVariantSets(datasetId);
        filter(variantSets, variantSet -> referencesetMatchesVariantset(variantSet, assemblyId));
        return variantSets;
    }

    private boolean referencesetMatchesVariantset(Variants.VariantSet variantSet, String assemblyId) throws BeaconAlleleRequestException {
        References.ReferenceSet referenceSet = loadReferenceSet(variantSet.getReferenceSetId());

        Optional<Set<String>> assemblySynonyms = ASSEMBLY_ALIASES.stream()
                .filter(bag -> containsCaseInsensitive(bag,
                        assemblyId))
                .findAny();

        return assemblySynonyms.isPresent() ? containsCaseInsensitive(assemblySynonyms.get(),
                referenceSet.getAssemblyId()) : false;
    }

    private boolean basesMatchVariant(Variants.Variant variant, String referenceBases, String alternateBases) {
        return StringUtils.equals(variant.getReferenceBases(), referenceBases) && variant.getAlternateBasesList()
                .contains(alternateBases);
    }

    private List<String> getDatasetIdsToSearch(List<String> requestedDatasetIds) throws BeaconAlleleRequestException {
        return CollectionUtils.isNotEmpty(requestedDatasetIds) ? requestedDatasetIds : loadAllDatasetIds();
    }

    private List<String> loadAllDatasetIds() throws BeaconAlleleRequestException {
        return loadAllDatasets().stream().map(Metadata.Dataset::getId).collect(Collectors.toList());
    }

    private List<Metadata.Dataset> loadAllDatasets() throws BeaconAlleleRequestException {
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

    private List<Variants.Variant> loadVariants(String variantSetId, String referenceName, long start) throws BeaconAlleleRequestException {
        try {
            return ga4ghClient.searchVariants(variantSetId, referenceName, start);
        } catch (Ga4ghClientException e) {
            BeaconAlleleRequestException alleleRequestException = new BeaconAlleleRequestException(
                    "Couldn't load reference set with id %s.",
                    Reason.CONN_ERR,
                    null);
            alleleRequestException.initCause(e);
            throw alleleRequestException;
        }
    }

    private List<Variants.VariantSet> loadVariantSets(String datasetId) throws BeaconAlleleRequestException {
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

    private References.ReferenceSet loadReferenceSet(String referenceSetId) throws BeaconAlleleRequestException {
        try {
            return ga4ghClient.loadReferenceSet(referenceSetId);
        } catch (Ga4ghClientException e) {
            BeaconAlleleRequestException alleleRequestException = new BeaconAlleleRequestException(
                    "Couldn't load reference set with id %s.",
                    Reason.CONN_ERR,
                    null);
            alleleRequestException.initCause(e);
            throw alleleRequestException;
        }
    }

    private Variants.CallSet loadCallSet(String callSetId) throws BeaconAlleleRequestException {
        try {
            return ga4ghClient.loadCallSet(callSetId);
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
        if (ga4ghClient == null) {
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
        String url = null;
        List<ConfigValue> configValues = adapterConfig.getConfigValues();

        for (ConfigValue configValue : configValues) {
            if (configValue.getName().equalsIgnoreCase("url")) {
                url = configValue.getValue();
            }
        }

        ga4ghClient = new Ga4ghClient(url);
    }

    @Override
    public BeaconAlleleResponse getBeaconAlleleResponse(BeaconAlleleRequest request) throws BeaconException {
        checkAdapterInit();
        try {
            List<String> datasetIdsToSearch = getDatasetIdsToSearch(request.getDatasetIds());

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
        checkAdapterInit();
        try {
            return ga4ghClient.getBeacon();
        } catch (Ga4ghClientException e) {
            throw new IllegalStateException("Beacon has not been initialized");
        }
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

}
