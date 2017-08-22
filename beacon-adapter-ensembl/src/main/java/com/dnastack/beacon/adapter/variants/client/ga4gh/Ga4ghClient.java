package com.dnastack.beacon.adapter.variants.client.ga4gh;

import com.dnastack.beacon.adapter.variants.client.ga4gh.retro.Ga4ghRetroService;
import com.dnastack.beacon.adapter.variants.client.ga4gh.retro.Ga4ghRetroServiceFactory;
import com.dnastack.beacon.adapter.variants.client.ga4gh.exceptions.Ga4ghClientException;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import ga4gh.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.ga4gh.beacon.Beacon;
import org.ga4gh.beacon.BeaconOuterClass;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
public class Ga4ghClient {

    private final Ga4ghRetroService ga4ghRetroService;

    public Ga4ghClient(String url) {
        ga4ghRetroService = Ga4ghRetroServiceFactory.create(url);
    }

    public Beacon getBeacon() throws Ga4ghClientException {
        BeaconOuterClass.Beacon beaconJson = executeCall(ga4ghRetroService.searchBeacon());

        return Beacon.newBuilder()
                .setId(beaconJson.getId())
                .setName(beaconJson.getName())
                .setApiVersion(beaconJson.getApiVersion())
                .setOrganization(convertToOrganization(beaconJson.getOrganization()))
                .setDescription(beaconJson.getDescription())
                .setVersion(beaconJson.getVersion())
                .setWelcomeUrl(beaconJson.getWelcomeUrl())
                .setAlternativeUrl(beaconJson.getAlternativeUrl())
                .setCreateDateTime(beaconJson.getCreateDateTime())
                .setUpdateDateTime(beaconJson.getUpdateDateTime())
                .setDatasets(beaconJson.getDatasetsList().stream().map(this::convertToDataset).collect(Collectors.toList()))
                .setSampleAlleleRequests(beaconJson.getSampleAlleleRequestsList().stream().map(this::convertToSampleAlleleRequest).collect(Collectors.toList()))
                .setInfo(beaconJson.getInfo())
                .build();
    }

    private org.ga4gh.beacon.BeaconAlleleRequest convertToSampleAlleleRequest(BeaconOuterClass.BeaconAlleleRequest beaconAlleleRequest) {
        return org.ga4gh.beacon.BeaconAlleleRequest.newBuilder()
                .setReferenceName(beaconAlleleRequest.getReferenceName())
                .setStart(beaconAlleleRequest.getStart())
                .setReferenceBases(beaconAlleleRequest.getReferenceBases())
                .setAlternateBases(beaconAlleleRequest.getAlternateBases())
                .setAssemblyId(beaconAlleleRequest.getAssemblyId())
                .setDatasetIds(beaconAlleleRequest.getDatasetIdsList())
                .setIncludeDatasetResponses(beaconAlleleRequest.getIncludeDatasetResponses())
                .build();
    }

    private org.ga4gh.beacon.BeaconDataset convertToDataset(BeaconOuterClass.BeaconDataset beaconDataset) {
        return org.ga4gh.beacon.BeaconDataset.newBuilder()
                .setId(beaconDataset.getId())
                .setName(beaconDataset.getName())
                .setDescription(beaconDataset.getDescription())
                .setAssemblyId(beaconDataset.getAssemblyId())
                .setCreateDateTime(beaconDataset.getCreateDateTime())
                .setUpdateDateTime(beaconDataset.getUpdateDateTime())
                .setVersion(beaconDataset.getVersion())
                .setVariantCount(beaconDataset.getVariantCount())
                .setCallCount(beaconDataset.getCallCount())
                .setSampleCount(beaconDataset.getSampleCount())
                .setExternalUrl(beaconDataset.getExternalUrl())
                .setInfo(beaconDataset.getInfo())
                .build();
    }

    private org.ga4gh.beacon.BeaconOrganization convertToOrganization(BeaconOuterClass.BeaconOrganization organization) {
        return org.ga4gh.beacon.BeaconOrganization.newBuilder()
                .setId(organization.getId())
                .setName(organization.getName())
                .setDescription(organization.getDescription())
                .setAddress(organization.getAddress())
                .setWelcomeUrl(organization.getWelcomeUrl())
                .setContactUrl(organization.getContactUrl())
                .setLogoUrl(organization.getLogoUrl())
                .setInfo(organization.getInfo())
                .build();
    }

    /**
     * A function that returns a single response page for the given request and throws {@link Ga4ghClientException} on
     * any IO error. In fact, this is just a copy of the the Java 8 function, but that throws {@link Ga4ghClientException}.
     */
    @FunctionalInterface
    private interface RequestExecutor<REQUEST, RESPONSE> {

        RESPONSE execute(REQUEST request) throws Ga4ghClientException;
    }

    private <T> T executeCall(Call<T> call) throws Ga4ghClientException {
        Response<T> response;
        try {
            response = call.execute();
        } catch (IOException | RuntimeException e) {
            throw new Ga4ghClientException("Error during communication to server.", e);
        }

        if (response.isSuccessful()) {
            return response.body();
        } else {
            throw new Ga4ghClientException(String.format("Received error response from server. HTTP code: %s",
                    response.code()));
        }
    }

    /**
     * Loads all response pages for given request. Ga4gh server returns responses by pages.
     *
     * @return list of all response pages
     * @throws Ga4ghClientException on IO error
     */
    private <REQUEST extends GeneratedMessage, RESPONSE> List<RESPONSE> requestAllResponsePages(REQUEST request, RequestExecutor<REQUEST, RESPONSE> requestExecutor) throws Ga4ghClientException {
        List<RESPONSE> responsePages = new ArrayList<>();

        Message.Builder requestBuilder = request.toBuilder();
        String nextPageToken = "";
        do {
            RESPONSE responsePage = loadResponsePage(requestBuilder, requestExecutor, nextPageToken);
            responsePages.add(responsePage);

            nextPageToken = invokeMethod(responsePage, "getNextPageToken");
        } while (StringUtils.isNotBlank(nextPageToken));

        return responsePages;
    }

    private <REQUEST extends GeneratedMessage, RESPONSE> RESPONSE loadResponsePage(Message.Builder requestBuilder, RequestExecutor<REQUEST, RESPONSE> requestExecutor, String nextPageToken) throws Ga4ghClientException {
        invokeMethod(requestBuilder, "setPageToken", nextPageToken);
        //noinspection unchecked
        REQUEST requestWithPageToken = (REQUEST) requestBuilder.build();
        return requestExecutor.execute(requestWithPageToken);
    }

    @SuppressWarnings("unchecked")
    private <T> T invokeMethod(Object response, String methodName, Object... args) throws Ga4ghClientException {
        try {
            return (T) MethodUtils.invokeMethod(response, methodName, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new Ga4ghClientException(String.format("Couldn't invoke method %s.", methodName), e);
        }
    }

    public List<Metadata.Dataset> searchDatasets() throws Ga4ghClientException {
        MetadataServiceOuterClass.SearchDatasetsRequest request = MetadataServiceOuterClass.SearchDatasetsRequest.newBuilder().build();

        List<MetadataServiceOuterClass.SearchDatasetsResponse> allResponsePages = new ArrayList<>();

        allResponsePages.addAll(requestAllResponsePages(request,
                pagedRequest -> executeCall(ga4ghRetroService.searchDatasets(
                        pagedRequest))));


        return allResponsePages.stream()
                .flatMap(responsePage -> responsePage.getDatasetsList().stream())
                .collect(Collectors.toList());
    }

    public List<Variants.Variant> searchVariants(String variantSetId, String referenceName, long start) throws Ga4ghClientException {
        VariantServiceOuterClass.SearchVariantsRequest request = VariantServiceOuterClass.SearchVariantsRequest.newBuilder()
                .setVariantSetId(variantSetId)
                .setReferenceName(referenceName)
                .setStart(start)
                .setEnd(start + 1)
                .build();

        List<VariantServiceOuterClass.SearchVariantsResponse> allResponsePages = requestAllResponsePages(request,
                pagedRequest -> executeCall(ga4ghRetroService.searchVariants(pagedRequest)));

        return allResponsePages.stream()
                .flatMap(responsePage -> responsePage.getVariantsList().stream())
                .collect(Collectors.toList());
    }

    public List<Variants.VariantSet> searchVariantSets(String datasetId) throws Ga4ghClientException {
        VariantServiceOuterClass.SearchVariantSetsRequest request = VariantServiceOuterClass.SearchVariantSetsRequest.newBuilder().setDatasetId(datasetId).build();

        List<VariantServiceOuterClass.SearchVariantSetsResponse> allResponsePages = requestAllResponsePages(request,
                pagedRequest -> executeCall(
                        ga4ghRetroService.searchVariantSets(pagedRequest)));

        return allResponsePages.stream()
                .flatMap(responsePage -> responsePage.getVariantSetsList().stream())
                .collect(Collectors.toList());
    }

    public References.ReferenceSet loadReferenceSet(String referenceSetId) throws Ga4ghClientException {
        return executeCall(ga4ghRetroService.loadReferenceSet(referenceSetId));
    }

    public Variants.CallSet loadCallSet(String callSetId) throws Ga4ghClientException {
        return executeCall(ga4ghRetroService.loadCallSet(callSetId));
    }

}
