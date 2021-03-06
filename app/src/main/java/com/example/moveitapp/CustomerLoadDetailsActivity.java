package com.example.moveitapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.widget.RatingBar;

public class CustomerLoadDetailsActivity extends AppCompatActivity {
    public static final String TAG = "TAG";
    String email, password, name, userID, loadDate, loadID, driverID;



    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    FirebaseFirestoreSettings settings;

    ListView lvDetails, lvColumns;
    List<String> loadDetails = new ArrayList<>();
    List<String> loadColumns = new ArrayList<>();
    ArrayAdapter<String> detailsAdapter, columnAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_load_details);

        final RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        Button submitRating = (Button) findViewById(R.id.btn_submitRating);


        lvDetails = (ListView) findViewById(R.id.lv_details);
        lvColumns=(ListView) findViewById(R.id.lv_columns);


        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        password = intent.getStringExtra("password");
        name =  intent.getStringExtra("name");
        userID = intent.getStringExtra("userID");
        loadDate = intent.getStringExtra("loadDate");
        getSupportActionBar().setTitle("User: "+name.toUpperCase());

        //firebase connection
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
        FirebaseUser user = firebaseAuth.getCurrentUser();



        
        if(firebaseAuth.getCurrentUser() != null)
        {
            final Task<QuerySnapshot> collection = firestore.collection("loads")
                    .whereEqualTo("DateTime", loadDate)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                int counter = 0;
                                String documentID = "";

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    loadDetails.add(document.getId());
                                    loadID = document.getId().toString();
                                    loadColumns.add("Load ID: ");
                                    loadDetails.add(document.getString("Status"));
                                    loadColumns.add("Status: ");
                                    loadDetails.add(document.getString("Category"));
                                    loadColumns.add("Category: ");
                                    loadDetails.add(document.getString("DateTime"));
                                    loadColumns.add("Date & Time:");
                                    double fees = (double) document.get("Delivery Fees");
                                    loadColumns.add("Delivery Fees:");
                                    loadDetails.add(Double.toString(fees));
                                    loadDetails.add(document.getString("Destination"));
                                    loadColumns.add("Destination: ");
                                    loadDetails.add(document.getString("Driver ID"));
                                    loadColumns.add("Driver ID: ");
                                    driverID= document.getString("Driver ID");
                                    loadDetails.add(document.getString("Pickup"));
                                    loadColumns.add("Pickup:");
                                    loadDetails.add(document.getString("Driver Location"));
                                    loadColumns.add("Driver Location");
                                    loadDetails.add(document.getString("Vehicle Wanted"));
                                    loadColumns.add("Vehicle Wanted: ");
                                    double weight = (double) document.get("Weight");
                                   loadDetails.add(Double.toString(weight));
                                   loadColumns.add("Weight: ");
                                    detailsAdapter = new ArrayAdapter<String>(
                                            getApplicationContext(),
                                            android.R.layout.simple_list_item_1, loadDetails
                                    );
                                    lvDetails.setAdapter(detailsAdapter);
                                    columnAdapter = new ArrayAdapter<String>(
                                            getApplicationContext(),
                                            android.R.layout.simple_list_item_1, loadColumns
                                    );
                                    lvColumns.setAdapter(columnAdapter);
                                }
                            }
                        }
                    });
        }

        // perform click event on button
        submitRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get values and then displayed in a toast
                String totalStars = "Total Stars:: " + ratingBar.getNumStars();
                String rating = "Your Rating: " + ratingBar.getRating();
                Toast.makeText(getApplicationContext(), totalStars + "\n" + rating, Toast.LENGTH_LONG).show();

                // submitting the rating object to database
                DocumentReference documentReference = firestore.collection("Ratings").document(loadID);
                Map<String, Object> loadRating = new HashMap<>();
                loadRating.put("Customer ID", userID);
                loadRating.put("Load ID", loadID );
                loadRating.put("Driver ID", driverID);
                loadRating.put("Rating", rating);
                loadRating.put("Rating Date", Calendar.getInstance().getTime());


                documentReference.set(loadRating).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Rating successfully filled for "+ userID  );
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: "+ e.toString());
                    }
                });
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }
}