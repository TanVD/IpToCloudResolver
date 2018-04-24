package org.jetbrains.logs.transformations.cloud.predicates

import com.sun.org.apache.xpath.internal.NodeSet
import inet.ipaddr.IPAddress
import inet.ipaddr.IPAddressString
import org.apache.commons.net.util.SubnetUtils
import org.jetbrains.logs.transformations.cloud.CloudIpPredicate
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.net.InetAddress
import java.net.URL
import java.util.*
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

class AzurePredicate : CloudIpPredicate {

    private val subnets = ArrayList<IPAddress>()

    private val azureIpAddressesUrl = "https://www.microsoft.com/en-us/download/confirmation.aspx?id=41653"

    private val downloadUrlPattern = Regex("(https://download.microsoft.com/.+?/PublicIPs_\\d+.xml)")

    init {
        val ipsDownloadPage = URL(azureIpAddressesUrl).readText()
        val downloadUrl = downloadUrlPattern.find(ipsDownloadPage)!!.groups[1]!!.value
        val xpath = XPathFactory.newInstance().newXPath()

        val ranges = xpath.compile("/AzurePublicIpAddresses/Region/IpRange").evaluate(
                InputSource(downloadUrl), XPathConstants.NODESET) as NodeList
        for (i in 0 until ranges.length) {
            val range = ranges.item(i)
            range.attributes.getNamedItem("Subnet")?.let {
                subnets.add(IPAddressString(it.nodeValue).address)
            }
        }
    }

    override fun check(ip: InetAddress) = subnets.any { it.contains(IPAddressString(ip.hostAddress).address) }

    override fun list(): List<IPAddress> = subnets
}