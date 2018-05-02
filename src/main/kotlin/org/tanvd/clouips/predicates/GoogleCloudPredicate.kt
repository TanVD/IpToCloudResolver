package org.tanvd.clouips.predicates

import inet.ipaddr.IPAddress
import inet.ipaddr.IPAddressString
import org.apache.commons.net.util.SubnetUtils
import org.tanvd.clouips.CloudIpPredicate
import org.xbill.DNS.Lookup
import org.xbill.DNS.TXTRecord
import org.xbill.DNS.Type
import java.net.InetAddress
import java.util.ArrayList

class GoogleCloudPredicate : CloudIpPredicate {

    private val subnets = ArrayList<IPAddress>()

    private val txtResponse = Regex("v=spf1 ([^\"]+) \\?all")

    init {
        val serversTxtRecord = Lookup("_cloud-netblocks.googleusercontent.com", Type.TXT).run().first() as TXTRecord
        val servers = txtResponse.matchEntire((serversTxtRecord.strings as List<String>).first())!!.groupValues[1].split("include:").map { it.trim() }.filter { it.isNotBlank() }

        for (server in servers) {
            val ipsTextRecords = (Lookup(server, Type.TXT).run().first() as TXTRecord)
            val ips = (ipsTextRecords.strings as List<String>).first().split(" ").filter { it.startsWith("ip4:") || it.startsWith("ip6:") }.
                    map { it.drop("ip*:".length) }
            for (ip in ips) {
                subnets.add(IPAddressString(ip).address)
            }
        }
    }

    override fun check(ip: InetAddress): Boolean = subnets.any { it.contains(IPAddressString(ip.hostAddress).address) }

    override fun list(): List<IPAddress> = subnets
}