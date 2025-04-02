package com.example.mtrchecker;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CheckScheduleFragment extends Fragment {
    private Spinner mtrLineSpinner, mtrStationSpinner;
    private TextView trainScheduleText;
    private MTRApiService mtrApiService;
    private ArrayAdapter<String> lineAdapter, stationAdapter;

    private String selectedLine = "TCL"; // 默認東涌線 (API 代碼)
    private String selectedStation = "HOK"; // 默認香港站 (API 代碼)

    // 线路名称对照表
    private final String[] lineNames = {"東涌線", "荃灣線", "港島線"};
    private final String[] lineCodes = {"TCL", "TWL", "ISL"}; // API 代碼

    // 站點名称对照表
    private final String[][] stationNames = {
            {"香港", "九龍", "奧運", "南昌", "青衣", "欣澳", "東涌"}, // 東涌線
            {"中環", "金鐘", "尖沙咀", "佐敦", "油麻地", "旺角", "太子", "深水埗", "長沙灣", "荔枝角", "美孚", "茘景","葵芳","葵興","大窩口","荃灣"}, // 荃灣線
            {"堅尼地城", "香港大學", "西營盤", "上環", "中環", "金鐘", "灣仔", "銅鑼灣", "天后", "炮台山", "北角",
                    "鰂魚涌", "太古", "西灣河", "筲箕灣", "杏花邨", "柴灣"}  // 港島線
    };

    private final String[][] stationCodes = {
            {"HOK", "KOW", "OLY", "NAM", "TSY", "SUN", "TUC"}, // 東涌線
            {"CEN", "ADM", "TST", "JOR", "YMT", "MOK", "PRE", "SSP", "CSW", "LCK", "MEF", "LAK","KWF","KWH","TWH","TSW"}, // 荃灣線
            {"KET", "HKU", "SHW", "SHW", "CEN", "ADM", "WAC", "CAB", "TIH", "FOH", "NOP", "QUB", "TAU", "SWH", "SKW", "HFC", "CWB"}  // 港島線
    };

    private final Map<String, String> stationMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_check_schedule, container, false);

        mtrLineSpinner = view.findViewById(R.id.mtrLineSpinner);
        mtrStationSpinner = view.findViewById(R.id.mtrStationSpinner);
        trainScheduleText = view.findViewById(R.id.trainScheduleText);

        mtrApiService = new MTRApiService(requireContext());

        // 構建站名對應關係
        for (int i = 0; i < lineNames.length; i++) {
            for (int j = 0; j < stationNames[i].length; j++) {
                stationMap.put(stationNames[i][j], stationCodes[i][j]);
            }
        }

        lineAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, lineNames);
        mtrLineSpinner.setAdapter(lineAdapter);

        mtrLineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLine = lineCodes[position]; // 使用 API 代碼
                updateStationSpinner(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        mtrStationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStationName = stationNames[mtrLineSpinner.getSelectedItemPosition()][position];
                selectedStation = stationMap.get(selectedStationName); // 轉換為 API 代碼
                fetchSchedule();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        updateStationSpinner(0); // 預設顯示第一條路線的站點
        return view;
    }

    private void updateStationSpinner(int lineIndex) {
        stationAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, stationNames[lineIndex]);
        mtrStationSpinner.setAdapter(stationAdapter);
        selectedStation = stationMap.get(stationNames[lineIndex][0]); // 選擇第一個站點
        fetchSchedule();
    }

    private void fetchSchedule() {
        mtrApiService.getTrainSchedule(selectedLine, selectedStation, new MTRApiService.TrainScheduleCallback() {
            @Override
            public void onSuccess(JSONArray upTrains, JSONArray downTrains) {
                try {
                    StringBuilder scheduleText = new StringBuilder("🚆 **列車時刻表**\n\n");

                    scheduleText.append("🔼 **往上行方向**\n");
                    for (int i = 0; i < upTrains.length(); i++) {
                        JSONObject train = upTrains.getJSONObject(i);
                        scheduleText.append(train.getString("time")).append(" - ").append(train.getString("dest")).append("\n");
                    }

                    scheduleText.append("\n🔽 **往下行方向**\n");
                    for (int i = 0; i < downTrains.length(); i++) {
                        JSONObject train = downTrains.getJSONObject(i);
                        scheduleText.append(train.getString("time")).append(" - ").append(train.getString("dest")).append("\n");
                    }

                    trainScheduleText.setText(scheduleText.toString());
                } catch (JSONException e) {
                    trainScheduleText.setText("數據解析錯誤！");
                    Log.e("CheckScheduleFragment", "Parsing error: " + e.getMessage());
                }
            }

            @Override
            public void onError(String errorMessage) {
                trainScheduleText.setText("❌ 無法獲取數據：" + errorMessage);
            }
        });
    }
}

