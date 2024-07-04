package com.example.utb_adehikmatfr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.utb_adehikmatfr.recyclerview.AdapterList;
import com.example.utb_adehikmatfr.recyclerview.ItemList;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;
    private AdapterList myAdapter;
    private List<ItemList> itemList;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        recyclerView = findViewById(R.id.recycler_view);
        floatingActionButton = findViewById(R.id.floatAddNews);
        progressDialog = new ProgressDialog(MainActivity.this);
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
                Intent toAddPage = new Intent(MainActivity.this, NewsAdd.class);
                startActivity(toAddPage);
            }
        });

        // Fetch data from Firestore
        getData();

        myAdapter.setOnItemClickListener(new AdapterList.OnItemClickListener() {
            @Override
            public void onItemClick(ItemList item) {
                Intent intent = new Intent(MainActivity.this, NewsDetail.class);
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