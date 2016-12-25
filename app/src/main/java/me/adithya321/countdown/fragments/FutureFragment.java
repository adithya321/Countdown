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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import me.adithya321.countdown.R;
import me.adithya321.countdown.activities.RealmBaseActivity;
import me.adithya321.countdown.adapters.FutureEventRealmAdapter;
import me.adithya321.countdown.models.FutureEvent;

public class FutureFragment extends Fragment {

    public static FutureEventRealmAdapter futureEventRealmAdapter;
    public static RealmRecyclerView recyclerView;
    @BindView(R.id.realm_recycler_view)
    RealmRecyclerView realmRecyclerView;
    private Realm realm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_future, container, false);
        ButterKnife.bind(this, view);
        realm = Realm.getInstance(((RealmBaseActivity) getActivity()).getRealmConfig());
        recyclerView = realmRecyclerView;
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        ButterKnife.bind(this, view);

        RealmResults<FutureEvent> futureEventRealmResults = realm
                .where(FutureEvent.class)
                .equalTo("added", true)
                .findAllSorted("date", Sort.ASCENDING);

        futureEventRealmAdapter = new FutureEventRealmAdapter(getActivity(),
                futureEventRealmResults, true, true);
        recyclerView.setAdapter(futureEventRealmAdapter);
    }
}