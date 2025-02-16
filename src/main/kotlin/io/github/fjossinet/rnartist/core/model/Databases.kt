package io.github.fjossinet.rnartist.core.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.github.fjossinet.rnartist.core.ss
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.collections.HashMap

class NCBI {

    val baseURL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/"

    /**
     * Return a map whose key is the entry id asked for and value is the title found (or null if nothing found)
     */
    fun getSummaryTitle(vararg nucleotideEntryId:String):Map<String,String?> {
        val titles = mutableMapOf<String, String?>()
        try {
            val ids = nucleotideEntryId.toList()
            ids.chunked(20).forEach { subids -> //to avoid to send to many ids in a single request
                subids.forEach {
                    titles[it] = null
                }
                var currentId: String? = null
                URL("${baseURL}/esummary.fcgi?db=nuccore&id=${subids.joinToString(",")}").readText()
                    .split("<Item Name=\"").forEach { line ->
                    if (line.startsWith("Caption")) {
                        Regex(".+>(.+)<.+").find(line)?.groups?.get(1)?.let { match ->
                            subids.find { it.startsWith(match.value) }?.let { id ->
                                currentId = id
                            }
                        }
                    }
                    if (line.startsWith("Title")) {
                        Regex(".+>(.+)<.+").find(line)?.groups?.get(1)?.let { match ->
                            currentId?.let {
                                titles[it] = match.value
                            }
                        }
                    }
                }
                Thread.sleep(1000)
            }

        } catch (e:Exception) {
            println(nucleotideEntryId)
            e.printStackTrace()
        }
        return titles
    }

}

class RNACentral {

    val baseURL = "https://rnacentral.org/api/v1/rna"

    fun fetch(id:String):SecondaryStructure? {
        //the sequence
        var text = URL("${baseURL}/${id}?format=json").readText()
        var data = Gson().fromJson(text, HashMap<String, String>().javaClass)
        val sequence = data["sequence"]
        sequence?.let {
            text = URL("${baseURL}/${id}/2d/1/?format=json").readText()
            data = Gson().fromJson(text, HashMap<String, String>().javaClass)
            val bn = (data["data"] as Map<String, String>)["secondary_structure"]
            bn?.let {
               ss {
                    bn {
                        value = bn
                        seq = sequence
                        name = id
                    }

                }.forEach {
                   it.source = RfamSource(id)
                   return it
               }
            }
        }
        return null
    }
}

enum class PDBQueryField {
    MINRES,
    MAXRES,
    MINDATE,
    MAXDATE,
    KEYWORDS,
    AUTHORS,
    PDBIDS,
    TITLE_CONTAINS,
    CONTAINS_RNA,
    CONTAINS_PROTEIN,
    CONTAINS_DNA,
    CONTAINS_HYBRID,
    EXPERIMENTAL_METHOD
}

data class PDBQuery(
    val query:Map<String, Any> = mapOf(
        "type" to "group",
        "logical_operator" to "and",
        "nodes" to listOf<Map<String, Any>>(
            mapOf(
                "type" to "terminal",
                "service" to "text",
                "parameters" to mapOf(
                    "operator" to  "in",
                    "value" to listOf("X-RAY DIFFRACTION", "ELECTRON MICROSCOPY"),
                    "attribute" to "exptl.method"
                )
            ),
            mapOf(
                "type" to "terminal",
                "service" to "text",
                "parameters" to mapOf(
                    "operator" to "less_or_equal",
                    "value" to 3.0,
                    "attribute" to "rcsb_entry_info.resolution_combined"
                )
            ),
            mapOf(
                "type" to "terminal",
                "service" to "text",
                "parameters" to mapOf(
                    "operator" to "greater_or_equal",
                    "value" to 1,
                    "attribute" to "rcsb_entry_info.polymer_entity_count_RNA"
                )
            )
        )
    ),
    val request_options:Map<String,Any> =  mapOf(
        "pager" to mapOf(
            "start" to 0
        )
    ),
    val return_type:String = "entry"
)

data class PDBResponse(
    val query_id:String,
    val result_type:String,
    val total_count:Int,
    val explain_meta_data:Map<String,Any>,
    val result_set:List<Map<String,Any>>
)

class PDB() {

    fun getEntry(pdbID: String) = StringReader(URL("https://files.rcsb.org/download/${pdbID}.pdb").readText())

    fun query():List<String> {
        val gson = GsonBuilder().setPrettyPrinting().create()
        var totalHits = 0
        var currentPage = 0
        var ids = mutableSetOf<String>()
        do {
            var reqParam =
                URLEncoder.encode("json", "UTF-8") + "=" + URLEncoder.encode(gson.toJson(PDBQuery(request_options = mapOf(
                    "pager" to mapOf(
                        "start" to currentPage*10,
                        "rows" to 10
                    )
                ))), "UTF-8")
            val url = URL("https://search.rcsb.org/rcsbsearch/v1/query?$reqParam")

            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"

                BufferedReader(InputStreamReader(inputStream)).use {
                    val response = StringBuffer()

                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }
                    val hits = gson.fromJson(response.toString(), PDBResponse::class.java)

                    totalHits = hits.total_count

                    hits.result_set.forEach {
                        ids.add(it["identifier"] as String)
                    }

                }
            }
            currentPage++
        } while ((currentPage-1)*10 < totalHits)

        return ids.toList().sorted()
    }


}

class NDB {

    fun listPDBFileNames():List<String> {
        var files = mutableSetOf<String>()
        val pattern = "([0-9].+?\\.pdb[0-9])".toRegex()
        val matches = pattern.findAll(URL("http://ndbserver.rutgers.edu/files/ftp/NDB/coordinates/na-biol/").readText())
        matches.forEach { matchResult ->
            files.add(matchResult.value)
        }
        return files.toList()
    }

    fun getEntry(pdbFileName: String) = StringReader(URL("http://ndbserver.rutgers.edu/files/ftp/NDB/coordinates/na-biol/$pdbFileName").readText())

}

class Rfam(var nameAsAccessionNumbers:Boolean = true) {
    fun getEntry(rfamID:String) = StringReader(URL("http://rfam.xfam.org/family/$rfamID/alignment?acc=$rfamID&format=stockholm&download=0").readText())
}