package com.alorma.gitskarios.core.client;

import android.content.Context;
import android.support.annotation.Nullable;
import com.alorma.gitskarios.core.ApiClient;
import java.util.concurrent.TimeUnit;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Client;
import retrofit.converter.Converter;
import rx.Observable;
import rx.functions.Func2;

public abstract class BaseClient<K> implements RequestInterceptor, RestAdapter.Log {

  protected StoreCredentials storeCredentials;

  protected Context context;
  private ApiClient client;

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

  public Observable<K> observable() {
    return getApiObservable(getRestAdapter()).retry(new Func2<Integer, Throwable, Boolean>() {
      @Override
      public Boolean call(Integer integer, Throwable throwable) {
        if (throwable instanceof RetrofitError) {
          return ((RetrofitError) throwable).getResponse().getStatus() == 202 && integer < 3;
        }
        return integer < 3;
      }
    }).debounce(100, TimeUnit.MILLISECONDS);
  }

  protected abstract Observable<K> getApiObservable(RestAdapter restAdapter);

  protected Converter customConverter() {
    return null;
  }

  protected String getToken() {
    return storeCredentials.token();
  }

  public Context getContext() {
    return context;
  }

  public ApiClient getClient() {
    return client;
  }

  public void setStoreCredentials(StoreCredentials storeCredentials) {
    this.storeCredentials = storeCredentials;
  }
}
