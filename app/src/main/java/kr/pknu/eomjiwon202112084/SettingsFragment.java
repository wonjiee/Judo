package kr.pknu.eomjiwon202112084;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsFragment extends Fragment {

    private Switch switchDarkMode, switchAutoRefresh;
    private Button btnExportFav, btnImportFav;
    private TextView txtVersion;

    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        // UI 연결
        switchDarkMode = v.findViewById(R.id.switchDarkMode);
        switchAutoRefresh = v.findViewById(R.id.switchAutoRefresh);
        btnExportFav = v.findViewById(R.id.btnExportFav);
        btnImportFav = v.findViewById(R.id.btnImportFav);
        txtVersion = v.findViewById(R.id.txtAppVersion);

        prefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);

        loadSwitchStates();
        setListeners();
        setVersionText();

        return v;
    }

    // 저장된 스위치 상태 불러오기
    private void loadSwitchStates() {
        boolean darkMode = prefs.getBoolean("darkMode", false);
        boolean autoRefresh = prefs.getBoolean("autoRefresh", true);

        switchDarkMode.setChecked(darkMode);
        switchAutoRefresh.setChecked(autoRefresh);

        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    // UI 리스너 설정
    private void setListeners() {

        // 다크모드
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("darkMode", isChecked).apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            // Activity 재생성 막기
            ((AppCompatActivity) requireActivity()).getDelegate().applyDayNight();
        });

        // 자동 갱신
        //switchAutoRefresh.setOnCheckedChangeListener((buttonView, isChecked) ->
        //        prefs.edit().putBoolean("autoRefresh", isChecked).apply()
        //);

        // 관심종목 내보내기
        //btnExportFav.setOnClickListener(v -> exportFavorites());

        // 관심종목 가져오기
        //btnImportFav.setOnClickListener(v -> importFavorites());
    }


     //앱 버전 자동 표시
    private void setVersionText() {
        try {
            String version = requireActivity().getPackageManager()
                    .getPackageInfo(requireActivity().getPackageName(), 0).versionName;

            txtVersion.setText("v" + version);

        } catch (Exception e) {
            txtVersion.setText("버전 정보를 불러올 수 없음");
        }
    }

    /*관심종목 리스트 JSON으로 내보내기

    private void exportFavorites() {
        try {
            ArrayList<String> favList = FavoriteManager.getFavorites(requireContext());

            File file = new File(requireActivity().getExternalFilesDir(null), "favorites.json");
            JsonWriter writer = new JsonWriter(new FileWriter(file));

            writer.beginArray();
            for (String symbol : favList) {
                writer.value(symbol);
            }
            writer.endArray();
            writer.close();

            Toast.makeText(requireContext(), "내보내기 완료: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(requireContext(), "내보내기 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    관심종목 JSON 파일 가져오기

    private void importFavorites() {
        try {
            File file = new File(requireActivity().getExternalFilesDir(null), "favorites.json");
            if (!file.exists()) {
                Toast.makeText(requireContext(), "favorites.json 파일이 없습니다", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonReader reader = new JsonReader(new FileReader(file));
            reader.beginArray();

            ArrayList<String> favList = new ArrayList<>();

            while (reader.hasNext()) {
                favList.add(reader.nextString());
            }

            reader.endArray();
            reader.close();

            FavoriteManager.saveFavorites(requireContext(), favList);

            Toast.makeText(requireContext(), "관심종목 가져오기 완료!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(requireContext(), "가져오기 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }*/
}
