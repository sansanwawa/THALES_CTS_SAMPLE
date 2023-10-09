import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.thales.tokenization.CTSPoster;
import org.thales.tokenization.entities.TokenizeCommonAuth;
import org.thales.tokenization.entities.TokenizeWrapper;
import org.thales.tokenization.entities.response.DeTokenizeResponse;
import org.thales.tokenization.entities.response.TokenizeResponse;


/**
 * Author :
 * sandy.haryono@thalesgroup.com
 */


public class SingleTokenizationTest {

    private static final Logger logger = LogManager.getLogger(SingleTokenizationTest.class);
    private static final String TOKEN_URL = "https://10.10.1.10/vts/rest/v2.0/tokenize";
    private static final String DETOKEN_URL = "https://10.10.1.10/vts/rest/v2.0/detokenize";
    private static final String USERNAME = "your_username";
    private static final String PASSWORD = "your_password";


    @Before
    public void setUp() {
        Configurator.setAllLevels("", Level.INFO);
    }

    @Test
    void testTokenizeRequest() {

        CTSPoster.tokenizeInit(TOKEN_URL);

        try {
            String groupName = "tokenGroup2";
            String templateName = "Tokening_Numeric";
            String data = "1111-2222-3333-4444";
            TokenizeWrapper wrapper = new TokenizeWrapper();
            wrapper.setHeaderAuth(new TokenizeCommonAuth(USERNAME, PASSWORD, groupName, templateName));
            TokenizeResponse response = CTSPoster.tokenize(wrapper, data);
            logger.info("data = [{}], token = [{}], status = [{}]", data, response.getToken(), response.getStatus());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void testDeTokenizeMultipleRequest() {

        CTSPoster.deTokenizeInit(DETOKEN_URL);

        try {
            String groupName = "tokenGroup2";
            String templateName = "Tokening_Numeric";
            String token = "5209-9884-6659-7031";
            TokenizeWrapper wrapper = new TokenizeWrapper();
            wrapper.setHeaderAuth(new TokenizeCommonAuth(USERNAME, PASSWORD, groupName, templateName));
            DeTokenizeResponse response = CTSPoster.deTokenize(wrapper, token);
            logger.info("data = [{}], token = [{}], status = [{}]", token, response.getData(), response.getStatus());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


}
