package fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;

import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import custum_views.BrushPaintView;
import com.basant.drawapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import model.User;
public class DrawFragment extends Fragment implements View.OnClickListener {
    LinearLayout colorLinearLayout;
    public static ImageButton currentColor;
    custum_views.PaintView paintView;
    SeekBar seekBar;
    BrushPaintView brushPaintView;

    private static float INITIAL_BRUSH_SIZE=10.0f;
    private static int INITIAL_BRUSH_COLOR=Color.BLACK;
    private static String userName="";

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    StorageReference mStoreRef;

    public DrawFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TAG","view");
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d("TAG","restore");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_draw, container, false);

        mAuth=FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();
        mStoreRef= FirebaseStorage.getInstance().getReference();

        getUsername();

        getActivity().findViewById(R.id.btn_brush).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().findViewById(R.id.btn_brush).setBackground(getActivity().getDrawable(R.drawable.border));
                getActivity().findViewById(R.id.btn_erase).setBackground(null);
                getActivity().findViewById(R.id.btn_save).setBackground(null);
                paintView.setEraser(false);
                paintView.setUpDrawing();

            }
        });
        getActivity().findViewById(R.id.btn_erase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().findViewById(R.id.btn_erase).setBackground(getActivity().getDrawable(R.drawable.border));
                getActivity().findViewById(R.id.btn_brush).setBackground(null);
                getActivity().findViewById(R.id.btn_save).setBackground(null);
                getActivity().findViewById(R.id.btn_erase).setPadding(8,8,8,8);

                paintView.setEraser(true);
                paintView.setBrushSize(paintView.getBrushSize());

            }

        });
        getActivity().findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                builder.setTitle("Save Drawing");
                builder.setMessage("Do you want to save it?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        saveToFirebase(getContext());
                    }
                }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog dialog=builder.create();
                dialog.setCancelable(false);
                dialog.show();
            }
        });

        paintView=view.findViewById(R.id.paint_view);
        brushPaintView=view.findViewById(R.id.brush_paint_view);
        seekBar=view.findViewById(R.id.seek_bar);

        if(savedInstanceState==null)
        {
            brushPaintView.initailize(INITIAL_BRUSH_SIZE,INITIAL_BRUSH_COLOR);
            brushPaintView.setDrawing();

            paintView.initailize(INITIAL_BRUSH_SIZE,INITIAL_BRUSH_COLOR);
            paintView.setUpDrawing();

            seekBar.setMax(50);
            seekBar.setProgress((int)INITIAL_BRUSH_SIZE);
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                brushPaintView.setBrushSize(i);
                brushPaintView.setColor(paintView.getBrushColor());
                brushPaintView.setDrawing();
                paintView.setBrushSize(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        colorLinearLayout=view.findViewById(R.id.color_linear_layout);
        colorLinearLayout.getChildAt(0).setOnClickListener(this);
        colorLinearLayout.getChildAt(1).setOnClickListener(this);
        colorLinearLayout.getChildAt(2).setOnClickListener(this);
        colorLinearLayout.getChildAt(3).setOnClickListener(this);
        colorLinearLayout.getChildAt(4).setOnClickListener(this);
        colorLinearLayout.getChildAt(5).setOnClickListener(this);
        colorLinearLayout.getChildAt(6).setOnClickListener(this);
        colorLinearLayout.getChildAt(7).setOnClickListener(this);

        if(savedInstanceState==null)
        {
            getActivity().findViewById(R.id.btn_brush).setBackground(getActivity().getDrawable(R.drawable.border));
            getActivity().findViewById(R.id.btn_erase).setBackground(null);
            getActivity().findViewById(R.id.btn_save).setBackground(null);

            currentColor=(ImageButton) colorLinearLayout.getChildAt(0);
            currentColor.setBackground(getActivity().getDrawable(R.drawable.border));
            currentColor.setPadding(10,10,10,10);
            paintView.setColor(currentColor.getTag().toString());
            paintView.setUpDrawing();
        }

        return view;
    }

    private void getUsername() {
        db.collection("users").document(mAuth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists())
                {
                    User user=documentSnapshot.toObject(User.class);
                    userName=user.getUname();
                }
            }
        });
    }
    public void saveToFirebase(Context context) {
        final ProgressDialog dialog=new ProgressDialog(context);

        dialog.setMessage("Saving...");
        dialog.show();

        Bitmap newBitmap=Bitmap.createBitmap(paintView.getBitmap().getWidth(),paintView.getBitmap().getHeight()
                ,paintView.getBitmap().getConfig());
        Canvas canvas=new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(paintView.getBitmap(),0,0,null);

        final Date date= Calendar.getInstance().getTime();
        String time=new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date);
        String currentDate=new SimpleDateFormat("dd-MM-yyyy",Locale.getDefault()).format(date);
        final String paintingId="Painting_"+currentDate+"_"+time;

        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        newBitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
        byte[] bytes=stream.toByteArray();
        newBitmap.recycle();
        mStoreRef.child("images").child(mAuth.getUid()).child(paintingId+".png").putBytes(bytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mStoreRef.child("images").child(mAuth.getUid()).child(paintingId+".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        HashMap<String,Object>hashMap=new HashMap<>();
                        hashMap.put("uid",mAuth.getUid());
                        hashMap.put("date",date);
                        hashMap.put("pid",paintingId);
                        hashMap.put("uname",userName);
                        hashMap.put("likes",0);
                        hashMap.put("url",uri.toString());
                        db.collection("paintings").document(paintingId).set(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                dialog.dismiss();
                                Toast.makeText(getContext(),"Saved",Toast.LENGTH_LONG).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                                mStoreRef.child("images").child(mAuth.getUid()).child(paintingId+".png").delete();
                            }
                        });



                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    public void onClick(View view) {
        ImageButton newColor=(ImageButton) view;
        if(newColor!=currentColor) {
            newColor.setBackground(getActivity().getDrawable(R.drawable.border));
            newColor.setPadding(10, 10, 10, 10);
            if(currentColor!=null)
            {
                currentColor.setBackground(null);
            }
            paintView.setColor(newColor.getTag().toString());
            currentColor = newColor;
            brushPaintView.setColor(Color.parseColor(newColor.getTag().toString()));
            brushPaintView.setDrawing();
        }
    }
}