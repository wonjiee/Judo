package kr.pknu.eomjiwon202112084;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.ViewHolder> {

    private ArrayList<Stock> originalList = new ArrayList<>();
    private ArrayList<Stock> displayedList = new ArrayList<>();
    private Context context;
    private String currentQuery = "";
    private String currentCategory = "ALL";
    private int currentSort = 0;

    public StockAdapter(Context context) {
        this.context = context;
    }

    // ViewModel → submitList로 갱신
    public void submitList(ArrayList<Stock> newList) {
        originalList.clear();
        originalList.addAll(newList);

        displayedList.clear();
        displayedList.addAll(newList);

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_stock_row, parent, false);
        return new ViewHolder(v);
    }

    private String formatUpdatedAt(String isoTime) {
        try {
            Instant instant = Instant.parse(isoTime);
            ZonedDateTime kst = instant.atZone(ZoneId.of("Asia/Seoul"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return "Updated " + formatter.format(kst);
        } catch (Exception e) {
            return "";
        }
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Stock item = displayedList.get(position);

        holder.textSymbol.setText(item.symbol);
        holder.textName.setText(item.name);
        holder.textPrice.setText(item.price);
        holder.textChangePercent.setText(item.changePercent);
        holder.textMarketCap.setText(item.marketCap);
        holder.textCategory.setText(getCategoryText(item));
        holder.textUpdatedAt.setText(formatUpdatedAt(item.updateAt));
    }

    @Override
    public int getItemCount() {
        return displayedList.size();
    }

    public void filterQuery(String query){
        query = query.toLowerCase();
        displayedList.clear();

        if(query.isEmpty()){
            displayedList.addAll(originalList);
        }
        else{
            for(Stock stock : originalList){
                if(stock.getName().toLowerCase().contains(query)||
                        stock.getSymbol().toLowerCase().contains(query)){
                    displayedList.add(stock);
                }
            }
        }
        notifyDataSetChanged();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textSymbol, textName, textPrice, textChangePercent, textMarketCap, textCategory,textUpdatedAt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textSymbol = itemView.findViewById(R.id.textSymbol);
            textName = itemView.findViewById(R.id.textName);
            textPrice = itemView.findViewById(R.id.textPrice);
            textChangePercent = itemView.findViewById(R.id.textChangePercent);
            textMarketCap = itemView.findViewById(R.id.textMarketCap);
            textCategory = itemView.findViewById(R.id.textCategory);
            textUpdatedAt = itemView.findViewById(R.id.textUpdatedAt);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();

                    if(position != RecyclerView.NO_POSITION){
                        Stock clickedStock = displayedList.get(position);
                        Log.d("StockAdapter","Clicked "+clickedStock.symbol);

                        Intent intent = new Intent(context, StockDetailActivity.class);

                        intent.putExtra("STOCK_SYMBOL",clickedStock.symbol);
                        intent.putExtra("STOCK_NAME",clickedStock.name);
                        intent.putExtra("STOCK_PRICE",clickedStock.price);
                        intent.putExtra("STOCK_CHANGEPERCENT",clickedStock.changePercent);
                        intent.putExtra("STOCK_MARKET_CAP",clickedStock.marketCap);
                        intent.putExtra("STOCK_CATEGORIES",getCategoryText(clickedStock));
                        intent.putExtra("STOCK_UPDATEDAT",formatUpdatedAt(clickedStock.updateAt));

                        // 전환 딜레이
                        itemView.postDelayed(() -> {
                            context.startActivity(intent);
                        }, 60);
                    }
                }
            });
        }
    }

    private String getCategoryText(Stock s) {
        ArrayList<String> list = new ArrayList<>();

        if (s.isValue) list.add("가치주");
        if (s.isGrowth) list.add("성장주");
        if (s.isDividend) list.add("배당주");
        if (s.isBlueChip) list.add("우량주");
        if (s.isFavorite) list.add("관심");

        if (list.isEmpty()) return "";
        return String.join(", ", list);
    }

    public void setSearchQuery(String q) {
        currentQuery = q.toLowerCase();
        applyAllFilters();
    }

    public void setCategory(String cat) {
        currentCategory = cat;
        applyAllFilters();
    }

    public void setSortOption(int option) {
        currentSort = option;
        applyAllFilters();
    }




    public void applyAllFilters(){
        ArrayList<Stock> filtered = new ArrayList<>();
        for(Stock s : originalList) {
            if (!currentCategory.equals("ALL")) {
                if (currentCategory.equals("FAVORITE") && !s.isFavorite) continue;
                if (currentCategory.equals("VALUE") && !s.isValue) continue;
                if (currentCategory.equals("GROWTH") && !s.isGrowth) continue;
                if (currentCategory.equals("BLUE") && !s.isBlueChip) continue;
                if (currentCategory.equals("DIVIDEND") && !s.isDividend) continue;
            }

            if (!currentQuery.isEmpty()) {
                if (!s.getName().toLowerCase().contains(currentQuery) &&
                        !s.getSymbol().toLowerCase().contains(currentQuery))
                    continue;
            }

            filtered.add(s);
        }
        sortList(filtered);

        displayedList.clear();
        displayedList.addAll(filtered);

        notifyDataSetChanged();
    }

    private void sortList(ArrayList<Stock> list){
        switch (currentSort){
            case 0:
                list.sort((a,b)->Long.compare(b.getMarketCapAsLong()==null?0:b.getMarketCapAsLong(),
                        a.getMarketCapAsLong()==null?0:a.getMarketCapAsLong()));
                break;
            case 1:
                list.sort(Comparator.comparingLong(s -> s.getMarketCapAsLong() == null ? 0 : s.getMarketCapAsLong()));
                break;
            case 2:
                list.sort((a,b) -> Double.compare(Double.parseDouble(b.price.replace(",","")),
                        Double.parseDouble(a.price.replace(",",""))));
                break;
            case 3:
                list.sort(Comparator.comparingDouble(s->Double.parseDouble(s.price.replace(",",""))));
                break;
        }
    }

}
