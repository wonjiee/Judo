package kr.pknu.eomjiwon202112084;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewAnimator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StockDetailActivity extends AppCompatActivity {

    NewsAdapter newsAdapter;
    ArrayList<News> newsList;

    RecyclerView recyclerNews;

    RelativeLayout loadingOverlay;
    TextView textSymbol, textName, textPrice, textChangePercent, textMarketCap, textCategories,textUpdatedAt;
    TextView textPbr,textPer,textRoe,textDividend,textRevenueGrowth;
    ImageView btnBack,btnFav;
    String symbol;
    StockViewModel vm;

    Button btnOpenAiAnalysis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        setContentView(R.layout.activity_stockdetail);
        View root = findViewById(R.id.root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        loadingOverlay = findViewById(R.id.loadingOverlay);
        loadingOverlay.setVisibility(View.VISIBLE);

        textSymbol = findViewById(R.id.textSymbol);
        textName = findViewById(R.id.textName);
        textPrice = findViewById(R.id.textPrice);
        textChangePercent = findViewById(R.id.textChangePercent);
        textMarketCap = findViewById(R.id.textMarketCap);
        textCategories = findViewById(R.id.textCategories);
        textUpdatedAt = findViewById(R.id.textUpdatedAt);
        textPbr = findViewById(R.id.textPbr);
        textPer = findViewById(R.id.textPer);
        textRoe = findViewById(R.id.textRoe);
        textDividend = findViewById(R.id.textDividend);
        textRevenueGrowth = findViewById(R.id.textRevenueGrowth);




        newsList = new ArrayList<>();
        newsAdapter = new NewsAdapter(newsList, this);
        recyclerNews = findViewById(R.id.recyclerNews);
        recyclerNews.setLayoutManager(new LinearLayoutManager(this));
        recyclerNews.setAdapter(newsAdapter);
        btnOpenAiAnalysis = findViewById(R.id.btnOpenAiAnalysis);


        Intent intent = getIntent();

        if (intent != null) {
            symbol = intent.getStringExtra("STOCK_SYMBOL");
            String name = intent.getStringExtra("STOCK_NAME");
            String price = intent.getStringExtra("STOCK_PRICE");
            String changePercent = intent.getStringExtra("STOCK_CHANGEPERCENT");
            String marketCap = intent.getStringExtra("STOCK_MARKET_CAP");
            String categories = intent.getStringExtra("STOCK_CATEGORIES");
            String updatedAt = intent.getStringExtra("STOCK_UPDATEDAT");
            double pbr = intent.getDoubleExtra("STOCK_PBR",0.0);
            double per = intent.getDoubleExtra("STOCK_PER",0.0);
            double roe = intent.getDoubleExtra("STOCK_ROE",0.0);
            double dividend = intent.getDoubleExtra("STOCK_DIVIDEND",0.0);
            double revenueGrowth = intent.getDoubleExtra("STOCK_REVENUEGROWTH",0.0);

            textSymbol.setText(symbol);
            textName.setText(name);
            textPrice.setText(price);
            textChangePercent.setText(changePercent);
            textMarketCap.setText(marketCap);
            textCategories.setText(categories);
            textUpdatedAt.setText(updatedAt);
            textPbr.setText(String.format("%.2f", pbr));
            textPer.setText(String.format("%.2f", per));
            textRoe.setText(String.format("%.2f", roe));
            textDividend.setText(String.format("%.2f", dividend));
            textRevenueGrowth.setText(String.format("%.2f", revenueGrowth));

            fetchNews(name);
        }

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());


        StockViewModel vm =
                new ViewModelProvider(MainActivity.viewModelOwner)
                        .get(StockViewModel.class);

        btnFav = findViewById(R.id.btnFavorite);
        boolean isFav = FavoriteManager.isFavorite(this, textSymbol.getText().toString());
        if(isFav){
            btnFav.setImageResource(R.drawable.ic_fav_on);
        }else{
            btnFav.setImageResource(R.drawable.ic_fav_off);
        }

        btnFav.setOnClickListener(v -> {
            boolean now = FavoriteManager.isFavorite(this, symbol);

            if (now) {
                FavoriteManager.removeFavorite(this, symbol);
                vm.updateFavorite(symbol,false);
                btnFav.setImageResource(R.drawable.ic_fav_off);
            } else {
                FavoriteManager.addFavorite(this, symbol);
                vm.updateFavorite(symbol,true);
                btnFav.setImageResource(R.drawable.ic_fav_on);
            }
            vm.updateFavorite(symbol, !now);
            textCategories.setText(getUpdatedCategoryText(symbol));
        });

        btnOpenAiAnalysis.setOnClickListener(v->{
            Intent i = new Intent(this, AiAnalysisActivity.class);
            i.putExtra("symbol", symbol);
            startActivity(i);
        });

        View cardBasic = findViewById(R.id.cardBasic);
        View cardDetail = findViewById(R.id.cardDetail);

        View btnShowBasic = findViewById(R.id.textShowBasic);
        View btnShowDetail = findViewById(R.id.textShowDetail);

        btnShowDetail.setOnClickListener(v -> {

            int width = cardBasic.getWidth();
            if (width == 0) width = cardBasic.getMeasuredWidth();

            // 세부카드를 화면 오른쪽에 미리 배치
            cardDetail.setVisibility(View.VISIBLE);
            cardDetail.setTranslationX(width);
            cardDetail.setAlpha(1f);

            // 기본카드는 왼쪽으로 사라짐
            cardBasic.animate()
                    .translationX(-width)
                    .setDuration(250)
                    .withEndAction(() -> {
                        cardBasic.setVisibility(View.GONE);
                        cardBasic.setTranslationX(0);  // 초기화
                    })
                    .start();

            // 세부카드는 오른쪽에서 들어옴
            cardDetail.animate()
                    .translationX(0)
                    .setDuration(250)
                    .start();
        });


        btnShowBasic.setOnClickListener(v -> {

            int width = cardDetail.getWidth();
            if (width == 0) width = cardDetail.getMeasuredWidth();

            // 기본카드를 화면 왼쪽에 미리 배치
            cardBasic.setVisibility(View.VISIBLE);
            cardBasic.setTranslationX(-width);
            cardBasic.setAlpha(1f);

            // 세부카드는 오른쪽으로 사라짐
            cardDetail.animate()
                    .translationX(width)
                    .setDuration(250)
                    .withEndAction(() -> {
                        cardDetail.setVisibility(View.GONE);
                        cardDetail.setTranslationX(0); // 초기화
                    })
                    .start();

            // 기본카드는 왼쪽에서 들어옴
            cardBasic.animate()
                    .translationX(0)
                    .setDuration(250)
                    .start();
        });

    }

    public void fetchNews(String symbol) {

        OkHttpClient client = new OkHttpClient();

        String keyword = symbol + " stock";
        String url = "https://news.google.com/rss/search?q=" + keyword + "&hl=en-US&gl=US&ceid=US:en";

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> loadingOverlay.setVisibility(View.INVISIBLE));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (!response.isSuccessful()) {
                    runOnUiThread(() -> loadingOverlay.setVisibility(View.INVISIBLE));
                    return;
                }

                String xml = response.body().string();

                parseNewsXML(xml);

                runOnUiThread(() -> {
                    newsAdapter.notifyDataSetChanged();
                    loadingOverlay.setVisibility(View.INVISIBLE);
                });
            }
        });
    }

    //  XML 파싱
    private void parseNewsXML(String xml) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);

            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xml));

            newsList.clear();

            int eventType = parser.getEventType();

            String title = "";
            String link = "";
            String guid = "";
            String pubDate = "";
            String source = "";
            boolean insideItem = false;
            String currentTag = "";

            while (eventType != XmlPullParser.END_DOCUMENT) {

                switch (eventType) {

                    case XmlPullParser.START_TAG:
                        currentTag = parser.getName();

                        if (currentTag.equalsIgnoreCase("item")) {
                            insideItem = true;
                            title = link = guid = pubDate = source = "";
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if (insideItem) {
                            String text = parser.getText().trim();
                            if (text.isEmpty()) break;


                            if (currentTag.equalsIgnoreCase("title"))
                                title += text;
                            else if (currentTag.equalsIgnoreCase("link"))
                                link += text;
                            else if (currentTag.equalsIgnoreCase("guid"))
                                guid += text;
                            else if (currentTag.equalsIgnoreCase("source"))
                                source += text;
                            else if (currentTag.equalsIgnoreCase("pubDate"))
                                pubDate += text;
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        String end = parser.getName();

                        if (end.equalsIgnoreCase("item")) {
                            insideItem = false;

                            if (link == null || link.isEmpty()) {
                                link = guid;
                            }

                            if (!isBlocked(source)) {
                                newsList.add(new News(title, link, source, pubDate));
                            }
                            title = link = guid = pubDate = source = "";
                        }

                        currentTag = "";
                        break;
                }

                eventType = parser.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //  블랙리스트 필터
    private boolean isBlocked(String source) {
        if (source == null) return false;

        // normalize
        String normalized = source
                .replace("\u00A0", "")   // NBSP
                .replace("\u2026", "")   // ellipsis
                .replace("|", "")
                .replace("…", "")        // 실제 ellipsis 문자
                .trim()
                .toLowerCase();

        String[] blockedPublishers = {"marketbeat", "newser.com"};

        for (String b : blockedPublishers) {
            if (normalized.contains(b)) return true;
        }
        return false;
    }

    private String getUpdatedCategoryText(String symbol) {

        // 현재 카드에 표시된 문자열 가져오기
        String current = textCategories.getText().toString().trim();

        boolean isFav = FavoriteManager.isFavorite(this, symbol);

        ArrayList<String> categoryList = new ArrayList<>();

        // 기존 문자열을 분리해서 리스트로 만듦
        if (!current.isEmpty()) {
            for (String s : current.split(",")) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty() && !trimmed.equals("관심")) {
                    categoryList.add(trimmed);
                }
            }
        }

        // 관심 등록 시 추가
        if (isFav) {
            categoryList.add("관심");
        }

        // 리스트가 비어 있으면 빈 문자열 반환
        if (categoryList.isEmpty()) {
            return "";
        }

        // 다시 ", " 로 합쳐서 문자열로 만듦
        return String.join(", ", categoryList);
    }




}
