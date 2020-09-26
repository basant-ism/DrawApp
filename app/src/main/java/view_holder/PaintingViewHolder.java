package view_holder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.basant.drawapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class PaintingViewHolder extends RecyclerView.ViewHolder {

    public TextView tvUserName,tvLike;
    public ImageView imgPainting,imgLike,imgDelete;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    Context context;

    public PaintingViewHolder(Context context, @NonNull View itemView) {
        super(itemView);
        this.context=context;
        tvUserName=itemView.findViewById(R.id.tv_user_name);
        tvLike=itemView.findViewById(R.id.tv_like);
        imgPainting=itemView.findViewById(R.id.img_painting);
        imgLike=itemView.findViewById(R.id.img_like);
        imgDelete=itemView.findViewById(R.id.img_delete);
        db=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
    }
    public void setLikeButton(String pid,String uid)
    {
        db.collection("paintings").document(pid).collection("likes")
                .document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists())
                {
                    imgLike.setImageDrawable(context.getDrawable(R.drawable.ic_like_blue));
                }
                else{
                    imgLike.setImageDrawable(context.getDrawable(R.drawable.ic_like_black));
                }
            }
        });
    }
}
