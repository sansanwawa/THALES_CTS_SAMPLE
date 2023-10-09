package org.thales.tokenization.entities;

import lombok.Getter;
import lombok.Setter;
import org.thales.tokenization.entities.TokenizeCommonAuth;

/**
 * Author :
 * sandy.haryono@thalesgroup.com
 */
@Getter
@Setter
public class TokenizeWrapper {
    private TokenizeCommonAuth headerAuth;
}
