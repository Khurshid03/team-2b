package com.example.astudio.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.example.astudio.R;
import com.example.astudio.controller.ControllerActivity;

public class LoginFragment extends Fragment {

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        Button loginButton = view.findViewById(R.id.LoginButton);
        // Get the EditText for username
        EditText usernameInput = view.findViewById(R.id.Text_username);

        loginButton.setOnClickListener(v -> {
            // Capture the username
            String username = usernameInput.getText().toString().trim();
            if (getActivity() instanceof ControllerActivity) {
                // Pass the username to onLoginSuccess()
                ((ControllerActivity) getActivity()).onLoginSuccess(username);
            }
        });
        return view;
    }
}