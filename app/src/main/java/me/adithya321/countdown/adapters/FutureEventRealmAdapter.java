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
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;
import io.realm.Sort;
import me.adithya321.countdown.R;
import me.adithya321.countdown.activities.RealmBaseActivity;
import me.adithya321.countdown.fragments.FutureFragment;
import me.adithya321.countdown.models.FutureEvent;
import me.adithya321.countdown.utils.DateUtils;

public class FutureEventRealmAdapter extends RealmBasedRecyclerViewAdapter<FutureEvent,
        FutureEventRealmAdapter.ViewHolder> {

    private Realm realm;
    private View view;

    public FutureEventRealmAdapter(Context context, RealmResults<FutureEvent> realmResults,
                                   boolean automaticUpdate, boolean animateResults) {
        super(context, realmResults, automaticUpdate, animateResults);
        realm = Realm.getInstance(((RealmBaseActivity) context).getRealmConfig());
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

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long eventID = futureEvent.getId();
                if (futureEvent.isAdded()) {
                    Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);
                    Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
                    getContext().startActivity(intent);
                } else {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            futureEvent.setAdded(true);
                        }
                    });
                }
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

        public ViewHolder(final View v) {
            super(v);
            view = v;
            ButterKnife.bind(this, v);
        }
    }

    @Override
    public void onItemSwipedDismiss(int position) {
        final FutureEvent deletedFutureEvent = realmResults.get(position);
        final long id = deletedFutureEvent.getId();
        final String title = deletedFutureEvent.getTitle();
        final long date = deletedFutureEvent.getDate();
        final boolean added = deletedFutureEvent.isAdded();

        super.onItemSwipedDismiss(position);

        Snackbar.make(view, R.string.countdown_deleted, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        realm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                FutureEvent futureEvent = realm.createObject(
                                        FutureEvent.class, id);
                                futureEvent.setTitle(title);
                                futureEvent.setDate(date);
                                futureEvent.setAdded(added);
                            }
                        }, new Realm.Transaction.OnSuccess() {
                            @Override
                            public void onSuccess() {
                                resetAdapter();
                            }
                        });
                    }
                })
                .setActionTextColor(getContext().getResources().getColor(R.color.colorAccent))
                .setDuration(3000).show();

        if (position == getItemCount())
            resetAdapter();
    }

    private void resetAdapter() {
        RealmResults<FutureEvent> futureEventRealmResults = realm
                .where(FutureEvent.class)
                .equalTo("added", true)
                .findAllSorted("date", Sort.ASCENDING);
        FutureFragment.futureEventRealmAdapter = new FutureEventRealmAdapter(
                getContext(), futureEventRealmResults, true, true);
        FutureFragment.recyclerView.setAdapter(FutureFragment.futureEventRealmAdapter);
    }
}
