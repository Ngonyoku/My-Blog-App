package com.ngonyoku.my_blog_app.Fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.ngonyoku.my_blog_app.Adapters.BlogPostRecyclerViewAdapter;
import com.ngonyoku.my_blog_app.Models.BlogPost;
import com.ngonyoku.my_blog_app.R;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private FirebaseFirestore mFirebaseFirestore;
    private RecyclerView mBlogListRecyclerView;
    private List<BlogPost> mPosts;
    private BlogPostRecyclerViewAdapter mAdapter;
    private ProgressBar mProgressBar;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mPosts = new ArrayList<>();
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mBlogListRecyclerView = view.findViewById(R.id.home_bloglist_recyclerView);
        mProgressBar = view.findViewById(R.id.home_progressBar);
        mAdapter = new BlogPostRecyclerViewAdapter(mPosts);

        mBlogListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        mBlogListRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
//        mBlogListRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mBlogListRecyclerView.setHasFixedSize(true);
        mBlogListRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mProgressBar.setVisibility(View.VISIBLE);
        mFirebaseFirestore
                .collection(getString(R.string.collection_Posts))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {/*Retrieves all the data in Realtime*/
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        mPosts.clear();
                        for (DocumentChange docs : value.getDocumentChanges()) { /*Loops over the items*/
                            if (docs.getType() == DocumentChange.Type.ADDED) {/*Check if the data is Added*/
                                BlogPost blogPost = docs.getDocument().toObject(BlogPost.class);
                                mPosts.add(blogPost);
                                mAdapter.notifyDataSetChanged();
                                mProgressBar.setVisibility(View.GONE);
                            }
                        }
                        mProgressBar.setVisibility(View.GONE);
                    }
                })
        ;
    }
}