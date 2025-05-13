package edu.vassar.litlore.view;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import edu.vassar.litlore.R;
import edu.vassar.litlore.controller.ControllerActivity;
import edu.vassar.litlore.databinding.FragmentBrowseBooksBinding;
import edu.vassar.litlore.databinding.ItemHotBookBinding;
import edu.vassar.litlore.databinding.ItemGenreButtonBinding;
import edu.vassar.litlore.databinding.ItemGenreBookBinding;
import edu.vassar.litlore.model.Book;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A Fragment that handles browsing books.
 * Displays top-rated books, genre-specific books, and allows navigation to search and other sections.
 * Includes a factory method to be instantiated with a specific genre.
 */
public class BrowseBooksFragment extends Fragment implements BrowseBooksUI {

    private static final String FRAGMENT_TAG = "BrowseBooksFragment";
    private static final String ARG_SELECTED_GENRE = "selected_genre";

    private FragmentBrowseBooksBinding binding;
    private BrowseBooksListener listener;

    private HotBookAdapter hotBookAdapter;
    private GenreBookAdapter genreBookAdapter;
    private GenreAdapter genreButtonsAdapter;

    private String initialGenreToLoad = "Fiction"; // Default genre

    /**
     * Required empty public constructor.
     */
    public BrowseBooksFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method to create a new instance of this fragment with a specific genre pre-selected.
     *
     * @param genre The genre to pre-select and load books for.
     * @return A new instance of fragment BrowseBooksFragment.
     */
    public static BrowseBooksFragment newInstanceWithGenre(String genre) {
        BrowseBooksFragment fragment = new BrowseBooksFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SELECTED_GENRE, genre);
        fragment.setArguments(args);
        Log.d(FRAGMENT_TAG, "newInstanceWithGenre called with genre: " + genre);
        return fragment;
    }

    /**
     * Called when the fragment is attached to its host activity.
     * Ensures the host activity implements {@link BrowseBooksListener}.
     *
     * @param context The context of the host activity.
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof BrowseBooksListener) {
            listener = (BrowseBooksListener) context;
            Log.d(FRAGMENT_TAG, "BrowseBooksListener attached.");
        } else {
            // Fallback for cases where activity might not directly implement,
            // though ControllerActivity should.
            if (getActivity() instanceof BrowseBooksListener) {
                listener = (BrowseBooksListener) getActivity();
                Log.d(FRAGMENT_TAG, "BrowseBooksListener attached via getActivity().");
            } else {
                Log.e(FRAGMENT_TAG, "Hosting activity must implement BrowseBooksListener: " + context.toString());
                throw new RuntimeException(context.toString() + " must implement BrowseBooksListener");
            }
        }
    }

    /**
     * Called to do initial creation of the fragment.
     * Handles arguments and initializes adapters.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_SELECTED_GENRE)) {
            initialGenreToLoad = getArguments().getString(ARG_SELECTED_GENRE);
            Log.d(FRAGMENT_TAG, "onCreate: Initial genre to load set from arguments: " + initialGenreToLoad);
        } else {
            Log.d(FRAGMENT_TAG, "onCreate: No specific genre in arguments, will use default: " + initialGenreToLoad);
        }

        // Initialize adapters here to avoid null pointer if updateData is called before onViewCreated
        hotBookAdapter = new HotBookAdapter(book -> {
            if (listener != null) listener.onBookSelected(book);
        });
        genreBookAdapter = new GenreBookAdapter(book -> {
            if (listener != null) listener.onBookSelected(book);
        });
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBrowseBooksBinding.inflate(inflater, container, false);
        Log.d(FRAGMENT_TAG, "onCreateView called.");
        return binding.getRoot();
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has returned,
     * but before any saved state has been restored in to the view.
     * Sets up UI elements, adapters, and initiates data fetching.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(FRAGMENT_TAG, "onViewCreated called.");

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

        List<String> genres = Arrays.asList("Mystery", "Fiction", "Romance", "History", "Fantasy", "Science Fiction", "Thriller", "Horror");
        genreButtonsAdapter = new GenreAdapter(genres, genreName -> {
            Log.d(FRAGMENT_TAG, "Genre button clicked: " + genreName);
            if (listener != null) {
                listener.onGenreSelected(genreName);
            }
        });
        binding.genreButtonRecycler.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));
        binding.genreButtonRecycler.setAdapter(genreButtonsAdapter);

        // Fetch initial data
        if (listener instanceof ControllerActivity) { // Ensure listener is ControllerActivity for these calls
            ControllerActivity controller = (ControllerActivity) listener;
            controller.fetchWelcomeMessage(this);
            controller.fetchTopRatedBooks(this);
            Log.d(FRAGMENT_TAG, "Requesting initial books for genre: " + initialGenreToLoad);
            controller.fetchBooksByGenre(initialGenreToLoad, this); // Use the determined genre
        } else {
            Log.e(FRAGMENT_TAG, "Listener is not ControllerActivity, cannot fetch initial data.");
        }
    }

    /**
     * Displays the welcome message text in the UI.
     * Implements {@link BrowseBooksUI#displayWelcomeMessage(String)}.
     *
     * @param welcomeText The text to display.
     */
    @Override
    public void displayWelcomeMessage(String welcomeText) {
        if (binding != null && isAdded()) {
            binding.welcomeMessage.setText(welcomeText);
        }
    }

    /**
     * Sets the {@link BrowseBooksListener} for this fragment.
     * Implements {@link BrowseBooksUI#setListener(BrowseBooksListener)}.
     *
     * @param listener The listener to set.
     */
    @Override
    public void setListener(BrowseBooksListener listener) {
        this.listener = listener;
    }

    /**
     * Updates the list of hot books displayed in the RecyclerView.
     * Implements {@link BrowseBooksUI#updateHotBooks(List)}.
     *
     * @param books The new list of hot books.
     */
    @Override
    public void updateHotBooks(List<Book> books) {
        if (hotBookAdapter != null && isAdded()) {
            hotBookAdapter.updateData(books);
            Log.d(FRAGMENT_TAG, "Hot books updated with " + (books != null ? books.size() : 0) + " books.");
        } else {
            Log.w(FRAGMENT_TAG, "updateHotBooks called but adapter or fragment not ready.");
        }
    }

    /**
     * Updates the list of genre-specific books displayed in the RecyclerView.
     * Implements {@link BrowseBooksUI#updateGenreBooks(List)}.
     *
     * @param books The new list of genre books.
     */
    @Override
    public void updateGenreBooks(List<Book> books) {
        if (genreBookAdapter != null && isAdded()) {
            genreBookAdapter.updateData(books);
            Log.d(FRAGMENT_TAG, "Genre books updated with " + (books != null ? books.size() : 0) + " books.");
        } else {
            Log.w(FRAGMENT_TAG, "updateGenreBooks called but adapter or fragment not ready.");
        }
    }

    /**
     * Gets the root view of the fragment's layout.
     * Implements {@link BrowseBooksUI#getRootView()}.
     *
     * @return The root View, or null if the binding is null.
     */
    @Override
    public View getRootView() {
        return binding != null ? binding.getRoot() : null;
    }

    /**
     * Called when the view previously created by {@link #onCreateView} has been detached from the fragment.
     * Cleans up the binding reference.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d(FRAGMENT_TAG, "onDestroyView called.");
    }

    /**
     * RecyclerView Adapter for displaying "Hot Books" horizontally.
     */
    public static class HotBookAdapter extends RecyclerView.Adapter<HotBookAdapter.HotBookViewHolder> {
        private List<Book> books = new ArrayList<>();
        private final BrowseBooksUI.OnHotBookClickListener clickListener;

        /**
         * Constructs a new HotBookAdapter.
         * @param listener The click listener for book items.
         */
        public HotBookAdapter(BrowseBooksUI.OnHotBookClickListener listener) {
            this.clickListener = listener;
        }

        /**
         * Called when RecyclerView needs a new {@link HotBookViewHolder} of the given type to represent an item.
         * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
         * @param viewType The view type of the new View.
         * @return A new HotBookViewHolder that holds a View of the given view type.
         */
        @NonNull
        @Override
        public HotBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ItemHotBookBinding itemBinding = ItemHotBookBinding.inflate(inflater, parent, false);
            return new HotBookViewHolder(itemBinding);
        }

        /**
         * Called by RecyclerView to display the data at the specified position.
         * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        @Override
        public void onBindViewHolder(@NonNull HotBookViewHolder holder, int position) {
            holder.bind(books.get(position), clickListener);
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         * @return The total number of items.
         */
        @Override
        public int getItemCount() { return books.size(); }

        /**
         * Updates the data set of the adapter and notifies the RecyclerView.
         * @param newBooks The new list of books.
         */
        public void updateData(List<Book> newBooks) {
            this.books = (newBooks != null) ? newBooks : new ArrayList<>();
            notifyDataSetChanged();
        }

        /**
         * ViewHolder for the Hot Book items.
         */
        static class HotBookViewHolder extends RecyclerView.ViewHolder {
            private final ItemHotBookBinding binding;
            /**
             * Constructs a new HotBookViewHolder.
             * @param itemBinding The ViewBinding for the item layout.
             */
            public HotBookViewHolder(@NonNull ItemHotBookBinding itemBinding) {
                super(itemBinding.getRoot());
                this.binding = itemBinding;
            }
            /**
             * Binds book data to the UI elements and sets click listener.
             * @param book The {@link Book} object to bind.
             * @param listener The click listener.
             */
            public void bind(final Book book, final BrowseBooksUI.OnHotBookClickListener listener) {
                Glide.with(itemView.getContext()).load(book.getThumbnailUrl()).placeholder(R.drawable.placeholder_cover).error(R.drawable.placeholder_cover).into(binding.bookCoverImage);
                binding.bookRating.setRating(book.getRating());
                itemView.setOnClickListener(v -> { if (listener != null) listener.onHotBookClick(book); });
            }
        }
    }

    /**
     * Listener for genre button clicks.
     */
    public interface OnGenreButtonClickListener {
        /**
         * Called when a genre button is clicked.
         * @param genreName The name of the clicked genre.
         */
        void onGenreButtonClick(String genreName);
    }

    /**
     * RecyclerView Adapter for displaying genre buttons horizontally.
     */
    private static class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.GenreViewHolder> {
        private final List<String> genres;
        private final OnGenreButtonClickListener clickListener;

        /**
         * Constructs a new GenreAdapter.
         * @param genres The list of genre names.
         * @param listener The click listener for genre buttons.
         */
        public GenreAdapter(List<String> genres, OnGenreButtonClickListener listener) {
            this.genres = genres;
            this.clickListener = listener;
        }

        /**
         * Called when RecyclerView needs a new {@link GenreViewHolder} of the given type to represent an item.
         * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
         * @param viewType The view type of the new View.
         * @return A new GenreViewHolder that holds a View of the given view type.
         */
        @NonNull
        @Override
        public GenreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ItemGenreButtonBinding itemBinding = ItemGenreButtonBinding.inflate(inflater, parent, false);
            return new GenreViewHolder(itemBinding);
        }

        /**
         * Called by RecyclerView to display the data at the specified position.
         * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        @Override
        public void onBindViewHolder(@NonNull GenreViewHolder holder, int position) {
            holder.bind(genres.get(position), clickListener);
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         * @return The total number of items.
         */
        @Override
        public int getItemCount() { return genres.size(); }

        /**
         * ViewHolder for the Genre Button items.
         */
        static class GenreViewHolder extends RecyclerView.ViewHolder {
            private final ItemGenreButtonBinding binding;
            /**
             * Constructs a new GenreViewHolder.
             * @param itemBinding The ViewBinding for the item layout.
             */
            public GenreViewHolder(@NonNull ItemGenreButtonBinding itemBinding) {
                super(itemBinding.getRoot());
                this.binding = itemBinding;
            }
            /**
             * Binds genre name to the UI element and sets click listener.
             * @param genreName The genre name to bind.
             * @param listener The click listener.
             */
            public void bind(final String genreName, final OnGenreButtonClickListener listener) {
                binding.genreButton.setText(genreName);
                binding.genreButton.setOnClickListener(v -> { if (listener != null) listener.onGenreButtonClick(genreName); });
            }
        }
    }

    /**
     * RecyclerView Adapter for displaying genre-specific books in a grid.
     */
    private static class GenreBookAdapter extends RecyclerView.Adapter<GenreBookAdapter.GenreBookViewHolder> {
        private List<Book> books = new ArrayList<>();
        private final BrowseBooksUI.OnGenreBookClickListener clickListener;

        /**
         * Constructs a new GenreBookAdapter.
         * @param listener The click listener for book items.
         */
        public GenreBookAdapter(BrowseBooksUI.OnGenreBookClickListener listener) {
            this.clickListener = listener;
        }

        /**
         * Updates the data set of the adapter and notifies the RecyclerView.
         * @param newBooks The new list of books.
         */
        public void updateData(List<Book> newBooks) {
            this.books = (newBooks != null) ? newBooks : new ArrayList<>();
            notifyDataSetChanged();
        }

        /**
         * Called when RecyclerView needs a new {@link GenreBookViewHolder} of the given type to represent an item.
         * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
         * @param viewType The view type of the new View.
         * @return A new GenreBookViewHolder that holds a View of the given view type.
         */
        @NonNull
        @Override
        public GenreBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ItemGenreBookBinding itemBinding = ItemGenreBookBinding.inflate(inflater, parent, false);
            return new GenreBookViewHolder(itemBinding);
        }

        /**
         * Called by RecyclerView to display the data at the specified position.
         * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        @Override
        public void onBindViewHolder(@NonNull GenreBookViewHolder holder, int position) {
            holder.bind(books.get(position), clickListener);
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         * @return The total number of items.
         */
        @Override
        public int getItemCount() { return books.size(); }

        /**
         * ViewHolder for the Genre Book items.
         */
        static class GenreBookViewHolder extends RecyclerView.ViewHolder {
            private final ItemGenreBookBinding binding;
            /**
             * Constructs a new GenreBookViewHolder.
             * @param itemBinding The ViewBinding for the item layout.
             */
            public GenreBookViewHolder(@NonNull ItemGenreBookBinding itemBinding) {
                super(itemBinding.getRoot());
                this.binding = itemBinding;
            }
            /**
             * Binds book data to the UI elements and sets click listener.
             * @param book The {@link Book} object to bind.
             * @param listener The click listener.
             */
            public void bind(final Book book, final BrowseBooksUI.OnGenreBookClickListener listener) {
                Glide.with(itemView.getContext()).load(book.getThumbnailUrl()).placeholder(R.drawable.placeholder_cover).error(R.drawable.placeholder_cover).into(binding.genreBookCover);
                if (binding.genreBookRating != null) binding.genreBookRating.setRating(book.getRating());
                itemView.setOnClickListener(v -> { if (listener != null) listener.onGenreBookClick(book); });
            }
        }
    }
}
