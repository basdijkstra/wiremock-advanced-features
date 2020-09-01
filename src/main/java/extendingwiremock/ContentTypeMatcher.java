package extendingwiremock;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;

public class ContentTypeMatcher extends RequestMatcherExtension {

    @Override
    public String getName() {
        return "content-type-matcher";
    }

    @Override
    public MatchResult match(Request request, Parameters parameters) {
        String contentType = parameters.getString("Content-Type");
        return MatchResult.of(request.getHeader("Content-Type").contains(contentType));
    }
}
