package com.example.mtrchecker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CheckScheduleFragment extends Fragment {
    private EditText lineInput, stationInput;
    private TextView trainSchedule;
    private MTRApiService mtrApiService;

    public CheckScheduleFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_check_schedule, container, false);

        lineInput = view.findViewById(R.id.lineInput);
        stationInput = view.findViewById(R.id.stationInput);
        trainSchedule = view.findViewById(R.id.trainSchedule);
        Button fetchButton = view.findViewById(R.id.fetchButton);

        mtrApiService = new MTRApiService(requireContext());

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String line = lineInput.getText().toString().toUpperCase().trim();
                String station = stationInput.getText().toString().toUpperCase().trim();

                if (!line.isEmpty() && !station.isEmpty()) {
                    fetchTrainSchedule(line, station);
                } else {
                    trainSchedule.setText("Please enter a valid line and station.");
                }
            }
        });

        return view;
    }

    private void fetchTrainSchedule(String line, String station) {
        mtrApiService.getTrainSchedule(line, station, new MTRApiService.TrainScheduleCallback() {
            @Override
            public void onSuccess(JSONArray upTrains, JSONArray downTrains) {
                StringBuilder scheduleText = new StringBuilder("Upcoming Trains:\n\n");

                try {
                    if (upTrains.length() == 0 && downTrains.length() == 0) {
                        scheduleText.append("No trains available.");
                    } else {
                        for (int i = 0; i < upTrains.length(); i++) {
                            JSONObject train = upTrains.getJSONObject(i);
                            scheduleText.append("Up Train to: ").append(train.getString("dest"))
                                    .append(" at ").append(train.getString("time")).append("\n");
                        }
                        for (int i = 0; i < downTrains.length(); i++) {
                            JSONObject train = downTrains.getJSONObject(i);
                            scheduleText.append("Down Train to: ").append(train.getString("dest"))
                                    .append(" at ").append(train.getString("time")).append("\n");
                        }
                    }
                } catch (JSONException e) {
                    scheduleText.append("Error parsing train data.");
                }

                trainSchedule.setText(scheduleText.toString());
            }

            @Override
            public void onError(String error) {
                trainSchedule.setText("Error fetching data: " + error);
            }
        });
    }
}


