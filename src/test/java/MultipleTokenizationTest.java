import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.thales.tokenization.CTSPoster;
import org.thales.tokenization.entities.request.DeTokenizeRequest;
import org.thales.tokenization.entities.request.TokenizeRequest;
import org.thales.tokenization.entities.response.DeTokenizeResponse;
import org.thales.tokenization.entities.response.TokenizeResponse;

import java.util.ArrayList;

/**
 * Author :
 * sandy.haryono@thalesgroup.com
 */

public class MultipleTokenizationTest {

    private static final Logger logger = LogManager.getLogger(MultipleTokenizationTest.class);
    private static final String TOKEN_URL = "https://10.10.1.10/vts/rest/v2.0/tokenize";
    private static final String DETOKEN_URL = "https://10.10.1.10/vts/rest/v2.0/detokenize";
    private static final String USERNAME = "your_username";
    private static final String PASSWORD = "your_password";


    @Before
    public void setUp() {
        Configurator.setAllLevels("", Level.INFO);
    }

    @Test
    void testTokenizeMultipleRequest() {
        CTSPoster.tokenizeInit(TOKEN_URL);
        ArrayList<TokenizeRequest> tokenizeMultipleRequest = new ArrayList<TokenizeRequest>();
        tokenizeMultipleRequest.add(new TokenizeRequest("tokenGroup2", "Tokening_Numeric", "1111-2222-3333-4444"));
        tokenizeMultipleRequest.add(new TokenizeRequest("tokenGroup2", "Tokening_Numeric", "08120820120"));

        try {
            TokenizeResponse[] response = CTSPoster.tokenizeMultiple(USERNAME, PASSWORD, tokenizeMultipleRequest);

            for (int i = 0; i < response.length; i++) {
                logger.info("data[{}] = [{}], tokenValue= [{}]", i,
                        tokenizeMultipleRequest.get(i).getData(), response[i].getToken());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void testDeTokenizeMultipleRequest() {

        CTSPoster.deTokenizeInit(DETOKEN_URL);
        ArrayList<DeTokenizeRequest> deTokenizeMultipleRequest = new ArrayList<DeTokenizeRequest>();
        deTokenizeMultipleRequest.add(new DeTokenizeRequest("tokenGroup2", "Tokening_Numeric", "5209-9884-6659-7031"));
        deTokenizeMultipleRequest.add(new DeTokenizeRequest("tokenGroup2", "Tokening_Numeric", "00517237770"));

        DeTokenizeResponse[] response = CTSPoster.deTokenizeMultiple(USERNAME, PASSWORD, deTokenizeMultipleRequest);

        for (int i = 0; i < response.length; i++) {
            logger.info("data[{}] = [{}], token = [{}]", i,
                    deTokenizeMultipleRequest.get(i).getToken(), response[i].getData());

        }

    }


}
