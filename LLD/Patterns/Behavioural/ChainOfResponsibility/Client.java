package Behavioural.ChainOfResponsibility;

public class Client {
    public static void main(String[] args) {
        RequestHandler authentication =
                new AuthenticationHandler();

        RequestHandler authorization =
                new AuthorizationHandler();

        RequestHandler validation =
                new ValidationHandler();

        authentication
                .setNext(authorization)
                .setNext(validation);

        Request request =
                new Request(
                        "valid-token",
                        "ADMIN",
                        "request-body"
                );

        authentication.handle(request);
    }
}
