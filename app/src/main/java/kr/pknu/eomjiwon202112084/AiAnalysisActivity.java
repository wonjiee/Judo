package kr.pknu.eomjiwon202112084;

import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class AiAnalysisActivity extends AppCompatActivity {

    TextView textAiSummary, textAiRating, textAiRatingReason, textAiDetails;
    RelativeLayout loadingOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        setContentView(R.layout.activity_ai_analysis);
        View root = findViewById(R.id.root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        String symbol = getIntent().getStringExtra("symbol");
        Log.d("AI_DEBUG", "받은 심볼 = " + symbol);

        textAiSummary = findViewById(R.id.textAiSummary);
        textAiRating = findViewById(R.id.textAiRating);
        textAiRatingReason = findViewById(R.id.textAiRatingReason);
        textAiDetails = findViewById(R.id.textAiDetails);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        fetchAiAnalysis(symbol);
    }

    private void fetchAiAnalysis(String symbol) {

        loadingOverlay.setVisibility(View.VISIBLE);

        StockApi apiService = RetrofitClient.getClient().create(StockApi.class);

        Call<AiResult> call = apiService.getAiAnalysis(symbol);

        call.enqueue(new retrofit2.Callback<AiResult>() {

            @Override
            public void onResponse(Call<AiResult> call, retrofit2.Response<AiResult> response) {
                loadingOverlay.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    // 성공, Retrofit과 Gson이 자동으로 AiResult 객체로 변환해 줌
                    AiResult result = response.body();

                    // UI 업데이트는 반드시 runOnUiThread 내에서 실행
                    runOnUiThread(() -> {
                        textAiSummary.setText(result.summary);
                        textAiRating.setText("판단: " + result.rating);
                        textAiRatingReason.setText(result.reason);

                        if (result.details != null && !result.details.isEmpty()) {
                            textAiDetails.setText("• " + String.join("\n• ", result.details));
                        } else {
                            textAiDetails.setText("세부 근거 없음");
                        }
                    });
                } else {
                    Log.e("AI_API", "API 응답 실패. 코드: " + response.code());
                    runOnUiThread(() -> {
                        textAiSummary.setText("AI 분석 실패 (서버 응답 오류)");
                    });
                }
            }

            @Override
            public void onFailure(Call<AiResult> call, Throwable t) {
                loadingOverlay.setVisibility(View.GONE);
                Log.e("AI_API", "API 호출 중 오류 발생: " + t.getMessage());
                runOnUiThread(() -> {
                    textAiSummary.setText("AI 분석 실패 (네트워크 연결 오류)");
                });
            }
        });
    }
}
