package com.example.astudio.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.astudio.databinding.FragmentLoginBinding;

/**
 * A Fragment that handles the login functionality. It collects the email and password input,
 * validates them, and delegates authentication handling to the controller.
 */
public class LoginFragment extends Fragment implements LoginUI {

    private FragmentLoginBinding binding;
    private LoginUI.LoginListener listener;

    public LoginFragment() {
        // Required empty public constructor.
    }

    /**
     * Called to create and inflate the view for the login fragment.
     */
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called after the view has been created. This sets up the login button logic.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.LoginButton.setOnClickListener(v -> {
            String email = binding.textEmail.getText().toString().trim();
            String password = binding.textPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Please enter both email and password.", Toast.LENGTH_SHORT).show();
            } else {
                if (listener != null) {
                    listener.onLogin(email, password, this);  // Delegate to Controller
                }
            }
        });
    }

    /**
     * Set the listener to be notified when login is attempted.
     */
    @Override
    public void setListener(LoginUI.LoginListener listener) {
        this.listener = listener;
    }

    /**
     * Clears binding to avoid memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}