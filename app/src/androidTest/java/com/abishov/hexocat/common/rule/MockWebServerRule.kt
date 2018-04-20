package com.abishov.hexocat.common.rule

import android.support.test.espresso.Espresso
import com.abishov.hexocat.HexocatTestApp
import com.jakewharton.espresso.OkHttp3IdlingResource
import io.appflate.restmock.RESTMockServer
import okhttp3.HttpUrl
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class MockWebServerRule : TestRule {

  override fun apply(base: Statement, description: Description): Statement {
    return object : Statement() {

      @Throws(Throwable::class)
      override fun evaluate() {
        val baseUrl = HttpUrl.parse(RESTMockServer.getUrl())
        HexocatTestApp.overrideBaseUrl(baseUrl!!)

        RESTMockServer.reset()

        val client = HexocatTestApp.instance.appComponent().okHttpClient()
        val okhttpIdlingResource = OkHttp3IdlingResource.create("okhttp", client)

        Espresso.registerIdlingResources(okhttpIdlingResource)
        try {
          base.evaluate()
        } finally {
          Espresso.unregisterIdlingResources(okhttpIdlingResource)
        }
      }
    }
  }
}