package org.jetbrains.logs.transformations.cloud

import java.net.InetAddress

interface CloudIpPredicate {
    fun check(ip: InetAddress): Boolean
}