package com.binh.android.cookies

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.binh.android.cookies.data.Post
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.binh.android.cookies", appContext.packageName)
    }

    @Test
    fun testDatabase() {
        val database = FirebaseDatabase.getInstance().getReference("posts")

        val query = database.orderByChild("type").equalTo("Easy")

        query.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val post = snapshot.getValue(Post::class.java)
                Log.d("TestDatabase", post.toString())
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val post = snapshot.getValue(Post::class.java)
                Log.d("TestDatabase", post.toString())
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val post = snapshot.getValue(Post::class.java)
                Log.d("TestDatabase", post.toString())
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                val post = snapshot.getValue(Post::class.java)
                Log.d("TestDatabase", post.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}