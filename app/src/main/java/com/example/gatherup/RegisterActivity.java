package com.example.gatherup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText inputEmail, inputUsername, inputPassword, inputConfirmPassword;
    Button btnRegister;

    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"; //VERIFICAR
    ProgressDialog progressDialog;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore store;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputEmail = findViewById(R.id.inputEmail);
        inputUsername = findViewById(R.id.inputUsername);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressDialog = new ProgressDialog(this);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        store = FirebaseFirestore.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PerformAuth();
            }
        });
    }

    private void PerformAuth() {
        String email = inputEmail.getText().toString().trim();
        String username = inputUsername.getText().toString();
        String password = inputPassword.getText().toString().trim();
        String confirmpassword = inputConfirmPassword.getText().toString().trim();

        if (!email.matches(emailPattern)) {
            inputEmail.setError("Invalid Email");
            inputEmail.requestFocus();
        } else if (username.isEmpty()) {
            inputUsername.setError("Choose a Username");
        } else {
            Task<Boolean> checkUsernameExistsTask = checkUsernameExists(username);
            checkUsernameExistsTask.addOnCompleteListener(new OnCompleteListener<Boolean>() {
                @Override
                public void onComplete(@NonNull Task<Boolean> task) {
                    if (task.isSuccessful()) {
                        Boolean usernameExists = task.getResult();
                        if (usernameExists) {
                            inputUsername.setError("Username already exists");
                        } else if (password.isEmpty() || password.length() < 8) {
                            inputPassword.setError("Invalid Password, must contain 8 caracteres");
                        } else if (!password.equals(confirmpassword)) {
                            inputConfirmPassword.setError("Password not match");
                        } else {
                            progressDialog.setMessage("Please Wait...");
                            progressDialog.setTitle("Registration");
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.show();

                            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        progressDialog.dismiss();

                                        userID = user.getUid();
                                        DocumentReference documentReference = store.collection("Users").document(userID);
                                        Map<String, Object> user = new HashMap<>();
                                        user.put("Username", username);
                                        user.put("Email", email);
                                        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Log.d("TAG", "onSuccess:" + userID + "profile created");
                                            }
                                        });

                                        AccountCreated();
                                        Toast.makeText(RegisterActivity.this, "Registration Completed", Toast.LENGTH_SHORT).show();
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(RegisterActivity.this, "" + task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }
    }


    private void AccountCreated() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private Task<Boolean> checkUsernameExists(String username) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference usersRef = firestore.collection("Users");
        Query query = usersRef.whereEqualTo("Username", username);
        Task<Boolean> task = query.get().continueWith(new Continuation<QuerySnapshot, Boolean>() {
            @Override
            public Boolean then(@NonNull Task<QuerySnapshot> task) throws Exception {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot.isEmpty()) {
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    return false;
                }
            }
        });
        return task;
    }
}