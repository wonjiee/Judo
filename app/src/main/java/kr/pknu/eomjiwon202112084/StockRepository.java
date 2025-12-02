package kr.pknu.eomjiwon202112084;

import android.content.Context;
import android.transition.ChangeBounds;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// 초기 버전. 현재 사용 안함
public class StockRepository {

    private Context context;
    private final OkHttpClient client = new OkHttpClient(); // OkHttpClient 인스턴스 추가

    // 전역 Stock 목록 저장소
    public static List<Stock> stockList = new ArrayList<>();

    public StockRepository(Context context) {
        this.context = context;
    }


    // json파일 읽기
    public List<Stock> loadLocalStocks() {
        stockList.clear();

        try {
            InputStream is = context.getAssets().open("stocks.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, "UTF-8");
            JSONArray arr = new JSONArray(json);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);

                String symbol = o.getString("symbol");
                String name = o.getString("name");
                String price = o.getString("price");
                String changePercent = o.getString("changePercent");
                String marketCap = o.getString("marketCap");

                // indices 배열
                JSONArray idxArr = o.getJSONArray("indices");
                List<String> indices = new ArrayList<>();
                for (int j = 0; j < idxArr.length(); j++) {
                    indices.add(idxArr.getString(j));
                }

                Stock s = new Stock(symbol, name, price, changePercent, marketCap, indices);

                boolean isFav = FavoriteManager.isFavorite(context,symbol);
                s.setFavorite(isFav);

                stockList.add(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return stockList;
    }

    public static void updateFavorite(String symbol,boolean fav){
        for(Stock s: stockList){
            if(s.getSymbol().equals(symbol)){
                s.setFavorite(fav);
                break;
            }
        }
    }


}
