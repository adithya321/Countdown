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

package me.adithya321.countdown.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.IdRes;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

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
import me.adithya321.countdown.adapters.FutureEventRealmAdapter;
import me.adithya321.countdown.adapters.PastEventRealmAdapter;
import me.adithya321.countdown.fragments.FutureFragment;
import me.adithya321.countdown.fragments.PastFragment;
import me.adithya321.countdown.models.FutureEvent;
import me.adithya321.countdown.models.PastEvent;
import me.adithya321.countdown.utils.DateUtils;
import me.everything.providers.android.calendar.Calendar;
import me.everything.providers.android.calendar.CalendarProvider;
import me.everything.providers.android.calendar.Event;

public class MainActivity extends RealmBaseActivity {


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbar_shadow)
    View toolbarShadow;
    @BindView(R.id.header)
    FrameLayout header;
    @BindView(R.id.bottom_bar)
    BottomBar bottomBar;

    private Realm realm;
    private RealmRecyclerView realmRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        realm = Realm.getInstance(getRealmConfig());

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Countdown");

        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                        .beginTransaction();
                switch (tabId) {
                    case R.id.tab_future:
                        fragmentTransaction.replace(R.id.fragment, new FutureFragment());
                        break;
                    case R.id.tab_past:
                        fragmentTransaction.replace(R.id.fragment, new PastFragment());
                        break;
                }
                fragmentTransaction.commit();
            }
        });
    }

    private void showChooseEventDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        View dialogView = getLayoutInflater().inflate(R.layout.fragment_add, null);
        realmRecyclerView = (RealmRecyclerView) dialogView
                .findViewById(R.id.realm_recycler_view);

        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder.setTitle(R.string.choose_event_dialog_title)
                .setPositiveButton(R.string.choose_event_dialog_done,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();

        if (bottomBar.getCurrentTabId() == R.id.tab_future) {
            RealmResults<FutureEvent> futureEventRealmResults = realm
                    .where(FutureEvent.class)
                    .equalTo("added", false)
                    .findAllSorted("date", Sort.ASCENDING);
            if (futureEventRealmResults.size() == 0) new getFutureEventsTask().execute();
            else {
                FutureEventRealmAdapter futureEventRealmAdapter = new FutureEventRealmAdapter(this,
                        futureEventRealmResults, true, true);
                realmRecyclerView.setAdapter(futureEventRealmAdapter);
                new getFutureEventsTask().execute();
            }
        } else if (bottomBar.getCurrentTabId() == R.id.tab_past) {
            RealmResults<PastEvent> pastEventRealmResults = realm
                    .where(PastEvent.class)
                    .equalTo("added", false)
                    .findAllSorted("date", Sort.DESCENDING);
            if (pastEventRealmResults.size() == 0) new getPastEventsTask().execute();
            else {
                PastEventRealmAdapter pastEventRealmAdapter = new PastEventRealmAdapter(this,
                        pastEventRealmResults, true, true);
                realmRecyclerView.setAdapter(pastEventRealmAdapter);
                new getPastEventsTask().execute();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                new LibsBuilder()
                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                        .start(this);
                break;
            case R.id.action_add:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(R.string.add_countdown_dialog_title)
                        .setPositiveButton(R.string.add_countdown_dialog_existing,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        showChooseEventDialog();
                                    }
                                })
                        .setNeutralButton(R.string.add_countdown_dialog_new,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Intent.ACTION_INSERT)
                                                .setData(CalendarContract.Events.CONTENT_URI);
                                        startActivity(intent);
                                    }
                                }).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class getFutureEventsTask extends AsyncTask<Void, Void, List<Event>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(List<Event> eventList) {
            super.onPostExecute(eventList);
            Collections.sort(eventList, new Comparator<Event>() {
                @Override
                public int compare(Event event1, Event event2) {
                    int days1 = DateUtils.getDaysLeft(event1.dTStart);
                    int days2 = DateUtils.getDaysLeft(event2.dTStart);
                    return days1 - days2;
                }
            });

            for (final Event e : eventList) {
                try {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            FutureEvent futureEvent = realm.createObject(
                                    FutureEvent.class, e.id);
                            futureEvent.setTitle(e.title);
                            futureEvent.setDate(e.dTStart);
                            futureEvent.setAdded(false);
                        }
                    });
                } catch (Exception exception) {
                    Log.e("AddRealmEvent", exception.toString());
                }
            }

            RealmResults<FutureEvent> futureEventRealmResults = realm
                    .where(FutureEvent.class)
                    .equalTo("added", false)
                    .findAllSorted("date", Sort.ASCENDING);
            FutureEventRealmAdapter futureEventRealmAdapter = new FutureEventRealmAdapter(MainActivity.this,
                    futureEventRealmResults, true, true);
            realmRecyclerView.setAdapter(futureEventRealmAdapter);
        }

        @Override
        protected List<Event> doInBackground(Void... params) {
            CalendarProvider calendarProvider = new CalendarProvider(MainActivity.this);
            List<Calendar> calendarList = calendarProvider.getCalendars().getList();
            List<Event> eventList = new ArrayList<>();
            for (Calendar c : calendarList) {
                List<Event> events = calendarProvider.getEvents(c.id).getList();
                for (Event e : events) {
                    if (DateUtils.getDaysLeft(e.dTStart) >= 0)
                        eventList.add(e);
                }
            }
            return eventList;
        }
    }

    private class getPastEventsTask extends AsyncTask<Void, Void, List<Event>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
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
                            pastEvent.setAdded(false);
                        }
                    });
                } catch (Exception exception) {
                    Log.e("AddRealmEvent", exception.toString());
                }
            }

            RealmResults<PastEvent> pastEventRealmResults = realm
                    .where(PastEvent.class)
                    .equalTo("added", false)
                    .findAllSorted("date", Sort.DESCENDING);
            PastEventRealmAdapter pastEventRealmAdapter = new PastEventRealmAdapter(MainActivity.this,
                    pastEventRealmResults, true, true);
            realmRecyclerView.setAdapter(pastEventRealmAdapter);
        }

        @Override
        protected List<Event> doInBackground(Void... params) {
            CalendarProvider calendarProvider = new CalendarProvider(MainActivity.this);
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
