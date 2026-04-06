package com.example.allot.view.shared;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.allot.R;
import java.util.ArrayList;
import java.util.List;
/**
 * Shows event list items in the shared RecyclerView rows.
 */
public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventViewHolder> {

    /**
     * Listener interface for handling event card clicks and heart icon clicks.
     */
    public interface OnEventClickListener {

        /**
         * Handles the event click callback.
         *
         * @param event the event
         */
        void onEventClick(EventListItem event);

        /**
         * Handles the heart click callback.
         *
         * @param event the event
         * @param position the position
         */
        void onHeartClick(EventListItem event, int position);
    }

    private final List<EventListItem> events;
    private final OnEventClickListener onEventClickListener;

    /**
     * Creates a new EventListAdapter instance.
     *
     * @param events the events
     * @param onEventClickListener the on event click listener
     */
    public EventListAdapter(List<EventListItem> events, OnEventClickListener onEventClickListener) {
        this.events = new ArrayList<>(events);
        this.onEventClickListener = onEventClickListener;
    }

    /**
     * Performs update events.
     *
     * @param updatedEvents the updated events
     */
    public void updateEvents(List<EventListItem> updatedEvents) {
        events.clear();
        events.addAll(updatedEvents);
        notifyDataSetChanged();
    }

    /**
     * Returns the result of on create view holder.
     *
     * @param parent the parent
     * @param viewType the view type
     * @return the result of this call
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_card, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Handles the bind view holder callback.
     *
     * @param holder the holder
     * @param position the position
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventListItem event = events.get(position);

        holder.titleText.setText(event.getTitle());
        holder.streetText.setText(event.street);
        holder.dateText.setText(event.date);
        holder.priceText.setText(event.getPrice());
        holder.daysLeftText.setText(event.daysLeft);

        // Show the right heart icon for this item
        if (event.isSaved) {
            holder.heartIcon.setImageResource(R.drawable.ic_heart_filled);
            holder.heartIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.favorite_active));
        } else {
            holder.heartIcon.setImageResource(R.drawable.ic_heart_outline);
            holder.heartIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
        }

        holder.heartIcon.setOnClickListener(v -> {
            if (onEventClickListener != null) {
                // Flip the icon right away
                event.isSaved = !event.isSaved;
                notifyItemChanged(position);
                onEventClickListener.onHeartClick(event, position);
            }
        });

        Glide.with(holder.itemView.getContext()).clear(holder.posterImage);
        holder.posterImage.setImageResource(R.drawable.no_image);

        if (!TextUtils.isEmpty(event.getPosterUrl())) {
            Glide.with(holder.itemView.getContext())
                    .load(event.getPosterUrl())
                    .centerCrop()
                    .placeholder(R.drawable.no_image)
                    .error(R.drawable.no_image)
                    .into(holder.posterImage);
        }

        holder.itemView.setOnClickListener(view -> {
            if (onEventClickListener != null) {
                onEventClickListener.onEventClick(event);
            }
        });
    }

    /**
     * Returns the item count.
     *
     * @return the item count
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * View holder for an event card displayed in the RecyclerView.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        FrameLayout imageFrame;
        ImageView posterImage;
        ImageView heartIcon;
        TextView titleText;
        TextView streetText;
        TextView dateText;
        TextView priceText;
        TextView daysLeftText;

        /**
         * Creates a new EventViewHolder instance.
         *
         * @param itemView the item view
         */
        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            imageFrame = itemView.findViewById(R.id.imageFrame);
            posterImage = itemView.findViewById(R.id.eventPosterImage);
            heartIcon = itemView.findViewById(R.id.heartIcon);
            titleText = itemView.findViewById(R.id.titleText);
            streetText = itemView.findViewById(R.id.streetText);
            dateText = itemView.findViewById(R.id.dateText);
            priceText = itemView.findViewById(R.id.priceText);
            daysLeftText = itemView.findViewById(R.id.daysLeftText);
        }
    }
}








