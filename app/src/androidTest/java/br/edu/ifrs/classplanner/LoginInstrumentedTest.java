package br.edu.ifrs.classplanner;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.edu.ifrs.classplanner.activity.AuthActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class LoginInstrumentedTest {

    @Rule
    public ActivityScenarioRule<AuthActivity> activityRule = new ActivityScenarioRule<>(AuthActivity.class);

    @Before
    public void checkUserIsNotLoggedInAndGoToLoginView() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseAuth.getInstance().signOut();
            onView(withId(R.id.action_sign_out)).perform(click());
        }
        onView(withId(R.id.buttonOptionLogin)).perform(click());
    }

    @Test
    public void snackbarIsShown_EmailIsEmpty() {
        String stringToBeTyped = "password";

        onView(withId(R.id.inputPassword)).perform(typeText(stringToBeTyped));
        onView(withId(R.id.buttonLogin)).perform(click());

        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("Preencha todos os campos")));
    }

    @Test
    public void snackbarIsShown_PasswordIsEmpty() {
        String stringToBeTyped = "email";

        onView(withId(R.id.inputEmail)).perform(typeText(stringToBeTyped));
        onView(withId(R.id.buttonLogin)).perform(click());

        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("Preencha todos os campos")));
    }
}
