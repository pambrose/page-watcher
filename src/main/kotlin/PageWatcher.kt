import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.extractIt
import it.skrape.fetcher.skrape
import it.skrape.selects.eachText
import it.skrape.selects.html5.p
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime.now
import kotlin.time.Duration.Companion.seconds

object PageWatcher {
  val watchUrl = System.getenv("WATCH_URL")
  val alertUrl = System.getenv("ALERT_URL")

  data class WebPage(
    var httpStatusCode: Int = 0,
    var httpStatusMessage: String = "",
    var allParagraphs: List<String> = emptyList(),
  )

  private fun fetchPage(url: String) =
    skrape(HttpFetcher) {
      println("Fetching page at ${now()}...")

      request { this.url = url }

      extractIt<WebPage> {
        it.httpStatusCode = responseStatus.code
        it.httpStatusMessage = responseStatus.message
        htmlDocument {
          it.allParagraphs = p { findAll { eachText } }
        }
      }
    }

  @JvmStatic
  fun main(args: Array<String>) {
    val origPage = fetchPage(watchUrl)

    while (true) {
      Thread.sleep(60.seconds.inWholeMilliseconds)
      val currPage = fetchPage(watchUrl)

      if (currPage != origPage) {
        println("Page changed at ${now()}:")
        println(currPage.allParagraphs.subtract(origPage.allParagraphs))

        runBlocking {
          val client = HttpClient(CIO)
          val response: HttpResponse = client.get(alertUrl)
        }
        break
      }

      println("Page unchanged at ${now()}")
    }
  }
}
