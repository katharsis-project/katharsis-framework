package io.katharsis.spring.boot;

import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.queryParams.QueryParamsParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
