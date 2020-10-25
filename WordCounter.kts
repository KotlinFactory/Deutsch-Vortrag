import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.system.exitProcess

data class Result(val words: Int, val uniqueWords: Int)

val PATH_TO_FILES = "IdeaProjects/Deutsch-Vortrag/Texte"

val results = mutableListOf<Pair<File, Result>>()

val homeDirectory = System.getProperty("user.home")

val folder = File("$homeDirectory/$PATH_TO_FILES")

if (!folder.isDirectory) {
  println("Folder does not exist")
  exitProcess(0)
}

if (!folder.exists()) {
  folder.mkdirs()
}

val arrayOfFiles =
    folder.listFiles { _, name -> name.endsWith(".txt") } ?: emptyArray()

println(arrayOfFiles.contentToString())

arrayOfFiles.forEach files@{ file ->

  val words = mutableListOf<String>()
  file.useLines { lines ->
    lines.forEach lines@{ line ->
      if (line.startsWith("#")) {
        return@lines
      }

      if (line.startsWith("[")) {
        return@lines
      }

      words.addAll(
          line
              .toLowerCase()
              .replace("ä", "ae")
              .replace("ü", "ue")
              .replace("ö", "oe")
              .replace(regex = "[^A-Za-z0-9 ]".toRegex(), replacement = "")
              .split(" "))
    }
  }
  results.add(Pair(file, Result(words.size, words.toSet().size)))
}

fun ratioRounded(words: Int, uniqueWords: Int): BigDecimal {
  val ratioUnrounded = uniqueWords.toDouble() / words.toDouble()

  return if (ratioUnrounded.isNaN()) 0.toBigDecimal()
  else ratioUnrounded.toBigDecimal().setScale(
      3,
      RoundingMode.HALF_EVEN)

}

fun ratioRounded(result: Result): BigDecimal {
  return ratioRounded(result.words, result.uniqueWords)
}

fun calculateUniqueWordsToWordsRatio(uniqueWords: Int, words: Int): BigDecimal {

  val ratioUnrounded = uniqueWords.toDouble() / words.toDouble()

  return if (ratioUnrounded.isNaN()) BigDecimal.ZERO else ratioUnrounded.toBigDecimal().setScale(
      3,
      RoundingMode.HALF_EVEN)
}

val output = results.map {
  Pair(it, calculateUniqueWordsToWordsRatio(it.second.uniqueWords, it.second.words))
}.sortedByDescending { it.second }

output.forEach { (key, value) ->
  if (!key.first.name.contains(".")) {
    println("Skipping ${key.first.name} as it does not match the formatting: AUTHOR-TRACKNAME.txt")
  }

  val (author, track) = key.first.name.split("-")

  println(
      "Author: $author workname: $track words: ${key.second.words} unique: ${key.second.uniqueWords}" + " " + "ratio: $value")

}

val file = File("Output.csv")
file.createNewFile()

GlobalScope.launch {
  val text = StringBuilder("author;track;words;unique_words;ratio\n")
  output.forEach { (key, value) ->
    val (author, track) = key.first.name.split("-")

    text.append("$author;$track;${key.second.words};${key.second.uniqueWords};$value\n")
  }

  file.writeText(text.toString())
}.start()





