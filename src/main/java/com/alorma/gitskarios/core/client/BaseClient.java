package com.alorma.gitskarios.core.client;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import android.util.Pair;
import com.alorma.gitskarios.core.ApiClient;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Client;
import retrofit.client.Response;
import retrofit.converter.Converter;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public abstract class BaseClient<K> implements Callback<K>, RequestInterceptor, RestAdapter.Log {

    protected StoreCredentials storeCredentials;

    protected Context context;
    private OnResultCallback<K> onResultCallback;
    private ApiClient client;

    public Uri last;
    public Uri next;
    public int lastPage;
    public int nextPage;

    public BaseClient(Context context, ApiClient client) {
        this.client = client;
        if (context != null) {
            this.context = context.getApplicationContext();
        }
        storeCredentials = new StoreCredentials(context);
    }

    protected RestAdapter getRestAdapter() {
        RestAdapter.Builder restAdapterBuilder = new RestAdapter.Builder().setEndpoint(client.getApiEndpoint())
            .setRequestInterceptor(this)
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setLog(this);

        if (customConverter() != null) {
            restAdapterBuilder.setConverter(customConverter());
        }

        if (getInterceptor() != null) {
            restAdapterBuilder.setClient(getInterceptor());
        }

        return restAdapterBuilder.build();
    }

    @Nullable
    protected Client getInterceptor() {
        return null;
    }

    public void execute() {
        if (getToken() != null) {
            executeService(getRestAdapter());
        }
    }

    public K executeSync() {
        if (getToken() != null) {
            return executeServiceSync(getRestAdapter());
        }
        return null;
    }

    public Observable<Pair<K, Response>> observable() {
        return Observable.create(new Observable.OnSubscribe<Pair<K, Response>>() {

            @Override
            public void call(Subscriber<? super Pair<K, Response>> subscriber) {
                setOnResultCallback(new Sbbscrib(subscriber));
                execute();
            }
        }).subscribeOn(Schedulers.io());
    }

    public class Sbbscrib implements OnResultCallback<K> {

        Subscriber<? super Pair<K, Response>> subscriber;

        public Sbbscrib(Subscriber<? super Pair<K, Response>> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void onResponseOk(K k, Response r) {
            subscriber.onNext(new Pair<>(k, r));
            subscriber.onCompleted();
        }

        @Override
        public void onFail(RetrofitError error) {
            subscriber.onError(error);
        }
    }

    protected Converter customConverter() {
        return null;
    }

    protected abstract void executeService(RestAdapter restAdapter);

    protected abstract K executeServiceSync(RestAdapter restAdapter);

    @Override
    public void success(final K k, final Response response) {
        sendResponse(k, response);
    }

    private void sendResponse(K k, Response response) {
        if (onResultCallback != null) {
            onResultCallback.onResponseOk(k, response);
        }
    }

    @Override
    public void failure(final RetrofitError error) {
        sendError(error);
    }

    private void sendError(RetrofitError error) {
        if (error.getResponse() != null && error.getResponse().getStatus() == 401) {
            if (context != null) {
                LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
                manager.sendBroadcast(new UnAuthIntent(storeCredentials.token()));
            }
        } else {
            if (onResultCallback != null) {
                onResultCallback.onFail(error);
            }
        }
    }

    public OnResultCallback<K> getOnResultCallback() {
        return onResultCallback;
    }

    public void setOnResultCallback(OnResultCallback<K> onResultCallback) {
        this.onResultCallback = onResultCallback;
    }

    protected String getToken() {
        return storeCredentials.token();
    }

    public Context getContext() {
        return context;
    }

    public interface OnResultCallback<K> {
        void onResponseOk(K k, Response r);

        void onFail(RetrofitError error);
    }

    public ApiClient getClient() {
        return client;
    }

    public void setStoreCredentials(StoreCredentials storeCredentials) {
        this.storeCredentials = storeCredentials;
    }
}
