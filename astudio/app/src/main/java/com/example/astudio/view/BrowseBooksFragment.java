package com.example.astudio.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// Removed android.widget.Button, ImageView, RatingBar as they will be accessed via binding
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
import com.example.astudio.databinding.ItemHotBookBinding; // Import for HotBookAdapter
import com.example.astudio.databinding.ItemGenreButtonBinding; // Import for GenreAdapter
import com.example.astudio.databinding.ItemGenreBookBinding; // Import for GenreBookAdapter
import com.example.astudio.model.Book;
// Unused imports like BookResponse, UserManager, GoogleBooksApi, RetrofitClient, FirebaseAuth, FirebaseFirestore can be removed if not directly used in this fragment.
// For this refactoring, they are not directly used in the fragment code itself.

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A Fragment that handles browsing and searching for books. It interacts with the ControllerActivity
 * to fetch top-rated and genre-specific books, and displays them in RecyclerViews.
 * This Fragment also supports searching for books based on a query.
 * ViewBinding is used throughout, including in RecyclerView Adapters.
 */
public class BrowseBooksFragment extends Fragment implements BrowseBooksUI {

    private FragmentBrowseBooksBinding binding; // ViewBinding for the fragment's layout
    private BrowseBooksListener listener; // Listener for communication with ControllerActivity

    private final HotBookAdapter hotBookAdapter = new HotBookAdapter(book -> {
        // The listener is already set via setListener, or directly call controller if preferred
        if (listener != null) {
            listener.onBookSelected(book);
        } else if (getActivity() instanceof ControllerActivity) { // Fallback, though listener should be set
            ((ControllerActivity) getActivity()).onBookSelected(book);
        }
    });

    private final GenreBookAdapter genreBookAdapter = new GenreBookAdapter(book -> {
        if (listener != null) {
            listener.onBookSelected(book);
        } else if (getActivity() instanceof ControllerActivity) { // Fallback
            ((ControllerActivity) getActivity()).onBookSelected(book);
        }
    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment using ViewBinding
        binding = FragmentBrowseBooksBinding.inflate(inflater, container, false);
        return binding.getRoot(); // Return the root view from the binding
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // It's good practice to set the listener if the activity implements it
        if (getActivity() instanceof BrowseBooksListener) {
            setListener((BrowseBooksListener) getActivity());
        }

        binding.goButton.setOnClickListener(v -> {
            String query = binding.searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                SearchBooksFragment searchFragment = SearchBooksFragment.newInstance(query);
                if (getActivity() instanceof ControllerActivity) {
                    ((ControllerActivity) getActivity()).mainUI.displayFragment(searchFragment);
                }
            } else {
                Toast.makeText(getContext(), "Please enter a search query", Toast.LENGTH_SHORT).show();
            }
        });

        binding.hotBooksRecycler.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));
        binding.hotBooksRecycler.setAdapter(hotBookAdapter);

        binding.genreBooksRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));
        binding.genreBooksRecycler.setAdapter(genreBookAdapter);

        List<String> genres = Arrays.asList("Mystery", "Fiction", "Romance", "History", "Fantasy", "Science");
        GenreAdapter genreAdapter = new GenreAdapter(genres, genreName -> {
            // The genreName is passed from the ViewHolder's click listener
            if (listener != null) {
                listener.onGenreSelected(genreName); // Notify controller/activity
            }
            // The actual fetching is now initiated from onGenreSelected in ControllerActivity
            // which then calls back to this fragment's updateGenreBooks if needed.
        });
        binding.genreButtonRecycler.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));
        binding.genreButtonRecycler.setAdapter(genreAdapter);

        if (getActivity() instanceof ControllerActivity controller) {
            controller.fetchWelcomeMessage(this);
            controller.fetchTopRatedBooks(this);
            controller.fetchBooksByGenre("Fiction", this); // Initial load for "Fiction"
        }
    }

    @Override
    public void displayWelcomeMessage(String welcomeText) {
        if (binding != null) {
            binding.welcomeMessage.setText(welcomeText);
        }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Release binding to prevent memory leaks
    }

    /**
     * Adapter for "Hot Books" RecyclerView.
     */
    public static class HotBookAdapter extends RecyclerView.Adapter<HotBookAdapter.HotBookViewHolder> {
        private List<Book> books = new ArrayList<>();
        private final BrowseBooksUI.OnHotBookClickListener clickListener;

        public HotBookAdapter(BrowseBooksUI.OnHotBookClickListener listener) {
            this.clickListener = listener;
        }

        @NonNull
        @Override
        public HotBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate using ItemHotBookBinding
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ItemHotBookBinding itemBinding = ItemHotBookBinding.inflate(inflater, parent, false);
            return new HotBookViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull HotBookViewHolder holder, int position) {
            Book book = books.get(position);
            holder.bind(book, clickListener);
        }

        @Override
        public int getItemCount() {
            return books.size();
        }

        public void updateData(List<Book> newBooks) {
            this.books = newBooks != null ? newBooks : new ArrayList<>();
            notifyDataSetChanged();
        }

        /**
         * ViewHolder for "Hot Books". Uses ItemHotBookBinding.
         */
        static class HotBookViewHolder extends RecyclerView.ViewHolder {
            private final ItemHotBookBinding binding; // Store the binding

            public HotBookViewHolder(@NonNull ItemHotBookBinding itemBinding) {
                super(itemBinding.getRoot());
                this.binding = itemBinding; // Assign it
            }

            public void bind(final Book book, final BrowseBooksUI.OnHotBookClickListener listener) {
                Glide.with(itemView.getContext())
                        .load(book.getThumbnailUrl())
                        .placeholder(R.drawable.placeholder_cover)
                        .error(R.drawable.placeholder_cover) // Add error placeholder
                        .into(binding.bookCoverImage); // Use binding
                binding.bookRating.setRating(book.getRating()); // Use binding
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onHotBookClick(book);
                    }
                });
            }
        }
    }

    /**
     * Listener interface for genre button clicks.
     */
    public interface OnGenreButtonClickListener {
        void onGenreButtonClick(String genreName);
    }


    /**
     * Adapter for genre buttons RecyclerView.
     */
    private static class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.GenreViewHolder> {
        private final List<String> genres;
        private final OnGenreButtonClickListener clickListener; // Changed to a more specific listener

        public GenreAdapter(List<String> genres, OnGenreButtonClickListener listener) {
            this.genres = genres;
            this.clickListener = listener;
        }

        @NonNull
        @Override
        public GenreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate using ItemGenreButtonBinding
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ItemGenreButtonBinding itemBinding = ItemGenreButtonBinding.inflate(inflater, parent, false);
            return new GenreViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull GenreViewHolder holder, int position) {
            String genreName = genres.get(position);
            holder.bind(genreName, clickListener);
        }

        @Override
        public int getItemCount() {
            return genres.size();
        }

        /**
         * ViewHolder for Genre Buttons. Uses ItemGenreButtonBinding.
         */
        static class GenreViewHolder extends RecyclerView.ViewHolder {
            private final ItemGenreButtonBinding binding; // Store the binding

            public GenreViewHolder(@NonNull ItemGenreButtonBinding itemBinding) {
                super(itemBinding.getRoot());
                this.binding = itemBinding; // Assign it
            }

            public void bind(final String genreName, final OnGenreButtonClickListener listener) {
                binding.genreButton.setText(genreName); // Use binding
                binding.genreButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onGenreButtonClick(genreName);
                    }
                });
            }
        }
    }

    /**
     * Adapter for "Genre Books" RecyclerView.
     */
    private static class GenreBookAdapter extends RecyclerView.Adapter<GenreBookAdapter.GenreBookViewHolder> {
        private List<Book> books = new ArrayList<>();
        private final BrowseBooksUI.OnGenreBookClickListener clickListener;

        public GenreBookAdapter(BrowseBooksUI.OnGenreBookClickListener listener) {
            this.clickListener = listener;
        }

        public void updateData(List<Book> newBooks) {
            this.books = newBooks != null ? newBooks : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public GenreBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate using ItemGenreBookBinding
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ItemGenreBookBinding itemBinding = ItemGenreBookBinding.inflate(inflater, parent, false);
            return new GenreBookViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull GenreBookViewHolder holder, int position) {
            Book book = books.get(position);
            holder.bind(book, clickListener);
        }

        @Override
        public int getItemCount() {
            return books.size();
        }

        /**
         * ViewHolder for "Genre Books". Uses ItemGenreBookBinding.
         */
        static class GenreBookViewHolder extends RecyclerView.ViewHolder {
            private final ItemGenreBookBinding binding; // Store the binding

            public GenreBookViewHolder(@NonNull ItemGenreBookBinding itemBinding) {
                super(itemBinding.getRoot());
                this.binding = itemBinding; // Assign it
            }

            public void bind(final Book book, final BrowseBooksUI.OnGenreBookClickListener listener) {
                Glide.with(itemView.getContext())
                        .load(book.getThumbnailUrl())
                        .placeholder(R.drawable.placeholder_cover)
                        .error(R.drawable.placeholder_cover) // Add error placeholder
                        .into(binding.genreBookCover); // Use binding
                binding.genreBookRating.setRating(book.getRating()); // Use binding
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onGenreBookClick(book);
                    }
                });
            }
        }
    }
}


