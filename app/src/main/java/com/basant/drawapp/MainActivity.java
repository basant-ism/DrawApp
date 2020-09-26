package com.basant.drawapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;


import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import fragments.AllPaintingsFragmant;
import fragments.DrawFragment;
import fragments.MyPaintingsFragment;
import model.User;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    Toolbar toolbar;
    DrawerLayout drawer;
    LinearLayout iconLinearLayout;
    private static int FRAGMENT_NUMBER=1;
    ImageButton btnSave,btnBrush,btnErase,btnRotation;
    TextView tvUserName,tvUserEmail;

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();

        toolbar=findViewById(R.id.toolbar);
        iconLinearLayout=findViewById(R.id.icon_linera_layout);

        btnBrush=findViewById(R.id.btn_brush);
        btnErase=findViewById(R.id.btn_erase);
        btnSave=findViewById(R.id.btn_save);
        btnRotation=findViewById(R.id.btn_rotation);

        setLayoutAndTitle();
        setSupportActionBar(toolbar);

        drawer=findViewById(R.id.drawer_layout);
        navigationView=findViewById(R.id.nav_view);
        tvUserEmail=navigationView.getHeaderView(0).findViewById(R.id.tv_user_email);
        tvUserName=navigationView.getHeaderView(0).findViewById(R.id.tv_user_name);

        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawer,toolbar
                ,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        if(savedInstanceState==null)
        {
            FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container,
                    new DrawFragment());
            transaction.addToBackStack(null)
                    .commit();
            navigationView.setCheckedItem(R.id.nav_new);
        }
        setNavUserData();
        btnRotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rotate();
            }
        });

    }

    private void rotate() {
        androidx.appcompat.app.AlertDialog.Builder builder=new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setMessage("You will loss your painting?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(getResources().getConfiguration().orientation== Configuration.ORIENTATION_LANDSCAPE)
                {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                }
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        androidx.appcompat.app.AlertDialog dialog=builder.create();
        builder.setCancelable(false);
        dialog.show();
    }

    private void setNavUserData() {
        db.collection("users").document(mAuth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists())
                {
                    User user=documentSnapshot.toObject(User.class);
                    tvUserEmail.setText(user.getUemail());
                    tvUserName.setText(user.getUname());
                }
            }
        });
    }

    private void setLayoutAndTitle() {
        switch (FRAGMENT_NUMBER)
        {
            case 1:
                toolbar.setTitle("Draw");
                iconLinearLayout.setVisibility(View.VISIBLE);
                break;
            case 2:
                toolbar.setTitle("My Paintings");
                iconLinearLayout.setVisibility(View.INVISIBLE);
                break;
            case 3:
                toolbar.setTitle("Paintings");
                iconLinearLayout.setVisibility(View.INVISIBLE);
                break;
        }
    }



    @Override
    public void onBackPressed() {

        if(drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId())
        {
            case R.id.nav_new:
                navigationView.setCheckedItem(R.id.nav_new);
                FragmentTransaction transaction= getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new DrawFragment());
                transaction.addToBackStack(null).commit();
                toolbar.setTitle("Draw");
                iconLinearLayout.setVisibility(View.VISIBLE);
                FRAGMENT_NUMBER=1;
                break;
            case R.id.nav_setting:
                toolbar.setTitle("Setting");
                break;
            case R.id.nav_logout:
                navigationView.setCheckedItem(R.id.nav_logout);
                logout();
                break;
            case R.id.nav_my_paintings:
                navigationView.setCheckedItem(R.id.nav_my_paintings);
                FragmentTransaction transaction1= getSupportFragmentManager().beginTransaction();
                transaction1.replace(R.id.fragment_container,new MyPaintingsFragment());
                transaction1.addToBackStack(null).commit();
                toolbar.setTitle("My Paintings");
                iconLinearLayout.setVisibility(View.INVISIBLE);
                FRAGMENT_NUMBER=2;
                break;
            case R.id.nav_all_paintings:
                navigationView.setCheckedItem(R.id.nav_all_paintings);
                toolbar.setTitle("Paintings");
                FragmentTransaction transaction2= getSupportFragmentManager().beginTransaction();
                transaction2.replace(R.id.fragment_container,new AllPaintingsFragmant());
                transaction2.addToBackStack(null).commit();
                iconLinearLayout.setVisibility(View.INVISIBLE);
                FRAGMENT_NUMBER=3;
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("Do you want lagout?");
        builder.setTitle("Logout DrawApp");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logoutUser();
            }

        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();

    }
    public   void logoutUser()
    {
        if(mAuth.getCurrentUser()!=null) {
            mAuth.signOut();
            if (LoginManager.getInstance() != null)
                LoginManager.getInstance().logOut();
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
            if (googleSignInClient != null)
                googleSignInClient.signOut();
            Intent intent1 = new Intent(this, UserLoginActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent1);
        }
    }

}