package kr.pknu.eomjiwon202112084;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface StockApi {
    @GET("stocks")
    Call<List<Stock>> getStockList();

    @GET("api/ai/{symbol}")
    Call<AiResult> getAiAnalysis(@Path("symbol") String symbol);
}
