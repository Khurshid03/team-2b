package com.example.astudio.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.astudio.R;
import com.example.astudio.model.Book;

import java.util.List;

/**
 * RecyclerView adapter for displaying a list of saved books.
 */
public class SavedBooksAdapter extends RecyclerView.Adapter<SavedBooksAdapter.ViewHolder> {
    private final List<Book> savedBooks;

    public SavedBooksAdapter(List<Book> savedBooks) {
        this.savedBooks = savedBooks;
    }

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    private OnBookClickListener clickListener;

    public SavedBooksAdapter(List<Book> savedBooks, OnBookClickListener listener) {
        this.savedBooks = savedBooks;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saved_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = savedBooks.get(position);
        holder.title.setText(book.getTitle());
        holder.author.setText(book.getAuthor());
        holder.ratingBar.setRating(book.getRating());
        Glide.with(holder.cover.getContext())
                .load(book.getThumbnailUrl())
                .placeholder(R.drawable.placeholder_cover)
                .into(holder.cover);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onBookClick(savedBooks.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return savedBooks.size();
    }

    /**
     * ViewHolder for saved book items.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView title;
        TextView author;
        RatingBar ratingBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.bookCover);
            title = itemView.findViewById(R.id.bookTitle);
            author = itemView.findViewById(R.id.bookAuthor);
            ratingBar = itemView.findViewById(R.id.bookRatingBar);
        }
    }
}
