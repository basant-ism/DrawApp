package com.basant.drawapp;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;


import java.util.Arrays;

import java.util.HashMap;

import authantication.UserRegistrationActivity;

public class UserLoginActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    ProgressDialog dialog;
    private static final int RC_SIGN_IN =123 ;
    EditText etUserEmail,etUserPassword;

    private GoogleSignInClient mGoogleSignInClient;
    CallbackManager mcallbackManager;

    private static String userName="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        etUserEmail=findViewById(R.id.et_user_email);
        etUserPassword=findViewById(R.id.et_user_password);

        dialog=new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("logging...");

        mAuth=FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();

        FacebookSdk.sdkInitialize(getApplicationContext());
        mcallbackManager=CallbackManager.Factory.create();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }
    public  void facebookLogin(View view) {
        dialog.show();
        LoginManager.getInstance().logInWithReadPermissions(UserLoginActivity.this, Arrays.asList("email","public_profile"));
        LoginManager.getInstance().registerCallback(mcallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if(loginResult.getAccessToken()!=null)
                {
                    RequestData();
                    handleAccessFacbookTocken(loginResult.getAccessToken());
                }

                else{
                    dialog.dismiss();
                    Toast.makeText(UserLoginActivity.this,"Try again...",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancel() {
                dialog.dismiss();
                Toast.makeText(UserLoginActivity.this,"Try again...",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                dialog.dismiss();
                Toast.makeText(UserLoginActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleAccessFacbookTocken(AccessToken accessToken) {
        AuthCredential credential= FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    HashMap<String,Object>hashMap=new HashMap<>();
                    hashMap.put("uname",userName);
                    db.collection("users").document(mAuth.getUid()).set(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent=new Intent(UserLoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    });

                }
                else
                {
                    dialog.dismiss();
                    Toast.makeText(UserLoginActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });

    }
    public void RequestData(){
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                JSONObject json = response.getJSONObject();
                try {
                    if(json != null){
                        userName=json.getString("name");
                    }

                } catch (JSONException e) {


                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "name");
        request.setParameters(parameters);
        request.executeAsync();
    }
    public  void googleLogin(View view)
    {
        dialog.show();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_CANCELED)
        {
            dialog.dismiss();
            Toast.makeText(UserLoginActivity.this,"Try again...",Toast.LENGTH_LONG).show();
            return;
        }
        if (requestCode == RC_SIGN_IN) {//For google sign in
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                dialog.dismiss();
                Toast.makeText(UserLoginActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
        else {//Facebook authantication
            mcallbackManager.onActivityResult(requestCode, resultCode, data);
        }

    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser user = mAuth.getCurrentUser();
                            String userName=user.getDisplayName();

                            HashMap<String,Object>hashMap=new HashMap<>();
                            hashMap.put("uname",userName);
                            db.collection("users").document(mAuth.getUid()).set(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Intent intent=new Intent(UserLoginActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                            });


                        } else {
                            dialog.dismiss();
                            Toast.makeText(UserLoginActivity.this,"Try again...",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void userLogin(View view)
    {
        String userEmail=etUserEmail.getText().toString().trim();
        String userPassword=etUserPassword.getText().toString().trim();

        if(TextUtils.isEmpty(userEmail))
        {
            etUserEmail.setError("Email can't be empty");
        }
        else if(TextUtils.isEmpty(userPassword))
        {
            etUserPassword.setError("Password can't be empty");
        }
        else{
            dialog.show();
            mAuth.signInWithEmailAndPassword(userEmail,userPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    if(mAuth.getCurrentUser().isEmailVerified()) {
                        Intent intent = new Intent(UserLoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                    else{
                        dialog.dismiss();
                        etUserEmail.setError("Firstly verify your email");
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    dialog.dismiss();
                    etUserPassword.setError(e.getMessage());
                }
            });
        }
    }
    public void goToRegister(View view)
    {
        Intent intent=new Intent(UserLoginActivity.this, UserRegistrationActivity.class);
        startActivity(intent);
    }
    public void resetPassword(View view)
    {
        ResetPasswordDialog dialog=new ResetPasswordDialog(this);
        dialog.setCancelable(false);
        dialog.show();
    }
}
