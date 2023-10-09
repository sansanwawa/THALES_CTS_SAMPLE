package org.thales.tokenization.entities.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Author :
 * sandy.haryono@thalesgroup.com
 */

@Getter
@Setter
@NoArgsConstructor
public class TokenizeResponse {

    private String token;
    private String status;

}
