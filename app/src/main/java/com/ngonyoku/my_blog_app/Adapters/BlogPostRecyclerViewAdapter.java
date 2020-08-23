package com.ngonyoku.my_blog_app.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ngonyoku.my_blog_app.Models.BlogPost;
import com.ngonyoku.my_blog_app.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogPostRecyclerViewAdapter extends RecyclerView.Adapter<BlogPostRecyclerViewAdapter.BlogPostViewHolder> {
    private List<BlogPost> mPosts;

    public BlogPostRecyclerViewAdapter(List<BlogPost> posts) {
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
    public void onBindViewHolder(@NonNull BlogPostViewHolder holder, int position) {
        BlogPost currentPost = mPosts.get(position);
        holder.mPostDescription.setText(currentPost.getDescription());
        Picasso
                .get()
                .load(currentPost.getImage_url())
                .fit()
                .placeholder(R.color.colorPictureGray)
                .centerCrop()
                .into(holder.mPostImage)
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
            mUsername = itemView.findViewById(R.id.blogpost_username);
            mPostDescription = itemView.findViewById(R.id.blogpost_description);
        }
    }
}
