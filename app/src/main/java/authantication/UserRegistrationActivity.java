package authantication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.basant.drawapp.R;
import com.basant.drawapp.UserLoginActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class UserRegistrationActivity extends AppCompatActivity implements View.OnClickListener {
    EditText etUserName,etUserEmail,etUserPassword;
    Button btnRegister,btnReSend,btnLoToLogin;
    ImageView imgBack;

    LinearLayout sentLinearLayout;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        initialize();

        mAuth=FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();

        dialog=new ProgressDialog(this);
        dialog.setMessage("User SignUp...");

        imgBack.setOnClickListener(this);
        btnLoToLogin.setOnClickListener(this);
        btnReSend.setOnClickListener(this);
        btnRegister.setOnClickListener(this);

    }
    private void initialize()
    {
        etUserEmail=findViewById(R.id.et_user_email);
        etUserName=findViewById(R.id.et_user_name);
        etUserPassword=findViewById(R.id.et_user_password);

        btnLoToLogin=findViewById(R.id.btn_go_to_login);
        btnReSend=findViewById(R.id.btn_send);
        btnRegister=findViewById(R.id.btn_register);

        imgBack=findViewById(R.id.img_back);

        sentLinearLayout=findViewById(R.id.resend_linear_layout);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btn_register:
                userRegistration();
                break;
            case R.id.btn_send:
                resendLink();
                break;
            case R.id.btn_go_to_login:
                goToLogin();
                break;
            case  R.id.img_back:
                finish();
                break;
        }
    }

    private void goToLogin() {
        startActivity(new Intent(UserRegistrationActivity.this, UserLoginActivity.class));
    }

    private void resendLink() {
        if(mAuth.getCurrentUser()!=null)
        {
            dialog.setMessage("Re-sending...");
            dialog.show();
            mAuth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    dialog.dismiss();
                    Toast.makeText(UserRegistrationActivity.this,"New Link have sent to your email",Toast.LENGTH_LONG).show();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    Toast.makeText(UserRegistrationActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();

                }
            });
        }
    }

    public void userRegistration()
    {
        final String uname=etUserName.getText().toString().trim();
        final String uemail=etUserEmail.getText().toString().trim();
        String password=etUserPassword.getText().toString().trim();
        if(TextUtils.isEmpty(uname))
        {
            etUserName.setError("User name can't be empty");
        }
        else if(TextUtils.isEmpty(uemail))
        {
            etUserEmail.setError("Email can't be empty");
        }
        else if(TextUtils.isEmpty(password))
        {
            etUserPassword.setError("Password can't be empty");
        }
        else
        {
            dialog.show();
            mAuth.createUserWithEmailAndPassword(uemail,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    mAuth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dialog.dismiss();
                            sentLinearLayout.setVisibility(View.VISIBLE);
                            HashMap<String,Object>hashMap=new HashMap<>();
                            hashMap.put("uname",uname);
                            hashMap.put("uemail",uemail);
                            db.collection("users").document(mAuth.getUid()).set(hashMap);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            etUserPassword.setError(e.getMessage());
                        }
                    });

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
}