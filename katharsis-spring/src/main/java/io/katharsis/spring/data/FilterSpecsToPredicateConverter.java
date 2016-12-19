package io.katharsis.spring.data;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import io.katharsis.queryspec.FilterOperator;
import io.katharsis.queryspec.FilterSpec;

/**
 * This class can be used to convert Katharsis FilterSpec objects to QueryDSL Predicates for use in persistences systems
 * that support QueryDSL, such as Spring Data and MongoDB.
 */
public class FilterSpecsToPredicateConverter extends BaseConverter {

    /**
     * This method converts a list of FilterSpec Katharsis objects to a QueryDSL predicate
     *
     * @param filterSpecs The list of FilterSpec objects to convert, can be null
     * @return Null if filterSpecs is empty or null, otherwise a Predicate object with multiple FilterSpec objects AND'ed
     * together
     */
    public Predicate convert(Iterable<FilterSpec> filterSpecs) {
        Predicate result = null;

        if (filterSpecs != null) {
            // Loop through all the FilterSpec objects from the Katharsis QuerySpec and build a QueryDSL Predicate
            // object from that. If there are multiple FilterSpecs, the predicates are AND'ed together.
            for (FilterSpec spec : filterSpecs) {
                Predicate predicate;
                Operator op = convertOperator(spec.getOperator());
                String attrPath = convertAttributePathToString(spec.getAttributePath());
                StringPath path = Expressions.stringPath(attrPath);
                Expression value = Expressions.constant(spec.getValue());

                predicate = Expressions.predicate(op, path, value);

                if (result == null) {
                    result = predicate;
                } else {
                    result = Expressions.predicate(Ops.AND, result, predicate);
                }
            }
        }

        return result;
    }

    /**
     * Converts a Katharsis FilterOperator to a QueryDSL Operator. Most operators will match by name with the
     * exception of GE, LE and NEQ
     *
     * @param op The FilterOperator to convert
     * @return The converted QueryDSL Operator or it will throw a IllegalArgumentException if a match could not be found
     */
    public Operator convertOperator(FilterOperator op) {
        Operator result;

        // Most operators will match by name with the exception of GE, LE and NEQ
        if (op.equals(FilterOperator.GE)) {
            result = Ops.GOE;
        } else if (op.equals(FilterOperator.LE)) {
            result = Ops.LOE;
        } else if (op.equals(FilterOperator.NEQ)) {
            result = Ops.NE;
        } else {
            // The rest of the FilterOperators match one-for-one by their name value
            result = Ops.valueOf(op.name()); // Throws IllegalArgumentException if none can be found (should not happen)
        }

        return result;
    }
}
