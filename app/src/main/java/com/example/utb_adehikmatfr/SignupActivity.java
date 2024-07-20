package com.example.utb_adehikmatfr;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.common.api.ApiException;
import com.password4j.Password;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private static final int RC_SIGN_IN = 9001;
    private EditText emailEditText, passwordEditText, nameEditText;
    private Button buttonRegister, buttonRegisterGoogle;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameEditText = findViewById(R.id.name);
        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        buttonRegister = findViewById(R.id.buttonSignUp);
        buttonRegisterGoogle = findViewById(R.id.buttonRegisterGoogle);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        buttonRegister.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String name = nameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                emailEditText.setError("Nama harus diisi");
                return;
            }

            if (TextUtils.isEmpty(email)) {
                emailEditText.setError("Email harus diisi");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                passwordEditText.setError("Password harus diisi");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            firebaseAuthWithEmail(name,email, password);
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        buttonRegisterGoogle.setOnClickListener(v -> signIn());

        TextView textViewSignUp = findViewById(R.id.textViewSignUp);
        textViewSignUp.setOnClickListener(this::goToSignin);
    }

    public void goToSignin(View view) {
        Intent intent = new Intent(SignupActivity.this, SigningActivity.class);
        startActivity(intent);
    }

    private void signIn() {
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void showToast(String message) {
        Toast.makeText(SignupActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void firebaseAuthWithEmail(String name,String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        Map<String, Object> user = new HashMap<>();
                        user.put("email", email);
                        user.put("name", name);

                        db.collection("users").document(userId)
                                .set(user)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        showToast("Registrasi berhasil!");
                                        startActivity(new Intent(SignupActivity.this, HomeActivity.class));
                                        finish();
                                    } else {
                                        showToast("Registrasi gagal: " + task1.getException().getMessage());
                                        Log.e(TAG, "createUserWithEmail:setUser:failure", task1.getException());
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    showToast("Registrasi gagal: " + e.getMessage());
                                    Log.e(TAG, "createUserWithEmail:setUser:failure", e);
                                });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        showToast("Registrasi gagal: " + task.getException().getMessage());
                        Log.e(TAG, "createUserWithEmail:failure", task.getException());
                    }
                });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "register:firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Log.d(TAG, "register:signInWithCredentials:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("name", user.getDisplayName());
                            userMap.put("email", user.getEmail());

                            db.collection("users").document(userId)
                                    .set(userMap)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            showToast("Daftar sukses");
                                            startActivity(new Intent(SignupActivity.this, HomeActivity.class));
                                            finish();
                                        } else {
                                            showToast("Daftar gagal: " + task1.getException().getMessage());
                                            Log.e(TAG, "register:setUser:failure", task1.getException());
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        progressBar.setVisibility(View.GONE);
                                        showToast("Daftar gagal: " + e.getMessage());
                                        Log.e(TAG, "register:setUser:failure", e);
                                    });
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        showToast("Login gagal: " + task.getException().getMessage());
                        Log.w(TAG, "register:signInWithCredentials:failure", task.getException());
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                showToast("Google sign in failed: " + e.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        }
    }
}
