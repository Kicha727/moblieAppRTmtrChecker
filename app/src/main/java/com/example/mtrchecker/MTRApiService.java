package com.example.mtrchecker;
import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

public class MTRApiService {
    private static final String BASE_URL = "https://rt.data.gov.hk/v1/transport/mtr/getSchedule.php";
    private RequestQueue queue;

    public MTRApiService(Context context) {
        queue = Volley.newRequestQueue(context);
    }

    public void getTrainSchedule(String line, String station, final TrainScheduleCallback callback) {
        String url = BASE_URL + "?line=" + line + "&sta=" + station + "&lang=en";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("MTRApiService", "API Response: " + response.toString());
                        try {
                            JSONObject data = response.getJSONObject("data");
                            JSONObject trainData = data.getJSONObject(line + "-" + station);
                            JSONArray upTrains = trainData.getJSONArray("UP");
                            JSONArray downTrains = trainData.getJSONArray("DOWN");

                            callback.onSuccess(upTrains, downTrains);
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
