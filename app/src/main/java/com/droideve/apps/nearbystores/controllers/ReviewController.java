package com.droideve.apps.nearbystores.controllers;

import com.droideve.apps.nearbystores.classes.Guest;
import com.droideve.apps.nearbystores.classes.Review;
import com.droideve.apps.nearbystores.controllers.sessions.GuestController;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Droideve on 12/25/2017.
 */

public class ReviewController {


    public static boolean isRated(int store_id) {

        Guest guest = GuestController.getGuest();
        Realm mRealm = Realm.getDefaultInstance();

        try {

            RealmResults<Review> result = mRealm.where(Review.class)
                    .equalTo("store_id", store_id)
                    .equalTo("guest_id", guest.getId()).findAll();


            if (result.isLoaded() && result.isValid() && result.size() > 0)
                return true;

        } catch (Exception e) {

        }

        return false;
    }

}
