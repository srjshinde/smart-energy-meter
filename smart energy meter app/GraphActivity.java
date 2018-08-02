package com.tss.noticeboard;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.tss.noticeboard.model.User;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class GraphActivity extends AppCompatActivity {

    boolean isOneTimeReadData = true;
    double mRechargeAmount = 0;
    int userId;
    private DatabaseReference mDatabase;
    private String TAG = "GraphActivity.java";

    List<User> users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bar_layout);

        final BarChart barChart =  (BarChart)findViewById(R.id.chart);

        users = new ArrayList<>();
        users.clear();


// you can directly pass Date objects to DataPoint-Constructor
// this will convert the Date to double via Date#getTime()

        mDatabase = FirebaseDatabase.getInstance().getReference("Users");
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference myRef = database.getReference("Users");

        // Read from the database
        ValueEventListener master = myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (isOneTimeReadData) {
                    final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String logedInUserEmailId = currentUser.getEmail();
                    boolean isUserMaster = false;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user.getUserType().equals("Master") && logedInUserEmailId.equals(user.getEmailId())) {
                            isUserMaster = true;
                            break;
                        }
                    }
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);

                        if (isUserMaster) {
                            Log.d(TAG, " MASTER Value is: " + user);
                            Map<Integer, Map<String, List<Map<String, Integer>>>> unitCountPerYear = new HashMap<>();
                            for (DataSnapshot ds : snapshot.child("2018").getChildren()) {

                                Map<String, List<Map<String, Integer>>> monthDay = new HashMap<>();
                                Log.d(TAG, "DAY" + ds.toString());
                                String month = ds.getKey();
                                Map<String, Integer> days = (Map<String, Integer>) ds.getValue();
                                List<Map<String, Integer>> dayList = new ArrayList<>();
                                for (Map.Entry<String, Integer> day : days.entrySet()) {
                                    Log.d(TAG, "DATE :: " + day.getKey() + " UNIT ::" + day.getValue());
                                    Map<String, Integer> d = new HashMap<>();
                                    d.put(day.getKey(), day.getValue());
                                    dayList.add(d);
                                }
                                monthDay.put(month, dayList);
                                unitCountPerYear.put(2018, monthDay);
                            }
                            user.setYearMonthDays(unitCountPerYear);
                            if(!users.contains(user)){
                               users.add(user);
                            }
                        } else {

                            if (logedInUserEmailId.equals(user.getEmailId())) {
                                Log.d(TAG, " USER Value is: " + user);
                                Map<Integer, Map<String, List<Map<String, Integer>>>> unitCountPerYear = new HashMap<>();
                                for (DataSnapshot ds : snapshot.child("2018").getChildren()) {
                                    Map<String, List<Map<String, Integer>>> monthDay = new HashMap<>();
                                    Log.d(TAG, "DAY" + ds.toString());
                                    String month = ds.getKey();
                                    Map<String, Integer> days = (Map<String, Integer>) ds.getValue();
                                    List<Map<String, Integer>> dayList = new ArrayList<>();
                                    for (Map.Entry<String, Integer> day : days.entrySet()) {
                                        Log.d(TAG, "DATE :: " + day.getKey() + " UNIT ::" + day.getValue());
                                        Map<String, Integer> d = new HashMap<>();
                                        d.put(day.getKey(), day.getValue());
                                        dayList.add(d);
                                    }
                                    monthDay.put(month, dayList);
                                    unitCountPerYear.put(2018, monthDay);
                                }
                                user.setYearMonthDays(unitCountPerYear);
                                if(!users.contains(user)){
                                    users.add(user);
                                }
                                break;
                            }
                        }
                    }

                    Log.d(TAG, "TOTAL USERS :: " + users.size());
                    isOneTimeReadData = false;
                    //CREATE GRAPH HERE

                    List<DataPoint> dataPoints = new ArrayList<>();
                    List<BarDataSet> datasets = new ArrayList();
                    List<List<String>> labelsList = new ArrayList<>();
                    for (User user : users) {
                        ArrayList<BarEntry> entries = new ArrayList<>();
                        ArrayList<String> labels = new ArrayList<String>();
                        Map<Integer, Map<String, List<Map<String, Integer>>>> dates = user.getYearMonthDays();
                        System.out.println("Dates :: " + dates.toString());
                        for (Map.Entry date : dates.entrySet()) {
                            String year = date.getKey().toString();
                            Map<String, List<Map<String, Integer>>> value = (Map<String, List<Map<String, Integer>>>) date.getValue();
                            for (Map.Entry monthDate : value.entrySet()) {
                                String month = monthDate.getKey().toString();
                                List<Map<String, Integer>> date_ = (List<Map<String, Integer>>) monthDate.getValue();

                                int counter = 0;
                                for (Map<String, Integer> d : date_) {
                                    Iterator itr = d.entrySet().iterator();
                                    while (itr.hasNext()) {
                                        Map.Entry entry = (Map.Entry) itr.next();
                                        try {
                                            String[] dateParts = entry.getKey().toString().split("-");
                                            entries.add(new BarEntry(Float.parseFloat(entry.getValue().toString()),counter));
                                            labels.add( entry.getKey().toString());
                                            Log.d(TAG, "DAY:: " + Integer.parseInt(dateParts[0]) +" UNIT :: "+Integer.parseInt(entry.getValue().toString()));
                                            counter++;
                                        } catch (Exception e) {
                                            e.printStackTrace(System.err);
                                        }
                                    }
                                }
                            }
                        }

                        BarDataSet dataset = new BarDataSet(entries, user.getFullName());
                        datasets.add(dataset);
                        dataset.setColors(ColorTemplate.COLORFUL_COLORS);
//                        BarData data = new BarData(labels,dataset);
//                        barChart.setData(data);
                        labelsList.add(labels);

                    }

                    //GRAPH LAST
                    BarData data = new BarData(labelsList.get(0),datasets);
                    barChart.setData(data);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }

        });


    }
}
