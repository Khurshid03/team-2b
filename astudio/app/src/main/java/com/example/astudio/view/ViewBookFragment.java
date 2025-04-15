package com.example.astudio.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.astudio.R;
import com.example.astudio.databinding.FragmentViewBookBinding;
import com.example.astudio.model.Book;

public class ViewBookFragment extends Fragment implements ViewBookUI {

    private FragmentViewBookBinding binding;
    private Book selectedBook;
    private ViewBookListener listener;
    private boolean isDescriptionExpanded = false;

    public ViewBookFragment() {
        // Required empty public constructor.
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflate using view binding
        binding = FragmentViewBookBinding.inflate(inflater, container, false);

        // Retrieve the book from the fragment arguments (if available)
        if (getArguments() != null) {
            selectedBook = (Book) getArguments().getSerializable("book");
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Update the UI with the book details if a book was provided.
        if (selectedBook != null) {
            updateBookDetails(selectedBook);
        }


        // for the back button in case we decide to implement it
        // binding.backButton.setOnClickListener(v -> {
        //    if(listener != null){
        //         listener.onBackButtonClicked();
        //    }
        // });
    }

    @Override
    public void updateBookDetails(Book book) {
        // Update text fields using string resources (with placeholders as needed)
        binding.bookTitle.setText(book.getTitle());
        binding.bookAuthor.setText(getString(R.string.book_author, book.getAuthor()));
        binding.bookDescription.setText(book.getDescription());
        binding.bookRating.setRating(book.getRating());

        // Load the book cover image using Glide
        Glide.with(requireContext())
                .load(book.getThumbnailUrl())
                .placeholder(R.drawable.placeholder_cover)
                .into(binding.bookCover);

        // Handle expand/collapse of the description text.
        binding.showMoreButton.setOnClickListener(v -> {
            isDescriptionExpanded = !isDescriptionExpanded;
            if (isDescriptionExpanded) {
                binding.bookDescription.setMaxLines(Integer.MAX_VALUE);
                binding.showMoreButton.setText(R.string.show_less);
            } else {
                binding.bookDescription.setMaxLines(5);
                binding.showMoreButton.setText(R.string.show_more);
            }
        });
    }

    @Override
    public void setListener(ViewBookListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}