package org.thales.tokenization.entities.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Author :
 * sandy.haryono@thalesgroup.com
 */

@Getter
@Setter
@AllArgsConstructor
public class DeTokenizeRequest {
    private String tokengroup;
    private String tokentemplate;
    private String token;

}
