package kr.pknu.eomjiwon202112084;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class MainActivity extends AppCompatActivity {
    public static ViewModelStoreOwner viewModelOwner;
    private StockViewModel vm;

    long lastUpdate = 0;
    final long INTERVAL = 600_000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);
        View root = findViewById(R.id.root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, 0);
            return insets;
        });

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);

        ViewCompat.setOnApplyWindowInsetsListener(nav, (v, insets) -> {
            Insets navInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars());

            v.setTranslationY(-navInset.bottom);// 하단 바 만큼 올림
            return insets;
        });

        vm = new ViewModelProvider(this).get(StockViewModel.class);
        startAutoRefreshLoop();


        viewModelOwner = this;

        nav.setOnItemSelectedListener(item -> {
            Fragment fragment = new HomeFragment();

            if (item.getItemId() == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_glossary) {
                fragment = new GlossaryFragment();
            } else if(item.getItemId() == R.id.nav_favorites){
                fragment = new FavoritesFragment();
            }
            else if(item.getItemId() == R.id.nav_settings){
                fragment = new SettingsFragment();
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainFragmentContainer, fragment)
                    .commit();

            return true;
        });

        if (savedInstanceState == null) {
            nav.setSelectedItemId(R.id.nav_home);
        }


    }

    //자동 갱신 로직
    private void startAutoRefreshLoop() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            boolean auto = getSharedPreferences("settings", MODE_PRIVATE)
                    .getBoolean("autoRefresh", true);

            long now = System.currentTimeMillis();

            if (auto && (now - lastUpdate > INTERVAL)) {
                vm.init(this);  // ▶ 단 1곳(MainActivity)에서만 API 호출
                lastUpdate = now;
                Log.d("AUTO_REFRESH", "🔄 자동 업데이트 실행됨 = " + new Date());
            }

            startAutoRefreshLoop(); // 반복 실행
        }, 30_000); // 30초에 한 번 체크 → API는 10분에 한 번만 실행
    }

}
