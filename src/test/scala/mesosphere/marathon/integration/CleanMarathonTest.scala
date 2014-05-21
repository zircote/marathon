package mesosphere.marathon.integration

import mesosphere.marathon.api.v1.AppDefinition


/**
 * Makes sure no apps exist in Marathon so we can use it for testing
 */
class CleanMarathonTest extends IntegrationTest {

  def run() {
    val response = get("/v2/apps")
    val json = parseJSON(response.entity.asString)

    assert((json \ "apps").extract[List[AppDefinition]].length == 0,
      "Should have no existing apps")
  }
}
