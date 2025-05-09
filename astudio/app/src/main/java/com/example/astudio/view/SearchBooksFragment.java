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
    private static final String API_KEY = "PUT_API_HERE";
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
            String query = binding.searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                fetchSearchBooks(query);
            } else {
                Toast.makeText(getContext(), "Please enter a search query", Toast.LENGTH_SHORT).show();
            }
        });

        // If a query was passed via Bundle (from BrowseBooksFragment), auto-search.
        if (getArguments() != null && getArguments().containsKey("query")) {
            String query = getArguments().getString("query");
            binding.searchInput.setText(query);
            fetchSearchBooks(query);
        }
    }

    /**
     * Fetches books from the Google Books API based on the search query.
     * @param query The search query entered by the user.
     */
    private void fetchSearchBooks(String query) {
        GoogleBooksApi api = RetrofitClient.getInstance();
        // Fetch up to 20 results for the given query.
        api.searchBooks(query + "+printType:books", API_KEY, 21).enqueue(new Callback<BookResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookResponse> call, @NonNull Response<BookResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Book> books = new ArrayList<>();
                    for (BookResponse.Item item : response.body().items) {
                        String title = item.volumeInfo.title;
                        String thumb = (item.volumeInfo.imageLinks != null) ? item.volumeInfo.imageLinks.thumbnail : "";
                        if (!thumb.isEmpty()) {
                            thumb = thumb.replace("http://", "https://");
                        }
                        float rating = (item.volumeInfo.averageRating != null) ? item.volumeInfo.averageRating : 0f;
                        String description = (item.volumeInfo.description != null) ? item.volumeInfo.description : "";
                        String author = (item.volumeInfo.authors != null && !item.volumeInfo.authors.isEmpty())
                                ? item.volumeInfo.authors.get(0) : "Unknown author";
                        books.add(new Book(title, thumb, rating, author, description));
                    }
                    adapter.updateData(books); // Updating the adapter with the fetched data
                } else {
                    Toast.makeText(getContext(), "No books found", Toast.LENGTH_SHORT).show();
                }

                Log.d("SearchBooks", "Response code: " + response.code());
                if (response.body() != null) {
                    Log.d("SearchBooks", "Items count: " + response.body().items.size());
                } else {
                    Log.d("SearchBooks", "Response body is null");
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Search failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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