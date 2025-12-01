package kr.pknu.eomjiwon202112084;

import android.content.Context;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import java.util.List;

public class StockBackupManager {

    private static final String FILE_NAME = "stocks_cache.json";

    public static void save(Context context, List<Stock> list) {
        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            FileWriter writer = new FileWriter(file);
            new Gson().toJson(list, writer);
            writer.close();
        } catch (Exception ignored) {}
    }

    public static List<Stock> load(Context context) {
        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            if (!file.exists()) return null;

            FileReader reader = new FileReader(file);
            Type type = new TypeToken<List<Stock>>(){}.getType();
            List<Stock> list = new Gson().fromJson(reader, type);
            reader.close();
            return list;
        } catch (Exception e) {
            return null;
        }
    }
}
