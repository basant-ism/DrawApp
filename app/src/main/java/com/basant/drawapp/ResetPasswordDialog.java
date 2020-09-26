
package com.basant.drawapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordDialog extends Dialog
{
    Context context;
    TextView tvSubmit,tvCancel;
    EditText etUserEmail;
    TextView tvSent;
    ProgressDialog dialog;

    FirebaseAuth mAuth;
    public ResetPasswordDialog(@NonNull Context context) {
        super(context);
        this.context=context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_reset_password);

        mAuth=FirebaseAuth.getInstance();

        dialog=new ProgressDialog(context);
        dialog.setMessage("Sending...");

        tvCancel=findViewById(R.id.tv_cancel);
        tvSubmit=findViewById(R.id.tv_sumbit);
        tvSent=findViewById(R.id.tv_sent);
        etUserEmail=findViewById(R.id.et_user_email);

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        tvSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvSent.setVisibility(View.GONE);
                String email=etUserEmail.getText().toString().trim();
                if(TextUtils.isEmpty(email))
                {
                    etUserEmail.setError("Email can't be empty");
                }
                else {
                    dialog.show();
                    mAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            tvSent.setVisibility(View.VISIBLE);
                            tvSubmit.setText("RE-SEND");
                            tvCancel.setText("OK");
                            dialog.dismiss();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            etUserEmail.setError(e.getMessage());
                            dialog.dismiss();
                        }
                    });
                }

            }
        });
    }
}
