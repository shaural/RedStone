package com.cs407.team15.redstone.ui.authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cs407.team15.redstone.MainActivity;
import com.cs407.team15.redstone.R;
import com.cs407.team15.redstone.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private String TAG = "CCC: LOGIN ACTIVITY";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference userref;

    private EditText editTextEmail;
    private EditText editTextPassword;
    Button signupButton,signinButton, forgotButton;
    private ProgressBar progressBar;


    private String email = "";
    private String password = "";

    public static final String COLLECTION_NAME_KEY = "users";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userref = db.collection(COLLECTION_NAME_KEY);

        initView();
        setListener();
    }

    /**
     * Init View
     */
    private void initView() {
        editTextEmail = (EditText) findViewById(R.id.et_eamil);
        editTextPassword = (EditText) findViewById(R.id.et_password);
        signupButton = (Button) findViewById(R.id.btn_signUp);
        signinButton = (Button) findViewById(R.id.btn_signIn);
        forgotButton = (Button) findViewById(R.id.btn_forgot);
        progressBar = findViewById(R.id.progressBar);
    }

    /**
     * set listener
     */
    private void setListener() {
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),RegisterActivity.class));
            }
        });
        forgotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),ForgotPasswordActivity.class));
            }
        });
    }


    /**
     * Sign in
     */
    public void signIn(View view) {
        progressBar.setVisibility(View.VISIBLE);

        email = editTextEmail.getText().toString();
        password = editTextPassword.getText().toString();

        if (!email.isEmpty() && !password.isEmpty()) {
            signInAttempt();
        } else {
            Toast.makeText(getApplicationContext(), "Check your Email or Password please", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Sign-in Attempts check
     */
    private void signInAttempt() {
        // Sign in attempts
        DocumentReference docRef = db.collection("users").document(email);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if (document.getLong("login_attempt") != null) {
                            long attempts = document.getLong("login_attempt");
                            Log.d(TAG, "DocumentSnapshot data: " + attempts);
                            if (attempts <= 5) {
                                signInUser(email, password);
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), "This account has been locked", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "This account has been locked", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Log.d(TAG, "No User document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    /**
     * Sign in user && check email verification
     */
    private void signInUser(final String email, final String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            /**
                             * Email verification
                             * comment this for test
                             */
//                            if(user.isEmailVerified()){
//                                Log.e(TAG, "signInWithEmail:success:emailVerified");
//                                Toast.makeText(getApplicationContext(), "Welcome!", Toast.LENGTH_SHORT).show();
//                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                                finish();
//                            } else {
//                                Log.e(TAG, "signInWithEmail:success:emailNotVerified");
//                                Toast.makeText(getApplicationContext(), "Please verify your email", Toast.LENGTH_SHORT).show();
//                            }

                            /**
                             * For test
                             */
                            Log.e(TAG, "signInWithEmail:success:emailVerified");

                            // sign in attempts init
                            db.collection("users").document(user.getEmail()).get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot document) {
                                            User me = document.toObject(User.class);
                                            me.login_attempt = 0;
                                            document.getReference().set(me);
                                        }
                                    });

                            progressBar.setVisibility(View.GONE);

                            Toast.makeText(getApplicationContext(), "Welcome! "+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();

                        } else {
                            // increase sign in attempts
                            db.collection("users").document(email).get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot document) {
                                            User me = document.toObject(User.class);
                                            Log.e(TAG, email + " Attempts:" + me.login_attempt);
                                            me.login_attempt++;
                                            document.getReference().set(me);
                                        }
                                    });

                            Log.e(TAG, "signInWithEmail:failure", task.getException());
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

//        if (currentUser != null && currentUser.isEmailVerified()) {
        if (currentUser != null) {
            Log.e(TAG, "ON START - User info detected.");
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
//        updateUI(currentUser);
    }
}
