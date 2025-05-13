package edu.vassar.litlore.view;

import android.content.Context; // Import Context for onAttach
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import edu.vassar.litlore.R;
import edu.vassar.litlore.controller.ControllerActivity;
import edu.vassar.litlore.databinding.FragmentSearchBooksBinding;
import edu.vassar.litlore.databinding.ItemGenreBookBinding; // Assuming this is the item layout
import edu.vassar.litlore.model.Book;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for searching books.
 * Displays a search input, a back button, and a grid of search results.
 * Delegates back navigation to the controller.
 */
public class SearchBooksFragment extends Fragment implements SearchBooksUI {

    private static final String FRAGMENT_TAG = "SearchBooksFragment";
    /** ViewBinding instance for the fragment layout. */
    private FragmentSearchBooksBinding binding;
    /** Adapter for displaying search results in the RecyclerView. */
    private SearchBooksAdapter adapter;
    /** Listener for navigation events, typically the hosting Activity. */
    private SearchBooksFragmentNavigationListener navigationListener; // Listener for navigation events

    /**
     * Listener interface for SearchBooksFragment to communicate navigation events
     * (like back button press) to the hosting Activity/Controller.
     */
    public interface SearchBooksFragmentNavigationListener {
        /**
         * Called when the user requests to navigate back from the search screen.
         */
        void onSearchBooksNavigateBack(); // Method to call when back is pressed
    }

    /**
     * Required empty public constructor.
     */
    public SearchBooksFragment() {
        // Required empty public constructor.
    }

    /**
     * Factory method to create a new instance of this fragment with an initial search query.
     *
     * @param query The initial search query string.
     * @return A new instance of fragment SearchBooksFragment.
     */
    public static SearchBooksFragment newInstance(String query) {
        SearchBooksFragment fragment = new SearchBooksFragment();
        Bundle args = new Bundle();
        args.putString("query", query);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called when the fragment is attached to its host context.
     * Attaches the {@link SearchBooksFragmentNavigationListener}.
     *
     * @param context The context of the host.
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Attach the navigation listener
        if (context instanceof SearchBooksFragmentNavigationListener) {
            navigationListener = (SearchBooksFragmentNavigationListener) context;
            Log.d(FRAGMENT_TAG, "SearchBooksFragmentNavigationListener attached.");
        } else {
            // Log a warning but don't crash immediately, allow fallback if activity is ControllerActivity directly
            Log.w(FRAGMENT_TAG, context.toString() + " must implement SearchBooksFragmentNavigationListener for optimal back navigation.");
            // For robustness, you could still try casting getActivity() later if navigationListener is null,
            // but implementing the interface is cleaner.
        }
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * Inflates the layout using ViewBinding.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBooksBinding.inflate(inflater, container, false);
        Log.d(FRAGMENT_TAG, "onCreateView called.");
        return binding.getRoot();
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has returned.
     * Sets up the RecyclerView, adapter, button listeners, and handles initial query from arguments.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(FRAGMENT_TAG, "onViewCreated called.");

        binding.searchBooksRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new SearchBooksAdapter();
        binding.searchBooksRecycler.setAdapter(adapter);
        Log.d(FRAGMENT_TAG, "RecyclerView and Adapter initialized.");

        // Back button implementation: delegate to listener (ControllerActivity)
        binding.backButton.setOnClickListener(v -> {
            Log.d(FRAGMENT_TAG, "Back button clicked. Notifying listener.");
            if (navigationListener != null) {
                navigationListener.onSearchBooksNavigateBack();
            } else if (getActivity() instanceof ControllerActivity) {
                // Fallback if listener wasn't set via onAttach (less ideal but provides some safety)
                Log.w(FRAGMENT_TAG, "NavigationListener not set via onAttach, attempting direct call to ControllerActivity for back navigation.");
                ((ControllerActivity) getActivity()).onSearchBooksNavigateBack();
            } else {
                Log.e(FRAGMENT_TAG, "Cannot navigate back: Listener not set and activity is not ControllerActivity.");
                // Last resort: pop back stack if possible, though controller should handle this
                if (getActivity() != null && getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        binding.goButton.setOnClickListener(v -> {
            String query = binding.searchInput.getText().toString().trim();
            Log.d(FRAGMENT_TAG, "Search button clicked with query: '" + query + "'");
            if (query.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a search query", Toast.LENGTH_SHORT).show();
            } else if (getActivity() instanceof ControllerActivity) {
                ((ControllerActivity) getActivity()).fetchSearchBooks(query, this);
            } else {
                Log.e(FRAGMENT_TAG, "Search button: getActivity() is not ControllerActivity or is null.");
            }
        });

        // Handle initial query passed via arguments
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

    /**
     * Called when a book search operation is successful.
     * Updates the RecyclerView with the search results.
     * Implements {@link SearchBooksUI#onSearchBooksSuccess(List)}.
     *
     * @param books The list of {@link Book} objects found.
     */
    @Override
    public void onSearchBooksSuccess(List<Book> books) {
        if (!isAdded() || binding == null) return; // Check if fragment is attached and view is available
        Log.i(FRAGMENT_TAG, "onSearchBooksSuccess: Received " + (books != null ? books.size() : "null") + " books.");
        if (adapter != null) {
            adapter.updateData(books);
        }
    }

    /**
     * Called when a book search operation fails.
     * Displays an error message to the user.
     * Implements {@link SearchBooksUI#onSearchBooksFailure(String)}.
     *
     * @param errorMessage The error message string.
     */
    @Override
    public void onSearchBooksFailure(String errorMessage) {
        if (!isAdded()) return; // Check if fragment is attached
        Log.e(FRAGMENT_TAG, "onSearchBooksFailure: " + errorMessage);
        Toast.makeText(getContext(), "Search failed: " + errorMessage, Toast.LENGTH_LONG).show();
    }

    /**
     * Called when the view previously created by onCreateView has been detached from the fragment.
     * Cleans up the binding reference.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(FRAGMENT_TAG, "onDestroyView called.");
        binding = null; // Important to null out the binding
    }

    /**
     * Called when the fragment is no longer attached to its activity.
     * Cleans up the navigation listener reference.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        navigationListener = null; // Clean up listener
        Log.d(FRAGMENT_TAG, "onDetach called, navigationListener nulled.");
    }

    // Adapter and ViewHolder remain the same as your provided version (already using ViewBinding)
    /**
     * RecyclerView Adapter for displaying search results in a grid.
     */
    public static class SearchBooksAdapter extends RecyclerView.Adapter<SearchBooksAdapter.SearchBookViewHolder> {
        private List<Book> books = new ArrayList<>();

        /**
         * Updates the data set of the adapter and notifies the RecyclerView.
         * @param newBooks The new list of books.
         */
        public void updateData(List<Book> newBooks) {
            this.books = (newBooks != null) ? newBooks : new ArrayList<>();
            notifyDataSetChanged();
        }

        /**
         * Called when RecyclerView needs a new {@link SearchBookViewHolder} of the given type to represent an item.
         * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
         * @param viewType The view type of the new View.
         * @return A new SearchBookViewHolder that holds a View of the given view type.
         */
        @NonNull
        @Override
        public SearchBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ItemGenreBookBinding itemBinding = ItemGenreBookBinding.inflate(inflater, parent, false);
            return new SearchBookViewHolder(itemBinding);
        }

        /**
         * Called by RecyclerView to display the data at the specified position.
         * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        @Override
        public void onBindViewHolder(@NonNull SearchBookViewHolder holder, int position) {
            Book book = books.get(position);
            holder.bind(book);
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         * @return The total number of items.
         */
        @Override
        public int getItemCount() {
            return books.size();
        }

        /**
         * ViewHolder for the Search Book items.
         */
        static class SearchBookViewHolder extends RecyclerView.ViewHolder {
            /** ViewBinding for the item layout. */
            private final ItemGenreBookBinding binding;

            /**
             * Constructs a new SearchBookViewHolder.
             * @param itemBinding The ViewBinding for the item layout.
             */
            public SearchBookViewHolder(@NonNull ItemGenreBookBinding itemBinding) {
                super(itemBinding.getRoot());
                this.binding = itemBinding;
            }

            /**
             * Binds book data to the UI elements and sets click listener for navigation.
             * @param book The {@link Book} object to bind.
             */
            public void bind(final Book book) {
                if (book == null) return;
                Glide.with(itemView.getContext())
                        .load(book.getThumbnailUrl())
                        .placeholder(R.drawable.placeholder_cover)
                        .error(R.drawable.placeholder_cover)
                        .into(binding.genreBookCover);

                binding.genreBookRating.setRating(book.getRating());

                itemView.setOnClickListener(v -> {
                    Log.d(FRAGMENT_TAG, "Book clicked in search results: " + book.getTitle());
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("book", book);
                    ViewBookFragment viewBookFragment = new ViewBookFragment();
                    viewBookFragment.setArguments(bundle);

                    if (v.getContext() instanceof FragmentActivity) {
                        ((FragmentActivity) v.getContext()).getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragmentContainerView, viewBookFragment)
                                .addToBackStack(null) // Add to back stack to allow returning to search results
                                .commit();
                    } else {
                        Log.e(FRAGMENT_TAG, "itemView.getContext() is not a FragmentActivity. Cannot navigate to ViewBookFragment.");
                    }
                });
            }
        }
    }
}
