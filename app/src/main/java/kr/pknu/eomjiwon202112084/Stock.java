package kr.pknu.eomjiwon202112084;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Stock {

    // Json 기본 데이터
    public String symbol;
    public String name;
    // 가격 관련
    public String price;

    @SerializedName("change_percent")
    public String changePercent;
    @SerializedName("market_cap")
    public String marketCap;
    public List<String> indices; // ["SP500", "NASDAQ100"]

    // 재무지표 (API로 받아옴)
    public double per;           // PER (Price/Earnings)
    public double pbr;           // PBR (Price/Book value)
    public double roe;       // ROE
    @SerializedName("dividend_yield")
    public double dividendYield; // 연 배당률

    @SerializedName("revenue_growth_5y")
    public double revenueGrowth5Y;  // 연간 매출 or EPS 성장률
    @SerializedName("updated_at")
    public String updateAt;

    // 자동 분류 결과
    public boolean isValue;
    public boolean isGrowth;
    public boolean isDividend;
    public boolean isBlueChip;
    public boolean isFavorite = false;
    public boolean isPinned = false;  // 고정 여부
    public String memo = "";          // 메모 저장





    // 생성자
    public Stock(String symbol,String name,String price,String changePercent,String marketCap, List<String> indices){
        this.symbol = symbol;
        this.name = name;
        this.price = price;
        this.changePercent = changePercent;
        this.marketCap = marketCap;
        this.indices = indices;
    }

    public String getSymbol(){
        return symbol;
    }
    public String getName(){
        return name;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        this.isFavorite = favorite;
    }

    // marketCap 문자열 → 숫자로 변환 함수
    //    "37.34B" → 37340000000L
    public Long getMarketCapAsLong() {
        if (marketCap == null) return null;
        try {
            if (marketCap.endsWith("T")) {
                return (long)(Double.parseDouble(marketCap.replace("T", "")) * 1_000_000_000_000L);
            }
            if (marketCap.endsWith("B")) {
                return (long)(Double.parseDouble(marketCap.replace("B", "")) * 1_000_000_000L);
            }
            if (marketCap.endsWith("M")) {
                return (long)(Double.parseDouble(marketCap.replace("M", "")) * 1_000_000L);
            }
            return Long.parseLong(marketCap);
        } catch (Exception e) {
            return null;
        }
    }


    // change 문자열 → 숫자 변환
    public Double getChangePercentAsDouble() {
        if (changePercent == null || changePercent.equals("-")) return null;
        try {
            return Double.parseDouble(changePercent.replace("%", ""));
        } catch (Exception e) {
            return null;
        }
    }

    public void classify() {

        Long mc = getMarketCapAsLong();

        // 가치주
        isValue =
                (per > 0 && per < 18) &&
                        (pbr > 0 && pbr < 3.5) &&
                        (roe > 5);



        // 성장주
        isGrowth =
                (revenueGrowth5Y > 15) &&
                        (per > 20);


        // 배당주
        isDividend = (dividendYield > 0.025);   // 2.5% 이상


        // 우량주 (수익성 + 안정성 + 성장성)

        isBlueChip =
                (mc != null && mc > 40_000_000_000L) &&  // 시총 400억 이상
                        (roe > 8);                           // 높은 수익성
    }

}
