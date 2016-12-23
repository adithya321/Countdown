/*
 * Countdown
 * Copyright (C) 2016  Adithya J
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package me.adithya321.countdown.adapters;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.adithya321.countdown.R;
import me.everything.providers.android.calendar.Event;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private Context context;
    private List<Event> eventList;

    public EventAdapter(Context context, List<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_event_list, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        final Event event = eventList.get(position);

        viewHolder.eventTitle.setText(event.title);
        LocalDate today = new LocalDate();
        LocalDate eventDate = new LocalDate(event.dTStart);
        int days = Days.daysBetween(today, eventDate).getDays();
        viewHolder.eventDaysLeft.setText(String.valueOf(days));
        if (days < 0)
            viewHolder.eventIcon.setImageDrawable(context.getResources()
                    .getDrawable(R.drawable.ic_notifications_off_white));

        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long eventID = event.id;
                Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.header_separator)
        TextView headerSeparator;
        @BindView(R.id.event_circle)
        ImageView eventCircle;
        @BindView(R.id.event_icon)
        ImageView eventIcon;
        @BindView(R.id.event_title)
        TextView eventTitle;
        @BindView(R.id.event_days_left)
        TextView eventDaysLeft;
        @BindView(R.id.event_card)
        CardView eventCard;

        private View view;

        public ViewHolder(final View view) {
            super(view);
            this.view = view;
            ButterKnife.bind(this, view);
        }
    }
}
