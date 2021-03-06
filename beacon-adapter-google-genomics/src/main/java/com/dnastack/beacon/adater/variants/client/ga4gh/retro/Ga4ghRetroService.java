package com.dnastack.beacon.adater.variants.client.ga4gh.retro;

import com.dnastack.beacon.adater.variants.client.ga4gh.model.*;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * @author Artem (tema.voskoboynick@gmail.com)
 * @author Miro Cupak (mirocupak@gmail.com)
 * @version 1.0
 */

public interface Ga4ghRetroService {

    String DATASET_SEARCH_GET_PATH = "datasets";
    String VARIANT_SETS_SEARCH_PATH = "variantsets/search";
    String VARIANTS_SEARCH_PATH = "variants/search";
    String REFERENCE_SETS_GET_PATH = "referencesets";
    String CALL_SETS_GET_PATH = "callsets";

    String REFERENCE_SET_ID_PARAM = "id";
    String CALL_SET_ID_PARAM = "id";
    String PROJECT_ID = "id";

    @GET(DATASET_SEARCH_GET_PATH + "/projectId={id}")
    Call<SearchDatasetsResponse> searchDatasets(@Path(PROJECT_ID) String id);

    @POST(VARIANTS_SEARCH_PATH)
    Call<SearchVariantsResponse> searchVariants(@Body SearchVariantsRequest request);

    @POST(VARIANT_SETS_SEARCH_PATH)
    Call<SearchVariantSetsResponse> searchVariantSets(@Body SearchVariantSetsRequest request);

    @GET(REFERENCE_SETS_GET_PATH + "/{id}")
    Call<ReferenceSet> loadReferenceSet(@Path(REFERENCE_SET_ID_PARAM) String id);

    @GET(CALL_SETS_GET_PATH + "/{id}")
    Call<CallSet> loadCallSet(@Path(CALL_SET_ID_PARAM) String id);
}
