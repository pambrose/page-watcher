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
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking
import mu.KLogging
import kotlin.time.Duration.Companion.seconds

object PageWatcher : KLogging() {
  data class WebPage(
    var httpStatusCode: Int = 0,
    var httpStatusMessage: String = "",
    var allParagraphs: List<String> = emptyList(),
  )

  private fun fetchPage(url: String) =
    skrape(HttpFetcher) {
      logger.info { "Fetching page..." }

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
    val parser = ArgParser("pagewatcher")
    val watchUrl by parser.option(ArgType.String, shortName = "w", description = "Watch URL")
    val alertUrl by parser.option(ArgType.String, shortName = "a", description = "Alert URL")
    parser.parse(args)

    val watchVal = watchUrl ?: System.getenv("WATCH_URL") ?: System.getProperty("watch.url")
    val alertVal = alertUrl ?: System.getenv("ALERT_URL") ?: System.getProperty("alert.url")

    val origPage = fetchPage(watchVal)

    while (true) {
      Thread.sleep(60.seconds.inWholeMilliseconds)
      val currPage = fetchPage(watchVal)

      if (currPage != origPage) {
        logger.info { "Page changes:" }
        logger.info { currPage.allParagraphs.subtract(origPage.allParagraphs) }

        runBlocking {
          val client = HttpClient(CIO)
          val response: HttpResponse = client.get(alertVal)
        }
        break
      }

      logger.info { "Page unchanged" }
    }
  }
}
