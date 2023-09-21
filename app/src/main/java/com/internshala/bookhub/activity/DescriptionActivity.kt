package com.internshala.bookhub.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.internshala.bookhub.R
import com.internshala.bookhub.database.BookDatabase
import com.internshala.bookhub.database.BookEntity
import com.internshala.bookhub.util.ConnectionManager
import com.squareup.picasso.Picasso
import org.json.JSONObject

class DescriptionActivity : AppCompatActivity() {
    lateinit var txtBookName: TextView
    lateinit var txtBookAuthor: TextView
    lateinit var txtBookPrice: TextView
    lateinit var txtBookRating: TextView
    lateinit var imgBookImage: ImageView
    lateinit var txtBookDesc: TextView
    lateinit var btnAddToFav: Button
    lateinit var progressBar: ProgressBar
    lateinit var progressLayout: RelativeLayout
    lateinit var toolbar: Toolbar

    var bookId: String? = "100"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        txtBookName = findViewById(R.id.txtBookName1)
        txtBookAuthor = findViewById(R.id.txtBookAuthor1)
        txtBookPrice = findViewById(R.id.txtBookPrice1)
        txtBookRating = findViewById(R.id.txtBookRating1)
        imgBookImage = findViewById(R.id.imgBookImage1)
        txtBookDesc = findViewById(R.id.txtBookDesc)
        btnAddToFav = findViewById(R.id.btnAddToFav)
        progressLayout = findViewById(R.id.progressLayout1)
        progressLayout.visibility = View.VISIBLE
        progressBar = findViewById(R.id.progressBar1)
        progressBar.visibility = View.VISIBLE

        toolbar = findViewById(R.id.toolbar1)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Book Details"

        if (intent != null) {
            bookId = intent.getStringExtra("book_id")
        } else {
            finish()
            Toast.makeText(
                this@DescriptionActivity,
                "Some Unexpected Error Occurred",
                Toast.LENGTH_SHORT
            ).show()
        }
        if (bookId == "100") {
            finish()
            Toast.makeText(
                this@DescriptionActivity,
                "Some Unexpected Error Occurred",
                Toast.LENGTH_SHORT
            ).show()
        }

        val queue = Volley.newRequestQueue(this@DescriptionActivity)
        val url = "http://13.235.250.119/v1/book/get_book/"

        if (ConnectionManager().checkConnectivity(this@DescriptionActivity)) {
            val jsonParams = JSONObject()
            jsonParams.put("book_id", bookId)

            val jsonRequest1 =
                object : JsonObjectRequest(Request.Method.POST, url, jsonParams, Response.Listener {
                    try {
                        val success = it.getBoolean("success")
                        if (success) {
                            val bookJsonObject = it.getJSONObject("book_data")
                            progressLayout.visibility = View.GONE
                            val bookImageUrl = bookJsonObject.getString("image")

                            Picasso.get().load(bookJsonObject.getString("image"))
                                .error(R.drawable.default_book_cover).into(imgBookImage)
                            txtBookName.text = bookJsonObject.getString("name")
                            txtBookAuthor.text = bookJsonObject.getString("author")
                            txtBookPrice.text = bookJsonObject.getString("price")
                            txtBookRating.text = bookJsonObject.getString("rating")
                            txtBookDesc.text = bookJsonObject.getString("description")

                            val bookEntity = BookEntity(
                                bookId?.toInt() as Int,
                                txtBookName.toString(),
                                txtBookAuthor.toString(),
                                txtBookPrice.toString(),
                                txtBookRating.toString(),
                                txtBookDesc.toString(),
                                bookImageUrl
                            )

                            val checkFav = DBAsyncTask(applicationContext, bookEntity, 1).execute()
                            val isFav = checkFav.get()
                            if (isFav) {
                                btnAddToFav.text = getString(R.string.remove_from_favourites)
                                val favColour = ContextCompat.getColor(
                                    applicationContext,
                                    R.color.orange_bright
                                )
                                btnAddToFav.setBackgroundColor(favColour)
                            } else {
                                btnAddToFav.text = getString(R.string.add_to_favourites)
                                val noFavColor =
                                    ContextCompat.getColor(applicationContext, R.color.favourite)
                                btnAddToFav.setBackgroundColor(noFavColor)
                            }
                            btnAddToFav.setOnClickListener {
                                if (!DBAsyncTask(applicationContext, bookEntity, 1).execute()
                                        .get()
                                ) {
                                    val async =
                                        DBAsyncTask(applicationContext, bookEntity, 2).execute()
                                    val result = async.get()
                                    if (result) {
                                        Toast.makeText(
                                            this@DescriptionActivity,
                                            "Book added to Favourites",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        btnAddToFav.text =
                                            getString(R.string.remove_from_favourites)
                                        val favColour = ContextCompat.getColor(
                                            applicationContext,
                                            R.color.orange_bright
                                        )
                                        btnAddToFav.setBackgroundColor(favColour)

                                    } else {
                                        Toast.makeText(
                                            this@DescriptionActivity,
                                            "Some error occurred",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    }


                                } else {
                                    val async =
                                        DBAsyncTask(applicationContext, bookEntity, 3).execute()
                                    val result = async.get()
                                    if (result) {
                                        Toast.makeText(
                                            this@DescriptionActivity,
                                            "Book removed from Favourites",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        btnAddToFav.text =
                                            getString(R.string.add_to_favourites)
                                        val nofavColour = ContextCompat.getColor(
                                            applicationContext,
                                            R.color.favourite
                                        )
                                        btnAddToFav.setBackgroundColor(nofavColour)

                                    } else {
                                        Toast.makeText(
                                            this@DescriptionActivity,
                                            "Some error occurred",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    }

                                }
                            }

                        } else {
                            finish()
                            Toast.makeText(
                                this@DescriptionActivity,
                                "Unexpected Error1 Occurred",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } catch (e: Exception) {
                        Toast.makeText(
                            this@DescriptionActivity,
                            "Unexpected Error2 Occurred",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, Response.ErrorListener {
                    Toast.makeText(
                        this@DescriptionActivity,
                        "Volley Error",
                        Toast.LENGTH_SHORT
                    ).show()
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content_type"] = "application/json"
                        headers["token"] = "38d8e92f11bb82"
                        return headers
                    }
                }
            queue.add(jsonRequest1)

        } else {
            val dialog = AlertDialog.Builder(this@DescriptionActivity)
            dialog.setTitle("Error")
            dialog.setMessage("Internet connection not found")
            dialog.setPositiveButton("Open Setting") { text, listener ->
                val settingIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingIntent)
                finish()
            }
            dialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(this@DescriptionActivity)
            }
            dialog.create()
            dialog.show()
        }
    }

    class DBAsyncTask(val context: Context, val bookEntity: BookEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {
        /*
        Mode 1 -> Check DB if book is in favourites or not
        Mode 2 -> Save book in DB as Favourites
        Mode3  -> Remove book from Favourites
        */

        val db = Room.databaseBuilder(context, BookDatabase::class.java, "book-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                1 -> {
                    // Check DB if book is in favourites or not
                    val book: BookEntity? = db.bookDao().getBookById(bookEntity.book_id.toString())
                    db.close()
                    return book != null
                }
                2 -> {
                    //Save book in DB as Favourites
                    db.bookDao().insertBook(bookEntity)
                    db.close()
                    return true
                }
                3 -> {
                    //Remove book from Favourites
                    db.bookDao().deleteBook(bookEntity)
                    db.close()
                    return true

                }
            }
            return false
        }
    }
}