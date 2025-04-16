package com.example.astudio.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.astudio.R;
import com.example.astudio.controller.ControllerActivity;
import com.example.astudio.databinding.FragmentLoginBinding;
import com.example.astudio.model.User;
import com.example.astudio.model.UserManager;

/**
 * A Fragment that handles the login functionality. It collects the username input, validates it,
 * and notifies the listener or activity upon successful login.
 */
public class LoginFragment extends Fragment implements LoginUI {

    private FragmentLoginBinding binding;
    private LoginUI.LoginListener listener;

    public LoginFragment() {
        // Required empty public constructor.
    }

    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        // You can pass parameters if needed.
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to create and inflate the view for the login fragment.
     *
     * @param inflater The LayoutInflater object to inflate the view.
     * @param container The container that the view will be attached to.
     * @param savedInstanceState A bundle containing saved instance state, if any.
     * @return The root view of the fragment.
     */
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called after the view has been created. This method sets up the login button and listens for
     * the button click to initiate the login process.
     *
     * @param view The fragment's root view.
     * @param savedInstanceState A bundle containing saved instance state, if any.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Set up the login button to capture the username.
        binding.LoginButton.setOnClickListener(v -> {
            String username = binding.TextUsername.getText().toString().trim();
            // Check that username is not empty (email is optional)
            if (username.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.please_enter_username), Toast.LENGTH_SHORT).show();
            } else {
                // Create a new User and store it in UserManager
                User user = new User(username);
                UserManager.getInstance().setCurrentUser(user);
                // Now invoke login success.
                if (listener != null) {
                    listener.onLogin(username);
                } else if (getActivity() instanceof ControllerActivity) {
                    ((ControllerActivity) getActivity()).onLoginSuccess(username);
                }
            }
        });

    }

    /**
     * Sets the listener to handle login success events.
     *
     * @param listener The listener to be set for login success events.
     */
    @Override
    public void setListener(LoginUI.LoginListener listener) {
        this.listener = listener;
    }

    /**
     * Called when the view is destroyed. This method clears the binding object to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}