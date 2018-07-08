package org.tanvd.clouips.predicates

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import inet.ipaddr.IPAddress
import inet.ipaddr.IPAddressString
import org.apache.commons.net.util.SubnetUtils
import org.tanvd.clouips.CloudIpPredicate
import java.net.InetAddress
import java.net.URL
import java.util.ArrayList

class AwsPredicate : CloudIpPredicate {

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Ranges(val prefixes: List<Range>)

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Range(val ip_prefix: String)

    private val subnets = ArrayList<IPAddress>()

    private val jsonMapper = ObjectMapper().apply {
        registerModule(KotlinModule())
    }

    private val awsIpAddressUrl = "https://ip-ranges.amazonaws.com/ip-ranges.json";

    init {
        val ipsPage = URL(awsIpAddressUrl).readText()
        val ranges = jsonMapper.readValue(ipsPage, Ranges::class.java)
        for (range in ranges.prefixes) {
            subnets.add(IPAddressString(range.ip_prefix).toAddress())
        }
    }

    override fun check(ip: InetAddress) = subnets.any { it.contains(IPAddressString(ip.hostAddress).address) }

    override fun list(): List<IPAddress> = subnets
}