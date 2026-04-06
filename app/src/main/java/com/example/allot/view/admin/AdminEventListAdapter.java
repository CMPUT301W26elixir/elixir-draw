package com.example.allot.view.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.allot.R;
import com.example.allot.model.event.Event;
import com.example.allot.view.shared.EventDisplayFormatter;
import java.util.List;

/**
 * Adapter for displaying events in the admin panel with delete functionality.
 */
public class AdminEventListAdapter extends RecyclerView.Adapter<AdminEventListAdapter.ViewHolder> {
    private final List<Event> events;
    private final OnDeleteClickListener onDeleteClickListener;

    /**
     * Interface for handling delete button clicks.
     */
    public interface OnDeleteClickListener {
        /**
         * Handles the delete click callback.
         *
         * @param event the event
         * @param position the position
         */
        void onDeleteClick(Event event, int position);
    }

    /**
     * Creates a new AdminEventListAdapter instance.
     *
     * @param events the events
     * @param onDeleteClickListener the on delete click listener
     */
    public AdminEventListAdapter(List<Event> events, OnDeleteClickListener onDeleteClickListener) {
        this.events = events;
        this.onDeleteClickListener = onDeleteClickListener;
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Handles the bind view holder callback.
     *
     * @param holder the holder
     * @param position the position
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, position, onDeleteClickListener);
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
     * ViewHolder for admin event list items.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView eventTitleText;
        private final TextView eventOrganizerText;
        private final TextView eventDateText;
        private final Button deleteButton;

        /**
         * Creates a new ViewHolder instance.
         *
         * @param itemView the item view
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitleText = itemView.findViewById(R.id.eventTitleText);
            eventOrganizerText = itemView.findViewById(R.id.eventOrganizerText);
            eventDateText = itemView.findViewById(R.id.eventDateText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        /**
         * Performs bind.
         *
         * @param event the event
         * @param position the position
         * @param listener the listener
         */
        void bind(Event event, int position, OnDeleteClickListener listener) {
            eventTitleText.setText(event.getTitle());
            eventOrganizerText.setText("Organizer ID: " + event.getOrganizerId());

            // Format the event date
            String formattedDate = EventDisplayFormatter.date(event);
            eventDateText.setText("Date: " + formattedDate);

            deleteButton.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onDeleteClick(event, position);
                }
            });
        }
    }
}
