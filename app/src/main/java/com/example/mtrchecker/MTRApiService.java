package com.example.mtrchecker;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MTRApiService {
    private static final String BASE_URL = "https://rt.data.gov.hk/v1/transport/mtr/getSchedule.php";
    private RequestQueue queue;

    public MTRApiService(Context context) {
        queue = Volley.newRequestQueue(context);
    }

    public void getTrainSchedule(String line, String station, final TrainScheduleCallback callback) {
        String url = BASE_URL + "?line=" + line + "&sta=" + station + "&lang=tc";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("MTRApiService", "Full API Response: " + response.toString());

                        try {
                            if (!response.has("data")) {
                                callback.onError("API Response Error: No data available");
                                return;
                            }

                            JSONObject data = response.getJSONObject("data");
                            String key = line + "-" + station;

                            // 檢查是否存在 "TCL-HOK"
                            if (!data.has(key)) {
                                callback.onError("No schedule data for " + key);
                                return;
                            }

                            JSONObject trainData = data.getJSONObject(key);

                            // 有些車站可能沒有 "DOWN"，所以用 optJSONArray 避免崩潰
                            JSONArray upTrains = trainData.optJSONArray("UP");
                            JSONArray downTrains = trainData.optJSONArray("DOWN");

                            // 確保回傳非空 JSON 陣列
                            callback.onSuccess(upTrains != null ? upTrains : new JSONArray(),
                                    downTrains != null ? downTrains : new JSONArray());

                        } catch (JSONException e) {
                            callback.onError("Parsing Error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Network Error: " + error.getMessage());
                    }
                });

        queue.add(jsonObjectRequest);
    }

    public interface TrainScheduleCallback {
        void onSuccess(JSONArray upTrains, JSONArray downTrains);
        void onError(String error);
    }
}
