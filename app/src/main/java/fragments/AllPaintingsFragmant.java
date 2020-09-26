package fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import model.Painting;
import view_holder.PaintingViewHolder;


public class AllPaintingsFragmant extends Fragment {
    FirestoreRecyclerAdapter<Painting, PaintingViewHolder> adapter;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    RecyclerView recyclerView;

    public AllPaintingsFragmant() {

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_all_paintings_fragmant, container, false);
        db=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        recyclerView=view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }
    @Override
    public void onStart() {
        super.onStart();
        Query query=db.collection("paintings")
                .orderBy("date", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Painting> options=new FirestoreRecyclerOptions.Builder<Painting>()
                .setQuery(query,Painting.class)
                .build();
        adapter=new FirestoreRecyclerAdapter<Painting, PaintingViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final PaintingViewHolder holder, int position, @NonNull final Painting model) {
                holder.tvUserName.setText(model.getUname());
                Picasso.get().load(model.getUrl()).into(holder.imgPainting);
                holder.imgDelete.setVisibility(View.INVISIBLE);
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
        if(adapter!=null)
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
                    holder.imgLike.setImageDrawable(getActivity().getDrawable(R.drawable.ic_like_black));
                    db.collection("paintings").document(pid).collection("likes")
                            .document(mAuth.getUid()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            db.collection("paintings").document(pid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Painting painting=documentSnapshot.toObject(Painting.class);
                                    HashMap<String,Object> hashMap=new HashMap<>();
                                    hashMap.put("like",painting.getLike()-1);
                                    holder.tvLike.setText(painting.getLike()-1+"");
                                    db.collection("paintings").document(pid).update(hashMap);

                                }
                            });
                        }
                    });

                }
                else {
                    holder.imgLike.setImageDrawable(getActivity().getDrawable(R.drawable.ic_like_blue));
                    HashMap<String,Object>hashMap=new HashMap<>();
                    hashMap.put("uid",mAuth.getUid());
                    db.collection("paintings").document(pid).collection("likes")
                            .document(mAuth.getUid()).set(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
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


}