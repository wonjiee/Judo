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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.List;

public class SettingsFragment extends Fragment {

    private Switch switchDarkMode, switchAutoRefresh;
    private Button btnExportFav, btnImportFav,btnResetFav;
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
        btnResetFav = v.findViewById(R.id.btnResetFav);

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
        switchAutoRefresh.setOnCheckedChangeListener((buttonView, isChecked) -> {

            // 상태 저장
            prefs.edit().putBoolean("autoRefresh", isChecked).apply();

            // 자동갱신을 켠 순간 즉시 새로고침 실행 (선택 사항)
            if (isChecked) {
                if (getActivity() instanceof MainActivity) {
                    // HomeFragment가 현재 보이는 중이라면 바로 갱신
                    Fragment f = requireActivity().getSupportFragmentManager().findFragmentById(R.id.mainFragmentContainer);
                    if (f instanceof HomeFragment) {
                        ((HomeFragment) f).refreshStockData(); // 네가 쓰는 로딩 함수 이름으로 변경
                    }
                }
            }
        });
        // 관심종목 내보내기
        btnExportFav.setOnClickListener(v -> exportFavorites());

        // 관심종목 가져오기
        btnImportFav.setOnClickListener(v -> importFavorites());

        // 관심종목 내보내기
        btnResetFav.setOnClickListener(v->{
            FavoriteManager.clearAll(requireContext());
            StockViewModel vm = new ViewModelProvider(requireActivity()).get(StockViewModel.class);
            vm.clearFavoriteStates();


            Toast.makeText(requireContext(),"관심종목이 모두 삭제되었습니다.",Toast.LENGTH_SHORT).show();
        });
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

    // JSON 내보내기
    private void exportFavorites(){
        try{
            List<FavoriteData> list = FavoriteManager.getFavoritesWithMemos(requireContext());

            File file = new File(requireActivity().getExternalFilesDir(null),"favorites.json");
            FileWriter writer = new FileWriter(file);

            new Gson().toJson(list, writer);
            writer.close();

            Toast.makeText(requireContext(),
                    "내보내기 완료 → "+file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();

        }catch(Exception e){
            Toast.makeText(requireContext(),"오류: "+e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    // JSON 가져오기
    private void importFavorites(){
        try{
            File file = new File(requireActivity().getExternalFilesDir(null),"favorites.json");
            if(!file.exists()){
                Toast.makeText(requireContext(),"백업 파일이 없습니다",Toast.LENGTH_SHORT).show();
                return;
            }

            FileReader reader = new FileReader(file);
            Type type = new TypeToken<List<FavoriteData>>(){}.getType();
            List<FavoriteData> list = new Gson().fromJson(reader, type);
            reader.close();

            FavoriteManager.saveFavoritesWithMemos(requireContext(), list);
            StockViewModel vm = new ViewModelProvider(requireActivity()).get(StockViewModel.class);
            vm.applyFavoriteBackup(list);

            Toast.makeText(requireContext(),"복원 완료!",Toast.LENGTH_SHORT).show();

        }catch(Exception e){
            Toast.makeText(requireContext(),"오류: "+e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }
}
