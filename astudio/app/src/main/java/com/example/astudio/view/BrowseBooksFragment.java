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
import com.example.astudio.model.UserManager;
import com.example.astudio.network.GoogleBooksApi;
import com.example.astudio.network.RetrofitClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A Fragment that handles browsing and searching for books. It interacts with the Google Books API
 * to fetch top-rated and genre-specific books, and displays them in RecyclerViews.
 * This Fragment also supports searching for books based on a query.
 */
public class BrowseBooksFragment extends Fragment implements BrowseBooksUI {

    private FragmentBrowseBooksBinding binding;
    private BrowseBooksListener listener;


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

    /**
     * Called when the fragment's view is created. This method inflates the layout for the fragment.
     *
     * @param inflater The LayoutInflater object to inflate the view.
     * @param container The container that the view will be attached to.
     * @param savedInstanceState A bundle containing saved instance state, if any.
     * @return The view for the fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBrowseBooksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called after the fragment's view has been created. This method sets up UI components,
     * including RecyclerViews, search functionality, and genre buttons.
     *
     * @param view The fragment's root view.
     * @param savedInstanceState A bundle containing saved instance state, if any.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //for searching books
        binding.goButton.setOnClickListener(v -> {
            String query = binding.searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                // Navigate to SearchBooksFragment with the query as an argument.
                SearchBooksFragment searchFragment = SearchBooksFragment.newInstance(query);
                if (getActivity() instanceof ControllerActivity) {
                    ((ControllerActivity) getActivity()).mainUI.displayFragment(searchFragment);
                }
            } else {
                Toast.makeText(getContext(), "Please enter a search query", Toast.LENGTH_SHORT).show();
            }
        });

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
            if (getActivity() instanceof ControllerActivity controller) {
                controller.fetchBooksByGenre(genre, this);
            }
        });

        binding.genreButtonRecycler.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));
        binding.genreButtonRecycler.setAdapter(genreAdapter);



        // inside onViewCreated()
        if (getActivity() instanceof ControllerActivity controller) {
            controller.fetchWelcomeMessage(this);
            controller.fetchTopRatedBooks(this);  // 'this' is BrowseBooksFragment implementing BrowseBooksUI
            controller.fetchBooksByGenre("Fiction", this);
        }
    }

    @Override
    public void displayWelcomeMessage(String welcomeText) {
        binding.welcomeMessage.setText(welcomeText);
    }



    @Override
    public void setListener(BrowseBooksListener listener) {
        this.listener = listener;
    }

    @Override
    public void updateHotBooks(List<Book> books) {
        hotBookAdapter.updateData(books);
    }

    @Override
    public void updateGenreBooks(List<Book> books) {
        genreBookAdapter.updateData(books);
    }

    @Override
    public View getRootView() {
        return binding != null ? binding.getRoot() : null;
    }


    /**
     * Adapter class for displaying hot books in a horizontal RecyclerView. Each item represents a book
     * with its cover image, rating, and click functionality.
     */
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

    /**
     * Adapter class for displaying genre buttons in a horizontal RecyclerView. Each button represents
     * a genre that the user can click to fetch books from that genre.
     */

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

    /**
     * Adapter class for displaying books of a specific genre in a grid RecyclerView. Each item represents
     * a book with its cover image, rating, and click functionality.
     */
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

