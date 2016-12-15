package org.pg6100.exam.gamecommands;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import org.pg6100.exam.quizapi.dto.ReadQuizDto;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Created by arcuri82 on 02-Dec-16.
 */
public class GetRandomQuiz extends HystrixCommand<ReadQuizDto> {

    private final Client client;
    private final UriBuilder base;

    public GetRandomQuiz(UriBuilder base) {
        super(HystrixCommandGroupKey.Factory.asKey("GetRandomQuiz"));
        this.base = base;
        client = ClientBuilder.newClient();
    }

    @Override
    protected ReadQuizDto run() throws Exception {

        URI uri = base.build();
        ReadQuizDto result = client.target(uri)
                .request("application/json")
                .get(ReadQuizDto.class);

        return result;
    }

    @Override
    protected ReadQuizDto getFallback() {
        return null;
    }
}
