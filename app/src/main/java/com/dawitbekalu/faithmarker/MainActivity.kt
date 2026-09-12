package com.dawitbekalu.faithmarker

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

data class BibleBook(
    val name: String,
    val chapters: Int,
    val testament: String,
    val group: String
)

data class Achievement(
    var name: String,
    var description: String,
    var book: String = "",
    var unlocked: Boolean = false
)

class MainActivity : AppCompatActivity() {

    // ------------------------------------------------------------
    // COLORS — FAITH MARK STYLE
    // ------------------------------------------------------------

    private val bg = Color.rgb(17, 17, 17)
    private val surface = Color.rgb(30, 30, 30)
    private val surface2 = Color.rgb(42, 42, 42)
    private val surface3 = Color.rgb(52, 52, 52)

    private val green = Color.rgb(156, 175, 136)
    private val greenDark = Color.rgb(110, 128, 94)

    private val blue = Color.rgb(121, 215, 245)
    private val blueDark = Color.rgb(64, 125, 145)

    private val white = Color.rgb(242, 242, 242)
    private val gray = Color.rgb(170, 170, 170)
    private val gray2 = Color.rgb(115, 115, 115)

    private val red = Color.rgb(220, 110, 110)

    private val prefs by lazy {
        getSharedPreferences("bible_tracking_data", MODE_PRIVATE)
    }

    private val books = mutableListOf<BibleBook>()
    private val read = mutableMapOf<String, MutableSet<Int>>()
    private val achievements = mutableListOf<Achievement>()

    private var currentTracker = "Bible"
    private var currentTestament = "Old Testament"
    private var currentScreen = "home"

    private var selectedTheme = green

    private var backupPendingData: String? = null

    // ------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadDefaultBooks()
        loadData()
        loadTheme()
        loadAchievements()

        // FIRST PAGE — KEEPING THE HOME DESIGN
        showTrackers()
    }

    // ------------------------------------------------------------
    // BACK
    // ------------------------------------------------------------

    @Deprecated("Use OnBackPressedDispatcher for new code")
    override fun onBackPressed() {

        when (currentScreen) {

            "book" -> showTracker()

            "stats" -> showTracker()

            "achievements" -> showTracker()

            "settings" -> showTrackers()

            "tracker" -> showTrackers()

            else -> super.onBackPressed()
        }
    }

    // ============================================================
    // DATA
    // ============================================================

    private fun loadDefaultBooks() {

        if (books.isNotEmpty()) return

        addBooks(
            "Old Testament",
            "Law",
            listOf(
                "Genesis" to 50,
                "Exodus" to 40,
                "Leviticus" to 27,
                "Numbers" to 36,
                "Deuteronomy" to 34
            )
        )

        addBooks(
            "Old Testament",
            "History",
            listOf(
                "Joshua" to 24,
                "Judges" to 21,
                "Ruth" to 4,
                "1 Samuel" to 31,
                "2 Samuel" to 24,
                "1 Kings" to 22,
                "2 Kings" to 25,
                "1 Chronicles" to 29,
                "2 Chronicles" to 36,
                "Ezra" to 10,
                "Nehemiah" to 13,
                "Esther" to 10
            )
        )

        addBooks(
            "Old Testament",
            "Poetry",
            listOf(
                "Job" to 42,
                "Psalms" to 150,
                "Proverbs" to 31,
                "Ecclesiastes" to 12,
                "Song of Solomon" to 8
            )
        )

        addBooks(
            "Old Testament",
            "Major Prophets",
            listOf(
                "Isaiah" to 66,
                "Jeremiah" to 52,
                "Lamentations" to 5,
                "Ezekiel" to 48,
                "Daniel" to 12
            )
        )

        addBooks(
            "Old Testament",
            "Minor Prophets",
            listOf(
                "Hosea" to 14,
                "Joel" to 3,
                "Amos" to 9,
                "Obadiah" to 1,
                "Jonah" to 4,
                "Micah" to 7,
                "Nahum" to 3,
                "Habakkuk" to 3,
                "Zephaniah" to 3,
                "Haggai" to 2,
                "Zechariah" to 14,
                "Malachi" to 4
            )
        )

        addBooks(
            "New Testament",
            "Gospels",
            listOf(
                "Matthew" to 28,
                "Mark" to 16,
                "Luke" to 24,
                "John" to 21
            )
        )

        addBooks(
            "New Testament",
            "History",
            listOf(
                "Acts" to 28
            )
        )

        addBooks(
            "New Testament",
            "Paul's Letters",
            listOf(
                "Romans" to 16,
                "1 Corinthians" to 16,
                "2 Corinthians" to 13,
                "Galatians" to 6,
                "Ephesians" to 6,
                "Philippians" to 4,
                "Colossians" to 4,
                "1 Thessalonians" to 5,
                "2 Thessalonians" to 3,
                "1 Timothy" to 6,
                "2 Timothy" to 4,
                "Titus" to 3,
                "Philemon" to 1
            )
        )

        addBooks(
            "New Testament",
            "General Letters",
            listOf(
                "Hebrews" to 13,
                "James" to 5,
                "1 Peter" to 5,
                "2 Peter" to 3,
                "1 John" to 5,
                "2 John" to 1,
                "3 John" to 1,
                "Jude" to 1
            )
        )

        addBooks(
            "New Testament",
            "Prophecy",
            listOf(
                "Revelation" to 22
            )
        )
    }

    private fun addBooks(
        testament: String,
        group: String,
        list: List<Pair<String, Int>>
    ) {

        list.forEach {
            books.add(
                BibleBook(
                    it.first,
                    it.second,
                    testament,
                    group
                )
            )
        }
    }

    private fun loadData() {

        val saved =
            prefs.getString("read_data", null)
                ?: return

        try {

            val obj = JSONObject(saved)

            for (book in books) {

                val array =
                    obj.optJSONArray(book.name)
                        ?: continue

                val set = mutableSetOf<Int>()

                for (i in 0 until array.length()) {
                    set.add(array.getInt(i))
                }

                read[book.name] = set
            }

        } catch (_: Exception) {
        }
    }

    private fun saveData() {

        val obj = JSONObject()

        read.forEach { (book, chapters) ->

            val array = JSONArray()

            chapters.forEach {
                array.put(it)
            }

            obj.put(book, array)
        }

        prefs.edit()
            .putString("read_data", obj.toString())
            .apply()
    }

    // ============================================================
    // THEME
    // ============================================================

    private fun loadTheme() {

        selectedTheme =
            prefs.getInt(
                "theme_color",
                green
            )
    }

    // ============================================================
    // ACHIEVEMENTS
    // ============================================================

    private fun loadAchievements() {

        achievements.clear()

        achievements.addAll(
            listOf(

                Achievement(
                    "The Man",
                    "Read Matthew",
                    "Matthew"
                ),

                Achievement(
                    "The Lion",
                    "Read Mark",
                    "Mark"
                ),

                Achievement(
                    "The Calf",
                    "Read Luke",
                    "Luke"
                ),

                Achievement(
                    "The Eagle",
                    "Read John",
                    "John"
                ),

                Achievement(
                    "Evangelist",
                    "Read the four Gospels"
                ),

                Achievement(
                    "Apostle",
                    "Read Acts",
                    "Acts"
                ),

                Achievement(
                    "The Rock",
                    "Read 1 Peter and 2 Peter"
                ),

                Achievement(
                    "The Beginning",
                    "Read Genesis",
                    "Genesis"
                ),

                Achievement(
                    "No longer slave",
                    "Read Exodus",
                    "Exodus"
                ),

                Achievement(
                    "Sanctified",
                    "Read Leviticus",
                    "Leviticus"
                ),

                Achievement(
                    "Are we there yet?",
                    "Read Numbers",
                    "Numbers"
                ),

                Achievement(
                    "Covenant",
                    "Read Deuteronomy",
                    "Deuteronomy"
                ),

                Achievement(
                    "No Longer Ruthless",
                    "Read Ruth",
                    "Ruth"
                ),

                Achievement(
                    "Scribe",
                    "Read the 5 books of Moses"
                ),

                Achievement(
                    "Poet",
                    "Read all the poetry books"
                ),

                Achievement(
                    "Historian",
                    "Read all History books"
                ),

                Achievement(
                    "Prophet",
                    "Read all major and minor prophets"
                ),

                Achievement(
                    "Wise Man",
                    "Read Proverbs, Job and Ecclesiastes"
                ),

                Achievement(
                    "Royal",
                    "Read Samuel, Kings and Chronicles"
                ),

                Achievement(
                    "Paul(in)ist",
                    "Read all of Paul's letters"
                ),

                Achievement(
                    "Penpal",
                    "Read all of the letters"
                ),

                Achievement(
                    "Apocalyptic",
                    "Read Daniel, Ezekiel, Zechariah and Revelation"
                ),

                Achievement(
                    "The New Covenant",
                    "Read the entire New Testament"
                ),

                Achievement(
                    "Before Christ",
                    "Read the entire Old Testament"
                ),

                Achievement(
                    "25%",
                    "Read 25% of the Bible"
                ),

                Achievement(
                    "50%",
                    "Read 50% of the Bible"
                ),

                Achievement(
                    "75%",
                    "Read 75% of the Bible"
                ),

                Achievement(
                    "Bible Nerd",
                    "Read the whole Bible"
                )
            )
        )
    }

    // ============================================================
    // UI HELPERS
    // ============================================================

    private fun dp(value: Int): Int {

        return (
            value *
                resources.displayMetrics.density
            ).toInt()
    }

    private fun rounded(
        color: Int,
        radius: Int = 16
    ): android.graphics.drawable.GradientDrawable {

        return android.graphics.drawable.GradientDrawable().apply {

            shape =
                android.graphics.drawable.GradientDrawable.RECTANGLE

            cornerRadius =
                dp(radius).toFloat()

            setColor(color)
        }
    }

    private fun text(
        value: String,
        size: Float = 16f,
        color: Int = white
    ): TextView {

        return TextView(this).apply {

            text = value

            textSize = size

            setTextColor(color)

            gravity =
                Gravity.CENTER_VERTICAL
        }
    }

    private fun addSpace(
        parent: LinearLayout,
        height: Int = 8
    ) {

        parent.addView(
            Space(this),
            LinearLayout.LayoutParams(
                1,
                dp(height)
            )
        )
    }

    private fun setScreen(view: View) {

        setContentView(view)
    }

    private fun modernButton(
        value: String,
        onClick: () -> Unit
    ): TextView {

        return TextView(this).apply {

            text = value

            textSize = 14f

            setTextColor(white)

            gravity = Gravity.CENTER

            setPadding(
                dp(12),
                dp(8),
                dp(12),
                dp(8)
            )

            background =
                rounded(surface2, 13)

            setOnClickListener {
                onClick()
            }
        }
    }

    private fun primaryButton(
        value: String,
        onClick: () -> Unit
    ): TextView {

        return TextView(this).apply {

            text = value

            textSize = 15f

            typeface =
                Typeface.DEFAULT_BOLD

            setTextColor(Color.BLACK)

            gravity = Gravity.CENTER

            background =
                rounded(selectedTheme, 15)

            setOnClickListener {
                onClick()
            }
        }
    }

    private fun page(): LinearLayout {

        return LinearLayout(this).apply {

            orientation =
                LinearLayout.VERTICAL

            setBackgroundColor(bg)

            setPadding(
                dp(16),
                dp(12),
                dp(16),
                dp(18)
            )
        }
    }

    private fun sectionTitle(
        value: String
    ): TextView {

        return text(
            value.uppercase(Locale.getDefault()),
            11f,
            gray
        ).apply {

            typeface =
                Typeface.DEFAULT_BOLD

            letterSpacing = 0.08f

            setPadding(
                dp(2),
                dp(8),
                dp(2),
                dp(8)
            )
        }
    }

    // ============================================================
    // HOME PAGE
    // KEEPING THE FIRST PAGE DESIGN
    // ============================================================

    private fun showTrackers() {

        currentScreen = "home"

        val root =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setBackgroundColor(bg)

                setPadding(
                    dp(16),
                    dp(10),
                    dp(16),
                    dp(20)
                )
            }

        val scroll =
            ScrollView(this).apply {

                isFillViewport = true

                setBackgroundColor(bg)
            }

        val content =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL
            }

        // HEADER

        val header =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL
            }

        val titleBox =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL
            }

        titleBox.addView(
            text(
                "Bible Trackers",
                26f,
                white
            ).apply {

                typeface =
                    Typeface.DEFAULT_BOLD

            },
            LinearLayout.LayoutParams(
                -1,
                dp(38)
            )
        )

        titleBox.addView(
            text(
                "Track your Bible reading progress",
                13f,
                gray
            ).apply {

                setPadding(
                    0,
                    dp(2),
                    0,
                    0
                )

            },
            LinearLayout.LayoutParams(
                -1,
                dp(25)
            )
        )

        header.addView(
            titleBox,
            LinearLayout.LayoutParams(
                0,
                dp(70),
                1f
            )
        )

        val settings =
            TextView(this).apply {

                text = "⚙"

                textSize = 25f

                setTextColor(white)

                gravity = Gravity.CENTER

                background =
                    rounded(surface2, 14)

                setOnClickListener {
                    showSettings()
                }
            }

        header.addView(
            settings,
            LinearLayout.LayoutParams(
                dp(52),
                dp(52)
            ).apply {

                setMargins(
                    dp(8),
                    0,
                    0,
                    0
                )
            }
        )

        content.addView(
            header,
            LinearLayout.LayoutParams(
                -1,
                dp(76)
            )
        )

        // ADD BUTTON

        val addButton =
            primaryButton(
                "＋   Add new Bible tracker"
            ) {
                addTrackerDialog()
            }

        content.addView(
            addButton,
            LinearLayout.LayoutParams(
                -1,
                dp(54)
            ).apply {

                setMargins(
                    0,
                    dp(4),
                    0,
                    dp(24)
                )
            }
        )

        // SECTION

        content.addView(
            sectionTitle("Your Trackers")
        )

        // CARD

        val card =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(18),
                    dp(17),
                    dp(18),
                    dp(16)
                )

                background =
                    rounded(surface2, 18)

                setOnClickListener {
                    showTracker()
                }
            }

        val topRow =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL
            }

        val icon =
            TextView(this).apply {

                text = "📖"

                textSize = 28f

                gravity = Gravity.CENTER

                background =
                    rounded(selectedTheme, 14)
            }

        topRow.addView(
            icon,
            LinearLayout.LayoutParams(
                dp(58),
                dp(58)
            )
        )

        val info =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(14),
                    0,
                    dp(8),
                    0
                )
            }

        info.addView(
            text(
                "Bible",
                20f,
                white
            ).apply {

                typeface =
                    Typeface.DEFAULT_BOLD

            },
            LinearLayout.LayoutParams(
                -1,
                dp(30)
            )
        )

        info.addView(
            text(
                "66 books  •  1,189 chapters",
                13f,
                gray
            ),
            LinearLayout.LayoutParams(
                -1,
                dp(25)
            )
        )

        topRow.addView(
            info,
            LinearLayout.LayoutParams(
                0,
                dp(60),
                1f
            )
        )

        val percent =
            overallPercent()

        topRow.addView(
            text(
                "$percent%",
                18f,
                blue
            ).apply {

                gravity = Gravity.CENTER

                typeface =
                    Typeface.DEFAULT_BOLD

            },
            LinearLayout.LayoutParams(
                dp(58),
                dp(58)
            )
        )

        card.addView(
            topRow,
            LinearLayout.LayoutParams(
                -1,
                dp(62)
            )
        )

        val progress =
            ProgressBar(
                this,
                null,
                android.R.attr.progressBarStyleHorizontal
            ).apply {

                max = 100

                progress =
                    overallPercentValue()
                        .toInt()

                progressTintList =
                    ColorStateList.valueOf(blue)

                progressBackgroundTintList =
                    ColorStateList.valueOf(
                        Color.rgb(65, 65, 65)
                    )
            }

        card.addView(
            progress,
            LinearLayout.LayoutParams(
                -1,
                dp(7)
            ).apply {

                setMargins(
                    0,
                    dp(16),
                    0,
                    dp(12)
                )
            }
        )

        val footer =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL
            }

        footer.addView(
            text(
                "Tap to continue reading",
                13f,
                gray
            ),
            LinearLayout.LayoutParams(
                0,
                dp(26),
                1f
            )
        )

        footer.addView(
            text(
                "›",
                28f,
                white
            ).apply {

                gravity = Gravity.CENTER
            },
            LinearLayout.LayoutParams(
                dp(32),
                dp(28)
            )
        )

        card.addView(footer)

        content.addView(
            card,
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {

                setMargins(
                    0,
                    0,
                    0,
                    dp(24)
                )
            }
        )

        // QUICK OVERVIEW

        content.addView(
            sectionTitle("Quick Overview")
        )

        val totalRead =
            read.values.sumOf {
                it.size
            }

        val completedBooks =
            books.count {
                (read[it.name]?.size ?: 0) >=
                    it.chapters
            }

        val stats =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL
            }

        fun stat(
            number: String,
            label: String
        ): LinearLayout {

            return LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER

                setPadding(
                    dp(6),
                    dp(12),
                    dp(6),
                    dp(10)
                )

                background =
                    rounded(surface2, 16)

                addView(
                    text(
                        number,
                        21f,
                        white
                    ).apply {

                        gravity =
                            Gravity.CENTER

                        typeface =
                            Typeface.DEFAULT_BOLD

                    },
                    LinearLayout.LayoutParams(
                        -1,
                        dp(32)
                    )
                )

                addView(
                    text(
                        label,
                        11f,
                        gray
                    ).apply {

                        gravity =
                            Gravity.CENTER

                    },
                    LinearLayout.LayoutParams(
                        -1,
                        dp(25)
                    )
                )
            }
        }

        stats.addView(
            stat(
                totalRead.toString(),
                "Chapters read"
            ),
            LinearLayout.LayoutParams(
                0,
                dp(78),
                1f
            ).apply {

                setMargins(
                    0,
                    0,
                    dp(5),
                    0
                )
            }
        )

        stats.addView(
            stat(
                completedBooks.toString(),
                "Books completed"
            ),
            LinearLayout.LayoutParams(
                0,
                dp(78),
                1f
            ).apply {

                setMargins(
                    dp(5),
                    0,
                    dp(5),
                    0
                )
            }
        )

        stats.addView(
            stat(
                "${overallPercent()}%",
                "Overall progress"
            ),
            LinearLayout.LayoutParams(
                0,
                dp(78),
                1f
            ).apply {

                setMargins(
                    dp(5),
                    0,
                    0,
                    0
                )
            }
        )

        content.addView(
            stats,
            LinearLayout.LayoutParams(
                -1,
                dp(78)
            )
        )

        content.addView(
            Space(this),
            LinearLayout.LayoutParams(
                1,
                dp(30)
            )
        )

        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        setScreen(root)
    }

    // ============================================================
    // ADD TRACKER
    // ============================================================

    private fun addTrackerDialog() {

        val input =
            EditText(this).apply {

                hint = "Tracker name"

                setTextColor(white)

                setHintTextColor(gray)
            }

        val box =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(24),
                    dp(10),
                    dp(24),
                    dp(4)
                )

                addView(input)
            }

        AlertDialog.Builder(this)
            .setTitle("Add new Bible tracker")
            .setView(box)
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Save"
            ) { _, _ ->

                val name =
                    input.text
                        .toString()
                        .trim()

                if (name.isNotEmpty()) {

                    currentTracker = name

                    Toast.makeText(
                        this,
                        "$name created",
                        Toast.LENGTH_SHORT
                    ).show()

                    showTrackers()
                }
            }
            .show()
    }

    // ============================================================
    // TRACKER PAGE
    // ============================================================

    private fun showTracker() {

        currentScreen = "tracker"

        val screen = page()

        // HEADER

        val header =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL
            }

        header.addView(
            modernButton("‹") {
                showTrackers()
            },
            LinearLayout.LayoutParams(
                dp(48),
                dp(48)
            )
        )

        val title =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER
            }

        title.addView(
            text(
                "Bible",
                21f,
                white
            ).apply {

                gravity =
                    Gravity.CENTER

                typeface =
                    Typeface.DEFAULT_BOLD
            }
        )

        title.addView(
            text(
                "${overallPercent()}% complete",
                11f,
                gray
            ).apply {

                gravity =
                    Gravity.CENTER
            }
        )

        header.addView(
            title,
            LinearLayout.LayoutParams(
                0,
                dp(56),
                1f
            )
        )

        header.addView(
            modernButton("🏆") {
                showAchievements()
            },
            LinearLayout.LayoutParams(
                dp(50),
                dp(48)
            )
        )

        screen.addView(header)

        addSpace(screen, 14)

        // PROGRESS CARD

        val progressCard =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(17),
                    dp(15),
                    dp(17),
                    dp(15)
                )

                background =
                    rounded(surface2, 18)
            }

        progressCard.addView(
            text(
                "Bible reading progress",
                15f,
                white
            ).apply {

                typeface =
                    Typeface.DEFAULT_BOLD
            }
        )

        progressCard.addView(
            text(
                "${countReadAll()} of 1,189 chapters read",
                12f,
                gray
            ).apply {

                setPadding(
                    0,
                    dp(4),
                    0,
                    dp(10)
                )
            }
        )

        progressCard.addView(
            ProgressBar(
                this,
                null,
                android.R.attr.progressBarStyleHorizontal
            ).apply {

                max = 100

                progress =
                    overallPercentValue()
                        .toInt()

                progressTintList =
                    ColorStateList.valueOf(
                        selectedTheme
                    )

                progressBackgroundTintList =
                    ColorStateList.valueOf(
                        Color.rgb(
                            62,
                            62,
                            62
                        )
                    )
            },
            LinearLayout.LayoutParams(
                -1,
                dp(7)
            )
        )

        screen.addView(
            progressCard,
            LinearLayout.LayoutParams(
                -1,
                dp(92)
            )
        )

        addSpace(screen, 14)

        // TABS

        val tabs =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                setPadding(
                    dp(3),
                    dp(3),
                    dp(3),
                    dp(3)
                )

                background =
                    rounded(surface2, 14)
            }

        val old =
            modernButton("Old Testament") {
                currentTestament =
                    "Old Testament"

                showTracker()
            }

        val new =
            modernButton("New Testament") {
                currentTestament =
                    "New Testament"

                showTracker()
            }

        tabs.addView(
            old,
            LinearLayout.LayoutParams(
                0,
                dp(45),
                1f
            )
        )

        tabs.addView(
            new,
            LinearLayout.LayoutParams(
                0,
                dp(45),
                1f
            )
        )

        screen.addView(tabs)

        addSpace(screen, 10)

        // STATS BUTTON

        screen.addView(
            modernButton("📊  View statistics") {
                showStats()
            },
            LinearLayout.LayoutParams(
                -1,
                dp(46)
            )
        )

        addSpace(screen, 10)

        // BOOK LIST

        val scroll =
            ScrollView(this)

        val content =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL
            }

        val groups =
            books
                .filter {
                    it.testament ==
                        currentTestament
                }
                .map {
                    it.group
                }
                .distinct()

        groups.forEach { group ->

            content.addView(
                sectionTitle(group)
            )

            books
                .filter {
                    it.testament ==
                        currentTestament &&
                    it.group ==
                        group
                }
                .forEach { book ->

                    content.addView(
                        bookCard(book),
                        LinearLayout.LayoutParams(
                            -1,
                            dp(74)
                        ).apply {

                            setMargins(
                                0,
                                dp(4),
                                0,
                                dp(4)
                            )
                        }
                    )
                }
        }

        scroll.addView(content)

        screen.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        setScreen(screen)
    }

    private fun bookCard(
        book: BibleBook
    ): LinearLayout {

        val done =
            read[book.name]?.size ?: 0

        val percent =
            if (book.chapters == 0)
                0
            else
                (
                    done * 100 /
                        book.chapters
                    )

        val card =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL

                setPadding(
                    dp(15),
                    dp(8),
                    dp(10),
                    dp(8)
                )

                background =
                    rounded(surface2, 15)

                setOnClickListener {
                    showBook(book)
                }
            }

        val icon =
            TextView(this).apply {

                text =
                    if (percent == 100)
                        "✓"
                    else
                        "📖"

                textSize =
                    if (percent == 100)
                        20f
                    else
                        19f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    if (percent == 100)
                        Color.BLACK
                    else
                        white
                )

                background =
                    rounded(
                        if (percent == 100)
                            selectedTheme
                        else
                            surface3,
                        12
                    )
            }

        card.addView(
            icon,
            LinearLayout.LayoutParams(
                dp(45),
                dp(45)
            )
        )

        val info =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(12),
                    0,
                    dp(8),
                    0
                )
            }

        info.addView(
            text(
                book.name,
                15f,
                white
            ).apply {

                typeface =
                    Typeface.DEFAULT_BOLD
            }
        )

        info.addView(
            text(
                "$done / ${book.chapters} chapters",
                11f,
                gray
            )
        )

        card.addView(
            info,
            LinearLayout.LayoutParams(
                0,
                dp(50),
                1f
            )
        )

        val percentText =
            text(
                "$percent%",
                14f,
                if (percent == 100)
                    selectedTheme
                else
                    blue
            ).apply {

                gravity =
                    Gravity.CENTER

                typeface =
                    Typeface.DEFAULT_BOLD
            }

        card.addView(
            percentText,
            LinearLayout.LayoutParams(
                dp(55),
                dp(45)
            )
        )

        return card
    }

    // ============================================================
    // BOOK PAGE
    // ============================================================

    private fun showBook(
        book: BibleBook
    ) {

        currentScreen = "book"

        val screen = page()

        val done =
            read[book.name]?.size ?: 0

        val percent =
            if (book.chapters == 0)
                0
            else
                done * 100 / book.chapters

        // HEADER

        val header =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL
            }

        header.addView(
            modernButton("‹") {
                showTracker()
            },
            LinearLayout.LayoutParams(
                dp(48),
                dp(48)
            )
        )

        header.addView(
            text(
                book.name,
                21f,
                white
            ).apply {

                gravity =
                    Gravity.CENTER

                typeface =
                    Typeface.DEFAULT_BOLD

            },
            LinearLayout.LayoutParams(
                0,
                dp(48),
                1f
            )
        )

        header.addView(
            text(
                "$percent%",
                15f,
                blue
            ).apply {

                gravity =
                    Gravity.CENTER

                typeface =
                    Typeface.DEFAULT_BOLD

            },
            LinearLayout.LayoutParams(
                dp(55),
                dp(48)
            )
        )

        screen.addView(header)

        addSpace(screen, 12)

        // BOOK PROGRESS

        val progressCard =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(16),
                    dp(13),
                    dp(16),
                    dp(13)
                )

                background =
                    rounded(surface2, 17)
            }

        progressCard.addView(
            text(
                "$done of ${book.chapters} chapters completed",
                13f,
                gray
            )
        )

        progressCard.addView(
            ProgressBar(
                this,
                null,
                android.R.attr.progressBarStyleHorizontal
            ).apply {

                max = 100

                progress = percent

                progressTintList =
                    ColorStateList.valueOf(
                        selectedTheme
                    )

                progressBackgroundTintList =
                    ColorStateList.valueOf(
                        Color.rgb(
                            65,
                            65,
                            65
                        )
                    )
            },
            LinearLayout.LayoutParams(
                -1,
                dp(7)
            ).apply {

                setMargins(
                    0,
                    dp(10),
                    0,
                    0
                )
            }
        )

        screen.addView(
            progressCard,
            LinearLayout.LayoutParams(
                -1,
                dp(70)
            )
        )

        addSpace(screen, 10)

        // ACTIONS

        val actions =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL
            }

        actions.addView(
            primaryButton("✓ Mark all") {
                markAll(book)
            },
            LinearLayout.LayoutParams(
                0,
                dp(46),
                1f
            ).apply {

                setMargins(
                    0,
                    0,
                    dp(4),
                    0
                )
            }
        )

        actions.addView(
            modernButton("Clear") {
                cancelAll(book)
            },
            LinearLayout.LayoutParams(
                0,
                dp(46),
                0.65f
            ).apply {

                setMargins(
                    dp(4),
                    0,
                    0,
                    0
                )
            }
        )

        screen.addView(actions)

        addSpace(screen, 10)

        screen.addView(
            text(
                "Tap a chapter to mark it as read",
                12f,
                gray
            ).apply {

                gravity =
                    Gravity.CENTER

                setPadding(
                    0,
                    dp(4),
                    0,
                    dp(8)
                )
            }
        )

        // CHAPTER GRID

        val scroll =
            ScrollView(this)

        val grid =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL
            }

        var row:
            LinearLayout? = null

        for (chapter in 1..book.chapters) {

            if ((chapter - 1) % 5 == 0) {

                row =
                    LinearLayout(this).apply {

                        orientation =
                            LinearLayout.HORIZONTAL
                    }

                grid.addView(row)
            }

            val isRead =
                read[book.name]
                    ?.contains(chapter) == true

            val chapterButton =
                TextView(this).apply {

                    text =
                        chapter.toString()

                    textSize = 15f

                    gravity =
                        Gravity.CENTER

                    typeface =
                        Typeface.DEFAULT_BOLD

                    setTextColor(
                        if (isRead)
                            Color.BLACK
                        else
                            white
                    )

                    background =
                        rounded(
                            if (isRead)
                                selectedTheme
                            else
                                surface2,
                            13
                        )

                    setOnClickListener {
                        toggleChapter(
                            book,
                            chapter
                        )
                    }
                }

            row?.addView(
                chapterButton,
                LinearLayout.LayoutParams(
                    0,
                    dp(58),
                    1f
                ).apply {

                    setMargins(
                        dp(3),
                        dp(3),
                        dp(3),
                        dp(3)
                    )
                }
            )
        }

        scroll.addView(grid)

        screen.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        setScreen(screen)
    }

    private fun toggleChapter(
        book: BibleBook,
        chapter: Int
    ) {

        val set =
            read.getOrPut(
                book.name
            ) {
                mutableSetOf()
            }

        if (set.contains(chapter)) {

            set.remove(chapter)

        } else {

            set.add(chapter)
        }

        saveData()

        checkAchievements()

        showBook(book)
    }

    private fun markAll(
        book: BibleBook
    ) {

        val set =
            read.getOrPut(
                book.name
            ) {
                mutableSetOf()
            }

        for (i in 1..book.chapters) {
            set.add(i)
        }

        saveData()

        checkAchievements()

        showBook(book)
    }

    private fun cancelAll(
        book: BibleBook
    ) {

        read[book.name]?.clear()

        saveData()

        checkAchievements()

        showBook(book)
    }

    // ============================================================
    // STATS
    // ============================================================

    private fun showStats() {

        currentScreen = "stats"

        val screen = page()

        val header =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL
            }

        header.addView(
            modernButton("‹") {
                showTracker()
            },
            LinearLayout.LayoutParams(
                dp(48),
                dp(48)
            )
        )

        header.addView(
            text(
                "Statistics",
                22f,
                white
            ).apply {

                gravity =
                    Gravity.CENTER

                typeface =
                    Typeface.DEFAULT_BOLD
            },
            LinearLayout.LayoutParams(
                0,
                dp(48),
                1f
            )
        )

        header.addView(
            Space(this),
            LinearLayout.LayoutParams(
                dp(48),
                dp(48)
            )
        )

        screen.addView(header)

        addSpace(screen, 14)

        // BIG OVERALL CARD

        val overall =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER

                setPadding(
                    dp(20),
                    dp(22),
                    dp(20),
                    dp(22)
                )

                background =
                    rounded(surface2, 20)
            }

        overall.addView(
            text(
                "${overallPercent()}%",
                42f,
                blue
            ).apply {

                gravity =
                    Gravity.CENTER

                typeface =
                    Typeface.DEFAULT_BOLD
            }
        )

        overall.addView(
            text(
                "Whole Bible",
                14f,
                gray
            ).apply {

                gravity =
                    Gravity.CENTER

                setPadding(
                    0,
                    dp(3),
                    0,
                    dp(10)
                )
            }
        )

        overall.addView(
            ProgressBar(
                this,
                null,
                android.R.attr.progressBarStyleHorizontal
            ).apply {

                max = 100

                progress =
                    overallPercentValue()
                        .toInt()

                progressTintList =
                    ColorStateList.valueOf(
                        blue
                    )

                progressBackgroundTintList =
                    ColorStateList.valueOf(
                        Color.rgb(
                            65,
                            65,
                            65
                        )
                    )
            },
            LinearLayout.LayoutParams(
                -1,
                dp(8)
            )
        )

        overall.addView(
            text(
                "${countReadAll()} / 1,189 chapters",
                12f,
                gray
            ).apply {

                gravity =
                    Gravity.CENTER

                setPadding(
                    0,
                    dp(8),
                    0,
                    0
                )
            }
        )

        screen.addView(
            overall,
            LinearLayout.LayoutParams(
                -1,
                dp(190)
            )
        )

        addSpace(screen, 14)

        screen.addView(
            sectionTitle("Testament progress")
        )

        // OT CARD

        screen.addView(
            statsTestamentCard(
                "Old Testament",
                percentageForValue(
                    "Old Testament"
                ),
                countRead(
                    "Old Testament"
                ),
                books
                    .filter {
                        it.testament ==
                            "Old Testament"
                    }
                    .sumOf {
                        it.chapters
                    }
            ),
            LinearLayout.LayoutParams(
                -1,
                dp(105)
            ).apply {

                setMargins(
                    0,
                    dp(4),
                    0,
                    dp(5)
                )
            }
        )

        // NT CARD

        screen.addView(
            statsTestamentCard(
                "New Testament",
                percentageForValue(
                    "New Testament"
                ),
                countRead(
                    "New Testament"
                ),
                books
                    .filter {
                        it.testament ==
                            "New Testament"
                    }
                    .sumOf {
                        it.chapters
                    }
            ),
            LinearLayout.LayoutParams(
                -1,
                dp(105)
            ).apply {

                setMargins(
                    0,
                    dp(5),
                    0,
                    dp(12)
                )
            }
        )

        screen.addView(
            sectionTitle("Reading summary")
        )

        val summary =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL
            }

        fun summaryBox(
            value: String,
            label: String
        ): LinearLayout {

            return LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER

                background =
                    rounded(surface2, 16)

                addView(
                    text(
                        value,
                        21f,
                        white
                    ).apply {

                        gravity =
                            Gravity.CENTER

                        typeface =
                            Typeface.DEFAULT_BOLD
                    },
                    LinearLayout.LayoutParams(
                        -1,
                        dp(32)
                    )
                )

                addView(
                    text(
                        label,
                        11f,
                        gray
                    ).apply {

                        gravity =
                            Gravity.CENTER
                    },
                    LinearLayout.LayoutParams(
                        -1,
                        dp(26)
                    )
                )
            }
        }

        summary.addView(
            summaryBox(
                countReadAll().toString(),
                "Chapters"
            ),
            LinearLayout.LayoutParams(
                0,
                dp(70),
                1f
            ).apply {

                setMargins(
                    0,
                    0,
                    dp(4),
                    0
                )
            }
        )

        summary.addView(
            summaryBox(
                completedBooks(
                    "Old Testament"
                ).toString(),
                "OT books"
            ),
            LinearLayout.LayoutParams(
                0,
                dp(70),
                1f
            ).apply {

                setMargins(
                    dp(4),
                    0,
                    dp(4),
                    0
                )
            }
        )

        summary.addView(
            summaryBox(
                completedBooks(
                    "New Testament"
                ).toString(),
                "NT books"
            ),
            LinearLayout.LayoutParams(
                0,
                dp(70),
                1f
            ).apply {

                setMargins(
                    dp(4),
                    0,
                    0,
                    0
                )
            }
        )

        screen.addView(
            summary,
            LinearLayout.LayoutParams(
                -1,
                dp(70)
            )
        )

        setScreen(screen)
    }

    private fun statsTestamentCard(
        name: String,
        percent: Double,
        done: Int,
        total: Int
    ): LinearLayout {

        val card =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(15),
                    dp(10),
                    dp(15),
                    dp(10)
                )

                background =
                    rounded(surface2, 16)
            }

        val row =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL
            }

        row.addView(
            text(
                name,
                14f,
                white
            ).apply {

                typeface =
                    Typeface.DEFAULT_BOLD
            },
            LinearLayout.LayoutParams(
                0,
                dp(28),
                1f
            )
        )

        row.addView(
            text(
                String.format(
                    Locale.getDefault(),
                    "%.1f%%",
                    percent
                ),
                14f,
                blue
            ).apply {

                gravity =
                    Gravity.CENTER
            },
            LinearLayout.LayoutParams(
                dp(60),
                dp(28)
            )
        )

        card.addView(row)

        card.addView(
            ProgressBar(
                this,
                null,
                android.R.attr.progressBarStyleHorizontal
            ).apply {

                max = 100

                progress =
                    percent.toInt()

                progressTintList =
                    ColorStateList.valueOf(
                        blue
                    )

                progressBackgroundTintList =
                    ColorStateList.valueOf(
                        Color.rgb(
                            65,
                            65,
                            65
                        )
                    )
            },
            LinearLayout.LayoutParams(
                -1,
                dp(7)
            ).apply {

                setMargins(
                    0,
                    dp(5),
                    0,
                    dp(5)
                )
            }
        )

        card.addView(
            text(
                "$done / $total chapters",
                10f,
                gray
            )
        )

        return card
    }

    // ============================================================
    // ACHIEVEMENTS
    // ============================================================

    private fun showAchievements() {

        currentScreen =
            "achievements"

        checkAchievements(
            false
        )

        val screen = page()

        val unlocked =
            achievements.count {
                it.unlocked
            }

        val header =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL
            }

        header.addView(
            modernButton("‹") {
                showTracker()
            },
            LinearLayout.LayoutParams(
                dp(48),
                dp(48)
            )
        )

        header.addView(
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER

                addView(
                    text(
                        "Achievements",
                        21f,
                        white
                    ).apply {

                        gravity =
                            Gravity.CENTER

                        typeface =
                            Typeface.DEFAULT_BOLD
                    }
                )

                addView(
                    text(
                        "$unlocked / ${achievements.size} unlocked",
                        10f,
                        gray
                    ).apply {

                        gravity =
                            Gravity.CENTER
                    }
                )
            },
            LinearLayout.LayoutParams(
                0,
                dp(52),
                1f
            )
        )

        header.addView(
            TextView(this).apply {

                text = "🏆"

                textSize = 22f

                gravity =
                    Gravity.CENTER
            },
            LinearLayout.LayoutParams(
                dp(48),
                dp(48)
            )
        )

        screen.addView(header)

        addSpace(screen, 12)

        // ACHIEVEMENT SUMMARY

        val summary =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL

                setPadding(
                    dp(15),
                    dp(10),
                    dp(15),
                    dp(10)
                )

                background =
                    rounded(surface2, 17)
            }

        summary.addView(
            text(
                "Your progress",
                13f,
                white
            ).apply {

                typeface =
                    Typeface.DEFAULT_BOLD
            },
            LinearLayout.LayoutParams(
                0,
                dp(28),
                1f
            )
        )

        summary.addView(
            text(
                "$unlocked / ${achievements.size}",
                13f,
                blue
            ).apply {

                gravity =
                    Gravity.CENTER
            },
            LinearLayout.LayoutParams(
                dp(65),
                dp(28)
            )
        )

        screen.addView(
            summary,
            LinearLayout.LayoutParams(
                -1,
                dp(52)
            )
        )

        addSpace(screen, 10)

        screen.addView(
            primaryButton("＋  Add Achievement") {
                addAchievementDialog()
            },
            LinearLayout.LayoutParams(
                -1,
                dp(48)
            )
        )

        addSpace(screen, 10)

        val scroll =
            ScrollView(this)

        val list =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL
            }

        achievements.forEachIndexed {
                index,
                achievement ->

            list.addView(
                achievementCard(
                    index,
                    achievement
                ),
                LinearLayout.LayoutParams(
                    -1,
                    dp(82)
                ).apply {

                    setMargins(
                        0,
                        dp(3),
                        0,
                        dp(3)
                    )
                }
            )
        }

        scroll.addView(list)

        screen.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        setScreen(screen)
    }

    private fun achievementCard(
        index: Int,
        achievement: Achievement
    ): LinearLayout {

        val unlocked =
            achievement.unlocked

        val card =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL

                setPadding(
                    dp(10),
                    dp(8),
                    dp(10),
                    dp(8)
                )

                background =
                    rounded(
                        if (unlocked)
                            Color.rgb(
                                55,
                                67,
                                58
                            )
                        else
                            surface2,
                        15
                    )

                setOnClickListener {
                    editAchievementDialog(
                        index
                    )
                }
            }

        val icon =
            TextView(this).apply {

                text =
                    if (unlocked)
                        "🏆"
                    else
                        "🔒"

                textSize = 22f

                gravity =
                    Gravity.CENTER

                background =
                    rounded(
                        if (unlocked)
                            selectedTheme
                        else
                            surface3,
                        12
                    )

                if (!unlocked) {
                    setTextColor(gray)
                }
            }

        card.addView(
            icon,
            LinearLayout.LayoutParams(
                dp(48),
                dp(56)
            )
        )

        val info =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER_VERTICAL

                setPadding(
                    dp(12),
                    0,
                    dp(5),
                    0
                )
            }

        info.addView(
            text(
                achievement.name,
                14f,
                if (unlocked)
                    white
                else
                    gray
            ).apply {

                typeface =
                    Typeface.DEFAULT_BOLD
            }
        )

        info.addView(
            text(
                achievement.description,
                11f,
                if (unlocked)
                    gray
                else
                    gray2
            )
        )

        card.addView(
            info,
            LinearLayout.LayoutParams(
                0,
                dp(60),
                1f
            )
        )

        card.addView(
            text(
                if (unlocked)
                    "✓"
                else
                    "›",
                18f,
                if (unlocked)
                    selectedTheme
                else
                    gray
            ).apply {

                gravity =
                    Gravity.CENTER
            },
            LinearLayout.LayoutParams(
                dp(32),
                dp(50)
            )
        )

        return card
    }

    private fun addAchievementDialog() {

        val layout =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(22),
                    dp(5),
                    dp(22),
                    dp(5)
                )
            }

        val name =
            EditText(this).apply {

                hint = "Achievement name"

                setTextColor(white)

                setHintTextColor(gray)
            }

        val description =
            EditText(this).apply {

                hint = "Description"

                setTextColor(white)

                setHintTextColor(gray)
            }

        layout.addView(name)

        layout.addView(
            description
        )

        AlertDialog.Builder(this)
            .setTitle(
                "Add Achievement"
            )
            .setView(layout)
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Save"
            ) { _, _ ->

                achievements.add(
                    Achievement(
                        name.text
                            .toString()
                            .ifEmpty {
                                "New Achievement"
                            },
                        description.text
                            .toString()
                    )
                )

                showAchievements()
            }
            .show()
    }

    private fun editAchievementDialog(
        index: Int
    ) {

        val achievement =
            achievements[index]

        val layout =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(22),
                    dp(5),
                    dp(22),
                    dp(5)
                )
            }

        val name =
            EditText(this).apply {

                setText(
                    achievement.name
                )

                setTextColor(white)
            }

        val desc =
            EditText(this).apply {

                setText(
                    achievement.description
                )

                setTextColor(white)
            }

        layout.addView(name)

        layout.addView(desc)

        AlertDialog.Builder(this)
            .setTitle(
                "Edit Achievement"
            )
            .setView(layout)
            .setNegativeButton(
                "Delete"
            ) { _, _ ->

                achievements.removeAt(
                    index
                )

                showAchievements()
            }
            .setPositiveButton(
                "Save"
            ) { _, _ ->

                achievement.name =
                    name.text.toString()

                achievement.description =
                    desc.text.toString()

                showAchievements()
            }
            .show()
    }

    // ============================================================
    // ACHIEVEMENT LOGIC
    // ============================================================

    private fun checkAchievements(
        showPopup: Boolean = true
    ) {

        fun complete(
            bookName: String
        ): Boolean {

            val book =
                books.firstOrNull {
                    it.name == bookName
                }
                    ?: return false

            return (
                read[book.name]
                    ?.size ?: 0
                ) >= book.chapters
        }

        achievements.forEach { a ->

            val wasUnlocked =
                a.unlocked

            a.unlocked =
                when (a.name) {

                    "The Man" ->
                        complete("Matthew")

                    "The Lion" ->
                        complete("Mark")

                    "The Calf" ->
                        complete("Luke")

                    "The Eagle" ->
                        complete("John")

                    "Evangelist" ->
                        complete("Matthew") &&
                        complete("Mark") &&
                        complete("Luke") &&
                        complete("John")

                    "Apostle" ->
                        complete("Acts")

                    "The Rock" ->
                        complete("1 Peter") &&
                        complete("2 Peter")

                    "The Beginning" ->
                        complete("Genesis")

                    "No longer slave" ->
                        complete("Exodus")

                    "Sanctified" ->
                        complete("Leviticus")

                    "Are we there yet?" ->
                        complete("Numbers")

                    "Covenant" ->
                        complete("Deuteronomy")

                    "No Longer Ruthless" ->
                        complete("Ruth")

                    "Scribe" ->
                        books
                            .filter {
                                it.group == "Law"
                            }
                            .all {
                                complete(it.name)
                            }

                    "Poet" ->
                        books
                            .filter {
                                it.group == "Poetry"
                            }
                            .all {
                                complete(it.name)
                            }

                    "Historian" ->
                        books
                            .filter {
                                it.group == "History"
                            }
                            .all {
                                complete(it.name)
                            }

                    "Prophet" ->
                        books
                            .filter {
                                it.group ==
                                    "Major Prophets" ||
                                it.group ==
                                    "Minor Prophets"
                            }
                            .all {
                                complete(it.name)
                            }

                    "Wise Man" ->
                        complete("Proverbs") &&
                        complete("Job") &&
                        complete("Ecclesiastes")

                    "Royal" -> {

                        val royal =
                            listOf(
                                "1 Samuel",
                                "2 Samuel",
                                "1 Kings",
                                "2 Kings",
                                "1 Chronicles",
                                "2 Chronicles"
                            )

                        royal.all {
                            complete(it)
                        }
                    }

                    "Paul(in)ist" ->
                        books
                            .filter {
                                it.group ==
                                    "Paul's Letters"
                            }
                            .all {
                                complete(it.name)
                            }

                    "Penpal" -> {

                        val letters =
                            books.filter {
                                it.group ==
                                    "Paul's Letters" ||
                                it.group ==
                                    "General Letters"
                            }

                        letters.all {
                            complete(it.name)
                        }
                    }

                    "Apocalyptic" ->
                        complete("Daniel") &&
                        complete("Ezekiel") &&
                        complete("Zechariah") &&
                        complete("Revelation")

                    "The New Covenant" ->
                        percentageForValue(
                            "New Testament"
                        ) >= 100.0

                    "Before Christ" ->
                        percentageForValue(
                            "Old Testament"
                        ) >= 100.0

                    "25%" ->
                        overallPercentValue() >= 25.0

                    "50%" ->
                        overallPercentValue() >= 50.0

                    "75%" ->
                        overallPercentValue() >= 75.0

                    "Bible Nerd" ->
                        overallPercentValue() >= 100.0

                    else ->
                        a.unlocked
                }

            if (
                showPopup &&
                !wasUnlocked &&
                a.unlocked
            ) {

                showAchievementUnlocked(a)
            }
        }
    }

    private fun showAchievementUnlocked(
        achievement: Achievement
    ) {

        val box =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER

                setPadding(
                    dp(25),
                    dp(20),
                    dp(25),
                    dp(20)
                )
            }

        box.addView(
            text(
                "🏆",
                52f
            ).apply {

                gravity =
                    Gravity.CENTER
            }
        )

        box.addView(
            text(
                "Achievement unlocked!",
                20f,
                blue
            ).apply {

                gravity =
                    Gravity.CENTER

                typeface =
                    Typeface.DEFAULT_BOLD
            }
        )

        box.addView(
            text(
                achievement.name,
                18f,
                white
            ).apply {

                gravity =
                    Gravity.CENTER

                setPadding(
                    0,
                    dp(8),
                    0,
                    dp(3)
                )
            }
        )

        box.addView(
            text(
                achievement.description,
                13f,
                gray
            ).apply {

                gravity =
                    Gravity.CENTER
            }
        )

        AlertDialog.Builder(this)
            .setView(box)
            .setPositiveButton(
                "Hurray!"
            ) { _, _ ->

                showAchievements()
            }
            .setOnDismissListener {

                if (currentScreen ==
                    "achievements"
                ) {
                    showAchievements()
                }
            }
            .show()
    }

    // ============================================================
    // SETTINGS
    // ============================================================

    private fun showSettings() {

        currentScreen =
            "settings"

        val screen = page()

        val header =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL
            }

        header.addView(
            modernButton("‹") {
                showTrackers()
            },
            LinearLayout.LayoutParams(
                dp(48),
                dp(48)
            )
        )

        header.addView(
            text(
                "Settings",
                22f,
                white
            ).apply {

                gravity =
                    Gravity.CENTER

                typeface =
                    Typeface.DEFAULT_BOLD
            },
            LinearLayout.LayoutParams(
                0,
                dp(48),
                1f
            )
        )

        header.addView(
            Space(this),
            LinearLayout.LayoutParams(
                dp(48),
                dp(48)
            )
        )

        screen.addView(header)

        addSpace(screen, 15)

        screen.addView(
            sectionTitle("Data")
        )

        screen.addView(
            settingsCard(
                "💾",
                "Backup progress",
                "Save your Bible reading progress to a file"
            ) {
                backup()
            },
            LinearLayout.LayoutParams(
                -1,
                dp(78)
            ).apply {

                setMargins(
                    0,
                    dp(4),
                    0,
                    dp(5)
                )
            }
        )

        screen.addView(
            settingsCard(
                "📂",
                "Restore progress",
                "Load your progress from a backup file"
            ) {
                restore()
            },
            LinearLayout.LayoutParams(
                -1,
                dp(78)
            ).apply {

                setMargins(
                    0,
                    dp(5),
                    0,
                    dp(15)
                )
            }
        )

        screen.addView(
            sectionTitle("Theme")
        )

        val colors =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER
            }

        val themeColors =
            listOf(
                green,
                blue,
                Color.rgb(
                    180,
                    140,
                    220
                ),
                Color.rgb(
                    210,
                    150,
                    110
                )
            )

        themeColors.forEach { color ->

            val circle =
                TextView(this).apply {

                    text =
                        if (
                            selectedTheme ==
                            color
                        )
                            "✓"
                        else
                            "●"

                    textSize =
                        if (
                            selectedTheme ==
                            color
                        )
                            17f
                        else
                            30f

                    setTextColor(
                        if (
                            selectedTheme ==
                            color
                        )
                            Color.BLACK
                        else
                            color
                    )

                    gravity =
                        Gravity.CENTER

                    background =
                        if (
                            selectedTheme ==
                            color
                        )
                            rounded(
                                color,
                                30
                            )
                        else
                            null

                    setOnClickListener {

                        selectedTheme =
                            color

                        prefs.edit()
                            .putInt(
                                "theme_color",
                                color
                            )
                            .apply()

                        showSettings()
                    }
                }

            colors.addView(
                circle,
                LinearLayout.LayoutParams(
                    dp(58),
                    dp(58)
                ).apply {

                    setMargins(
                        dp(5),
                        dp(5),
                        dp(5),
                        dp(5)
                    )
                }
            )
        }

        screen.addView(
            colors,
            LinearLayout.LayoutParams(
                -1,
                dp(70)
            )
        )

        addSpace(screen, 15)

        screen.addView(
            sectionTitle("About")
        )

        val about =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(16),
                    dp(15),
                    dp(16),
                    dp(15)
                )

                background =
                    rounded(surface2, 16)
            }

        about.addView(
            text(
                "Faith Mark",
                17f,
                white
            ).apply {

                typeface =
                    Typeface.DEFAULT_BOLD
            }
        )

        about.addView(
            text(
                "Offline Bible Reading Tracker",
                12f,
                gray
            ).apply {

                setPadding(
                    0,
                    dp(4),
                    0,
                    0
                )
            }
        )

        about.addView(
            text(
                "Your reading data stays on this device.",
                11f,
                gray2
            ).apply {

                setPadding(
                    0,
                    dp(8),
                    0,
                    0
                )
            }
        )

        screen.addView(
            about,
            LinearLayout.LayoutParams(
                -1,
                dp(105)
            )
        )

        setScreen(screen)
    }

    private fun settingsCard(
        icon: String,
        title: String,
        subtitle: String,
        action: () -> Unit
    ): LinearLayout {

        val card =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL

                setPadding(
                    dp(12),
                    dp(8),
                    dp(12),
                    dp(8)
                )

                background =
                    rounded(surface2, 15)

                setOnClickListener {
                    action()
                }
            }

        card.addView(
            text(
                icon,
                22f
            ).apply {

                gravity =
                    Gravity.CENTER

            },
            LinearLayout.LayoutParams(
                dp(45),
                dp(55)
            )
        )

        val info =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(12),
                    0,
                    dp(5),
                    0
                )
            }

        info.addView(
            text(
                title,
                14f,
                white
            ).apply {

                typeface =
                    Typeface.DEFAULT_BOLD
            }
        )

        info.addView(
            text(
                subtitle,
                11f,
                gray
            )
        )

        card.addView(
            info,
            LinearLayout.LayoutParams(
                0,
                dp(55),
                1f
            )
        )

        card.addView(
            text(
                "›",
                22f,
                gray
            ).apply {

                gravity =
                    Gravity.CENTER
            },
            LinearLayout.LayoutParams(
                dp(30),
                dp(50)
            )
        )

        return card
    }

    // ============================================================
    // BACKUP
    // ============================================================

    private fun backup() {

        val data =
            JSONObject()

        data.put(
            "app",
            "Faith Mark"
        )

        data.put(
            "version",
            1
        )

        val reading =
            JSONObject()

        read.forEach { (book, chapters) ->

            val array =
                JSONArray()

            chapters.forEach {
                array.put(it)
            }

            reading.put(
                book,
                array
            )
        }

        data.put(
            "reading",
            reading
        )

        backupPendingData =
            data.toString()

        val intent =
            Intent(
                Intent.ACTION_CREATE_DOCUMENT
            ).apply {

                type =
                    "application/json"

                putExtra(
                    Intent.EXTRA_TITLE,
                    "faith-mark-backup.json"
                )
            }

        startActivityForResult(
            intent,
            1001
        )
    }

    private fun restore() {

        val intent =
            Intent(
                Intent.ACTION_OPEN_DOCUMENT
            ).apply {

                type =
                    "application/json"

                addCategory(
                    Intent.CATEGORY_OPENABLE
                )
            }

        startActivityForResult(
            intent,
            1002
        )
    }

    @Deprecated("Use Activity Result API")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            resultCode !=
            RESULT_OK
        ) {
            return
        }

        val uri =
            data?.data
                ?: return

        try {

            if (requestCode == 1001) {

                contentResolver
                    .openOutputStream(uri)
                    ?.use {

                        it.write(
                            (
                                backupPendingData
                                    ?: "{}"
                                ).toByteArray()
                        )
                    }

                Toast.makeText(
                    this,
                    "Backup completed successfully",
                    Toast.LENGTH_LONG
                ).show()
            }

            if (requestCode == 1002) {

                val json =
                    contentResolver
                        .openInputStream(uri)
                        ?.bufferedReader()
                        ?.use {
                            it.readText()
                        }
                        ?: return

                val obj =
                    JSONObject(json)

                val reading =
                    obj.optJSONObject(
                        "reading"
                    )

                if (reading != null) {

                    read.clear()

                    val keys =
                        reading.keys()

                    while (
                        keys.hasNext()
                    ) {

                        val book =
                            keys.next()

                        val array =
                            reading.getJSONArray(
                                book
                            )

                        val set =
                            mutableSetOf<Int>()

                        for (
                            i in
                            0 until array.length()
                        ) {

                            set.add(
                                array.getInt(i)
                            )
                        }

                        read[book] =
                            set
                    }

                    saveData()

                    checkAchievements(
                        false
                    )

                    showTrackers()

                    Toast.makeText(
                        this,
                        "Restore completed successfully",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        } catch (_: Exception) {

            Toast.makeText(
                this,
                "Backup/Restore error",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // ============================================================
    // CALCULATIONS
    // ============================================================

    private fun overallPercentValue():
        Double {

        val total =
            books.sumOf {
                it.chapters
            }

        if (total == 0)
            return 0.0

        return (
            countReadAll() *
                100.0 /
                total
            )
    }

    private fun overallPercent():
        String {

        return String.format(
            Locale.getDefault(),
            "%.1f",
            overallPercentValue()
        )
    }

    private fun percentageForValue(
        testament: String
    ): Double {

        val total =
            books
                .filter {
                    it.testament ==
                        testament
                }
                .sumOf {
                    it.chapters
                }

        val done =
            countRead(testament)

        if (total == 0)
            return 0.0

        return done *
            100.0 /
            total
    }

    private fun countRead(
        testament: String
    ): Int {

        return books
            .filter {
                it.testament ==
                    testament
            }
            .sumOf {
                read[it.name]
                    ?.size ?: 0
            }
    }

    private fun countReadAll():
        Int {

        return books.sumOf {
            read[it.name]
                ?.size ?: 0
        }
    }

    private fun completedBooks(
        testament: String
    ): Int {

        return books.count {

            it.testament ==
                testament &&
            (
                read[it.name]
                    ?.size ?: 0
                ) >= it.chapters
        }
    }
}