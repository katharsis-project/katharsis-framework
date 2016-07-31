package io.katharsis.itests.json

import com.fasterxml.jackson.databind.ObjectMapper
import io.katharsis.domain.CollectionResponse
import io.katharsis.domain.JsonApiImpl
import io.katharsis.domain.LinkImpl
import io.katharsis.domain.MetaResponse
import io.katharsis.domain.SingleResponse
import io.katharsis.domain.api.LinksInformation
import io.katharsis.domain.api.MetaInformation
import io.katharsis.jackson.BaseResponseSerializerTest.LinksData
import io.katharsis.jackson.BaseResponseSerializerTest.MetaData
import io.katharsis.jackson.JsonApiModuleBuilder
import io.katharsis.resource.annotations.JsonApiId
import io.katharsis.resource.annotations.JsonApiResource
import io.katharsis.resource.annotations.JsonApiToOne
import net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DataSerializationTest {

    val mapper = ObjectMapper()
            .registerModule(JsonApiModuleBuilder.create());

    class ComplexLinks(val self: String = "Self", val related: LinkImpl) : LinksInformation

    @Test
    fun testSerializeMetadataResponse() {
        val expected = """{"meta":{"author":"Fave Author"}, "jsonapi":null, "links":null }"""
        val resource = MetaResponse(MetaData("Fave Author"), null, null)

        val res = mapper.writeValueAsString(resource);

        assertNotNull(res);
        assertThatJson(res).isEqualTo(expected);
    }

    @Test
    fun testSimpleLinkSerialization() {
        val resource = SingleResponse(null, null, null, LinksData("self-link"), null)

        val res = mapper.writeValueAsString(resource);

        assertNotNull(res);
        assertEquals("""{"links":{"self":"self-link"}}""", res)
    }

    @Test
    fun testComplexLinksSerialization() {
        //    "links": {
        //        "related": {
        //            "href": "http://example.com/articles/1/comments",
        //            "meta": {
        //            "count": 10
        //        }
        //        }

        data class CountMeta(val count: Int = 10) : MetaInformation

        val resource = SingleResponse(null, null, null,
                ComplexLinks("self", LinkImpl("http://example.com/articles/1/comments", CountMeta())), null)

        val res = mapper.writeValueAsString(resource);

        assertNotNull(res);
        assertEquals("""{"links":{"self":"self","related":{"href":"http://example.com/articles/1/comments","meta":{"count":10}}}}""", res)
    }

    @Test
    fun testJsonApiObjectSerialization() {
        val expected = """
                {
                    "jsonapi": {
                    "version": "1.0"
                }, "links":null, "meta":null
                }"""

        val resource = MetaResponse(null, JsonApiImpl(null), null);
        val res = mapper.writeValueAsString(resource);

        assertNotNull(res);
        println(res)
        assertThatJson(res).isEqualTo(expected)
    }

    @JsonApiResource(type = "articles")
    data class Article(@JsonApiId val id: Int, val title: String);

    @Test
    fun testFetch_EmptyCollection() {
        val expected = """
                {
                    "links": {
                    "self": "http://example.com/articles"
                },
                    "data": []
                }"""

        val resource = CollectionResponse(listOf(), null, null, LinksData("http://example.com/articles"), null);

        val res = mapper.writeValueAsString(resource);

        assertThatJson(res).isEqualTo(expected);

    }

    @Test
    @Ignore
    fun testarticleWithCollectionSerialization() {
        val expected = """
            {
              "links": {
                "self": "http://example.com/articles/1"
              },
              "data": {
                "type": "articles",
                "id": "1",
                "attributes": {
                  "title": "JSON API paints my bikeshed!"
                },
                "relationships": {
                  "author": {
                    "links": {
                      "related": "http://example.com/articles/1/author"
                    }
                  }
                }
              }
            }"""

        @JsonApiResource(type = "people")
        data class Person(@JsonApiId val id: Int, val name: String = "default name")

        @JsonApiResource(type = "articles")
        data class Article(@JsonApiId val id: Int, val title: String, @JsonApiToOne val author: Person)

        val data = SingleResponse(Article(1, "JSON API paints my bikeshed!", Person(1)), null, null,
                LinksData("http://example.com/articles/1"), null);

        val res = mapper.writeValueAsString(data);

        println(res);
        assertThatJson(res).isEqualTo(expected);
    }

    @Test
    @Ignore("Fails with Maven")
    fun testFetch_articleCollection() {
        val expected = """{
          "data": [
            {
              "type": "articles",
              "id": "1",
              "attributes": {
                "title": "JSON API paints my bikeshed!"
              }
            },
            {
              "type": "articles",
              "id": "2",
              "attributes": {
                "title": "Rails is Omakase"
              }
            }
          ],
          "links": {
            "self": "http://exaKmple.com/articles"
          }
        }"""

        val data = CollectionResponse(listOf(Article(1, "JSON API paints my bikeshed!"),
                Article(2, "Rails is Omakase")), null, null, LinksData("http://example.com/articles"), null);

        val res = mapper.writeValueAsString(data);

        println(res);
        assertThatJson(res).isEqualTo(expected);
    }

}
