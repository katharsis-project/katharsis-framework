package io.katharsis.spring.legacy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.katharsis.legacy.queryParams.DefaultQueryParamsParser;
import io.katharsis.legacy.queryParams.QueryParamsBuilder;
import io.katharsis.legacy.queryParams.QueryParamsParser;

@Configuration
public class QueryParamsBuilderConfiguration {

    @Bean
    public QueryParamsParser queryParamsParser() {
        return new DefaultQueryParamsParser();
    }

    @Bean
    public QueryParamsBuilder queryParamsBuilder(QueryParamsParser queryParamsParser) {
        return new QueryParamsBuilder(queryParamsParser);
    }
}
