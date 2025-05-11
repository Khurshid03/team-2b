package com.example.astudio.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.astudio.databinding.FragmentCreateAccountBinding;

/**
 * A Fragment for user account creation.
 * Provides UI for entering username, email, and password, and buttons for creating an account or proceeding to login.
 */
public class CreateAccountFragment extends Fragment implements CreateAccountUI {

    private FragmentCreateAccountBinding binding;
    private CreateAccountListener listener;

    /**
     * Required empty public constructor.
     */
    public CreateAccountFragment() {
        // Required empty public constructor
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreateAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has returned.
     * Sets up button click listeners.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.CreateAccountButton.setOnClickListener(v -> {
            if (listener != null) {
                String username = binding.createAccountUsername.getText().toString().trim();
                String email = binding.CreateAccountEmail.getText().toString().trim();
                String password = binding.CreateAccountPassword.getText().toString().trim();
                listener.onCreateAccount(username, email, password, this);
            }
        });

        binding.ProceedToLoginButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProceedToLogin();
            }
        });
    }

    /**
     * Sets the listener for account creation events.
     * Implements {@link CreateAccountUI#setListener(CreateAccountListener)}.
     *
     * @param listener The listener to set.
     */
    @Override
    public void setListener(CreateAccountListener listener) {
        this.listener = listener;
    }
}
