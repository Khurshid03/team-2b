package com.example.astudio.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.astudio.R;
import com.example.astudio.controller.ControllerActivity;
import com.example.astudio.databinding.FragmentSearchBooksBinding;
import com.example.astudio.model.Book;
import com.example.astudio.model.BookResponse;
import com.example.astudio.network.GoogleBooksApi;
import com.example.astudio.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment for searching books using the Google Books API.
 * It allows users to enter a search query and displays the results in a grid format.
 * This fragment implements the SearchBooksUI interface to handle book search events.
 */
public class SearchBooksFragment extends Fragment implements SearchBooksUI {

    private FragmentSearchBooksBinding binding;
    private SearchBooksAdapter adapter;
    private MainUI mainUI;

    public SearchBooksFragment() {
        // Required empty public constructor.
    }

    /**
     * Factory method to create a new instance of SearchBooksFragment using the provided query.
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
        binding = FragmentSearchBooksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup RecyclerView to display search results in a grid.
        binding.searchBooksRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new SearchBooksAdapter(); // Initializing the adapter
        binding.searchBooksRecycler.setAdapter(adapter);

        // Back button implementation: navigate to the home page (BrowseBooksFragment)
        binding.backButton.setOnClickListener(v -> {
            // Create a new instance of your home page fragment.
            BrowseBooksFragment browseFragment = new BrowseBooksFragment();

            if (getActivity() instanceof ControllerActivity) {
                ((ControllerActivity) getActivity()).mainUI.displayFragment(browseFragment);
            } else {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainerView, browseFragment)
                            .commit();
                }
            }
        });

        /**
         * Search button implementation: fetch books based on the search query.
         * When the search button is clicked, it retrieves the text from the input field,
         * validates it, and calls the fetchSearchBooks method to perform the search.
         */
        binding.goButton.setOnClickListener(v -> {
            String q = binding.searchInput.getText().toString().trim();
            if (q.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a search query", Toast.LENGTH_SHORT).show();
            } else if (getActivity() instanceof ControllerActivity) {
                ((ControllerActivity) getActivity()).fetchSearchBooks(q, this);
            }
        });

// And if you auto‐run the initial “query” argument:
        if (getArguments()!=null && getArguments().containsKey("query")) {
            String q = getArguments().getString("query");
            binding.searchInput.setText(q);
            ((ControllerActivity)getActivity()).fetchSearchBooks(q, this);
        }
    }

    @Override
    public void onSearchBooksSuccess(List<Book> books) {
        // Handle the successful book search results, possibly by updating the UI
        adapter.updateData(books);
    }

    @Override
    public void onSearchBooksFailure(String errorMessage) {
        // Handle failure in book search
        Toast.makeText(getContext(), "Search failed: " + errorMessage, Toast.LENGTH_SHORT).show();
    }

    /**
     * Adapter for displaying search results in a RecyclerView.
     */
    public static class SearchBooksAdapter extends RecyclerView.Adapter<SearchBooksAdapter.SearchBookViewHolder> {
        private List<Book> books = new ArrayList<>();

        public void updateData(List<Book> books) {
            this.books = books;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public SearchBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_genre_book, parent, false);
            return new SearchBookViewHolder(view);
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
         * ViewHolder for displaying individual book items.
         */
        static class SearchBookViewHolder extends RecyclerView.ViewHolder {
            private final ImageView bookCoverImage;

            public SearchBookViewHolder(@NonNull View itemView) {
                super(itemView);
                bookCoverImage = itemView.findViewById(R.id.genreBookCover);
            }

            public void bind(Book book) {
                Glide.with(itemView.getContext())
                        .load(book.getThumbnailUrl())
                        .placeholder(R.drawable.placeholder_cover)
                        .into(bookCoverImage);

                itemView.setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("book", book);
                    Fragment fragment = new ViewBookFragment();
                    fragment.setArguments(bundle);

                    ((FragmentActivity) v.getContext()).getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainerView, fragment)
                            .addToBackStack(null)
                            .commit();
                });
            }
        }
    }
}