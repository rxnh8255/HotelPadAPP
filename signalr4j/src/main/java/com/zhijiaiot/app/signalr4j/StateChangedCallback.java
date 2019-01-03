/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package com.zhijiaiot.app.signalr4j;

/**
 * Callback invoked when a connection changes its state
 */
public interface StateChangedCallback {
    public void stateChanged(ConnectionState oldState, ConnectionState newState);
}
