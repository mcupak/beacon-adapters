package com.dnastack.beacon.adater.variants.client.ga4gh;

import com.dnastack.beacon.adater.variants.client.ga4gh.exceptions.Ga4ghClientException;
import com.dnastack.beacon.adater.variants.client.ga4gh.retro.Ga4ghRetroService;
import com.dnastack.beacon.adater.variants.client.ga4gh.retro.Ga4ghRetroServiceFactory;
import com.google.common.base.Preconditions;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message.Builder;
import ga4gh.Metadata.Dataset;
import ga4gh.MetadataServiceOuterClass.SearchDatasetsRequest;
import ga4gh.MetadataServiceOuterClass.SearchDatasetsResponse;
import ga4gh.References.ReferenceSet;
import ga4gh.VariantServiceOuterClass.SearchVariantSetsRequest;
import ga4gh.VariantServiceOuterClass.SearchVariantSetsResponse;
import ga4gh.VariantServiceOuterClass.SearchVariantsRequest;
import ga4gh.VariantServiceOuterClass.SearchVariantsResponse;
import ga4gh.Variants.CallSet;
import ga4gh.Variants.Variant;
import ga4gh.Variants.VariantSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Executes requests to the given Ga4gh server.
 *
 * @author Artem (tema.voskoboynick@gmail.com)
 * @author Miro Cupak (mirocupak@gmail.com)
 * @version 1.0
 */
public class Ga4ghClient {

    public static final String DEFAULT_BASE_URL = "http://1kgenomes.ga4gh.org/";

    private Ga4ghRetroService ga4ghRetroService;

    /**
     * A function that returns a single response page for the given request and throws {@link Ga4ghClientException} on
     * any IO error. In fact, this is just a copy of the the Java 8 function, but that throws {@link Ga4ghClientException}.
     */
    @FunctionalInterface
    private interface RequestExecutor<REQUEST, RESPONSE> {

        RESPONSE execute(REQUEST request) throws Ga4ghClientException;
    }

    /**
     * Creates a Ga4gh client with the default
     * base url: {@value Ga4ghClient#DEFAULT_BASE_URL}.
     */
    public Ga4ghClient() {
        setBaseUrl(DEFAULT_BASE_URL);
    }

    /**
     * Creates a Ga4gh Client with the specified base url.
     */
    public Ga4ghClient(String baseUrl) {
        setBaseUrl(baseUrl);
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

        Builder requestBuilder = request.toBuilder();
        String nextPageToken = "";
        do {
            RESPONSE responsePage = loadResponsePage(requestBuilder, requestExecutor, nextPageToken);
            responsePages.add(responsePage);

            nextPageToken = invokeMethod(responsePage, "getNextPageToken");
        } while (StringUtils.isNotBlank(nextPageToken));

        return responsePages;
    }

    private <REQUEST extends GeneratedMessage, RESPONSE> RESPONSE loadResponsePage(Builder requestBuilder, RequestExecutor<REQUEST, RESPONSE> requestExecutor, String nextPageToken) throws Ga4ghClientException {
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

    public void setBaseUrl(String baseUrl) {
        Preconditions.checkArgument(StringUtils.isNotBlank(baseUrl), "baseUrl mustn't be null or empty.");
        ga4ghRetroService = Ga4ghRetroServiceFactory.create(baseUrl);
    }

    public List<Dataset> searchDatasets() throws Ga4ghClientException {
        SearchDatasetsRequest request = SearchDatasetsRequest.newBuilder().build();

        List<SearchDatasetsResponse> allResponsePages = requestAllResponsePages(request,
                                                                                pagedRequest -> executeCall(
                                                                                        ga4ghRetroService.searchDatasets(
                                                                                                pagedRequest)));

        List<Dataset> allDatasets = allResponsePages.stream()
                                                    .flatMap(responsePage -> responsePage.getDatasetsList().stream())
                                                    .collect(Collectors.toList());
        return allDatasets;
    }

    public List<Variant> searchVariants(String variantSetId, String referenceName, long start) throws Ga4ghClientException {
        SearchVariantsRequest request = SearchVariantsRequest.newBuilder()
                                                             .setVariantSetId(variantSetId)
                                                             .setReferenceName(referenceName)
                                                             .setStart(start)
                                                             .setEnd(start + 1)
                                                             .build();

        List<SearchVariantsResponse> allResponsePages = requestAllResponsePages(request,
                                                                                pagedRequest -> executeCall(
                                                                                        ga4ghRetroService.searchVariants(
                                                                                                pagedRequest)));

        List<Variant> variants = allResponsePages.stream()
                                                 .flatMap(responsePage -> responsePage.getVariantsList().stream())
                                                 .collect(Collectors.toList());
        return variants;
    }

    public List<VariantSet> searchVariantSets(String datasetId) throws Ga4ghClientException {
        SearchVariantSetsRequest request = SearchVariantSetsRequest.newBuilder().setDatasetId(datasetId).build();

        List<SearchVariantSetsResponse> allResponsePages = requestAllResponsePages(request,
                                                                                   pagedRequest -> executeCall(
                                                                                           ga4ghRetroService.searchVariantSets(
                                                                                                   pagedRequest)));

        List<VariantSet> variantSets = allResponsePages.stream()
                                                       .flatMap(responsePage -> responsePage.getVariantSetsList()
                                                                                            .stream())
                                                       .collect(Collectors.toList());
        return variantSets;
    }

    public ReferenceSet loadReferenceSet(String id) throws Ga4ghClientException {
        return executeCall(ga4ghRetroService.loadReferenceSet(id));
    }

    public CallSet loadCallSet(String id) throws Ga4ghClientException {
        return executeCall(ga4ghRetroService.loadCallSet(id));
    }
}
