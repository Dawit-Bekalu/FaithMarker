package com.dawitbekalu.faithmarker

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

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

    private val bg = Color.rgb(17, 17, 17)
    private val surface = Color.rgb(38, 38, 38)
    private val surface2 = Color.rgb(48, 48, 48)
    private val green = Color.rgb(156, 175, 136)
    private val blue = Color.rgb(121, 215, 245)
    private val white = Color.rgb(242, 242, 242)
    private val gray = Color.rgb(180, 180, 180)

    private val prefs by lazy {
        getSharedPreferences("bible_tracking_data", MODE_PRIVATE)
    }

    private val books = mutableListOf<BibleBook>()
    private val read = mutableMapOf<String, MutableSet<Int>>()
    private val achievements = mutableListOf<Achievement>()

    private var currentTracker = "Bible"
    private var currentTestament = "Old Testament"

    private lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadDefaultBooks()
        loadData()
        loadAchievements()

        showTrackers()
    }

    // ------------------------------------------------------------
    // DATA
    // ------------------------------------------------------------

    private fun loadDefaultBooks() {
        if (books.isNotEmpty()) return

        addBooks(
            "Old Testament", "Law",
            listOf(
                "Genesis" to 50,
                "Exodus" to 40,
                "Leviticus" to 27,
                "Numbers" to 36,
                "Deuteronomy" to 34
            )
        )

        addBooks(
            "Old Testament", "History",
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
            "Old Testament", "Poetry",
            listOf(
                "Job" to 42,
                "Psalms" to 150,
                "Proverbs" to 31,
                "Ecclesiastes" to 12,
                "Song of Solomon" to 8
            )
        )

        addBooks(
            "Old Testament", "Major Prophets",
            listOf(
                "Isaiah" to 66,
                "Jeremiah" to 52,
                "Lamentations" to 5,
                "Ezekiel" to 48,
                "Daniel" to 12
            )
        )

        addBooks(
            "Old Testament", "Minor Prophets",
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
            "New Testament", "Gospels",
            listOf(
                "Matthew" to 28,
                "Mark" to 16,
                "Luke" to 24,
                "John" to 21
            )
        )

        addBooks(
            "New Testament", "History",
            listOf(
                "Acts" to 28
            )
        )

        addBooks(
            "New Testament", "Paul's Letters",
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
            "New Testament", "General Letters",
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
            "New Testament", "Prophecy",
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
            books.add(BibleBook(it.first, it.second, testament, group))
        }
    }

    private fun loadData() {
        val saved = prefs.getString("read_data", null) ?: return

        try {
            val obj = JSONObject(saved)

            for (book in books) {
                val array = obj.optJSONArray(book.name) ?: continue
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
            chapters.forEach { array.put(it) }
            obj.put(book, array)
        }

        prefs.edit()
            .putString("read_data", obj.toString())
            .apply()
    }

    private fun loadAchievements() {
        achievements.clear()

        achievements.addAll(
            listOf(
                Achievement("The Man", "Read Matthew", "Matthew"),
                Achievement("The Lion", "Read Mark", "Mark"),
                Achievement("The Calf", "Read Luke", "Luke"),
                Achievement("The Eagle", "Read John", "John"),
                Achievement("Evangelist", "Read the four Gospels"),
                Achievement("Apostle", "Read Acts", "Acts"),
                Achievement("The Rock", "Read Peter's first and second letters"),
                Achievement("The Beginning", "Read Genesis", "Genesis"),
                Achievement("No longer slave", "Read Exodus", "Exodus"),
                Achievement("Sanctified", "Read Leviticus", "Leviticus"),
                Achievement("Are we there yet?", "Read Numbers", "Numbers"),
                Achievement("Covenant", "Read Deuteronomy", "Deuteronomy"),
                Achievement("No Longer Ruthless", "Read Ruth", "Ruth"),
                Achievement("Scribe", "Read the 5 books of Moses"),
                Achievement("Poet", "Read all the poetry books"),
                Achievement("Historian", "Read all History books"),
                Achievement("Prophet", "Read all major and minor prophets"),
                Achievement("Wise Man", "Read Proverbs, Job and Ecclesiastes"),
                Achievement("Royal", "Read Samuel, Kings and Chronicles"),
                Achievement("Paul(in)ist", "Read all of Paul's letters"),
                Achievement("Penpal", "Read all of the letters"),
                Achievement("Apocalyptic", "Read Daniel, Ezekiel, Zechariah and Revelation"),
                Achievement("The New Covenant", "Read the entire New Testament"),
                Achievement("Before Christ", "Read the entire Old Testament"),
                Achievement("25%", "Read 25% of the Bible"),
                Achievement("50%", "Read 50% of the Bible"),
                Achievement("75%", "Read 75% of the Bible"),
                Achievement("Bible Nerd", "Read the whole Bible")
            )
        )
    }

    // ------------------------------------------------------------
    // UI HELPERS
    // ------------------------------------------------------------

    private fun baseLayout(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(12, 8, 12, 12)
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
            gravity = Gravity.CENTER_VERTICAL
        }
    }

    private fun button(
        value: String,
        onClick: () -> Unit
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = 14f
            setTextColor(white)
            gravity = Gravity.CENTER
            setPadding(14, 12, 14, 12)
            setBackgroundColor(surface2)
            setOnClickListener { onClick() }
        }
    }

    private fun addSpace(parent: LinearLayout, height: Int = 8) {
        val space = Space(this)
        parent.addView(
            space,
            LinearLayout.LayoutParams(
                1,
                height
            )
        )
    }

    private fun setScreen(view: View) {
        setContentView(view)
    }

    // ------------------------------------------------------------
    // TRACKERS
    // ------------------------------------------------------------

    private fun showTrackers() {
        root = baseLayout()

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = text("Bible Trackers", 22f)
        header.addView(
            title,
            LinearLayout.LayoutParams(0, 58, 1f)
        )

        val settings = text("⚙", 25f, green).apply {
            gravity = Gravity.CENTER
            setOnClickListener { showSettings() }
        }

        header.addView(
            settings,
            LinearLayout.LayoutParams(55, 58)
        )

        root.addView(header)

        val add = button("＋  Add new") {
            addTrackerDialog()
        }

        root.addView(
            add,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                52
            )
        )

        addSpace(root)

        val hasTracker = true

        if (!hasTracker) {
            val empty = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
            }

            val icon = text("📖", 70f)
            icon.gravity = Gravity.CENTER

            empty.addView(icon)

            empty.addView(
                text("No Bible Trackers", 20f).apply {
                    gravity = Gravity.CENTER
                }
            )

            empty.addView(
                text("Add a new Bible tracker to keep track\nof your reading progress", 14f, gray).apply {
                    gravity = Gravity.CENTER
                }
            )

            root.addView(
                empty,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    500
                )
            )
        } else {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(12, 8, 12, 8)
                setBackgroundColor(surface2)
                setOnClickListener {
                    showTracker()
                }
            }

            val progress = text("${overallPercent()}%", 13f, Color.BLACK).apply {
                gravity = Gravity.CENTER
                setBackgroundColor(blue)
            }

            card.addView(
                progress,
                LinearLayout.LayoutParams(48, 48)
            )

            val name = text("Bible", 16f).apply {
                setPadding(15, 0, 0, 0)
            }

            card.addView(
                name,
                LinearLayout.LayoutParams(0, 55, 1f)
            )

            card.addView(
                text("›", 28f, white).apply {
                    gravity = Gravity.CENTER
                },
                LinearLayout.LayoutParams(45, 55)
            )

            root.addView(
                card,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    65
                )
            )
        }

        setScreen(root)
    }

    private fun addTrackerDialog() {
        val input = EditText(this).apply {
            hint = "Name"
            setTextColor(white)
            setHintTextColor(gray)
        }

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 10, 30, 10)
            addView(input)
        }

        AlertDialog.Builder(this)
            .setTitle("Add new Bible Tracker")
            .setView(box)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val name = input.text.toString().trim()

                if (name.isNotEmpty()) {
                    currentTracker = name
                    showTrackers()
                }
            }
            .show()
    }

    // ------------------------------------------------------------
    // MAIN TRACKER
    // ------------------------------------------------------------

    private fun showTracker() {
        val screen = baseLayout()

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        top.addView(
            button("‹ Back") {
                showTrackers()
            },
            LinearLayout.LayoutParams(95, 50)
        )

        val percent = text("${overallPercent()}%\nof the Bible", 18f).apply {
            gravity = Gravity.CENTER
        }

        top.addView(
            percent,
            LinearLayout.LayoutParams(0, 65, 1f)
        )

        top.addView(
            button("🏆\nAchievements") {
                showAchievements()
            },
            LinearLayout.LayoutParams(115, 65)
        )

        screen.addView(top)

        addSpace(screen)

        val tabs = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        tabs.addView(
            button("Old Testament") {
                currentTestament = "Old Testament"
                showTracker()
            },
            LinearLayout.LayoutParams(0, 50, 1f)
        )

        tabs.addView(
            button("New Testament") {
                currentTestament = "New Testament"
                showTracker()
            },
            LinearLayout.LayoutParams(0, 50, 1f)
        )

        tabs.addView(
            button("Stats") {
                showStats()
            },
            LinearLayout.LayoutParams(0, 50, 0.7f)
        )

        screen.addView(tabs)

        addSpace(screen)

        val groups = books
            .filter { it.testament == currentTestament }
            .map { it.group }
            .distinct()

        val scroll = ScrollView(this)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        groups.forEach { group ->
            content.addView(
                text(group, 17f, white).apply {
                    setPadding(4, 10, 4, 5)
                }
            )

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            var count = 0

            books.filter {
                it.testament == currentTestament && it.group == group
            }.forEach { book ->

                val b = button(book.name) {
                    showBook(book)
                }

                row.addView(
                    b,
                    LinearLayout.LayoutParams(
                        0,
                        55,
                        1f
                    ).apply {
                        setMargins(3, 3, 3, 3)
                    }
                )

                count++

                if (count == 4) {
                    content.addView(row)
                    row.removeAllViews()
                    count = 0
                }
            }

            if (row.childCount > 0) {
                while (row.childCount < 4) {
                    row.addView(
                        Space(this),
                        LinearLayout.LayoutParams(0, 55, 1f)
                    )
                }
                content.addView(row)
            }
        }

        scroll.addView(content)

        screen.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setScreen(screen)
    }

    // ------------------------------------------------------------
    // BOOK / CHAPTERS
    // ------------------------------------------------------------

    private fun showBook(book: BibleBook) {
        val screen = baseLayout()

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        top.addView(
            button("‹ Back") {
                showTracker()
            },
            LinearLayout.LayoutParams(90, 50)
        )

        top.addView(
            button("☑ Mark all") {
                markAll(book)
            },
            LinearLayout.LayoutParams(0, 50, 1f)
        )

        top.addView(
            button("☐ Unread") {
                cancelAll(book)
            },
            LinearLayout.LayoutParams(90, 50)
        )

        screen.addView(top)

        val done = read[book.name]?.size ?: 0
        val percent = if (book.chapters == 0) 0.0
        else done * 100.0 / book.chapters

        screen.addView(
            text(
                "${book.name}\n${String.format("%.1f", percent)}%",
                23f
            ).apply {
                gravity = Gravity.CENTER
                setPadding(0, 10, 0, 15)
            }
        )

        val scroll = ScrollView(this)
        val grid = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        var row: LinearLayout? = null

        for (chapter in 1..book.chapters) {
            if ((chapter - 1) % 5 == 0) {
                row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                }
                grid.addView(row)
            }

            val isRead = read[book.name]?.contains(chapter) == true

            val chapterButton = button(
                chapter.toString()
            ) {
                toggleChapter(book, chapter)
            }

            if (isRead) {
                chapterButton.setBackgroundColor(blue)
                chapterButton.setTextColor(Color.BLACK)
            }

            row?.addView(
                chapterButton,
                LinearLayout.LayoutParams(
                    0,
                    58,
                    1f
                ).apply {
                    setMargins(3, 3, 3, 3)
                }
            )
        }

        scroll.addView(grid)

        screen.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setScreen(screen)
    }

    private fun toggleChapter(book: BibleBook, chapter: Int) {
        val set = read.getOrPut(book.name) { mutableSetOf() }

        if (set.contains(chapter)) {
            set.remove(chapter)
        } else {
            set.add(chapter)
        }

        saveData()
        checkAchievements()
        showBook(book)
    }

    private fun markAll(book: BibleBook) {
        val set = read.getOrPut(book.name) { mutableSetOf() }

        for (i in 1..book.chapters) {
            set.add(i)
        }

        saveData()
        checkAchievements()
        showBook(book)
    }

    private fun cancelAll(book: BibleBook) {
        read[book.name]?.clear()

        saveData()
        checkAchievements()
        showBook(book)
    }

    // ------------------------------------------------------------
    // STATS
    // ------------------------------------------------------------

    private fun showStats() {
        val screen = baseLayout()

        screen.addView(
            button("‹ Back") {
                showTracker()
            },
            LinearLayout.LayoutParams(90, 50)
        )

        screen.addView(
            text("Percentage", 20f).apply {
                setPadding(0, 15, 0, 10)
            }
        )

        val ot = percentageFor("Old Testament")
        val nt = percentageFor("New Testament")
        val all = overallPercent()

        val percentages = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        listOf(
            "Old Testament" to ot,
            "New Testament" to nt,
            "Whole Bible" to all
        ).forEach {
            val box = TextView(this).apply {
                text = "${it.second}%\n${it.first}"
                textSize = 15f
                setTextColor(white)
                gravity = Gravity.CENTER
                setPadding(5, 20, 5, 20)
                setBackgroundColor(surface2)
            }

            percentages.addView(
                box,
                LinearLayout.LayoutParams(0, 110, 1f).apply {
                    setMargins(4, 4, 4, 4)
                }
            )
        }

        screen.addView(percentages)

        addSpace(screen, 20)

        screen.addView(text("Chapters", 20f))

        val otChapters = books
            .filter { it.testament == "Old Testament" }
            .sumOf { it.chapters }

        val ntChapters = books
            .filter { it.testament == "New Testament" }
            .sumOf { it.chapters }

        val allChapters = otChapters + ntChapters

        screen.addView(
            text(
                "Read chapters\n" +
                        "${countRead("Old Testament")} / $otChapters     " +
                        "${countRead("New Testament")} / $ntChapters     " +
                        "${countReadAll()} / $allChapters",
                16f
            ).apply {
                setPadding(0, 15, 0, 15)
            }
        )

        screen.addView(text("Books", 20f))

        screen.addView(
            text(
                "Completed books\n" +
                        "${completedBooks("Old Testament")} / " +
                        books.count { it.testament == "Old Testament" } +
                        "        " +
                        "${completedBooks("New Testament")} / " +
                        books.count { it.testament == "New Testament" },
                16f
            ).apply {
                setPadding(0, 15, 0, 15)
            }
        )

        setScreen(screen)
    }

    // ------------------------------------------------------------
    // ACHIEVEMENTS
    // ------------------------------------------------------------

    private fun showAchievements() {
        checkAchievements()

        val screen = baseLayout()

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        header.addView(
            button("‹ Back") {
                showTracker()
            },
            LinearLayout.LayoutParams(85, 50)
        )

        header.addView(
            text("Achievements", 24f, blue).apply {
                gravity = Gravity.CENTER
            },
            LinearLayout.LayoutParams(0, 55, 1f)
        )

        header.addView(
            text("1/${achievements.size}", 14f, blue).apply {
                gravity = Gravity.CENTER
            },
            LinearLayout.LayoutParams(50, 55)
        )

        screen.addView(header)

        screen.addView(
            button("＋ Add Achievement") {
                addAchievementDialog()
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                50
            )
        )

        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        achievements.forEachIndexed { index, achievement ->

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(10, 8, 8, 8)
                setBackgroundColor(
                    if (achievement.unlocked) blue else Color.rgb(220, 220, 220)
                )
            }

            val icon = text(
                if (achievement.unlocked) "🏆" else "🔒",
                25f
            ).apply {
                gravity = Gravity.CENTER
                setTextColor(Color.DKGRAY)
            }

            card.addView(
                icon,
                LinearLayout.LayoutParams(55, 62)
            )

            val info = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_VERTICAL
            }

            info.addView(
                text(
                    achievement.name,
                    16f,
                    if (achievement.unlocked) Color.BLACK else Color.DKGRAY
                )
            )

            info.addView(
                text(
                    achievement.description,
                    12f,
                    if (achievement.unlocked) Color.DKGRAY else Color.DKGRAY
                )
            )

            card.addView(
                info,
                LinearLayout.LayoutParams(0, 62, 1f)
            )

            card.setOnClickListener {
                editAchievementDialog(index)
            }

            list.addView(
                card,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    75
                ).apply {
                    setMargins(0, 3, 0, 3)
                }
            )
        }

        scroll.addView(list)

        screen.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setScreen(screen)
    }

    private fun addAchievementDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(25, 5, 25, 5)
        }

        val name = EditText(this).apply {
            hint = "Achievement name"
        }

        val description = EditText(this).apply {
            hint = "Reward / description"
        }

        layout.addView(name)
        layout.addView(description)

        AlertDialog.Builder(this)
            .setTitle("Add Achievement")
            .setView(layout)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                achievements.add(
                    Achievement(
                        name.text.toString().ifEmpty { "New Achievement" },
                        description.text.toString()
                    )
                )
                showAchievements()
            }
            .show()
    }

    private fun editAchievementDialog(index: Int) {
        val a = achievements[index]

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(25, 5, 25, 5)
        }

        val name = EditText(this).apply {
            setText(a.name)
        }

        val desc = EditText(this).apply {
            setText(a.description)
        }

        layout.addView(name)
        layout.addView(desc)

        AlertDialog.Builder(this)
            .setTitle("Edit Achievement")
            .setView(layout)
            .setNegativeButton("Delete") { _, _ ->
                achievements.removeAt(index)
                showAchievements()
            }
            .setPositiveButton("Save") { _, _ ->
                a.name = name.text.toString()
                a.description = desc.text.toString()
                showAchievements()
            }
            .show()
    }

    private fun checkAchievements() {

        fun complete(bookName: String): Boolean {
            val b = books.firstOrNull { it.name == bookName } ?: return false
            return read[b.name]?.size == b.chapters
        }

        achievements.forEach { a ->

            val wasUnlocked = a.unlocked

            a.unlocked = when (a.name) {
                "The Man" -> complete("Matthew")
                "The Lion" -> complete("Mark")
                "The Calf" -> complete("Luke")
                "The Eagle" -> complete("John")

                "Evangelist" ->
                    complete("Matthew") &&
                    complete("Mark") &&
                    complete("Luke") &&
                    complete("John")

                "Apostle" -> complete("Acts")

                "The Beginning" -> complete("Genesis")

                "No longer slave" -> complete("Exodus")

                "Sanctified" -> complete("Leviticus")

                "Are we there yet?" -> complete("Numbers")

                "Covenant" -> complete("Deuteronomy")

                "No Longer Ruthless" -> complete("Ruth")

                "Scribe" ->
                    books.filter { it.group == "Law" }
                        .all { complete(it.name) }

                "Poet" ->
                    books.filter { it.group == "Poetry" }
                        .all { complete(it.name) }

                "Historian" ->
                    books.filter {
                        it.testament == "Old Testament" &&
                                it.group == "History"
                    }.all { complete(it.name) }

                "Prophet" ->
                    books.filter {
                        it.testament == "Old Testament" &&
                                (
                                    it.group == "Major Prophets" ||
                                    it.group == "Minor Prophets"
                                )
                    }.all { complete(it.name) }

                "Paul(in)ist" ->
                    books.filter { it.group == "Paul's Letters" }
                        .all { complete(it.name) }

                "The New Covenant" ->
                    percentageForValue("New Testament") >= 100.0

                "Before Christ" ->
                    percentageForValue("Old Testament") >= 100.0

                "25%" -> overallPercentValue() >= 25.0
                "50%" -> overallPercentValue() >= 50.0
                "75%" -> overallPercentValue() >= 75.0
                "Bible Nerd" -> overallPercentValue() >= 100.0

                else -> a.unlocked
            }

            if (!wasUnlocked && a.unlocked) {
                showAchievementUnlocked(a)
            }
        }
    }

    private fun showAchievementUnlocked(a: Achievement) {
        val message = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(25, 20, 25, 20)
        }

        message.addView(
            text("🎉", 55f).apply {
                gravity = Gravity.CENTER
            }
        )

        message.addView(
            text("Achievement", 22f, blue).apply {
                gravity = Gravity.CENTER
            }
        )

        message.addView(
            text(a.name, 19f).apply {
                gravity = Gravity.CENTER
            }
        )

        message.addView(
            text(a.description, 14f, gray).apply {
                gravity = Gravity.CENTER
                setPadding(0, 10, 0, 15)
            }
        )

        message.addView(
            text(
                when (a.book) {
                    "Matthew" -> "👤"
                    "Mark" -> "🦁"
                    "Luke" -> "🐂"
                    "John" -> "🦅"
                    "Genesis" -> "🌍"
                    "Exodus" -> "🔥"
                    else -> "📖"
                },
                55f
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        AlertDialog.Builder(this)
            .setView(message)
            .setPositiveButton("Hurray!") { _, _ ->
                showAchievements()
            }
            .setOnDismissListener {
                showAchievements()
            }
            .show()
    }

    // ------------------------------------------------------------
    // SETTINGS / BACKUP / RESTORE
    // ------------------------------------------------------------

    private fun showSettings() {
        val screen = baseLayout()

        screen.addView(
            text("Settings", 24f, green).apply {
                gravity = Gravity.CENTER
                setPadding(0, 10, 0, 20)
            }
        )

        screen.addView(
            button("‹ Back") {
                showTrackers()
            },
            LinearLayout.LayoutParams(90, 50)
        )

        addSpace(screen, 15)

        screen.addView(
            button("💾 Backup progress to file") {
                backup()
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                60
            )
        )

        addSpace(screen)

        screen.addView(
            button("📂 Restore progress from file") {
                restore()
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                60
            )
        )

        addSpace(screen, 25)

        screen.addView(text("Theme color", 18f))

        val colors = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val themeColors = listOf(
            green,
            blue,
            Color.GRAY,
            Color.rgb(180, 140, 220)
        )

        themeColors.forEach { color ->
            val c = TextView(this).apply {
                text = "●"
                textSize = 30f
                setTextColor(color)
                gravity = Gravity.CENTER

                setOnClickListener {
                    prefs.edit()
                        .putInt("theme_color", color)
                        .apply()

                    Toast.makeText(
                        this@MainActivity,
                        "Theme saved",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            colors.addView(
                c,
                LinearLayout.LayoutParams(0, 60, 1f)
            )
        }

        screen.addView(colors)

        setScreen(screen)
    }

    private fun backup() {
        val data = JSONObject()
        data.put("app", "Bible Tracking")
        data.put("version", 1)

        val reading = JSONObject()

        read.forEach { (book, chapters) ->
            val arr = JSONArray()
            chapters.forEach { arr.put(it) }
            reading.put(book, arr)
        }

        data.put("reading", reading)

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            type = "application/json"
            putExtra(
                Intent.EXTRA_TITLE,
                "bible-tracking-backup.json"
            )
        }

        startActivityForResult(intent, 1001)
        backupPendingData = data.toString()
    }

    private var backupPendingData: String? = null

    private fun restore() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "application/json"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        startActivityForResult(intent, 1002)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK) return

        val uri: Uri = data?.data ?: return

        try {
            if (requestCode == 1001) {
                contentResolver.openOutputStream(uri)?.use {
                    it.write((backupPendingData ?: "{}").toByteArray())
                }

                Toast.makeText(
                    this,
                    "Backup completed successfully",
                    Toast.LENGTH_LONG
                ).show()
            }

            if (requestCode == 1002) {
                val json = contentResolver
                    .openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: return

                val obj = JSONObject(json)
                val reading = obj.optJSONObject("reading")

                if (reading != null) {
                    read.clear()

                    val keys = reading.keys()

                    while (keys.hasNext()) {
                        val book = keys.next()
                        val arr = reading.getJSONArray(book)
                        val set = mutableSetOf<Int>()

                        for (i in 0 until arr.length()) {
                            set.add(arr.getInt(i))
                        }

                        read[book] = set
                    }

                    saveData()
                    checkAchievements()
                    showTrackers()

                    Toast.makeText(
                        this,
                        "Restore completed successfully",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Backup/Restore error",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // ------------------------------------------------------------
    // CALCULATIONS
    // ------------------------------------------------------------

    private fun overallPercentValue(): Double {
        val total = books.sumOf { it.chapters }

        if (total == 0) return 0.0

        return countReadAll() * 100.0 / total
    }

    private fun overallPercent(): String {
        return String.format("%.1f", overallPercentValue())
    }

    private fun percentageForValue(testament: String): Double {
        val total = books
            .filter { it.testament == testament }
            .sumOf { it.chapters }

        val done = countRead(testament)

        if (total == 0) return 0.0

        return done * 100.0 / total
    }

    private fun percentageFor(testament: String): String {
        val total = books
            .filter { it.testament == testament }
            .sumOf { it.chapters }

        val done = countRead(testament)

        if (total == 0) return "0.0"

        return String.format("%.1f", done * 100.0 / total)
    }

    private fun countRead(testament: String): Int {
        return books
            .filter { it.testament == testament }
            .sumOf { read[it.name]?.size ?: 0 }
    }

    private fun countReadAll(): Int {
        return books.sumOf { read[it.name]?.size ?: 0 }
    }

    private fun completedBooks(testament: String): Int {
        return books.count {
            it.testament == testament &&
                    (read[it.name]?.size ?: 0) == it.chapters
        }
    }
}
