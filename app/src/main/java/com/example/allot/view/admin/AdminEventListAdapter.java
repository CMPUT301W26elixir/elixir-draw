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
         * Called when the delete button is clicked for an event.
         *
         * @param event the event to delete
         * @param position the position in the list
         */
        void onDeleteClick(Event event, int position);
    }

    /**
     * Creates an AdminEventListAdapter with the given events and click listener.
     *
     * @param events the list of events to display
     * @param onDeleteClickListener the listener for delete button clicks
     */
    public AdminEventListAdapter(List<Event> events, OnDeleteClickListener onDeleteClickListener) {
        this.events = events;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    /**
     * Handles on Create View Holder.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Handles on Bind View Holder.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, position, onDeleteClickListener);
    }

    /**
     * Returns whether g.et Item Count
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
         * Documents view Holder.
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitleText = itemView.findViewById(R.id.eventTitleText);
            eventOrganizerText = itemView.findViewById(R.id.eventOrganizerText);
            eventDateText = itemView.findViewById(R.id.eventDateText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        /**
         * Binds an event to the view holder.
         *
         * @param event the event to bind
         * @param position the position in the list
         * @param listener the delete click listener
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
