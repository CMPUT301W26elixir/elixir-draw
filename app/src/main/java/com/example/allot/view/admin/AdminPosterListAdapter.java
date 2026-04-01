package com.example.allot.view.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.allot.R;
import com.example.allot.model.event.Event;
import java.util.List;

/**
 * Adapter for displaying uploaded event posters in admin panel.
 */
public class AdminPosterListAdapter extends RecyclerView.Adapter<AdminPosterListAdapter.ViewHolder> {
    private final List<Event> posterEvents;
    private final OnDeleteClickListener onDeleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Event event, int position);
    }

    public AdminPosterListAdapter(List<Event> posterEvents, OnDeleteClickListener onDeleteClickListener) {
        this.posterEvents = posterEvents;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_poster, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = posterEvents.get(position);
        holder.bind(event, position, onDeleteClickListener);
    }

    @Override
    public int getItemCount() {
        return posterEvents.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView posterImageView;
        private final TextView eventTitleText;
        private final TextView eventOrganizerText;
        private final Button deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            posterImageView = itemView.findViewById(R.id.posterImageView);
            eventTitleText = itemView.findViewById(R.id.eventTitleText);
            eventOrganizerText = itemView.findViewById(R.id.eventOrganizerText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        void bind(Event event, int position, OnDeleteClickListener listener) {
            eventTitleText.setText(event.getTitle());
            eventOrganizerText.setText("Event ID: " + event.getEventId());

            Glide.with(itemView.getContext())
                    .load(event.getPosterUrl())
                    .placeholder(R.drawable.bg_event_image_one)
                    .error(R.drawable.bg_event_image_two)
                    .into(posterImageView);

            deleteButton.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onDeleteClick(event, position);
                }
            });
        }
    }
}
