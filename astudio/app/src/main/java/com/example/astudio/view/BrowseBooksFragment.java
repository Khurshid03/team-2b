package com.example.astudio.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.astudio.R;
import com.example.astudio.controller.ControllerActivity;
import com.example.astudio.databinding.FragmentBrowseBooksBinding;
import com.example.astudio.databinding.ItemHotBookBinding;
import com.example.astudio.model.Book;
import com.example.astudio.model.BookResponse;
import com.example.astudio.network.GoogleBooksApi;
import com.example.astudio.network.RetrofitClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BrowseBooksFragment extends Fragment implements BrowseBooksUI {

    private FragmentBrowseBooksBinding binding;
    private BrowseBooksListener listener;

    // Replace "YOUR_API_KEY" with your actual key or load it from configuration.
    private static final String API_KEY = "PUT_YOUR_API_HERE";

    // Instantiate the adapter with a lambda that delegates to the activity via the listener
    private final HotBookAdapter hotBookAdapter = new HotBookAdapter(book -> {
        if (getActivity() instanceof ControllerActivity) {
            ((ControllerActivity) getActivity()).onBookSelected(book);
        }
    });

    // Replace the existing initialization of genreBookAdapter with:
    private final GenreBookAdapter genreBookAdapter = new GenreBookAdapter(book -> {
        if (getActivity() instanceof ControllerActivity) {
            ((ControllerActivity) getActivity()).onBookSelected(book);
        }
    });
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBrowseBooksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up RecyclerView for hot books (horizontal)
        binding.hotBooksRecycler.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));
        binding.hotBooksRecycler.setAdapter(hotBookAdapter);

        // Set up RecyclerView for genre books (grid)
        binding.genreBooksRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));
        binding.genreBooksRecycler.setAdapter(genreBookAdapter);

        // Set up horizontal RecyclerView for genre buttons
        List<String> genres = Arrays.asList("Mystery", "Fiction", "Romance", "History", "Fantasy", "Science");
        GenreAdapter genreAdapter = new GenreAdapter(genres, v -> {
            String genre = ((Button) v).getText().toString();
            fetchBooksByGenre(genre);
        });
        binding.genreButtonRecycler.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));
        binding.genreButtonRecycler.setAdapter(genreAdapter);

        fetchTopRatedBooks();
        fetchBooksByGenre("Fiction");
    }

    private void fetchTopRatedBooks() {
        GoogleBooksApi api = RetrofitClient.getInstance();
        api.searchBooks("top rated fiction", API_KEY, 10).enqueue(new Callback<BookResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookResponse> call, @NonNull Response<BookResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Book> books = new ArrayList<>();
                    for (BookResponse.Item item : response.body().items) {
                        String title = item.volumeInfo.title;
                        String thumb = item.volumeInfo.imageLinks != null
                                ? item.volumeInfo.imageLinks.thumbnail : "";
                        if (!thumb.isEmpty()) {
                            thumb = thumb.replace("http://", "https://");
                        }
                        float rating = item.volumeInfo.averageRating != null ? item.volumeInfo.averageRating : 0f;
                        String author = (item.volumeInfo.authors != null && !item.volumeInfo.authors.isEmpty())
                                ? item.volumeInfo.authors.get(0) : "Unknown Author";
                        String description = item.volumeInfo.description != null ? item.volumeInfo.description : "No description available";
                        books.add(new Book(title, thumb, rating, author, description));
                    }
                    hotBookAdapter.updateData(books);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Failed to load hot books", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchBooksByGenre(String genre) {
        GoogleBooksApi api = RetrofitClient.getInstance();
        api.searchBooks(genre, API_KEY, 12).enqueue(new Callback<BookResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookResponse> call, @NonNull Response<BookResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Book> books = new ArrayList<>();
                    for (BookResponse.Item item : response.body().items) {
                        String title = item.volumeInfo.title;
                        String thumb = item.volumeInfo.imageLinks != null
                                ? item.volumeInfo.imageLinks.thumbnail : "";
                        if (!thumb.isEmpty()) {
                            thumb = thumb.replace("http://", "https://");
                        }
                        float rating = item.volumeInfo.averageRating != null ? item.volumeInfo.averageRating : 0f;
                        String author = (item.volumeInfo.authors != null && !item.volumeInfo.authors.isEmpty())
                                ? item.volumeInfo.authors.get(0) : "Unknown Author";
                        String description = item.volumeInfo.description != null ? item.volumeInfo.description : "No description available";
                        books.add(new Book(title, thumb, rating, author, description));
                    }
                    genreBookAdapter.updateData(books);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Failed to load genre books", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void setListener(BrowseBooksListener listener) {
        this.listener = listener;
    }

    @Override
    public void updateHotBooks(List<Book> books) {
        // Implement if needed
    }

    @Override
    public void updateGenreBooks(List<Book> books) {
        // Implement if needed
    }

    // Inner adapter for hot books
    public static class HotBookAdapter extends RecyclerView.Adapter<HotBookAdapter.HotBookViewHolder> {

        private List<Book> books = new ArrayList<>();
        private final BrowseBooksUI.OnHotBookClickListener listener;

        public HotBookAdapter(BrowseBooksUI.OnHotBookClickListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public HotBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemHotBookBinding binding = ItemHotBookBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new HotBookViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull HotBookViewHolder holder, int position) {
            Book book = books.get(position);
            holder.bind(book, listener);
        }

        @Override
        public int getItemCount() {
            return books.size();
        }

        public void updateData(List<Book> books) {
            this.books = books;
            notifyDataSetChanged();
        }

        static class HotBookViewHolder extends RecyclerView.ViewHolder {
            private final ImageView bookCoverImage;
            private final RatingBar bookRating;

            public HotBookViewHolder(ItemHotBookBinding binding) {
                super(binding.getRoot());
                bookCoverImage = binding.bookCoverImage;
                bookRating = binding.bookRating;
            }

            public void bind(Book book, BrowseBooksUI.OnHotBookClickListener listener) {
                Glide.with(itemView.getContext())
                        .load(book.getThumbnailUrl())
                        .placeholder(R.drawable.placeholder_cover)
                        .into(bookCoverImage);
                bookRating.setRating(book.getRating());
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onHotBookClick(book);
                    }
                });
            }
        }
    }

    // Inner adapter for genre buttons
    private static class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.GenreViewHolder> {
        private final List<String> genres;
        private final View.OnClickListener listener;

        public GenreAdapter(List<String> genres, View.OnClickListener listener) {
            this.genres = genres;
            this.listener = listener;
        }

        @NonNull
        @Override
        public GenreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_genre_button, parent, false);
            return new GenreViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GenreViewHolder holder, int position) {
            holder.button.setText(genres.get(position));
            holder.button.setOnClickListener(listener);
        }

        @Override
        public int getItemCount() {
            return genres.size();
        }

        static class GenreViewHolder extends RecyclerView.ViewHolder {
            Button button;

            public GenreViewHolder(@NonNull View itemView) {
                super(itemView);
                button = itemView.findViewById(R.id.genreButton);
            }
        }
    }

    // Inner adapter for genre books (grid view)
    private static class GenreBookAdapter extends RecyclerView.Adapter<GenreBookAdapter.GenreBookViewHolder> {
        private List<Book> books = new ArrayList<>();
        // Add a listener field to handle click events
        private final BrowseBooksUI.OnGenreBookClickListener listener;

        // Update the constructor to accept a listener
        public GenreBookAdapter(BrowseBooksUI.OnGenreBookClickListener listener) {
            this.listener = listener;
        }

        public void updateData(List<Book> books) {
            this.books = books;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public GenreBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_genre_book, parent, false);
            return new GenreBookViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GenreBookViewHolder holder, int position) {
            Book book = books.get(position);
            // Instead of just loading the image, call bind() to set the click listener.
            holder.bind(book, listener);
        }

        @Override
        public int getItemCount() {
            return books.size();
        }

        static class GenreBookViewHolder extends RecyclerView.ViewHolder {
            ImageView bookCoverImage;
            RatingBar bookRating;

            public GenreBookViewHolder(@NonNull View itemView) {
                super(itemView);
                bookCoverImage = itemView.findViewById(R.id.genreBookCover);
                // Make sure that your item_genre_book.xml defines a RatingBar with the id "genreBookRating"
                bookRating = itemView.findViewById(R.id.genreBookRating);
            }

            public void bind(Book book, BrowseBooksUI.OnGenreBookClickListener listener) {
                Glide.with(itemView.getContext())
                        .load(book.getThumbnailUrl())
                        .placeholder(R.drawable.placeholder_cover)
                        .into(bookCoverImage);
                bookRating.setRating(book.getRating());
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onGenreBookClick(book);
                    }
                });
            }
        }
    }
}

