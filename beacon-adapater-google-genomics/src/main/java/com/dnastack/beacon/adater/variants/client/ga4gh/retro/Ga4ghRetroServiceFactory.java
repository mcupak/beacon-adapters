package com.dnastack.beacon.adater.variants.client.ga4gh.retro;

import okhttp3.HttpUrl;
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

    private static final AvroJsonConverter AVRO_JSON_CONVERTER = AvroJsonConverter.create();

    private static OkHttpClient createHttpClient() {
        HttpLoggingInterceptor bodyInterceptor = new HttpLoggingInterceptor();
        bodyInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder().readTimeout(5, TimeUnit.MINUTES)
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
    }

    private static OkHttpClient createHttpClient(String apiKey) {
        HttpLoggingInterceptor bodyInterceptor = new HttpLoggingInterceptor();
        bodyInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder().readTimeout(5, TimeUnit.MINUTES)
                .addNetworkInterceptor(chain -> {
                    Request request = chain.request()
                            .newBuilder()
                            .addHeader(
                                    "Accept",
                                    "application/json")
                            .build();
                    return chain.proceed(request);
                })
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    HttpUrl originalHttpUrl = original.url();

                    HttpUrl url = originalHttpUrl.newBuilder()
                            .addQueryParameter("key", apiKey)
                            .build();

                    Request.Builder requestBuilder = original.newBuilder()
                            .url(url);

                    Request request = requestBuilder.build();

                    return chain.proceed(request);
                })
                .addInterceptor(bodyInterceptor)
                .build();
    }

    public static Ga4ghRetroService create(String baseUrl) {
        return new Retrofit.Builder().client(createHttpClient())
                .addConverterFactory(AVRO_JSON_CONVERTER)
                .baseUrl(baseUrl)
                .build()
                .create(Ga4ghRetroService.class);
    }

    public static Ga4ghRetroService create(String baseUrl, String apiKey) {
        return new Retrofit.Builder().client(createHttpClient(apiKey))
                .addConverterFactory(AVRO_JSON_CONVERTER)
                .baseUrl(baseUrl)
                .build()
                .create(Ga4ghRetroService.class);
    }

    private Ga4ghRetroServiceFactory() {
    }

}