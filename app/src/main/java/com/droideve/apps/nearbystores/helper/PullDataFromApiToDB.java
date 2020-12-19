package com.droideve.apps.nearbystores.helper;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.controllers.banners.BannersController;
import com.droideve.apps.nearbystores.controllers.categories.CategoryController;
import com.droideve.apps.nearbystores.network.VolleySingleton;
import com.droideve.apps.nearbystores.network.api_request.SimpleRequest;
import com.droideve.apps.nearbystores.parser.api_parser.BannerParser;
import com.droideve.apps.nearbystores.parser.api_parser.CategoryParser;
import com.droideve.apps.nearbystores.parser.tags.Tags;

import org.json.JSONException;
import org.json.JSONObject;

import static com.droideve.apps.nearbystores.appconfig.AppConfig.APP_DEBUG;

public class PullDataFromApiToDB {


    public static void getCategories(Context context) {

        RequestQueue queue = VolleySingleton.getInstance(context).getRequestQueue();

        SimpleRequest request = new SimpleRequest(Request.Method.GET,
                Constances.API.API_USER_GET_CATEGORY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    if (APP_DEBUG) {
                        Log.e("catsResponse", response);
                    }

                    JSONObject jsonObject = new JSONObject(response);
                    // Log.e("response", jsonObject.toString());
                    final CategoryParser mCategoryParser = new CategoryParser(jsonObject);
                    int success = Integer.parseInt(mCategoryParser.getStringAttr(Tags.SUCCESS));

                    if (success == 1 && mCategoryParser.getCategories().size() > 0) {
                        CategoryController.removeAll();
                        CategoryController.insertCategories(
                                mCategoryParser.getCategories()
                        );
                    }

                } catch (JSONException e) {
                    //send a rapport to support
                    e.printStackTrace();

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (APP_DEBUG) {
                    Log.e("ERROR", error.toString());
                }
            }
        }) {

        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);


    }


    public static void getBanners(Context context) {

        RequestQueue queue = VolleySingleton.getInstance(context).getRequestQueue();

        SimpleRequest request = new SimpleRequest(Request.Method.GET,
                Constances.API.API_GET_SLIDERS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    if (APP_DEBUG) {
                        Log.e("bannersResponse", response);
                    }

                    JSONObject jsonObject = new JSONObject(response);
                    final BannerParser mBannerParser = new BannerParser(jsonObject);
                    int success = Integer.parseInt(mBannerParser.getStringAttr(Tags.SUCCESS));

                    if (success == 1 && mBannerParser.getBanners().size() > 0) {
                        BannersController.removeAll();
                        BannersController.insertBanners(
                                mBannerParser.getBanners()
                        );
                    }

                } catch (JSONException e) {
                    //send a rapport to support
                    e.printStackTrace();

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (APP_DEBUG) {
                    Log.e("ERROR", error.toString());
                }
            }
        }) {

        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);


    }
}
