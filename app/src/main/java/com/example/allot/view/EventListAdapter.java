package com.example.allot.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.allot.R;

import java.util.ArrayList;
import java.util.List;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventViewHolder> {

    private final List<EventListItem> events;

    public EventListAdapter(List<EventListItem> events) {
        this.events = new ArrayList<>(events);
    }

    public void updateEvents(List<EventListItem> updatedEvents) {
        events.clear();
        events.addAll(updatedEvents);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_card, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventListItem event = events.get(position);

        holder.titleText.setText(event.title);
        holder.streetText.setText(event.street);
        holder.dateText.setText(event.date);
        holder.priceText.setText(event.price);
        holder.daysLeftText.setText(event.daysLeft);

        int imageBackground = (position % 2 == 0) ? R.drawable.bg_event_image_one : R.drawable.bg_event_image_two;
        holder.imageFrame.setBackgroundResource(imageBackground);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        FrameLayout imageFrame;
        TextView titleText;
        TextView streetText;
        TextView dateText;
        TextView priceText;
        TextView daysLeftText;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            imageFrame = (FrameLayout) itemView.findViewById(R.id.imageFrame);
            titleText = (TextView) itemView.findViewById(R.id.titleText);
            streetText = (TextView) itemView.findViewById(R.id.streetText);
            dateText = (TextView) itemView.findViewById(R.id.dateText);
            priceText = (TextView) itemView.findViewById(R.id.priceText);
            daysLeftText = (TextView) itemView.findViewById(R.id.daysLeftText);
        }
    }
}
