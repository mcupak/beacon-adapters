package com.dnastack.beacon.adapter.variants


import com.dnastack.beacon.adater.variants.VariantsBeaconAdapter
import com.dnastack.beacon.utils.AdapterConfig
import com.dnastack.beacon.utils.ConfigValue
import com.github.tomakehurst.wiremock.WireMockServer
import com.google.gson.Gson
import org.apache.commons.lang.StringUtils
import org.ga4gh.models.CallSet
import org.ga4gh.models.ReferenceSet
import org.testng.annotations.AfterMethod
import org.testng.annotations.AfterSuite
import org.testng.annotations.BeforeSuite
import org.testng.annotations.Test

import static com.dnastack.beacon.adapter.variants.TestData.*
import static com.dnastack.beacon.adater.variants.client.ga4gh.retro.Ga4ghRetroService.*
import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

/**
 * Helper class for variants adapter tests.
 * </p>
 * When the java property ga4gh.testServer.url is not specified, the Ga4gh server is mocked, otherwise tests
 * are run against the specified one.
 * </p>
 * Not all tests might support integration testing against a real Beacon server - this is defined by
 * {@link com.dnastack.beacon.adapter.variants.BaseTest#isIntegrationTestingSupported()}
 * </p>
 * For integration tests against a real server to work properly, the test data should equal to the real data. Currently,
 * it doesn't, anyway the tests infrastructure allows integration tests per se (may be added in the future).
 *
 * @author Artem (tema.voskoboynick@gmail.com)
 * @version 1.0
 */
public abstract class BaseTest {
    static final def MOCK_GA4GH_PORT = 8089
    static final def MOCK_GA4GH_SERVER = new WireMockServer(wireMockConfig().port(MOCK_GA4GH_PORT))
    static final VariantsBeaconAdapter ADAPTER
    static final boolean MOCKED_TESTING
    private final static String BEACON_FILE = "test_beacon.json";
    private final static Gson GSON = new Gson()


/**
 * Define if the testing will be against a real Ga4gh server, or the mocked one.
 */
    static {
        def ga4ghTestUrl = System.properties.getProperty("ga4gh.testServer.url")
        MOCKED_TESTING = StringUtils.isBlank(ga4ghTestUrl)

        // Adapter initialization.

        ClassLoader cl = VariantsBeaconAdapter.class.getClassLoader();
        String beaconJson = cl.getResource(BEACON_FILE).toURI().getPath();

        ADAPTER = new VariantsBeaconAdapter()

        def configValues = [new ConfigValue("beaconJsonFile", beaconJson), new ConfigValue("projectId", "id")]

        def adapterConfig = new AdapterConfig("Variants Test Adapter", VariantsBeaconAdapter.getName(), configValues)
        ADAPTER.initAdapter(adapterConfig)
    }

    @BeforeSuite
    void startServer() {
        if (MOCKED_TESTING) {
            MOCK_GA4GH_SERVER.start();
        }
    }

    @AfterSuite
    void stopServer() {
        if (MOCKED_TESTING) {
            MOCK_GA4GH_SERVER.stop();
        }
    }

    @AfterMethod
    void resetMappings() {
        if (MOCKED_TESTING) {
            MOCK_GA4GH_SERVER.resetMappings();
        }
    }

    @Test
    void test() {
        if (!MOCKED_TESTING && !isIntegrationTestingSupported()) {
            return
        }

        if (MOCKED_TESTING) {
            setupMappings()
        }

        doTest()
    }

    void setupMappings() {
        setupGeneralMappings()
    }

    boolean isIntegrationTestingSupported() { return true }

    abstract void doTest();

    /**
     * Setups up the mappings usually shared by all tests.
     */
    protected setupGeneralMappings() {
        setupSearchDatasetMapping()
        setupSearchVariantSetsMapping()
        setupSearchVariantsMapping()
        setupGetReferenceSetMapping(getTestReferenceSet())
        setupGetCallSetMapping()
    }

    private setupSearchDatasetMapping() {
        MOCK_GA4GH_SERVER.stubFor(get(urlEqualTo("/$DATASET_SEARCH_GET_PATH/projectId=id"))

                .willReturn(aResponse()
                .withBody(GSON.toJson(getSearchDatasetsResponse()))))
    }

    private setupSearchVariantSetsMapping() {
        MOCK_GA4GH_SERVER.stubFor(post(urlEqualTo("/$VARIANT_SETS_SEARCH_PATH"))
                .withRequestBody(equalToJson(GSON.toJson(getSearchVariantSetsRequest())))

                .willReturn(aResponse()
                .withBody(GSON.toJson(getSearchVariantSetsResponse()))))
    }

    private setupSearchVariantsMapping() {
        MOCK_GA4GH_SERVER.stubFor(post(urlEqualTo("/$VARIANTS_SEARCH_PATH"))
                .withRequestBody(equalToJson(GSON.toJson(getSearchVariantsRequest())))

                .willReturn(aResponse()
                .withBody(GSON.toJson(getSearchVariantsResponse()))))
    }

    private setupGetReferenceSetMapping(ReferenceSet referenceSet) {
        MOCK_GA4GH_SERVER.stubFor(get(urlEqualTo("/$REFERENCE_SETS_GET_PATH/$referenceSet.id"))

                .willReturn(aResponse()
                .withBody(GSON.toJson(getTestReferenceSet()))))
    }

    private setupGetCallSetMapping() {
        setupGetCallSetMapping(getTestCallSet1())
        setupGetCallSetMapping(getTestCallSet2())
    }

    private setupGetCallSetMapping(CallSet callSet) {
        MOCK_GA4GH_SERVER.stubFor(get(urlEqualTo("/$CALL_SETS_GET_PATH/$callSet.id"))

                .willReturn(aResponse()
                .withBody(GSON.toJson(callSet))))
    }
}
