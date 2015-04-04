package com.ayuget.redface.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ayuget.redface.R;
import com.ayuget.redface.account.RedfaceAccountManager;
import com.ayuget.redface.account.UserManager;
import com.ayuget.redface.data.api.hfr.HFRAuthenticator;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.data.rx.EndlessObserver;
import com.ayuget.redface.data.rx.SubscriptionHandler;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.rengwuxian.materialedittext.MaterialEditText;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;

public class AccountActivity extends BaseActivity {
    private static final String LOG_TAG = AccountActivity.class.getSimpleName();

    @InjectView(R.id.username)
    MaterialEditText usernameTextView;

    @InjectView(R.id.password)
    MaterialEditText passwordTextView;

    @Inject
    HFRAuthenticator authenticator;

    @Inject
    RedfaceAccountManager accountManager;

    @Inject
    UserManager userManager;

    private SubscriptionHandler<User, Boolean> loginSubscriptionHandler = new SubscriptionHandler<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
    }

   @OnClick(R.id.sign_in_button)
   protected void onLoginAttempt() {
       final String username = usernameTextView.getText().toString().trim();

       if (username.equals("")) {
           usernameTextView.setError(getString(R.string.login_username_empty));
           return;
       }
       else {
           usernameTextView.setError(null);
       }

       final String password = passwordTextView.getText().toString().trim();
       if (password.equals("")) {
           passwordTextView.setError(getString(R.string.login_password_empty));
           return;
       }

       Log.d(LOG_TAG, String.format("Login attempt for user '%s'", username));

       final User user = new User(username, password);

       subscribe(loginSubscriptionHandler.load(user, authenticator.login(user), new EndlessObserver<Boolean>() {
           @Override
           public void onNext(Boolean loginWorked) {

               if (loginWorked) {
                   Log.d(LOG_TAG, "Login is successful !!");
                   accountManager.addAccount(user);
                   userManager.setActiveUser(user);
                   Intent intent = new Intent(AccountActivity.this, TopicsActivity.class);
                   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                   startActivity(intent);
                   finish();
               }
               else {
                   Log.d(LOG_TAG, "Error while logging in");
                   SnackbarManager.show(
                           Snackbar.with(AccountActivity.this)
                                   .text(R.string.login_failed)
                                   .colorResource(R.color.theme_primary_light)
                                   .textColorResource(R.color.tabs_text_color)
                   );
               }
           }

           @Override
           public void onError(Throwable throwable) {
               Log.d(LOG_TAG, "Unknow error while logging in :(", throwable);
           }
       }));
   }
}
