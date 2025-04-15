package com.example.astudio.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.astudio.R;
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

public class SearchBooksFragment extends Fragment {

    private FragmentSearchBooksBinding binding;
    private SearchBooksAdapter adapter;
    private static final String API_KEY = "PUT_YOUR_API_KEY_HERE";

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

        // When the search button is clicked, perform a search.
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

    private void fetchSearchBooks(String query) {
        GoogleBooksApi api = RetrofitClient.getInstance();
        // Fetch up to 20 results for the given query.
        api.searchBooks(query + "+printType:books", API_KEY, 20).enqueue(new Callback<BookResponse>() {
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

    // Adapter for displaying search results (book covers)
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

                    // Replace with your fragment container ID
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