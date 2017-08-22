package com.dnastack.beacon.adapter.variants.client.ga4gh.retro;

import ga4gh.References.ReferenceSet;
import ga4gh.VariantServiceOuterClass.SearchVariantSetsRequest;
import ga4gh.VariantServiceOuterClass.SearchVariantSetsResponse;
import ga4gh.VariantServiceOuterClass.SearchVariantsRequest;
import ga4gh.VariantServiceOuterClass.SearchVariantsResponse;
import ga4gh.Variants.CallSet;
import org.ga4gh.beacon.BeaconOuterClass.Beacon;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

import static ga4gh.MetadataServiceOuterClass.SearchDatasetsRequest;
import static ga4gh.MetadataServiceOuterClass.SearchDatasetsResponse;

/**
 * @author Artem (tema.voskoboynick@gmail.com)
 * @author Miro Cupak (mirocupak@gmail.com)
 * @version 1.0
 */

public interface Ga4ghRetroService {

    String DATASET_SEARCH_PATH = "ga4gh/datasets/search";
    String VARIANT_SETS_SEARCH_PATH = "ga4gh/variantsets/search";
    String VARIANTS_SEARCH_PATH = "ga4gh/com.dnastack.beacon.adapter.variants.variants/search";
    String REFERENCE_SETS_GET_PATH = "ga4gh/referencesets";
    String CALL_SETS_GET_PATH = "ga4gh/callsets";
    String BEACON_SEARCH_PATH = "ga4gh/beacon";

    String REFERENCE_SET_ID_PARAM = "id";
    String CALL_SET_ID_PARAM = "id";

    @POST(DATASET_SEARCH_PATH)
    Call<SearchDatasetsResponse> searchDatasets(@Body SearchDatasetsRequest request);

    @POST(VARIANTS_SEARCH_PATH)
    Call<SearchVariantsResponse> searchVariants(@Body SearchVariantsRequest request);

    @POST(VARIANT_SETS_SEARCH_PATH)
    Call<SearchVariantSetsResponse> searchVariantSets(@Body SearchVariantSetsRequest request);

    @GET(REFERENCE_SETS_GET_PATH + "/{id}")
    Call<ReferenceSet> loadReferenceSet(@Path(REFERENCE_SET_ID_PARAM) String id);

    @GET(CALL_SETS_GET_PATH + "/{id}")
    Call<CallSet> loadCallSet(@Path(CALL_SET_ID_PARAM) String id);

    @GET(BEACON_SEARCH_PATH)
    Call<Beacon> searchBeacon();
}
