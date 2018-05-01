package com.example.nikikrish.bcprice;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    LineChart lineChart;
    Button button;
    private static String URL = "https://api.coindesk.com/v1/bpi/historical/close.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lineChart = findViewById(R.id.chart);
        button = findViewById(R.id.currencyButton);

        final HistoryFetcher historyFetcher = new HistoryFetcher();
        historyFetcher.execute("?currency=USD");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (button.getText().toString().equalsIgnoreCase("INR")){
                    new HistoryFetcher().execute("?currency=INR");
                    button.setText(R.string.USD);
                }else{
                    new HistoryFetcher().execute("?currency=USD");
                    button.setText(R.string.INR);
                }

            }
        });
    }

    private  class HistoryFetcher extends AsyncTask<String,String,String>{

        ArrayList<Entry> yEntries = new ArrayList<>();
        ArrayList<String> xEntries = new ArrayList<>();
        String label ="";
        @Override
        protected String doInBackground(String... voids) {

            String response = null ;

            if(voids[0].contains("USD")){
                label = "Price in USD";
            }
            if(voids[0].contains("INR")){
                label = "Price in INR ";
            }

            try {
                //append the URl parameter as per user requirement
                java.net.URL url = new URL(URL+voids[0]);
                //create a new connection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                // read the response
                InputStream in = new BufferedInputStream(conn.getInputStream());
                response = convertStreamToString(in);

                if(response !=null ){
                    try {
                        float i = 0;
                        //create the object
                        JSONObject jsonObject = new JSONObject(response);
                        //extract the bpi object
                        JSONObject bpiObject = jsonObject.getJSONObject("bpi");
                        //get all the keys and store it to a iterator
                        Iterator<String> keys = bpiObject.keys();
                        while(keys.hasNext()){
                            String key = keys.next();
                            //get the double values from the object using the above key
                            double value = bpiObject.getDouble(key);
                            //add the key to xEntries
                            xEntries.add(key);
                            //add the double to yEntries
                            yEntries.add(new Entry(i, (float) value));
                            i++;
                        }
                    }catch (Exception e){
                        Log.e("Exception e ",e.getLocalizedMessage());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);
            //create a new data set with y entries and label
            LineDataSet dataSet = new LineDataSet(yEntries,label);
            dataSet.setCircleColor(Color.RED);
            dataSet.setColor(Color.RED);

            //XAxis customization
            XAxis xAxis = lineChart.getXAxis();
            //set the position to bottom
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            //set the rotation angle
            xAxis.setLabelRotationAngle(-45f);
            //valueformatter
            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return xEntries.get((int)value);
                }
            });
            //create line data to store the data set
            LineData lineData = new LineData(dataSet);

            lineChart.setData(lineData);
            //Description
            Description description = new Description();
            description.setText(getString(R.string.Chart_desc));
            lineChart.setDescription(description);
            //set the maximum visible range to six
            lineChart.setVisibleXRangeMaximum(6f);
            //Animation
            lineChart.animateX(700);
            //refresh the chart
            lineChart.invalidate();

            //Legend Related coding
            Legend chartLegend = lineChart.getLegend();
            chartLegend.setTextColor(Color.RED);
            chartLegend.setForm(Legend.LegendForm.CIRCLE);

        }

    }
    private  String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
