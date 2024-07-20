package com.example.utb_adehikmatfr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

public class NewsDetail extends AppCompatActivity {

    TextView newsTitle, newsSubtitle;
    ImageView newsImage;
    Button edit, delete;
    private FirebaseFirestore dbNews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        // Initialize UI components
        newsTitle = findViewById(R.id.newsTitle);
        newsSubtitle = findViewById(R.id.newsSubtitle);
        newsImage = findViewById(R.id.newsImage);
        edit = findViewById(R.id.editButton);
        delete = findViewById(R.id.deleteButton);
        dbNews = FirebaseFirestore.getInstance();

        // Get data from Intent
        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        String title = intent.getStringExtra("title");
        String subtitle = intent.getStringExtra("desc");
        String imageUrl = intent.getStringExtra("imageUrl");

        // Set data to UI components
        newsTitle.setText(title);
        newsSubtitle.setText(subtitle);
        Glide.with(this).load(imageUrl).into(newsImage);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewsDetail.this, NewsAdd.class);
                intent.putExtra("id", id);
                intent.putExtra("title", title);
                intent.putExtra("desc", subtitle);
                intent.putExtra("imageUrl", imageUrl);
                startActivity(intent);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbNews.collection("news").document(id)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(NewsDetail.this, "News deleted successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(NewsDetail.this, HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish(); // Close the activity and go back to the previous screen
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(NewsDetail.this, "Error deleting news: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.w("NewsDetail", "Error deleting document", e);
                        });
            }
        });
    }
}