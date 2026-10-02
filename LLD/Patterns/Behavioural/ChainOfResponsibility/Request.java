package Behavioural.ChainOfResponsibility;

public class Request {

    private final String token;
    private final String role;
    private final String body;

    public Request(
            String token,
            String role,
            String body
    ) {
        this.token = token;
        this.role = role;
        this.body = body;
    }

    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }

    public String getBody() {
        return body;
    }
}
