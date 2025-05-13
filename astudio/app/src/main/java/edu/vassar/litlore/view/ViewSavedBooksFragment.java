package edu.vassar.litlore.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import edu.vassar.litlore.R;
import edu.vassar.litlore.controller.ControllerActivity;
import edu.vassar.litlore.databinding.FragmentViewSavedBooksBinding;
import edu.vassar.litlore.model.Book;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays the current user's saved books in a RecyclerView.
 */
public class ViewSavedBooksFragment extends Fragment implements ViewSavedBooksUI {
    private FragmentViewSavedBooksBinding binding;
    private final List<Book> savedBooks = new ArrayList<>();
    private SavedBooksAdapter adapter;
    private ControllerActivity controller;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentViewSavedBooksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        controller = (ControllerActivity) requireActivity();

        // 1) RecyclerView layout manager:
        binding.savedBooksRecyclerView
                .setLayoutManager(new LinearLayoutManager(getContext()));

        // 2) Adapter with click-through to the book detail:
        adapter = new SavedBooksAdapter(savedBooks, book -> {
            Bundle args = new Bundle();
            args.putSerializable("book", book);
            ViewBookFragment frag = new ViewBookFragment();
            frag.setArguments(args);
            frag.setListener((ViewBookUI.ViewBookListener) getActivity());
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, frag)
                    .addToBackStack(null)
                    .commit();
        });

        // 3) Wire it up:
        binding.savedBooksRecyclerView.setAdapter(adapter);

        // 4) Kick off the initial load:
        controller.fetchSavedBooks(this);
    }

    /**
     * Called by ControllerActivity when the saved books are loaded.
     */
    @Override
    public void displaySavedBooks(List<Book> books) {
        savedBooks.clear();
        if (books != null) {
            savedBooks.addAll(books);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * Called by ControllerActivity on error fetching saved books.
     */
    @Override
    public void showError(String message) {
        Toast.makeText(getContext(), "Error loading saved books: " + message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
