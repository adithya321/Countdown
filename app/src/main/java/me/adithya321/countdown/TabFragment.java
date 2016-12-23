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

package me.adithya321.countdown;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.adithya321.countdown.adapters.EventAdapter;
import me.everything.providers.android.calendar.Calendar;
import me.everything.providers.android.calendar.CalendarProvider;
import me.everything.providers.android.calendar.Event;

public class TabFragment extends Fragment {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.empty_icon)
    ImageView emptyIcon;
    @BindView(R.id.empty_text)
    TextView emptyText;
    @BindView(R.id.empty_view)
    LinearLayout emptyView;

    private String eventType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tabs, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        ButterKnife.bind(this, view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        eventType = getArguments().getString("TYPE");
        if (eventType.equals("PAST")) {
            emptyText.setText(R.string.no_past_events);
            emptyIcon.setImageResource(R.drawable.ic_notifications_off_fade);
        }

        new getEventsTask().execute(eventType);
    }

    private class getEventsTask extends AsyncTask<String, Void, List<Event>> {

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
                    LocalDate today = new LocalDate();
                    LocalDate eventDate1 = new LocalDate(event1.dTStart);
                    LocalDate eventDate2 = new LocalDate(event2.dTStart);
                    int days1 = Days.daysBetween(today, eventDate1).getDays();
                    int days2 = Days.daysBetween(today, eventDate2).getDays();

                    if (eventType.equals("FUTURE")) return days1 - days2;
                    else return days2 - days1;
                }
            });

            EventAdapter eventAdapter = new EventAdapter(getActivity(), eventList);
            recyclerView.setAdapter(eventAdapter);

            if (eventAdapter.getItemCount() == 0) {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected List<Event> doInBackground(String... type) {
            CalendarProvider calendarProvider = new CalendarProvider(getActivity());
            List<Calendar> calendarList = calendarProvider.getCalendars().getList();
            List<Event> eventList = new ArrayList<>();
            for (Calendar c : calendarList) {
                List<Event> events = calendarProvider.getEvents(c.id).getList();
                for (Event e : events) {
                    LocalDate today = new LocalDate();
                    LocalDate eventDate = new LocalDate(e.dTStart);
                    int days = Days.daysBetween(today, eventDate).getDays();
                    if ((type[0].equals("FUTURE") && days >= 0) || (type[0].equals("PAST") && days < 0))
                        eventList.add(e);
                }
            }
            return eventList;
        }
    }
}