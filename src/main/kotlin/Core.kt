import java.io.BufferedReader
import java.io.File

fun main() {
    print("complete paths to input and then output files (separated by a space): ")
    val input = readln().split(' ')
    val inputFile = File(input[0])
    val outputFile = File(input[1])

    // doing the ungrouped map as a separate function because
    // it makes things more readable in my opinion ...
    val ungroupedMap = populateUngroupedMap(inputFile.bufferedReader())

    /// ... although mainly because this function is long and spacious
    val groupedMap = createGroupedMap(ungroupedMap)

    // Output is not a separate function because it's easier this way and the process is so short.
    // Also, because it's just this, and will never need to be anything but
    val bufOutputWriter = outputFile.bufferedWriter()
    for ((occurrence, words) in groupedMap.entries.reversed()) {
        // i could just put \n in the .write calls but this is probably better.
        bufOutputWriter.write("$occurrence:")
        bufOutputWriter.newLine()
        bufOutputWriter.write(words.joinToString("\n"))
        bufOutputWriter.newLine()
        bufOutputWriter.newLine()
        bufOutputWriter.flush()
    }
    bufOutputWriter.close()

    // Enjoy!
    println("Done!")

}
fun populateUngroupedMap(bufInputReader: BufferedReader) : MutableMap<String, Int> {
    val linesIterator = bufInputReader.lines().iterator()
    val ungroupedMap = mutableMapOf<String, Int>()

    // I'm using an iterator on all the lines of the file because I felt like it lol.
    // I could have just used .forEachLine and achieved basically the same result I guess.
    // I suppose this may be a more general approach
    while (linesIterator.hasNext()) {

        // exhuming any sense of character from the text :3
        val line = linesIterator.next()
            .lowercase()
            .filter { it !in listOf('!','@','#','$','%','^','&','*','(',')','-','_',
                '+','=', ',','.',':',';','?',']','[','}','{','/','\\','<','>','`','\'') }
            .split(' ')
            .iterator()

        for (word in line) {
            if (word.isBlank()) continue
            // I don't know if `in` or `.contains()` is faster.
            // nvm they are the same operation
            if (word in ungroupedMap.keys) {
                ungroupedMap.run {
                    // I can assert newValue will not be null because
                    // it is only using existing keys due to the if statement above
                    val newValue = get(word)!! + 1
                    set(word, newValue)
                }
            } else {
                ungroupedMap[word] = 1
            }
        }

    }
    bufInputReader.close()
    return ungroupedMap
}
fun createGroupedMap(ungroupedMap: MutableMap<String, Int>) : MutableMap<Int, List<String>> {
    // using a .run closure because I think it is more clear
    // than declaring the variable and then mutating it later
    val valuesThatExist = mutableListOf<Int>().run {
        // reversed so grouping begins with the highest occurrence values
        // a.k.a the least common words (there's always a lot of them)
        for (number in ungroupedMap.values.sorted().reversed()) {
            if (number !in this) {
                this.add(number)
            }
        }
        this
    }

    // Starting with the least common words so that the huge list of
    // words that occur once is quickly grouped and then removed from
    // the ungrouped map for further iteration.
    val groupedMap = mutableMapOf<Int, List<String>>()
    var currentOccurrenceValue: Int
    var indexOfCurrentVal = valuesThatExist.size - 1


    while (indexOfCurrentVal >= 0) {
        // setting the next occurrence value this way avoids setting it
        // to values that don't exist in the ungrouped map and is more efficient
        currentOccurrenceValue = valuesThatExist[indexOfCurrentVal]

        val group = mutableListOf<String>().run {
            for ((word, occurrence) in ungroupedMap.entries) {
                if (occurrence == currentOccurrenceValue)
                    this.add(word)
            }
            this
        }

        groupedMap[currentOccurrenceValue] = group

        // removing words that have already been grouped obviously makes
        // subsequent iterations way faster.
        val forRemoval = mutableListOf<String>().run {
            for (word in ungroupedMap.keys) {
                if (ungroupedMap.getValue(word) < currentOccurrenceValue) {
                    this.add(word)
                }
            }
            this
        }
        for (word in forRemoval) {
            ungroupedMap.remove(word)
        }
        indexOfCurrentVal--
    }
    // cleaning up
    ungroupedMap.clear()
    return groupedMap
}