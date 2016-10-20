package io.katharsis.jackson.serializer.include;


@lombok.EqualsAndHashCode
@lombok.Getter
@lombok.AllArgsConstructor
@lombok.ToString
public class ResourceDigest {
    private final Object id;
    private final String type;

}
