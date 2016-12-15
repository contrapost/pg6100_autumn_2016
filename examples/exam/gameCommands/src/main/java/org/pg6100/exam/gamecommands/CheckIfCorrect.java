package org.pg6100.exam.gamecommands;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import org.pg6100.exam.quizapi.dto.ReadQuizDto;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Created by arcuri82 on 02-Dec-16.
 */
public class CheckIfCorrect extends HystrixCommand<CheckIfCorrect.QuizResult> {

    public enum QuizResult{CORRECT, WRONG, INVALID, ERROR}

    private final String id;
    private final int index;

    private final Client client;
    private final UriBuilder base;

    public CheckIfCorrect(UriBuilder base, String id, int index) {
        super(HystrixCommandGroupKey.Factory.asKey("CheckIfCorrect"));
        this.id = id;
        this.index = index;
        this.base = base;
        client = ClientBuilder.newClient();
    }

    @Override
    protected QuizResult run() throws Exception {

        URI uri = base.path("/"+id).build();
        Response response = client.target(uri)
                .request("application/json")
                .get();
        if(response.getStatus() >= 400 && response.getStatus() < 500){
            return QuizResult.INVALID;
        } else if(response.getStatus() >= 500){
            return QuizResult.ERROR;
        }

        ReadQuizDto dto = response.readEntity(ReadQuizDto.class);
        if(dto.indexOfCorrectAnswer == index){
            return QuizResult.CORRECT;
        } else {
            return QuizResult.WRONG;
        }
    }

    @Override
    protected QuizResult getFallback() {
        return QuizResult.ERROR;
    }
}