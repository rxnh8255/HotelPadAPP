/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package com.zhijiaiot.app.signalr4j;

public interface ErrorCallback {
    /**
     * Callback invoked when an error is found
     * 
     * @param error
     *            The error
     */
    public void onError(Throwable error);
}
