package kr.pknu.eomjiwon202112084;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    private ArrayList<Stock> favorites = new ArrayList<>();
    private Context context;

    public FavoriteAdapter(ArrayList<Stock> list, Context context) {
        this.context = context;
        this.favorites = list;
    }

    public void updateList(ArrayList<Stock> newList) {
        favorites.clear();
        favorites.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavoriteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_favorite_row, parent, false);
        return new FavoriteAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteAdapter.ViewHolder holder, int position) {
        Stock item = favorites.get(position);

        holder.textSymbol.setText(item.symbol);
        holder.textName.setText(item.name);
        holder.textPrice.setText(item.price);
        holder.textChangePercent.setText(item.changePercent);
        holder.textMarketCap.setText(item.marketCap);
        holder.textCategory.setText(getCategoryText(item));
        holder.textUpdatedAt.setText(formatUpdatedAt(item.updateAt));

        // RecyclerView 재활용 대비 — 항상 접힌 기본 상태
        holder.cardMemoPreview.setVisibility(View.VISIBLE);
        holder.cardMemoFull.setVisibility(View.GONE);
        holder.isExpanded = false;

        String memo = MemoManager.getMemo(context, item.symbol);

        // 프리뷰 = 첫 줄만
        if (memo.isEmpty()) {
            holder.memoPreviewText.setText("메모를 작성하려면 길게 눌러주세요");
            holder.memoFullText.setText("");
        } else {
            holder.memoPreviewText.setText(memo.split("\n")[0]);  // 첫 줄만
            holder.memoFullText.setText(memo);                    // 전체 메모
        }
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    // 시간 포맷
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



    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView textSymbol, textName, textPrice, textChangePercent, textMarketCap, textCategory, textUpdatedAt;

        // 메모 관련
        View cardStock;
        View cardMemoPreview;
        View cardMemoFull;
        ImageView btnExpand;

        TextView memoPreviewText;
        TextView memoFullText;

        boolean isExpanded = false;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textSymbol = itemView.findViewById(R.id.textSymbol);
            textName = itemView.findViewById(R.id.textName);
            textPrice = itemView.findViewById(R.id.textPrice);
            textChangePercent = itemView.findViewById(R.id.textChangePercent);
            textMarketCap = itemView.findViewById(R.id.textMarketCap);
            textCategory = itemView.findViewById(R.id.textCategory);
            textUpdatedAt = itemView.findViewById(R.id.textUpdatedAt);

            // 카드 영역
            cardStock = itemView.findViewById(R.id.cardStock);
            cardMemoPreview = itemView.findViewById(R.id.cardMemoPreview);
            cardMemoFull = itemView.findViewById(R.id.cardMemoFull);

            memoPreviewText = itemView.findViewById(R.id.textMemoPreview);
            memoFullText = itemView.findViewById(R.id.textMemoFull);

            btnExpand = itemView.findViewById(R.id.btnExpand);

            // 주식 카드 → 상세 이동
            cardStock.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    Stock clickedStock = favorites.get(pos);

                    Intent intent = new Intent(context, StockDetailActivity.class);

                    intent.putExtra("STOCK_SYMBOL", clickedStock.symbol);
                    intent.putExtra("STOCK_NAME", clickedStock.name);
                    intent.putExtra("STOCK_PRICE", clickedStock.price);
                    intent.putExtra("STOCK_CHANGEPERCENT", clickedStock.changePercent);
                    intent.putExtra("STOCK_MARKET_CAP", clickedStock.marketCap);
                    intent.putExtra("STOCK_CATEGORIES", getCategoryText(clickedStock));
                    intent.putExtra("STOCK_UPDATEDAT", formatUpdatedAt(clickedStock.updateAt));

                    context.startActivity(intent);
                }
            });

            cardMemoPreview.setOnClickListener(v -> toggleMemo());

            cardMemoFull.setOnClickListener(v -> toggleMemo());

            cardMemoPreview.setOnLongClickListener(v -> {
                showMemoDialog();
                return true;
            });

            cardMemoFull.setOnLongClickListener(v -> {
                showMemoDialog();
                return true;
            });
        }
        private void showMemoDialog() {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            Stock stock = favorites.get(pos);

            BottomSheetDialog dialog = new BottomSheetDialog(context);
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_memo, null);
            dialog.setContentView(view);

            EditText editMemo = view.findViewById(R.id.editMemo);
            TextView btnSave = view.findViewById(R.id.btnSave);

            // 현재 저장된 메모 불러오기
            String current = MemoManager.getMemo(context, stock.symbol);
            editMemo.setText(current);

            btnSave.setOnClickListener(v -> {
                String newMemo = editMemo.getText().toString();
                MemoManager.saveMemo(context, stock.symbol, newMemo);

                // 화면 즉시 반영
                if (newMemo.isEmpty()) {
                    memoPreviewText.setText("메모를 작성하려면 길게 눌러주세요");
                    memoFullText.setText("");
                } else {
                    memoPreviewText.setText(newMemo.split("\n")[0]);
                    memoFullText.setText(newMemo);
                }

                dialog.dismiss();
            });

            dialog.show();
        }


        private void toggleMemo() {
            isExpanded = !isExpanded;

            if (isExpanded) {
                cardMemoPreview.setVisibility(View.GONE);
                cardMemoFull.setVisibility(View.VISIBLE);
                btnExpand.setImageResource(R.drawable.ic_expand_up);
            } else {
                cardMemoPreview.setVisibility(View.VISIBLE);
                cardMemoFull.setVisibility(View.GONE);
                btnExpand.setImageResource(R.drawable.ic_expand_down);
            }
        }


    }
}
