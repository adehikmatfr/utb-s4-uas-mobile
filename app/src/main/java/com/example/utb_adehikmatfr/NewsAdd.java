package com.example.utb_adehikmatfr;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class NewsAdd extends AppCompatActivity {
    String id="", judul, deskripsi, image;
    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText title, desc;
    private ImageView imageView;
    private Button saveNews, chooseImage;
    private Uri imageUri;

    private FirebaseFirestore dbNews;
    private FirebaseStorage storage;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_add);

        // Initialize Firebase
        dbNews = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize UI components
        title = findViewById(R.id.title);
        desc = findViewById(R.id.desc);
        imageView = findViewById(R.id.imageView);
        saveNews = findViewById(R.id.btnAdd);
        chooseImage = findViewById(R.id.btnChooseImage);

        progressDialog = new ProgressDialog(NewsAdd.this);
        progressDialog.setTitle("Loading...");

        chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        saveNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newsTitle = title.getText().toString().trim();
                String newsDesc = desc.getText().toString().trim();

                if (newsTitle.isEmpty() || newsDesc.isEmpty()) {
                    Toast.makeText(NewsAdd.this, "Title and Description cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog.show();

                if (imageUri != null) {
                    uploadImageToStorage(newsTitle, newsDesc);
                } else {
                    saveData(newsTitle, newsDesc, image);
                }
            }
        });

        Intent updateOption = getIntent();
        if (updateOption != null) {
            id = updateOption.getStringExtra("id");
            judul = updateOption.getStringExtra("title");
            deskripsi = updateOption.getStringExtra("desc");
            image = updateOption.getStringExtra("imageUrl");

            title.setText(judul);
            desc.setText(deskripsi);
            if (image != null && !image.isEmpty()) {
                Glide.with(this).load(image).into(imageView);
            } else {
                imageView.setImageResource(0);
            }
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
            Glide.with(this).load(imageUri).into(imageView);
        }
    }

    private void uploadImageToStorage(String newsTitle, String newsDesc) {
        if (imageUri != null) {
            StorageReference storageRef = storage.getReference().child("news_images/" + System.currentTimeMillis() + ".jpg");
            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveData(newsTitle, newsDesc, imageUrl);
                    }))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(NewsAdd.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void saveData(String newsTitle, String newsDesc, String imageUrl) {
        Map<String, Object> news = new HashMap<>();
        news.put("title", newsTitle);
        news.put("desc", newsDesc);
        news.put("imageUrl", imageUrl);

        if (id != null) {
            dbNews.collection("news").document(id)
                    .update(news)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        Toast.makeText(NewsAdd.this, "News updated successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(NewsAdd.this, MainActivity.class);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(NewsAdd.this, "Error updating news: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.w("NewsAdd", "Error updating document", e);
                    });
        } else {
            dbNews.collection("news")
                    .add(news)
                    .addOnSuccessListener(documentReference -> {
                        progressDialog.dismiss();
                        Toast.makeText(NewsAdd.this, "News added successfully", Toast.LENGTH_SHORT).show();
                        title.setText("");
                        desc.setText("");
                        imageView.setImageResource(0); // Clear the ImageView
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(NewsAdd.this, "Error adding news: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.w("NewsAdd", "Error adding document", e);
                    });
        }
    }
}