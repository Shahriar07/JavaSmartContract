/*
SPDX-License-Identifier: Apache-2.0
*/

package org.konasl.ledgerapi;

@FunctionalInterface
public interface StateDeserializer {
    State deserialize(byte[] buffer);
}
