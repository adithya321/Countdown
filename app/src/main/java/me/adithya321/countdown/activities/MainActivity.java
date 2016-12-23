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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

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
import me.adithya321.countdown.adapters.ViewPageAdapter;
import me.adithya321.countdown.models.FutureEvent;
import me.adithya321.countdown.utils.DateUtils;
import me.everything.providers.android.calendar.Calendar;
import me.everything.providers.android.calendar.CalendarProvider;
import me.everything.providers.android.calendar.Event;

public class MainActivity extends RealmBaseActivity {

    @BindView(R.id.tabs)
    PagerSlidingTabStrip tabs;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tab_header)
    LinearLayout tabHeader;
    @BindView(R.id.viewpager)
    ViewPager viewpager;
    @BindView(R.id.toolbar_shadow)
    View toolbarShadow;
    @BindView(R.id.header)
    FrameLayout header;
    @BindView(R.id.fab_button)
    FloatingActionButton fabButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        realm = Realm.getInstance(getRealmConfig());

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(null);

        ViewPageAdapter viewPageAdapter = new ViewPageAdapter(getSupportFragmentManager());
        viewpager.setAdapter(viewPageAdapter);

        tabs.setViewPager(viewpager);
        int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                getResources().getDisplayMetrics());
        viewpager.setPageMargin(pageMargin);
    }

    public void onAddFabClick(View view) {
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
    }

    private Realm realm;
    private RealmRecyclerView realmRecyclerView;

    private void showChooseEventDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        View dialogView = getLayoutInflater().inflate(R.layout.fragment_future, null);
        realmRecyclerView = (RealmRecyclerView) dialogView
                .findViewById(R.id.realm_recycler_view);

        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder.setTitle(R.string.choose_event_dialog_title)
                .setPositiveButton(R.string.choose_event_dialog_done,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();

        RealmResults<FutureEvent> futureEventRealmResults = realm
                .where(FutureEvent.class)
                .equalTo("added", false)
                .findAllSorted("date", Sort.ASCENDING);
        if (futureEventRealmResults.size() == 0) new getEventsTask().execute();
        else {
            FutureEventRealmAdapter futureEventRealmAdapter = new FutureEventRealmAdapter(this,
                    futureEventRealmResults, true, true);
            realmRecyclerView.setAdapter(futureEventRealmAdapter);
            new getEventsTask().execute();
        }
    }

    private class getEventsTask extends AsyncTask<Void, Void, List<Event>> {

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
        }
        return super.onOptionsItemSelected(item);
    }
}
