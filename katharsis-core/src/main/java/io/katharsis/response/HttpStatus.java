package io.katharsis.response;

public interface HttpStatus {

    int OK_200 = 200;
    int CREATED_201 = 201;
    int NO_CONTENT_204 = 204;
    int NOT_FOUND_404 = 404;
    int BAD_REQUEST_400 = 400;
    int FORBIDDEN_403 = 403;
    int CONFLICT_409 = 409;
    int UNPROCESSABLE_422 = 422;
    int INTERNAL_SERVER_ERROR_500 = 500;
    int NOT_IMPLEMENTED_501 = 501;
    int BAD_GATEWAY_502 = 502;
    int SERVICE_UNAVAILABLE_503 = 503;
    int GATEWAY_TIMEOUT_504 = 504;
    int HTTP_VERSION_NOT_SUPPORTED_505 = 505;
}
