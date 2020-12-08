package com.example.rentit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivityRegister extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference cardRef;
    private TextView textViewWarnEmail, textViewWarnPassword1, textViewWarnPassword2, textViewWarnAll;

    private FirebaseAuth mAuth;
    private RegisterInformation registerInformation=null;

    private Button registerButton,buttonPhone,buttonSmsCode;
    private Button returnButton;
    private EditText editTextEmail,editTextPhone,editTextSmsCode;
    private EditText editTextPass1;
    private EditText editTextPass2;
    private  String mVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    private String mobile;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_register);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        setmCallBacks();


        textViewWarnEmail = findViewById(R.id.textWarnEmail);
        textViewWarnPassword1 = findViewById(R.id.textWarnPassword1);
        textViewWarnPassword2 = findViewById(R.id.textWarnPassword2);
        textViewWarnAll = findViewById(R.id.textWarnAll);


        registerButton = findViewById(R.id.register);
        buttonSmsCode=findViewById(R.id.buttonCodeGo);
        buttonPhone=findViewById(R.id.buttonRegisterPhone);
        returnButton = findViewById(R.id.mainPage);
        editTextSmsCode=findViewById(R.id.editTextSmsCode);
        editTextPhone=findViewById(R.id.editTextPhone);
        editTextEmail = findViewById(R.id.email);
        editTextPass1 = findViewById(R.id.editTextTextPassword);
        editTextPass2 = findViewById(R.id.password2);
        buttonPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 mobile = editTextPhone.getText().toString().trim();

                if(mobile.isEmpty() || mobile.length() < 10){
                    editTextPhone.setError("Enter a valid mobile");
                   editTextPhone.requestFocus();
                   return;
                }
                else{
                    mobile=mobile;
//                    Intent intent = new Intent(MainActivityRegister.this, MainActivityPhone.class);
//                    intent.putExtra("num", mobile);
//                    startActivityForResult(intent, 0);
                 sendVerificationCode(mobile);
                    editTextSmsCode.setVisibility(View.VISIBLE);
                    buttonSmsCode.setVisibility(View.VISIBLE);


                }
            }
        });

        buttonSmsCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = editTextSmsCode.getText().toString().trim();
                if (code.isEmpty() || code.length() < 6) {
                    editTextSmsCode.setError("Enter valid code");
                    editTextSmsCode.requestFocus();
                    return;
                }
                verifyVerificationCode(code);

            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();

                //   saveRegisterDataFireBase();
            }
        });
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivityRegister.this, MainActivity.class);
                startActivity(intent);
            }
        });


    }

    private void setmCallBacks() {
        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                //Getting the code sent by SMS
                String code = phoneAuthCredential.getSmsCode();

                //sometime the code is not detected automatically
                //in this case the code will be null
                //so user has to manually enter the code
                if (code != null) {
                    editTextSmsCode.setText(code);
                    //verifying the code
                    verifyVerificationCode(code);
                }
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(MainActivityRegister.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                mVerificationId = s;


                //  mResendToken = forceResendingToken;
            }
        };
    }

    private void saveRegisterDataFireBase() {


        cardRef = database.getReference("RegisterInformation").push();


        cardRef.setValue(registerInformation);

        Toast.makeText(MainActivityRegister.this, "הייתי פה", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivityRegister.this, MainActivityPageUser.class);
        startActivity(intent);
    }



    private void registerFirebase(String email, String password) {
        final String TAG = "tag";
        Toast.makeText(MainActivityRegister.this, email, Toast.LENGTH_SHORT).show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            saveRegisterDataFireBase();
                        } else {
                            textViewWarnAll.setText("אימייל או סיסמא לא תקינים");
                            textViewWarnAll.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    public void register() {
        textViewWarnPassword2.setVisibility(View.GONE);
        textViewWarnPassword1.setVisibility(View.GONE);
        textViewWarnAll.setVisibility(View.GONE);
        textViewWarnEmail.setVisibility(View.GONE);

        String email = editTextEmail.getText().toString();
        if (ErrWarn.errEmail(email)) {
            textViewWarnEmail.setText("אימייל לא קיים");
            textViewWarnEmail.setVisibility(View.VISIBLE);
        }else {
            String pass1 =""+ editTextPass1.getText().toString();
            if (pass1.length() < 6) {
                textViewWarnPassword1.setText("סיסמא קצרה מדי, לפחות 6 תווים");
                textViewWarnPassword1.setVisibility(View.VISIBLE);
            }
            else {
                String pass2 = editTextPass2.getText().toString();
                if (pass1.equals(pass2)) {

                    registerInformation = new RegisterInformation();
                    registerInformation.setEmail(email);
                    registerInformation.setPassword(pass1);
                    registerFirebase(email, pass1);


                } else {
                    textViewWarnPassword2.setText("סיסמא לא תואמת");
                    textViewWarnPassword2.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void sendVerificationCode(String mobile) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber("+972"+mobile)
                .setTimeout(60L , TimeUnit.SECONDS)
                .setActivity(MainActivityRegister.this)
                .setCallbacks(mCallBacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }



    private void verifyVerificationCode(String code) {
        //creating the credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

        //signing the user
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(MainActivityRegister.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mobile=mobile.substring(1);
                            DatabaseReference ref3 = FirebaseDatabase.getInstance().getReference("RegisterInformation");
                            ref3.orderByChild("email").equalTo("+972"+mobile).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    // Toast.makeText(MainActivityRegisterCar.this, snapshot.toString(), Toast.LENGTH_LONG).show();
                                    for (DataSnapshot child : snapshot.getChildren()) {
                                        registerInformation = child.getValue(RegisterInformation.class);
                                    }
                                    if(registerInformation==null){
                                        registerInformation=new RegisterInformation();
                                        registerInformation.setEmail("+972"+mobile);
                                        saveRegisterDataFireBase();
                                    }
                                    else{
                                        Intent intent = new Intent(MainActivityRegister.this, MainActivityPageUser.class);
                                        startActivity(intent);


                                    }


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                            //verification successful we will start the profile activity


                        } else {

                            //verification unsuccessful.. display an error message

                            String message = "Somthing is wrong, we will fix it soon...";

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid code entered...";
                            }

//                            Snackbar snackbar = Snackbar.make(findViewById(R.id.parent), message, Snackbar.LENGTH_LONG);
//                            snackbar.setAction("Dismiss", new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//
//                                }
//                            });
//                            snackbar.show();
                        }
                    }
                });
    }


}