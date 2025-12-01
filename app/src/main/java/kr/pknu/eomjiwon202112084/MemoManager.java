package kr.pknu.eomjiwon202112084;

import android.content.Context;
import android.content.SharedPreferences;

public class MemoManager {

    private static final String PREF_NAME = "stock_memos";

    public static void saveMemo(Context context, String symbol, String memo) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(symbol, memo).apply();
    }

    public static String getMemo(Context context, String symbol) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(symbol, ""); // 없으면 빈 문자열
    }

    public static void clearAllMemos(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }


}
