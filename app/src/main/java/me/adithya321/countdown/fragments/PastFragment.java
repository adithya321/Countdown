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

package me.adithya321.countdown.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import me.adithya321.countdown.R;
import me.adithya321.countdown.activities.RealmBaseActivity;
import me.adithya321.countdown.adapters.PastEventRealmAdapter;
import me.adithya321.countdown.models.PastEvent;
import me.adithya321.countdown.utils.DateUtils;
import me.everything.providers.android.calendar.Calendar;
import me.everything.providers.android.calendar.CalendarProvider;
import me.everything.providers.android.calendar.Event;

public class PastFragment extends Fragment {

    @BindView(R.id.realm_recycler_view)
    RealmRecyclerView realmRecyclerView;

    private Realm realm;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_past, container, false);
        ButterKnife.bind(this, view);
        realm = Realm.getInstance(((RealmBaseActivity) getActivity()).getRealmConfig());
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        ButterKnife.bind(this, view);

        RealmResults<PastEvent> pastEventRealmResults = realm
                .where(PastEvent.class)
                .findAllSorted("date", Sort.DESCENDING);
        if (pastEventRealmResults.size() == 0) new getEventsTask().execute();
        else {
            PastEventRealmAdapter pastEventRealmAdapter = new PastEventRealmAdapter(getActivity(),
                    pastEventRealmResults, true, true);
            RealmRecyclerView realmRecyclerView = (RealmRecyclerView) view
                    .findViewById(R.id.realm_recycler_view);
            realmRecyclerView.setAdapter(pastEventRealmAdapter);
            new getEventsTask().execute();
        }
    }

    private class getEventsTask extends AsyncTask<Void, Void, List<Event>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getActivity(), "Loading...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(List<Event> eventList) {
            super.onPostExecute(eventList);
            Collections.sort(eventList, new Comparator<Event>() {
                @Override
                public int compare(Event event1, Event event2) {
                    int days1 = DateUtils.getDaysLeft(event1.dTStart);
                    int days2 = DateUtils.getDaysLeft(event2.dTStart);
                    return days2 - days1;
                }
            });

            for (final Event e : eventList) {
                try {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            PastEvent pastEvent = realm.createObject(
                                    PastEvent.class, e.id);
                            pastEvent.setTitle(e.title);
                            pastEvent.setDate(e.dTStart);
                        }
                    });
                } catch (Exception exception) {
                    Log.e("AddRealmEvent", exception.toString());
                }
            }

            RealmResults<PastEvent> pastEventRealmResults = realm
                    .where(PastEvent.class)
                    .findAllSorted("date", Sort.DESCENDING);
            PastEventRealmAdapter pastEventRealmAdapter = new PastEventRealmAdapter(getActivity(),
                    pastEventRealmResults, true, true);
            RealmRecyclerView realmRecyclerView = (RealmRecyclerView) view
                    .findViewById(R.id.realm_recycler_view);
            realmRecyclerView.setAdapter(pastEventRealmAdapter);
        }

        @Override
        protected List<Event> doInBackground(Void... params) {
            CalendarProvider calendarProvider = new CalendarProvider(getActivity());
            List<Calendar> calendarList = calendarProvider.getCalendars().getList();
            List<Event> eventList = new ArrayList<>();
            for (Calendar c : calendarList) {
                List<Event> events = calendarProvider.getEvents(c.id).getList();
                for (Event e : events) {
                    if (DateUtils.getDaysLeft(e.dTStart) < 0)
                        eventList.add(e);
                }
            }
            return eventList;
        }
    }
}