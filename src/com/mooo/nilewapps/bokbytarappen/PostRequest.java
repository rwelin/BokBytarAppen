/**
 *  Copyright 2013 Robert Welin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mooo.nilewapps.bokbytarappen;

import java.security.KeyStore;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import com.mooo.nilewapps.androidnilewapp.HttpPostString;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;

/**
 * Class that performs an asynchronous HTTP POST request. Always sends
 * the AuthenticationToken stored on the device or a username/password pair
 * to the server. It also expects that an AuthenticationToken should be 
 * sent in the response. The previously stored token is replaced by the
 * received token.
 * @author nilewapp
 *
 */
public class PostRequest {
    
    /**
     * Name of the JSON object containing the AuthenticationToken.
     */
    public static final String TOKEN = "sess";
    
    /**
     * Name of the JSON object containing the response message.
     */
    public static final String MESSAGE = "mess";
    
    private final Fragment fragment;
    private final String url;
    private final List<NameValuePair> requestEntity;

    private final String profile;
    private final String password;
    
    /**
     * Constructor. Sends the AuthenticationToken with the request.
     * @param fragment host fragment
     * @param url service url
     * @param requestEntity request body
     */
    public PostRequest(Fragment fragment, String url, List<NameValuePair> requestEntity) {
        this.fragment = fragment;
        this.url = url;
        this.requestEntity = requestEntity;
        this.profile = null;
        this.password = null;
    }
    
    /**
     * Constructor. Sends a username/password pair with the request.
     * @param fragment
     * @param url
     * @param profile
     * @param password
     * @param requestEntity
     */
    public PostRequest(Fragment fragment, String url, String profile, String password, List<NameValuePair> requestEntity) {
        this.fragment = fragment;
        this.url = url;
        this.requestEntity = requestEntity;
        this.profile = profile;
        this.password = password;
    }
    
    /**
     * Performs the POST request.
     * @return the request task
     */
    public PostRequestTask execute() {
        PostRequestTask task = new PostRequestTask(); 
        task.execute();
        return task;
    }

    class PostRequestTask extends AsyncTask<Void, Void, String> {

        private Exception e = null;

        public Exception getException() {
            return e;
        }

        @Override
        protected String doInBackground(Void...voids) {
            try {
                /* Load trust store */
                KeyStore trustStore = TrustManager.getKeyStore(fragment.getActivity());
                
                if (profile == null) {
                    /* Add AuthenticationToken to the request body */
                    requestEntity.addAll(TokenManager.getToken(fragment.getActivity()).getRequestEntity());
                }

                /* Perform HTTP request */
                String response;
                if (profile == null) {
                    response = HttpPostString.request(trustStore, url, requestEntity);
                } else {
                    response = HttpPostString.request(trustStore, url, profile, password, requestEntity);
                }

                JSONObject json = new JSONObject(response);

                /* Get and store the new AuthenticationToken */
                JSONObject session = json.getJSONObject(TOKEN);
                TokenManager.setToken(fragment.getActivity(), new AuthenticationToken(session));

                /* Return rest of response */
                return json.getString(MESSAGE);
            } catch (Exception e) {
                this.e = e;
                return null;
            }
        }
    }
}
