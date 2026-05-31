package me.karthikeyang.handcricket;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private final JSONArray data;

    public LeaderboardAdapter(JSONArray data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JSONObject item = data.optJSONObject(position);
        String name = item != null ? item.optString("username", "Player") : "Player";
        int wins = item != null ? item.optInt("total_wins", 0) : 0;
        int losses = item != null ? item.optInt("total_losses", 0) : 0;
        int best = item != null ? item.optInt("highest_score", 0) : 0;
        double winRate = item != null ? item.optDouble("win_rate", 0) : 0;

        holder.rank.setText("#" + (position + 1));
        holder.name.setText(name);
        holder.stats.setText(wins + "W / " + losses + "L | Best: " + best + " | " + winRate + "%");
    }

    @Override
    public int getItemCount() {
        return data.length();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView rank;
        TextView name;
        TextView stats;

        ViewHolder(View itemView) {
            super(itemView);
            rank = itemView.findViewById(R.id.rank_text);
            name = itemView.findViewById(R.id.name_text);
            stats = itemView.findViewById(R.id.stats_text);
        }
    }
}
