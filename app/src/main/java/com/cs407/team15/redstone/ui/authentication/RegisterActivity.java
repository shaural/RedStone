package com.cs407.team15.redstone.ui.authentication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cs407.team15.redstone.MainActivity;
import com.cs407.team15.redstone.R;
import com.cs407.team15.redstone.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import org.w3c.dom.Text;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private String TAG = "CCC: REGISTER ACTIVITY";

    // Password Regex
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{4,16}$");

    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextPassword_chk;
    private EditText editTextUsername;
    private TextView isSamePwTxt;
    private TextView textViewUsername_chk;
    Button signupButton,signinButton;
    private ProgressBar progressBar;


    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private FirebaseDatabase db2;
    private DatabaseReference usernameref;
    private CollectionReference userref;

    private String email = "";
    private String password = "";
    private String username = "";
    private boolean isSamePassword = false;

    public static final String COLLECTION_NAME_KEY = "users";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userref = db.collection(COLLECTION_NAME_KEY);

        db2 = FirebaseDatabase.getInstance();
        usernameref = db2.getReference("TakenUserNames");


//        if(firebaseAuth.getCurrentUser()!=null){
//            startActivity(new Intent(getApplicationContext(),MainActivity.class));
//        }

        initView();
        setListener();
    }

    /**
     * initView
     */
    private void initView() {
        editTextEmail = (EditText) findViewById(R.id.et_eamil);
        editTextPassword = (EditText) findViewById(R.id.et_password);
        editTextPassword_chk = (EditText) findViewById(R.id.et_password_chk);
        editTextUsername = (EditText) findViewById(R.id.et_username);

        textViewUsername_chk = (TextView) findViewById(R.id.tv_username_chk);
        isSamePwTxt = (TextView) findViewById(R.id.isSamePwTxt);
        signupButton = (Button) findViewById(R.id.btn_signUp);
        signinButton = (Button) findViewById(R.id.btn_signIn);

        progressBar = findViewById(R.id.progressBar);

        isSamePwTxt.setVisibility(View.GONE);
        textViewUsername_chk.setVisibility(View.GONE);
    }

    /**
     * setListener
     */
    private void setListener() {
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                finish();
            }
        });

        editTextPassword_chk.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isSamePassword = isSamePasswordCheck(editTextPassword.getText().toString(), charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!isSamePassword) {
                    isSamePwTxt.setVisibility(View.VISIBLE);
                } else {
                    isSamePwTxt.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * SignUp
     */
    public void signUp(View view) {
        progressBar.setVisibility(View.VISIBLE);

        email = editTextEmail.getText().toString();
        password = editTextPassword.getText().toString();
        username = editTextUsername.getText().toString();

        signUpTask();

    }

    /**
     * SignUp Task with Username Validation Check
     */
    private void signUpTask() {
        if(isValidEmail() && isValidPasswd() && isSamePassword) {
            usernameref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(username)) {
                        textViewUsername_chk.setVisibility(View.VISIBLE);
                    } else if (!dataSnapshot.hasChild(username)){
                        textViewUsername_chk.setVisibility(View.GONE);
                        createUser(email, password, username);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), "Connection Error. Please try again in some time.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "Failed to create user.");
        }
    }

    /**
     * email valid check
     */
    private boolean isValidEmail() {
        if (email.isEmpty()) {
            // empty email
            Toast.makeText(getApplicationContext(),"Please fill in the required fields",Toast.LENGTH_SHORT).show();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // invalid pattern
            Toast.makeText(getApplicationContext(),"Invalid Email address",Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    /**
     * password valid check
     */
    private boolean isValidPasswd() {
        if (password.isEmpty()) {
            // empty pw
            Toast.makeText(getApplicationContext(),"Please fill in the required fields",Toast.LENGTH_SHORT).show();
            return false;
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            // invalid pattern
            Toast.makeText(getApplicationContext(),"Invalid Password",Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    /**
     * create user
     */
    private void createUser(String email, String password, final String username) {
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            final FirebaseUser user = firebaseAuth.getCurrentUser();
                            /**
                             * Email verification
                             * comment for test
                             */
//                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    // Email verification
//                                    if (task.isSuccessful()) {
//                                        Toast.makeText(getApplicationContext(), "Verification Email is sent", Toast.LENGTH_SHORT).show();
//                                    } else {
//                                        Toast.makeText(getApplicationContext(), "Failed to send verification email", Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                            });

                            String refreshedToken = FirebaseInstanceId.getInstance().getId();
                            User member = new User();
                            member.uid = user.getUid();
                            member.token = refreshedToken;
                            member.email = user.getEmail();
                            member.login_attempt = 0;
                            member.username = username;

                            Log.e("Token", refreshedToken);
                            userref.document(user.getEmail()).set(member);
                            usernameref.child(username).setValue(true);

                            progressBar.setVisibility(View.GONE);

                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                            finish();
                        }
                        else{
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), R.string.failed_signup, Toast.LENGTH_SHORT).show();                        }
                    }
                });
    }

    /**
     * password match check
     */
    private boolean isSamePasswordCheck(String pw1, String pw2) {
        return pw1.equals(pw2);
    }

}