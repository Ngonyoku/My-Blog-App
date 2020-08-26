package com.ngonyoku.my_blog_app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ngonyoku.my_blog_app.Models.BlogPost;
import com.ngonyoku.my_blog_app.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogPostRecyclerViewAdapter extends RecyclerView.Adapter<BlogPostRecyclerViewAdapter.BlogPostViewHolder> {
    private List<BlogPost> mPosts;
    private Context mContext;

    public BlogPostRecyclerViewAdapter(Context context, List<BlogPost> posts) {
        this.mContext = context;
        mPosts = posts;
    }

    @NonNull
    @Override
    public BlogPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BlogPostViewHolder(
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.item_blogpost, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull final BlogPostViewHolder holder, int position) {
        final BlogPost currentPost = mPosts.get(position);
        String userId = currentPost.getUser_id();

        FirebaseFirestore
                .getInstance()
                .collection("Users")
                .document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            String username = task.getResult().getString("username");
                            final String imageUrl = task.getResult().getString("profile_image_url");

                            holder.mUsername.setText(username);
                            Picasso
                                    .get()
                                    .load(imageUrl)
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .fit()
                                    .placeholder(R.color.colorPictureGray)
                                    .centerCrop()
                                    .into(holder.mUserImage, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            Picasso.get().load(imageUrl).fit().placeholder(R.color.colorPictureGray).centerCrop().into(holder.mUserImage);
                                        }
                                    })
                            ;
                        } else {
                            Toast.makeText(mContext, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
        ;
        holder.mPostDescription.setText(currentPost.getDescription());
        holder.mTimestamp.setText(currentPost.getTimestamp());

        Picasso
                .get()
                .load(currentPost.getThumbnail_url())
                .networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.color.colorPictureGray)
                .into(holder.mPostImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(currentPost.getImage_url()).fit().placeholder(R.color.colorPictureGray).centerCrop().into(holder.mPostImage);
                    }
                })
        ;
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    static class BlogPostViewHolder extends RecyclerView.ViewHolder {
        private TextView mUsername, mTimestamp, mPostDescription;
        private ImageView mPostImage;
        private CircleImageView mUserImage;

        public BlogPostViewHolder(@NonNull View itemView) {
            super(itemView);

            mPostImage = itemView.findViewById(R.id.blogpost_image);
            mUserImage = itemView.findViewById(R.id.blogpost_user_image);
            mUsername = itemView.findViewById(R.id.blogpost_username);
            mPostDescription = itemView.findViewById(R.id.blogpost_description);
            mTimestamp = itemView.findViewById(R.id.blogpost_timestamp);
            mUsername = itemView.findViewById(R.id.blogpost_username);
        }
    }
}
