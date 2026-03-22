package com.example.allot.view.shared;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.allot.R;
import java.util.ArrayList;
import java.util.List;
public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventViewHolder> {

    /**
     * Listener interface for handling event card clicks and heart icon clicks.
     */
    public interface OnEventClickListener {

        /**
         * Called when an event card is clicked.
         *
         * @param event the clicked event item
         */
        void onEventClick(EventListItem event);

        /**
         * Called when the heart icon for an event is clicked.
         *
         * @param event the event whose heart icon was clicked
         * @param position the position of the event in the adapter
         */
        void onHeartClick(EventListItem event, int position); // ADDED for the save feature
    }

    private final List<EventListItem> events;
    private final OnEventClickListener onEventClickListener;

    /**
     * Creates an adapter for displaying event list items.
     *
     * @param events the initial list of event items
     * @param onEventClickListener the listener used for click callbacks
     */
    public EventListAdapter(List<EventListItem> events, OnEventClickListener onEventClickListener) {
        this.events = new ArrayList<>(events);
        this.onEventClickListener = onEventClickListener;
    }

    /**
     * Replaces the current list of events and refreshes the displayed items.
     *
     * @param updatedEvents the new list of event items to display
     */
    public void updateEvents(List<EventListItem> updatedEvents) {
        events.clear();
        events.addAll(updatedEvents);
        notifyDataSetChanged();
    }

    /**
     * Creates a new view holder for an event card.
     *
     * @param parent the parent view group
     * @param viewType the view type of the new view
     * @return a new EventViewHolder
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_card, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds event data to a view holder at the given position.
     * Also updates the save-heart state and click listeners.
     *
     * @param holder the view holder to bind
     * @param position the position of the item in the list
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventListItem event = events.get(position);

        holder.titleText.setText(event.getTitle());
        holder.streetText.setText(event.street);
        holder.dateText.setText(event.date);
        holder.priceText.setText(event.getPrice());
        holder.daysLeftText.setText(event.daysLeft);

        // --- ADDED: Heart UI Toggle Logic ---
        if (event.isSaved) {
            holder.heartIcon.setImageResource(R.drawable.ic_heart_filled);
            holder.heartIcon.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.bottom_nav_selected));
        } else {
            // Uses your ic_heart_outline.xml
            holder.heartIcon.setImageResource(R.drawable.ic_heart_outline);
            holder.heartIcon.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.white));
        }

        holder.heartIcon.setOnClickListener(v -> {
            if (onEventClickListener != null) {
                event.isSaved = !event.isSaved; // Visually toggle immediately
                notifyItemChanged(position);
                onEventClickListener.onHeartClick(event, position);
            }
        });
        // -------------------------------------

        int imageBackground = (position % 2 == 0) ? R.drawable.bg_event_image_one : R.drawable.bg_event_image_two;
        holder.imageFrame.setBackgroundResource(imageBackground);
        holder.itemView.setOnClickListener(view -> {
            if (onEventClickListener != null) {
                onEventClickListener.onEventClick(event);
            }
        });
    }

    /**
     * Returns the number of event items in the adapter.
     *
     * @return the number of items in the list
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
        ImageView heartIcon;
        TextView titleText;
        TextView streetText;
        TextView dateText;
        TextView priceText;
        TextView daysLeftText;

        /**
         * Creates a view holder and binds its child views.
         *
         * @param itemView the root view for the event card
         */
        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            imageFrame = itemView.findViewById(R.id.imageFrame);
            heartIcon = itemView.findViewById(R.id.heartIcon);
            titleText = itemView.findViewById(R.id.titleText);
            streetText = itemView.findViewById(R.id.streetText);
            dateText = itemView.findViewById(R.id.dateText);
            priceText = itemView.findViewById(R.id.priceText);
            daysLeftText = itemView.findViewById(R.id.daysLeftText);
        }
    }
}








