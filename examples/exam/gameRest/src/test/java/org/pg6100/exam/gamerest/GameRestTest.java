package org.pg6100.exam.gamerest;

import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;

/**
 * Created by arcuri82 on 05-Dec-16.
 */
public class GameRestTest extends GameRestTestBase {

    static {
        System.setProperty(GameRest.QUIZ_URL_PROP, QUIZ_MOCKED_URL);
    }

    @ClassRule
    public static final DropwizardAppRule<GameRestConfiguration> RULE =
            new DropwizardAppRule<>(GameRestApplication.class);

}
