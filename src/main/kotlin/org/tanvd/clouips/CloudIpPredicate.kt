package org.tanvd.clouips

import inet.ipaddr.IPAddress
import java.net.InetAddress

interface CloudIpPredicate {
    fun check(ip: InetAddress): Boolean

    fun list(): List<IPAddress>
}