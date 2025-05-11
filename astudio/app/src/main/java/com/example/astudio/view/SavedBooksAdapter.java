package com.example.astudio.view;

import android.view.LayoutInflater;
import android.view.ViewGroup;
// ImageView, TextView, RatingBar imports are no longer needed as views are accessed via binding
// import android.widget.ImageView;
// import android.widget.RatingBar;
// import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.astudio.R;
import com.example.astudio.databinding.ItemSavedBookBinding; // Import the generated binding class
import com.example.astudio.model.Book;

import java.util.List;
import java.util.ArrayList; // It's good practice to initialize lists

/**
 * RecyclerView adapter for displaying a list of saved books.
 * This version uses ViewBinding in its ViewHolder.
 */
public class SavedBooksAdapter extends RecyclerView.Adapter<SavedBooksAdapter.ViewHolder> {
    private List<Book> savedBooks; // Keep the list mutable if you plan to update it
    private OnBookClickListener clickListener;

    /**
     * Listener interface for handling clicks on book items.
     */
    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    /**
     * Constructor for the adapter.
     * @param savedBooks List of books to display.
     * @param listener Listener for book click events.
     */
    public SavedBooksAdapter(List<Book> savedBooks, OnBookClickListener listener) {
        this.savedBooks = (savedBooks != null) ? savedBooks : new ArrayList<>(); // Defensive copy or handle null
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout using ItemSavedBookBinding
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemSavedBookBinding binding = ItemSavedBookBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding); // Pass the binding to the ViewHolder
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = savedBooks.get(position);
        // The binding of data to views is now handled within the ViewHolder's bind method
        holder.bind(book, clickListener);
    }

    @Override
    public int getItemCount() {
        return savedBooks != null ? savedBooks.size() : 0;
    }

    /**
     * Updates the data in the adapter and refreshes the RecyclerView.
     * @param newSavedBooks The new list of books to display.
     */
    public void updateData(List<Book> newSavedBooks) {
        this.savedBooks.clear();
        if (newSavedBooks != null) {
            this.savedBooks.addAll(newSavedBooks);
        }
        notifyDataSetChanged(); // Consider using DiffUtil for better performance with large lists
    }


    /**
     * ViewHolder for saved book items. Uses ItemSavedBookBinding.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSavedBookBinding binding; // Store the binding object

        /**
         * Constructor for the ViewHolder.
         * @param itemBinding The ViewBinding object for the item_saved_book layout.
         */
        ViewHolder(@NonNull ItemSavedBookBinding itemBinding) {
            super(itemBinding.getRoot());
            this.binding = itemBinding; // Assign the binding object
        }

        /**
         * Binds a Book object to the views in the ViewHolder.
         * @param book The Book object to display.
         * @param listener The click listener for the item.
         */
        void bind(final Book book, final OnBookClickListener listener) {
            if (book == null) return; // Guard against null book object

            // Access views via the binding object
            binding.bookTitle.setText(book.getTitle());
            binding.bookAuthor.setText(book.getAuthor());
            binding.bookRatingBar.setRating(book.getRating());

            Glide.with(binding.bookCover.getContext())
                    .load(book.getThumbnailUrl())
                    .placeholder(R.drawable.placeholder_cover) // Ensure this drawable exists
                    .error(R.drawable.placeholder_cover)       // Fallback image on error
                    .into(binding.bookCover);

            // Set the click listener for the entire item view
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookClick(book);
                }
            });
        }
    }
}





