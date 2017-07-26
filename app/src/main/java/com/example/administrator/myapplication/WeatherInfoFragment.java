package com.example.administrator.myapplication;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WeatherInfoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WeatherInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WeatherInfoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public static final int SEARCH_WEATHER = 0;
    TextView cityText;
    TextView dateText;
    TextView typeText;
    TextView currentTempText;
    TextView lowTempText;
    TextView highTempText;
    TextView windDirectionText;
    TextView windPowerText;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SEARCH_WEATHER:
                    handleWeatherInfo((String)msg.obj);
            }
        }
    };

    public WeatherInfoFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WeatherInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WeatherInfoFragment newInstance(String param1, String param2) {
        WeatherInfoFragment fragment = new WeatherInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_weather_info, container, false);
        cityText = (TextView) view.findViewById(R.id.textView1);
        dateText = (TextView) view.findViewById(R.id.textView2);
        typeText = (TextView) view.findViewById(R.id.textView3);
        currentTempText = (TextView) view.findViewById(R.id.textView4);
        lowTempText = (TextView) view.findViewById(R.id.textView5);
        highTempText = (TextView) view.findViewById(R.id.textView6);
        windDirectionText = (TextView) view.findViewById(R.id.textView7);
        windPowerText = (TextView) view.findViewById(R.id.textView8);
        sendHttpRequest();
        return view;
    }

    private void handleWeatherInfo(String jsonString) {
        try {
            JSONObject object = new JSONObject(jsonString);
            JSONObject jsonObject1 = object.getJSONObject("data");
            cityText.setText(jsonObject1.getString("city"));
            currentTempText.setText("当前温度：" + jsonObject1.getString("wendu"));
            JSONArray jsonArray = jsonObject1.getJSONArray("forecast");
            JSONObject weatherObject = jsonArray.getJSONObject(0);
            typeText.setText("天气状况：" + weatherObject.getString("type"));
            dateText.setText(weatherObject.getString("date"));
            lowTempText.setText("最低温度：" + weatherObject.getString("low"));
            highTempText.setText("最高温度：" + weatherObject.getString("high"));
            windDirectionText.setText("风向：" + weatherObject.getString("fengxiang"));
            windPowerText.setText("风力：" + weatherObject.getString("fengli"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendHttpRequest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    StringBuilder urlStr = new StringBuilder();
                    urlStr.append("http://wthrcdn.etouch.cn/weather_mini?city=")
                            .append(URLEncoder.encode(mParam1,"utf-8"));
                    String urlString = urlStr.toString();
                    URL url = new URL(urlString);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    Message message = new Message();
                    message.what = SEARCH_WEATHER;
                    message.obj = response.toString();
                    mHandler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
