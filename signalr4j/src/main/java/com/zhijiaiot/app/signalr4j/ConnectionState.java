/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package com.zhijiaiot.app.signalr4j;

/***
 * Represents the state of a connection
 */
public enum ConnectionState {
    Connecting, Connected, Reconnecting, Disconnected
}