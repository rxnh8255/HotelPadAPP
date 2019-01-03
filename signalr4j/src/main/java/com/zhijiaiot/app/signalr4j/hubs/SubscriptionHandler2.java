/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package com.zhijiaiot.app.signalr4j.hubs;

public interface SubscriptionHandler2<E1, E2> {
    public void run(E1 p1, E2 p2);
}