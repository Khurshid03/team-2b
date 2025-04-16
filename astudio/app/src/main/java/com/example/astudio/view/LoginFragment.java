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

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

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

    @Override
    public void setListener(LoginUI.LoginListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}