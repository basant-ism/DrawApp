package fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.basant.drawapp.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import model.Like;
import model.Painting;
import view_holder.PaintingViewHolder;


public class MyPaintingsFragment extends Fragment {
    FirestoreRecyclerAdapter< Painting,PaintingViewHolder>adapter;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    StorageReference mStoreRef;
    ProgressDialog dialog;
    RecyclerView recyclerView;
    public MyPaintingsFragment() {

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_my_paintings, container, false);
        db=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        mStoreRef= FirebaseStorage.getInstance().getReference();
        dialog=new ProgressDialog(getContext());
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Query query=db.collection("paintings").whereEqualTo("uid",mAuth.getUid())
                .orderBy("date", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Painting>options=new FirestoreRecyclerOptions.Builder<Painting>()
                .setQuery(query,Painting.class)
                .build();

        adapter=new FirestoreRecyclerAdapter<Painting, PaintingViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final PaintingViewHolder holder, int position, @NonNull final Painting model) {
                holder.tvUserName.setText(model.getUname());
                Picasso.get().load(model.getUrl()).into(holder.imgPainting);
                holder.imgDelete.setVisibility(View.VISIBLE);
                holder.imgDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deletePainting(model.getPid());
                    }
                });
                holder.tvLike.setText(model.getLike()+"");
                holder.setLikeButton(model.getPid(),mAuth.getUid());

                holder.imgLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setLike(model.getPid(),holder);
                    }
                });

            }

            @NonNull
            @Override
            public PaintingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.painting_layout,parent,false);
                return new PaintingViewHolder(getContext(),view);
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onStop() {
        super.onStop();
    if (adapter!=null)
    {
        adapter.stopListening();
    }
    }

    private void setLike(final String pid, final PaintingViewHolder holder) {
        db.collection("paintings").document(pid).collection("likes")
                .document(mAuth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists())
                {
                    db.collection("paintings").document(pid).collection("likes")
                            .document(mAuth.getUid()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            holder.imgLike.setImageDrawable(getActivity().getDrawable(R.drawable.ic_like_black));
                            db.collection("paintings").document(pid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Painting painting=documentSnapshot.toObject(Painting.class);
                                    HashMap<String,Object>hashMap=new HashMap<>();
                                    hashMap.put("like",painting.getLike()-1);
                                    holder.tvLike.setText(painting.getLike()-1+"");
                                    db.collection("paintings").document(pid).update(hashMap);

                                }
                            });

                        }
                    });

                }
                else {
                    HashMap<String,Object>hashMap=new HashMap<>();
                    hashMap.put("uid",mAuth.getUid());
                    db.collection("paintings").document(pid).collection("likes")
                            .document(mAuth.getUid()).set(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            holder.imgLike.setImageDrawable(getActivity().getDrawable(R.drawable.ic_like_blue));
                            db.collection("paintings").document(pid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Painting painting=documentSnapshot.toObject(Painting.class);
                                    HashMap<String,Object>hashMap=new HashMap<>();
                                    hashMap.put("like",painting.getLike()+1);
                                    holder.tvLike.setText(painting.getLike()+1+"");
                                    db.collection("paintings").document(pid).update(hashMap);

                                }
                            });

                        }
                    });
                }
            }
        });
    }
    private void deletePainting(final String pid) {

        androidx.appcompat.app.AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Painting");
        builder.setMessage("Do you want to delete this?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                db.collection("paintings").document(pid).delete();
                mStoreRef.child("images").child(mAuth.getUid()).child(pid).delete();
                onStart();
                db.collection("paintings").document(pid).collection("likes")
                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty())

                        {
                            for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots)
                            {
                                Like like=documentSnapshot.toObject(Like.class);
                                db.collection("paintings").document(pid).collection("likes")
                                        .document(like.getUid()).delete();
                            }
                        }
                    }
                });
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        androidx.appcompat.app.AlertDialog dialog=builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }
}