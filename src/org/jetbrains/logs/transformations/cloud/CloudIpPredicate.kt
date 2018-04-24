package org.jetbrains.logs.transformations.cloud

import inet.ipaddr.IPAddress
import java.net.InetAddress

interface CloudIpPredicate {
    fun check(ip: InetAddress): Boolean

    fun list(): List<IPAddress>
}