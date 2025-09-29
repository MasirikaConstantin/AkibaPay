package com.example.akibapay.api;


import android.content.Context;
import android.widget.Toast;


import com.example.akibapay.utils.NetworkUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://pos.brainstormtechs.com/";
    private static final int CACHE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final int TIMEOUT = 30; // 30 seconds

    private static Retrofit retrofit = null;
    private static Context appContext = null;

    // Initialiser avec le contexte application
    public static void initialize(Context context) {
        appContext = context.getApplicationContext();
    }

    public static ApiService getApiService() {
        if (retrofit == null) {
            if (appContext == null) {
                throw new IllegalStateException("RetrofitClient must be initialized with context first");
            }

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(createOkHttpClient())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }

    private static OkHttpClient createOkHttpClient() {
        // Cache
        Cache cache = new Cache(appContext.getCacheDir(), CACHE_SIZE);

        // Logging Interceptor
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Network Interceptor pour vérifier la connexion
        Interceptor networkInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();

                // Vérifier la connexion internet
                if (!NetworkUtils.isNetworkAvailable(appContext)) {
                    NetworkUtils.showNoInternetToast(appContext);

                    // Utiliser le cache si pas de connexion
                    CacheControl cacheControl = new CacheControl.Builder()
                            .onlyIfCached()
                            .maxStale(7, TimeUnit.DAYS)
                            .build();

                    request = request.newBuilder()
                            .cacheControl(cacheControl)
                            .build();
                }

                return chain.proceed(request);
            }
        };

        // Offline Interceptor pour forcer le cache quand offline
        Interceptor offlineInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();

                if (!NetworkUtils.isNetworkAvailable(appContext)) {
                    CacheControl cacheControl = new CacheControl.Builder()
                            .maxStale(7, TimeUnit.DAYS)
                            .build();

                    request = request.newBuilder()
                            .cacheControl(cacheControl)
                            .build();
                }

                return chain.proceed(request);
            }
        };

        return new OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor(logging)
                .addInterceptor(offlineInterceptor)
                .addNetworkInterceptor(networkInterceptor)
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .build();
    }

    // Méthode utilitaire pour vérifier la connexion avant de faire un appel API
    public static boolean checkConnection() {
        if (appContext == null) return false;

        boolean isConnected = NetworkUtils.isNetworkAvailable(appContext);
        if (!isConnected) {
            NetworkUtils.showNoInternetToast(appContext);
        }
        return isConnected;
    }

    // Méthode pour forcer le rechargement (utile pour les tests)
    public static void reset() {
        retrofit = null;
    }
}