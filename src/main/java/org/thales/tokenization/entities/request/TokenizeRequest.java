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
public class TokenizeRequest {
    private String tokengroup;
    private String tokentemplate;
    private String data;
}
