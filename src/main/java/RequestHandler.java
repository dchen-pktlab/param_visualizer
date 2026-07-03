import burp.api.montoya.proxy.http.InterceptedRequest;
import burp.api.montoya.proxy.http.ProxyRequestHandler;
import burp.api.montoya.proxy.http.ProxyRequestReceivedAction;
import burp.api.montoya.proxy.http.ProxyRequestToBeSentAction;

public class RequestHandler implements ProxyRequestHandler {
    private final RequestParser parser;
    private final Runnable onUpdate;

    public RequestHandler(RequestParser parser) {
        this(parser, null);
    }

    public RequestHandler(RequestParser parser, Runnable onUpdate) {
        this.parser = parser;
        this.onUpdate = onUpdate;
    }

    @Override
    public ProxyRequestReceivedAction handleRequestReceived(InterceptedRequest interceptedRequest) {
        // No-op: let the request flow through unchanged
        return ProxyRequestReceivedAction.continueWith(interceptedRequest);
    }

    @Override
    public ProxyRequestToBeSentAction handleRequestToBeSent(InterceptedRequest interceptedRequest) {
        parser.parse_proxy_http(interceptedRequest);
        if (onUpdate != null) {
            onUpdate.run();
        }
        return ProxyRequestToBeSentAction.continueWith(interceptedRequest);
    }
}