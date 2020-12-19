package com.droideve.apps.nearbystores.network.api_request;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;


public class StoreRequest extends StringRequest {


    public StoreRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }
}
