package kr.pknu.eomjiwon202112084;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StockViewModel extends ViewModel {

    private MutableLiveData<List<Stock>> stocks = new MutableLiveData<>();
    private StockRepository repository;
    private Context appContext;

    private boolean isLoaded = false; // 중복 실행 방지

    public void init(Context context) {
        if (isLoaded) return;   // ViewModel은 Activity보다 오래 살아있음
        isLoaded = true;

        appContext = context.getApplicationContext();
        repository = new StockRepository(context);

        loadRemoteStocks();  // JSON 로딩 시작
    }

    public LiveData<List<Stock>> getStocks() {
        return stocks;
    }

    // JSON 로딩 (기본 데이터)

    private static final int MAX_RETRY = 3;   // 최대 재시도 횟수
    private int retryCount = 0;               // 현재 재시도 횟수

    private void loadRemoteStocks() {


        StockApi api = RetrofitClient.getClient().create(StockApi.class);

        api.getStockList().enqueue(new Callback<List<Stock>>() {
            @Override
            public void onResponse(Call<List<Stock>> call, Response<List<Stock>> response) {
                if (response.isSuccessful()) {
                    List<Stock> list = response.body();
                    // 각 주식 자동 분류
                    for (Stock s : list) {
                        s.classify();
                        boolean fav = FavoriteManager.isFavorite(appContext, s.getSymbol());
                        s.setFavorite(fav);
                    }
                    stocks.setValue(list);

                    retryCount = 0; // 성공했으므로 초기화
                    Log.d("StockViewModel", "Stock load success");


                } else {
                    retryOrFail("Server response error");
                }
            }

            @Override
            public void onFailure(Call<List<Stock>> call, Throwable t) {
                Log.e("StockViewModel", "Failed to load stocks", t);
            }
        });
    }

    public void updateFavorite(String symbol, boolean fav) {
        List<Stock>oldList = stocks.getValue();
        if(oldList == null) return;

        List<Stock> newList = new ArrayList<>(oldList);

        for (Stock s : newList) {
            if (s.getSymbol().equals(symbol)) {
                s.setFavorite(fav);
                break;
            }
        }

        stocks.setValue(newList); // LiveData 갱신 → UI 자동 업데이트


    }

    private void retryOrFail(String message) {
        if (retryCount < MAX_RETRY) {
            retryCount++;
            Log.e("StockViewModel", "Retry " + retryCount + "/" + MAX_RETRY + " - " + message);

            // 1초 뒤 재시도
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                loadRemoteStocks();
            }, 1000);

        } else {
            Log.e("StockViewModel", "Final fail: " + message);
        }
    }


}
