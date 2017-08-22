package com.dnastack.beacon.adapter.variants.client.ga4gh.retro;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

import java.util.concurrent.TimeUnit;

/**
 * @author Artem (tema.voskoboynick@gmail.com)
 * @author Miro Cupak (mirocupak@gmail.com)
 * @version 1.0
 */
public class Ga4ghRetroServiceFactory {

    /**
     * GsonConverterFactory is thread-safe. Can declare it static.
     */
    private static final ProtoJsonConverter CONVERTER_FACTORY = ProtoJsonConverter.create();
    private static final HttpLoggingInterceptor bodyInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);

    /**
     * OkHttpClient is thread-safe. Can declare it static.
     * Set read timeout to 5 minutes as querying beacons may take quite a long time.
     */
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder().readTimeout(5, TimeUnit.MINUTES)
                                                                              .addNetworkInterceptor(chain -> {
                                                                                  Request request = chain.request()
                                                                                                         .newBuilder()
                                                                                                         .addHeader(
                                                                                                                 "Accept",
                                                                                                                 "application/json")
                                                                                                         .build();
                                                                                  return chain.proceed(request);
                                                                              })
                                                                              .addInterceptor(bodyInterceptor)
                                                                              .build();

    public static Ga4ghRetroService create(String baseUrl) {
        return new Retrofit.Builder().client(HTTP_CLIENT)
                                     .addConverterFactory(CONVERTER_FACTORY)
                                     .baseUrl(baseUrl)
                                     .build()
                                     .create(Ga4ghRetroService.class);
    }

    private Ga4ghRetroServiceFactory() {
    }
}