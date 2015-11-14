package com.alorma.gitskarios.core.client;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Pair;
import com.alorma.gitskarios.core.ApiClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Client;
import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.converter.Converter;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public abstract class BaseListClient<K> implements Callback<K>, RequestInterceptor, RestAdapter.Log {

    protected StoreCredentials storeCredentials;

    protected Context context;
    private OnResultCallback<K> onResultCallback;
    private ApiClient client;

    public Uri last;
    public Uri next;
    public int lastPage;
    public int nextPage;

    private Observable<Pair<K, Integer>> apiObservable;
    private Action1<? super K> saveCache;

    public BaseListClient(Context context, ApiClient client) {
        this.client = client;
        if (context != null) {
            this.context = context.getApplicationContext();
        }
        storeCredentials = new StoreCredentials(context);
    }

    protected RestAdapter getRestAdapter() {
        RestAdapter.Builder restAdapterBuilder =
            new RestAdapter.Builder().setEndpoint(client.getApiEndpoint())
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

    public Observable<? extends Pair<K, Integer>> observable() {
        return getApiObservable().subscribeOn(Schedulers.io());
    }

    public Observable<? extends Pair<K, Integer>> getApiObservable() {
        return Observable.create(new Observable.OnSubscribe<Pair<K, Integer>>() {

            @Override
            public void call(Subscriber<? super Pair<K, Integer>> subscriber) {
                setOnResultCallback(new Sbbscrib(subscriber));
                execute();
            }
        });
    }

    public class Sbbscrib implements OnResultCallback<K> {

        Subscriber<? super Pair<K, Integer>> subscriber;

        public Sbbscrib(Subscriber<? super Pair<K, Integer>> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void onResponseOk(K k, Response r) {
            subscriber.onNext(new Pair<K, Integer>(k, getLinkData(r)));
            subscriber.onCompleted();
        }

        @Override
        public void onFail(RetrofitError error) {
            subscriber.onError(error);
        }

        private int getLinkData(Response r) {
            if (r != null) {
                List<Header> headers = r.getHeaders();
                Map<String, String> headersMap = new HashMap<String, String>(headers.size());
                for (Header header : headers) {
                    headersMap.put(header.getName(), header.getValue());
                }

                String link = headersMap.get("Link");

                if (link != null) {
                    String[] parts = link.split(",");
                    try {
                        return new PaginationLink(parts[0]).page;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return 0;
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
