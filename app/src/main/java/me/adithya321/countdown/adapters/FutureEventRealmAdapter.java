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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;
import me.adithya321.countdown.R;
import me.adithya321.countdown.models.FutureEvent;
import me.adithya321.countdown.utils.DateUtils;

public class FutureEventRealmAdapter extends RealmBasedRecyclerViewAdapter<FutureEvent,
        FutureEventRealmAdapter.ViewHolder> {

    public FutureEventRealmAdapter(Context context, RealmResults<FutureEvent> realmResults,
                                   boolean automaticUpdate, boolean animateResults) {
        super(context, realmResults, automaticUpdate, animateResults);
    }

    @Override
    public ViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_future_event_list, viewGroup, false));
    }

    @Override
    public void onBindRealmViewHolder(final ViewHolder viewHolder, int position) {
        final FutureEvent futureEvent = realmResults.get(position);

        viewHolder.eventTitle.setText(futureEvent.getTitle());
        int days = DateUtils.getDaysLeft(futureEvent.getDate());
        viewHolder.eventDaysLeft.setText(String.valueOf(days));

        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long eventID = futureEvent.getId();
                Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
                getContext().startActivity(intent);
            }
        });
    }

    public class ViewHolder extends RealmViewHolder {
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
