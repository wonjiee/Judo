package kr.pknu.eomjiwon202112084;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import kr.pknu.eomjiwon202112084.FavoriteAdapter;
import kr.pknu.eomjiwon202112084.Stock;
import kr.pknu.eomjiwon202112084.StockViewModel;

public class FavoritesFragment extends Fragment {

    private RecyclerView recyclerFavorites;
    private TextView textEmpty;
    private LinearLayout emptyStateLayout;
    private FavoriteAdapter adapter;
    private StockViewModel vm;

    public FavoritesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        recyclerFavorites = view.findViewById(R.id.recyclerFavorites);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);


        recyclerFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FavoriteAdapter(new ArrayList<>(), getContext());
        recyclerFavorites.setAdapter(adapter);


        // MainActivity와 동일한 ViewModel 가져오기
        vm = new ViewModelProvider(requireActivity())
                .get(StockViewModel.class);

        // 전체 스톡 리스트 변화 감지
        vm.getStocks().observe(getViewLifecycleOwner(), stocks -> {
            updateFavorites(stocks);
        });

        return view;
    }

    private void updateFavorites(List<Stock> allStocks) {

        ArrayList<Stock> favorites = new ArrayList<>();

        for (Stock s : allStocks) {
            if (s.isFavorite) {
                favorites.add(s);
            }
        }

        if (favorites.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerFavorites.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerFavorites.setVisibility(View.VISIBLE);
            adapter.updateList(favorites);
        }
    }




}
