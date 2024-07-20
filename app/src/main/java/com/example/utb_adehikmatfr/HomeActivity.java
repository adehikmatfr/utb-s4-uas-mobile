package com.example.utb_adehikmatfr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.util.Log;

import com.example.utb_adehikmatfr.recyclerview.AdapterList;
import com.example.utb_adehikmatfr.recyclerview.ItemList;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private AdapterList myAdapter;
    private List<ItemList> itemList;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        FloatingActionButton floatingActionButton = findViewById(R.id.floatAddNews);
        progressDialog = new ProgressDialog(HomeActivity.this);
        progressDialog.setTitle("Loading...");

        // Setup RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemList = new ArrayList<>();
        myAdapter = new AdapterList(itemList);
        recyclerView.setAdapter(myAdapter);

        // FloatingActionButton click listener
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toAddPage = new Intent(HomeActivity.this, NewsAdd.class);
                startActivity(toAddPage);
            }
        });

        FloatingActionButton floatLogout = findViewById(R.id.floatLogout);
        floatLogout.setOnClickListener(view -> {
            // Handle logout action
            mAuth.signOut();
            Intent intent = new Intent(HomeActivity.this, SigningActivity.class);
            startActivity(intent);
            finish(); // Hentikan HomeActivity
        });

        // Fetch data from Firestore
        getData();

        myAdapter.setOnItemClickListener(new AdapterList.OnItemClickListener() {
            @Override
            public void onItemClick(ItemList item) {
                Intent intent = new Intent(HomeActivity.this, NewsDetail.class);
                intent.putExtra("id", item.getId());
                intent.putExtra("title", item.getTitle());
                intent.putExtra("desc", item.getDescription());
                intent.putExtra("imageUrl", item.getThumbnailUrl());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        getData();
    }

    private void getData() {
        progressDialog.show();
        db.collection("news")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            itemList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ItemList item = new ItemList(
                                        document.getString("title"),
                                        document.getString("desc"),
                                        document.getString("imageUrl")
                                );
                                item.setId(document.getId());
                                itemList.add(item);
                                Log.d("data", document.getId() + " => " + document.getData());
                            }
                            myAdapter.notifyDataSetChanged();
                        } else {
                            Log.w("data", "Error getting documents.", task.getException());
                        }
                        progressDialog.dismiss();
                    }
                });
    }
}