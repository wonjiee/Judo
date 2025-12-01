package kr.pknu.eomjiwon202112084;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//SharedPerferences 기반
public class FavoriteManager {

    private static final String PREF_NAME = "favorites";
    private static final String KEY_FAV = "favList";

    public static void addFavorite(Context context, String symbol) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(prefs.getStringSet(KEY_FAV, new HashSet<>()));
        set.add(symbol);
        prefs.edit().putStringSet(KEY_FAV, set).apply();
    }

    public static void removeFavorite(Context context, String symbol) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(prefs.getStringSet(KEY_FAV, new HashSet<>()));
        set.remove(symbol);
        prefs.edit().putStringSet(KEY_FAV, set).apply();
    }

    public static boolean isFavorite(Context context, String symbol) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> set = prefs.getStringSet(KEY_FAV, new HashSet<>());
        return set.contains(symbol);
    }

    public static Set<String> getFavorites(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getStringSet(KEY_FAV, new HashSet<>());
    }

    public static void saveFavorites(Context context, Set<String> list){
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putStringSet(KEY_FAV, list).apply();
    }

    public static void clearAll(Context context){
        saveFavorites(context, new HashSet<>());
        MemoManager.clearAllMemos(context);
    }



    public static List<FavoriteData> getFavoritesWithMemos(Context context){
        Set<String> favs = getFavorites(context);
        List<FavoriteData> list = new ArrayList<>();

        for(String symbol : favs){
            String memo = MemoManager.getMemo(context, symbol);
            list.add(new FavoriteData(symbol, memo));
        }
        return list;
    }

    public static void saveFavoritesWithMemos(Context context, List<FavoriteData> list){
        Set<String> favSet = new HashSet<>();
        for(FavoriteData d : list){
            favSet.add(d.symbol);
            MemoManager.saveMemo(context, d.symbol, d.memo);
        }
        saveFavorites(context, favSet);
    }



}
