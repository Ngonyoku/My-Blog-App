package com.ngonyoku.my_blog_app.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.ngonyoku.my_blog_app.Adapters.BlogPostRecyclerViewAdapter;
import com.ngonyoku.my_blog_app.Models.BlogPost;
import com.ngonyoku.my_blog_app.R;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private FirebaseFirestore mFirebaseFirestore;
    private DocumentSnapshot mLastVisiblePost;

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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mPosts = new ArrayList<>();
        RecyclerView blogListRecyclerView = view.findViewById(R.id.home_bloglist_recyclerView);
        mProgressBar = view.findViewById(R.id.home_progressBar);
        mAdapter = new BlogPostRecyclerViewAdapter(getActivity(), mPosts);

        if (user != null) {
            blogListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        mBlogListRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
//        mBlogListRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
            blogListRecyclerView.setHasFixedSize(true);
            blogListRecyclerView.setAdapter(mAdapter);

            /*-------------------Listen to the Scroll state---------------------------------------*/
            blogListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    boolean hasReachedBottom = !recyclerView.canScrollVertically(1);/*Check if the recyclerView has reached the Bottom*/
                    if (hasReachedBottom) {
                        mProgressBar.setVisibility(View.VISIBLE);
                        loadMorePosts();/*We load more recent posts*/
                    }
                }
            });

        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mProgressBar.setVisibility(View.VISIBLE);
        loadPosts();
    }

    /*---------------------------Loads the posts-----------------------------------------------*/
    private void loadPosts() {
        /*Sort the Posts According to Timestamp*/
        Query firstQuery = mFirebaseFirestore
                .collection(getString(R.string.collection_Posts))
                .orderBy(getString(R.string.field_timestamp), Query.Direction.DESCENDING)
                .limit(20);/*Limits to 3 Posts*/

        mProgressBar.setVisibility(View.VISIBLE);

        /*Load All the posts*/
        firstQuery
                .addSnapshotListener(new EventListener<QuerySnapshot>() {/*Retrieves all the data in Realtime*/
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        mPosts.clear();
                        if (error == null) {
                            mLastVisiblePost = value.getDocuments().get(value.size() - 1);/*Get the last Post that is visible on the Screen*/
                            for (DocumentChange docs : value.getDocumentChanges()) { /*Loops over the items*/
                                if (docs.getType() == DocumentChange.Type.ADDED) {/*Check if the data is Added*/
                                    BlogPost blogPost = docs.getDocument().toObject(BlogPost.class);
                                    mPosts.add(blogPost);
                                    mAdapter.notifyDataSetChanged();
                                    mProgressBar.setVisibility(View.GONE);
                                }
                            }
                        }
                        mProgressBar.setVisibility(View.GONE);
                    }
                })
        ;
    }

    /*-------------------------Load more recent posts ----------------------------*/
    private void loadMorePosts() {
        Query nextQuery = mFirebaseFirestore
                .collection(getString(R.string.collection_Posts))
                .orderBy(getString(R.string.field_timestamp), Query.Direction.DESCENDING)
                .startAfter(mLastVisiblePost)
                .limit(20);

        nextQuery
                .addSnapshotListener(new EventListener<QuerySnapshot>() {/*Retrieves all the data in Realtime*/
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error == null) {
                            if (!value.isEmpty()) {
                                mLastVisiblePost = value.getDocuments().get(value.size() - 1);/*Get the last Post that is visible on the Screen*/
                                for (DocumentChange docs : value.getDocumentChanges()) { /*Loops over the items*/
                                    if (docs.getType() == DocumentChange.Type.ADDED) {/*Check if the data is Added*/
                                        BlogPost blogPost = docs.getDocument().toObject(BlogPost.class);
                                        mPosts.add(blogPost);
                                        mAdapter.notifyDataSetChanged();
                                        mProgressBar.setVisibility(View.GONE);
                                    }
                                }
                            } else {
                                Toast.makeText(getContext(), "You have reached the last post", Toast.LENGTH_SHORT).show();
                            }
                        }
                        mProgressBar.setVisibility(View.GONE);
                    }
                })
        ;
    }
}