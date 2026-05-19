package com.estudiante.strennus_proyweb

import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.estudiante.strennus_proyweb.ui.LogInActivity
import org.junit.Rule
import org.junit.Test
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.estudiante.strennus_proyweb.data.AppDataBase
import com.estudiante.strennus_proyweb.entities.Usuario
import kotlinx.coroutines.runBlocking
import org.junit.Before


class TestingLogIn {
    @get:Rule
    val activityRule = ActivityScenarioRule(LogInActivity::class.java)

    @Before
    fun setup(){
        activityRule.scenario.onActivity { activity ->
            val db = AppDataBase.getInstance(activity)
            runBlocking {
                db.usuarioDao().insert(
                    Usuario(
                        name = "tester",
                        username = "tester",
                        correo = "tester@test.com",
                        password = "1234",
                        fechaRegistro = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    @Test
    fun testLoginFlow_Successful(){
        onView(withId(R.id.etUsername)).perform(typeText("tester"))
        onView(withId(R.id.etPassword)).perform(typeText("1234"))

        closeSoftKeyboard()

        onView(withId(R.id.btnLogin)).perform(click())
        onView(withId(R.id.bottomNavigation)).check(matches(isDisplayed()))
    }

    @Test
    fun testLoginFlow_Failure(){
        onView(withId(R.id.etUsername)).perform(typeText("tester"))
        onView(withId(R.id.etPassword)).perform(typeText("tester"))

        closeSoftKeyboard()

        onView(withId(R.id.btnLogin)).perform(click())
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))
    }

    @Test
    fun testLoginFlow_MissingPassword(){
        onView(withId(R.id.etUsername)).perform(typeText("tester"))

        closeSoftKeyboard()

        onView(withId(R.id.btnLogin)).perform(click())
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))
    }

    @Test
    fun testLoginFlow_MissingUsername(){
        onView(withId(R.id.etPassword)).perform(typeText("1234"))

        closeSoftKeyboard()

        onView(withId(R.id.btnLogin)).perform(click())
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))
    }

    @Test
    fun testLoginFlow_Register(){
        onView(withId(R.id.tvCreateAccount)).perform(click())
        onView(withId(R.id.btnRegister)).check(matches(isDisplayed()))
    }
}