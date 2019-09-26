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
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private String TAG = "CCC: REGISTER ACTIVITY";

    // Password Regex
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{4,16}$");

    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextPassword_chk;
    private TextView isSamePwTxt;
    Button signupButton,signinButton;
    FirebaseAuth firebaseAuth;

    private String email = "";
    private String password = "";
    private String password_chk = "";
    private boolean isSamePassword = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser()!=null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
        }

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

        isSamePwTxt = (TextView) findViewById(R.id.isSamePwTxt);
        signupButton = (Button) findViewById(R.id.btn_signUp);
        signinButton = (Button) findViewById(R.id.btn_signIn);

        isSamePwTxt.setVisibility(View.GONE);

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
        email = editTextEmail.getText().toString();
        password = editTextPassword.getText().toString();
        password_chk = editTextPassword_chk.getText().toString();

        if(isValidEmail() && isValidPasswd()&& isSamePassword) {
            createUser(email, password);
        } else {
            Toast.makeText(getApplicationContext(),"Check Sign up form please",Toast.LENGTH_SHORT).show();
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
    private void createUser(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            final FirebaseUser user = firebaseAuth.getCurrentUser();
                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // Email verification
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Verification Email is sent", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Failed to send verification email", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
//                            String refreshedToken = FirebaseInstanceId.getInstance().getId();
//                            User member = new User();
//                            member.id = user.getUid();
//                            member.token = refreshedToken;
//                            member.email = user.getEmail();
//                            Log.e("Token", refreshedToken);
                            //userRef.child(user.getUid()).setValue(member);
//                            Toast.makeText(getApplicationContext(), R.string.success_signup, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                            finish();
                        }
                        else{
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