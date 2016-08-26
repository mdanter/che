/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.git.params;

import org.eclipse.che.api.git.shared.PullRequest;

/**
 * Arguments holder for {@link org.eclipse.che.api.git.GitConnection#pull(PullParams)}.
 *
 * @author Igor Vinokur
 */
public class PullParams {

    private String refSpec;
    private String remote;
    private int    timeout;

    private PullParams() {
    }

    /**
     * Create new {@link PullParams} instance
     *
     * @param remote
     *         remote name to pull from
     */
    public static PullParams create(String remote) {
        return new PullParams().withRemote(remote);
    }

    /** @see PullRequest#getRefSpec() */
    public String getRefSpec() {
        return refSpec;
    }

    /** @see PullRequest#withRefSpec(String) */
    public PullParams withRefSpec(String refSpec) {
        this.refSpec = refSpec;
        return this;
    }

    /** @see PullRequest#getRemote() */
    public String getRemote() {
        return remote;
    }

    /** @see PullRequest#withRemote(String) */
    public PullParams withRemote(String remote) {
        this.remote = remote;
        return this;
    }

    /** @see PullRequest#getTimeout() */
    public int getTimeout() {
        return timeout;
    }

    /** @see PullRequest#withTimeout(int) */
    public PullParams withTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }
}