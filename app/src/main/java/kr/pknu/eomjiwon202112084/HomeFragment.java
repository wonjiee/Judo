package kr.pknu.eomjiwon202112084;

import static android.app.PendingIntent.getActivity;

import static com.google.android.material.internal.ViewUtils.hideKeyboard;
import static java.security.AccessController.getContext;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    RecyclerView recyclerView;
    StockAdapter adapter;
    StockViewModel viewModel;

    EditText editTextSearch;
    Chip chipAll, chipfav, chipValue, chipGrowth, chipBlue, chipDividend;
    Spinner spinnerSort;
    RelativeLayout loadingOverlay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = v.findViewById(R.id.recyclerStocks);
        editTextSearch = v.findViewById(R.id.editSearch);

        chipAll = v.findViewById(R.id.chip_all);
        chipfav = v.findViewById(R.id.chip_fav);
        chipValue = v.findViewById(R.id.chip_value);
        chipGrowth = v.findViewById(R.id.chip_growth);
        chipBlue = v.findViewById(R.id.chip_bluechip);
        chipDividend = v.findViewById(R.id.chip_dividend);

        spinnerSort = v.findViewById(R.id.spinnerSort);
        loadingOverlay = v.findViewById(R.id.loadingOverlay);
        loadingOverlay.setVisibility(View.VISIBLE);

        // RecyclerView 세팅
        adapter = new StockAdapter(getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        viewModel = new ViewModelProvider(requireActivity()).get(StockViewModel.class);
        viewModel.getStocks().observe(getViewLifecycleOwner(), stocks -> {
            adapter.submitList(new ArrayList<>(stocks));

            spinnerSort.post(() -> {
                adapter.setSortOption(spinnerSort.getSelectedItemPosition());
                recyclerView.scrollToPosition(0);
            });

            loadingOverlay.setVisibility(View.GONE);
        });
        viewModel.init(requireContext());

        setupUI(v);

        // 검색
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.setSearchQuery(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //키보드 숨기기
        editTextSearch.setOnEditorActionListener((v1, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                hideKeyboard();

                return true;
            }
            return false;
        });



        // 칩 클릭
        chipAll.setOnClickListener(vw -> {
            adapter.setCategory("ALL");
            recyclerView.scrollToPosition(0);
        });
        chipfav.setOnClickListener(vw -> {
            adapter.setCategory("FAVORITE");
            recyclerView.scrollToPosition(0);
        });
        chipValue.setOnClickListener(vw -> {
            adapter.setCategory("VALUE");
            recyclerView.scrollToPosition(0);
        });
        chipGrowth.setOnClickListener(vw -> {
            adapter.setCategory("GROWTH");
            recyclerView.scrollToPosition(0);
        });
        chipBlue.setOnClickListener(vw -> {
            adapter.setCategory("BLUE");
            recyclerView.scrollToPosition(0);
        });
        chipDividend.setOnClickListener(vw -> {
            adapter.setCategory("DIVIDEND");
            recyclerView.scrollToPosition(0);
        });

        // 정렬 스피너
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{ "시총 높은순", "시총 낮은순","가격 높은순", "가격 낮은순"}
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);
        adapter.setSortOption(0);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                adapter.setSortOption(position);

                recyclerView.scrollToPosition(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return v;


    }
    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.applyAllFilters();
        }
    }

    private void hideKeyboard() {
        if (getActivity() == null || getView() == null) return;

        // InputMethodManager를 사용하여 키보드 관리
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

        editTextSearch.clearFocus();
    }

    private void setupUI(@NonNull View view){
        if(!(view instanceof EditText)){
            view.setOnTouchListener((v,event)->{
                hideKeyboard();

                return false;
            });
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView); // 재귀 호출
            }
        }
    }

}
