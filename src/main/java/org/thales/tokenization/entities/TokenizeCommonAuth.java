package org.thales.tokenization.entities;

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
public class TokenizeCommonAuth {

    private String username;
    private String password;
    private String tokengroup;
    private String tokentemplate;
}
