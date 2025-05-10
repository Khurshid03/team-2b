package com.example.astudio.view;

import android.os.Bundle;
import android.util.Log; // Import Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// ImageView import is no longer needed if accessed only via binding
// import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity; // For casting context
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.astudio.R;
import com.example.astudio.controller.ControllerActivity;
import com.example.astudio.databinding.FragmentSearchBooksBinding; // Binding for the fragment
import com.example.astudio.databinding.ItemGenreBookBinding; // Binding for the item layout (assuming it's reused)
import com.example.astudio.model.Book;
// Unused imports like BookResponse, GoogleBooksApi, RetrofitClient, Call, Callback, Response can be removed
// as API calls are handled by ControllerActivity/GoogleApiFacade.

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for searching books using the Google Books API via ControllerActivity.
 * It allows users to enter a search query and displays the results in a grid format.
 * This fragment implements the SearchBooksUI interface to handle book search events.
 * ViewBinding is used throughout, including in the RecyclerView Adapter.
 */
public class SearchBooksFragment extends Fragment implements SearchBooksUI {

    private static final String FRAGMENT_TAG = "SearchBooksFragment"; // For logging
    private FragmentSearchBooksBinding binding; // ViewBinding for the fragment's layout
    private SearchBooksAdapter adapter;
    // private MainUI mainUI; // MainUI is typically managed by ControllerActivity, direct reference might not be needed here

    public SearchBooksFragment() {
        // Required empty public constructor.
    }

    /**
     * Factory method to create a new instance of SearchBooksFragment using the provided query.
     * @param query The initial search query.
     * @return A new instance of SearchBooksFragment.
     */
    public static SearchBooksFragment newInstance(String query) {
        SearchBooksFragment fragment = new SearchBooksFragment();
        Bundle args = new Bundle();
        args.putString("query", query);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment using ViewBinding
        binding = FragmentSearchBooksBinding.inflate(inflater, container, false);
        Log.d(FRAGMENT_TAG, "onCreateView called and binding inflated.");
        return binding.getRoot(); // Return the root view from the binding
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(FRAGMENT_TAG, "onViewCreated called.");

        // Setup RecyclerView to display search results in a grid.
        binding.searchBooksRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new SearchBooksAdapter(); // Initializing the adapter
        binding.searchBooksRecycler.setAdapter(adapter);
        Log.d(FRAGMENT_TAG, "RecyclerView and Adapter initialized.");

        // Back button implementation: navigate to the home page (BrowseBooksFragment)
        binding.backButton.setOnClickListener(v -> {
            Log.d(FRAGMENT_TAG, "Back button clicked.");
            BrowseBooksFragment browseFragment = new BrowseBooksFragment();
            if (getActivity() instanceof ControllerActivity) {
                // Use the displayFragment method from ControllerActivity's MainUI instance
                ((ControllerActivity) getActivity()).mainUI.displayFragment(browseFragment);
            } else if (getActivity() != null) {
                // Fallback if activity is not ControllerActivity (less ideal)
                Log.w(FRAGMENT_TAG, "Activity is not ControllerActivity, using direct fragment transaction.");
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainerView, browseFragment) // Ensure this ID is correct
                        .commit();
            } else {
                Log.e(FRAGMENT_TAG, "getActivity() is null, cannot navigate back.");
            }
        });

        /**
         * Search button implementation: fetch books based on the search query.
         */
        binding.goButton.setOnClickListener(v -> {
            String query = binding.searchInput.getText().toString().trim();
            Log.d(FRAGMENT_TAG, "Search button clicked with query: '" + query + "'");
            if (query.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a search query", Toast.LENGTH_SHORT).show();
            } else if (getActivity() instanceof ControllerActivity) {
                ((ControllerActivity) getActivity()).fetchSearchBooks(query, this); // 'this' implements SearchBooksUI
            } else {
                Log.e(FRAGMENT_TAG, "Search button: getActivity() is not ControllerActivity or is null.");
            }
        });

        // Auto-run the initial "query" argument if provided
        if (getArguments() != null && getArguments().containsKey("query")) {
            String initialQuery = getArguments().getString("query");
            if (initialQuery != null && !initialQuery.isEmpty()) {
                binding.searchInput.setText(initialQuery);
                Log.d(FRAGMENT_TAG, "Initial query from arguments: '" + initialQuery + "'. Performing search.");
                if (getActivity() instanceof ControllerActivity) {
                    ((ControllerActivity) getActivity()).fetchSearchBooks(initialQuery, this);
                } else {
                    Log.e(FRAGMENT_TAG, "Initial query: getActivity() is not ControllerActivity or is null.");
                }
            }
        }
    }

    @Override
    public void onSearchBooksSuccess(List<Book> books) {
        if (!isAdded() || binding == null) { // Check if fragment is still active
            Log.w(FRAGMENT_TAG, "onSearchBooksSuccess called but fragment not added or binding is null.");
            return;
        }
        Log.i(FRAGMENT_TAG, "onSearchBooksSuccess: Received " + (books != null ? books.size() : "null") + " books.");
        if (adapter != null) {
            adapter.updateData(books);
        } else {
            Log.e(FRAGMENT_TAG, "onSearchBooksSuccess: Adapter is null.");
        }
    }

    @Override
    public void onSearchBooksFailure(String errorMessage) {
        if (!isAdded()) { // Check if fragment is still active
            Log.w(FRAGMENT_TAG, "onSearchBooksFailure called but fragment not added.");
            return;
        }
        Log.e(FRAGMENT_TAG, "onSearchBooksFailure: " + errorMessage);
        Toast.makeText(getContext(), "Search failed: " + errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(FRAGMENT_TAG, "onDestroyView called.");
        binding = null; // Release binding to prevent memory leaks
    }

    /**
     * Adapter for displaying search results in a RecyclerView.
     * Uses ItemGenreBookBinding assuming R.layout.item_genre_book is reused.
     */
    public static class SearchBooksAdapter extends RecyclerView.Adapter<SearchBooksAdapter.SearchBookViewHolder> {
        private List<Book> books = new ArrayList<>();

        public void updateData(List<Book> newBooks) {
            this.books = (newBooks != null) ? newBooks : new ArrayList<>();
            notifyDataSetChanged(); // Consider DiffUtil for better performance
        }

        @NonNull
        @Override
        public SearchBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate using ItemGenreBookBinding
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            // Assuming item_genre_book.xml is used for search results as well
            ItemGenreBookBinding itemBinding = ItemGenreBookBinding.inflate(inflater, parent, false);
            return new SearchBookViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchBookViewHolder holder, int position) {
            Book book = books.get(position);
            holder.bind(book);
        }

        @Override
        public int getItemCount() {
            return books.size();
        }

        /**
         * ViewHolder for displaying individual book items in search results.
         * Uses ItemGenreBookBinding.
         */
        static class SearchBookViewHolder extends RecyclerView.ViewHolder {
            private final ItemGenreBookBinding binding; // Store the binding object

            public SearchBookViewHolder(@NonNull ItemGenreBookBinding itemBinding) {
                super(itemBinding.getRoot());
                this.binding = itemBinding; // Assign the binding object
            }

            public void bind(final Book book) {
                if (book == null) return;

                // Access views via the binding object
                // Assuming ItemGenreBookBinding has 'genreBookCover' and 'genreBookRating'
                Glide.with(itemView.getContext()) // itemView.getContext() is same as binding.getRoot().getContext()
                        .load(book.getThumbnailUrl())
                        .placeholder(R.drawable.placeholder_cover) // Ensure this drawable exists
                        .error(R.drawable.placeholder_cover)       // Fallback image on error
                        .into(binding.genreBookCover);

                // If your item_genre_book.xml has a rating bar, you can set it:
                if (binding.genreBookRating != null) { // Check if the rating bar exists in the layout
                    binding.genreBookRating.setRating(book.getRating());
                } else {
                    // Log if rating bar is unexpectedly missing from the reused layout
                    Log.w(FRAGMENT_TAG, "SearchBookViewHolder: genreBookRating view not found in ItemGenreBookBinding. Rating will not be displayed.");
                }


                itemView.setOnClickListener(v -> {
                    Log.d(FRAGMENT_TAG, "Book clicked in search results: " + book.getTitle());
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("book", book);
                    ViewBookFragment viewBookFragment = new ViewBookFragment(); // Use specific fragment type
                    viewBookFragment.setArguments(bundle);

                    // Get FragmentManager from the context of the item view (which is the activity)
                    if (v.getContext() instanceof FragmentActivity) {
                        ((FragmentActivity) v.getContext()).getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragmentContainerView, viewBookFragment) // Ensure this ID is correct
                                .addToBackStack(null) // Add to back stack to allow returning to search results
                                .commit();
                    } else {
                        Log.e(FRAGMENT_TAG, "itemView.getContext() is not a FragmentActivity. Cannot navigate.");
                    }
                });
            }
        }
    }
}


