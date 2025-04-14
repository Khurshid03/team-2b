package com.example.astudio.view;

import android.os.Bundle;
import android.util.Log;
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

public class ViewBookFragment extends Fragment {

    private FragmentViewBookBinding binding;
    private Book selectedBook;

    public ViewBookFragment() {
        // Empty public constructor required for fragments.
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflate using view binding
        binding = FragmentViewBookBinding.inflate(inflater, container, false);

        // Assign selectedBook from arguments if available
        if (getArguments() != null) {
            selectedBook = (Book) getArguments().getSerializable("book");
        }

        return binding.getRoot();
    }

    private boolean isDescriptionExpanded = false;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (selectedBook != null) {
            binding.bookTitle.setText(selectedBook.getTitle());
            binding.bookAuthor.setText(getString(R.string.book_author, selectedBook.getAuthor()));
            binding.bookDescription.setText(selectedBook.getDescription());
            binding.bookRating.setRating(selectedBook.getRating());

            Glide.with(requireContext())
                    .load(selectedBook.getThumbnailUrl())
                    .placeholder(R.drawable.placeholder_cover)
                    .into(binding.bookCover);

            // Handle toggle
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
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}