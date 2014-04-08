package cn.heroes.ud.protocol;
/* 
 *  This file is part of dhcp4java, a DHCP API for the Java language. 
 *  (c) 2006 Stephan Hadinger 
 * 
 *  This library is free software; you can redistribute it and/or 
 *  modify it under the terms of the GNU Lesser General Public 
 *  License as published by the Free Software Foundation; either 
 *  version 2.1 of the License, or (at your option) any later version. 
 * 
 *  This library is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU 
 *  Lesser General Public License for more details. 
 * 
 *  You should have received a copy of the GNU Lesser General Public 
 *  License along with this library; if not, write to the Free Software 
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA 
 */  
  
import static org.dhcp4java.DHCPConstants.BOOTREPLY;  
import static org.dhcp4java.DHCPConstants.DHCPACK;  
import static org.dhcp4java.DHCPConstants.DHCPOFFER;  
import static org.dhcp4java.DHCPConstants.DHO_DHCP_LEASE_TIME;  
import static org.dhcp4java.DHCPConstants.DHO_DHCP_SERVER_IDENTIFIER;  
import static org.dhcp4java.DHCPConstants.DHO_ROUTERS;  
import static org.dhcp4java.DHCPConstants.DHO_SUBNET_MASK;  
import static org.dhcp4java.DHCPConstants.HTYPE_ETHER;  
  
import java.net.InetAddress;  
import java.net.UnknownHostException;  
import java.util.HashMap;  
import java.util.Properties;  
import java.util.logging.Level;  
import java.util.logging.Logger;  
  
import org.dhcp4java.DHCPCoreServer;  
import org.dhcp4java.DHCPOption;  
import org.dhcp4java.DHCPPacket;  
import org.dhcp4java.DHCPResponseFactory;  
import org.dhcp4java.DHCPServerInitException;  
import org.dhcp4java.DHCPServlet;  
  
/** 
 * A sample DHCP servlet (under construction). 
 *  
 * @author Stephan Hadinger 
 * @version 0.60 
 */  
public class DHCP extends DHCPServlet {  
  
    private static final Logger logger = Logger  
            .getLogger("org.dhcp4java.examplesserver.dhcpstaticserver");  
  
    private HashMap<String, InetAddress> macIpMap = new HashMap<String, InetAddress>();  
    DHCPOption[] commonOptions;  
  
    /* 
     * (non-Javadoc) 
     *  
     * @see org.dhcp4java.DHCPServlet#init(java.util.Properties) 
     */  
    @Override  
    public void init(Properties props) {  
        // we create a dummy packet to extract "common options"  
        DHCPPacket temp = new DHCPPacket();  
        try {  
            // parse all properties to extract static client definitions  
            for (Object keyObject : props.keySet()) {  
                String key = ((String) keyObject);  
                if (key.startsWith("client.")) {  
                    String addrString = (String) props.get(keyObject);  
                    try {  
                        InetAddress addr = InetAddress.getByName(addrString);  
                        this.macIpMap.put(key.substring("client.".length())  
                                .toLowerCase(), addr);  
                    } catch (UnknownHostException e) {  
                        logger.log(Level.SEVERE, "Could not parse InetAddress "  
                                + addrString, e);  
                    }  
                }  
            }  
  
            temp.setOptionAsInetAddress(DHO_DHCP_SERVER_IDENTIFIER,  
                    "192.168.1.1");  
            temp.setOptionAsInt(DHO_DHCP_LEASE_TIME, 3600 * 24 * 7); /* 1 week */  
            temp.setOptionAsInetAddress(DHO_SUBNET_MASK, "255.255.255.0");  
            temp.setOptionAsInetAddress(DHO_ROUTERS, "192.168.1.1");  
  
            // store options in a instance array  
            this.commonOptions = temp.getOptionsArray();  
        } catch (UnknownHostException e) {  
            throw new RuntimeException(e);  
        }  
    }  
  
    /** 
     * @see org.dhcp4java.DHCPServlet#doDiscover(org.dhcp4java.DHCPPacket) 
     */  
    @Override  
    protected DHCPPacket doDiscover(DHCPPacket request) {  
        InetAddress clientIp = this.calcAddrFromMac(request);  
        if (clientIp == null) {  
            return null;  
        }  
  
        DHCPPacket response = new DHCPPacket();  
        response.setOp(BOOTREPLY);  
        response.setHtype(request.getHtype());  
        response.setHlen(request.getHlen());  
        response.setHops((byte) 0);  
        response.setXid(request.getXid());  
        response.setSecs((short) 0);  
        response.setYiaddr(clientIp);  
        response.setSiaddr(server.getSockAddress().getAddress()); // server  
        // identifier  
        response.setFlags(request.getFlags());  
        response.setGiaddrRaw(request.getGiaddrRaw());  
        response.setChaddr(request.getChaddr());  
  
        response.setDHCPMessageType(DHCPOFFER);  
        response.setOptions(this.commonOptions);  
  
        response.setAddrPort(DHCPResponseFactory.getDefaultSocketAddress(  
                request, response.getOp()));  
  
        return response;  
    }  
  
    /** 
     * @see org.dhcp4java.DHCPServlet#doRequest(org.dhcp4java.DHCPPacket) 
     */  
    @Override  
    protected DHCPPacket doRequest(DHCPPacket request) {  
        InetAddress clientIp = this.calcAddrFromMac(request);  
        if (clientIp == null) {  
            return null;  
        }  
  
        DHCPPacket response = new DHCPPacket();  
        response.setOp(BOOTREPLY);  
        response.setHtype(request.getHtype());  
        response.setHlen(request.getHlen());  
        response.setHops((byte) 0);  
        response.setXid(request.getXid());  
        response.setSecs((short) 0);  
        response.setCiaddrRaw(request.getCiaddrRaw());  
        response.setYiaddr(clientIp);  
        response.setSiaddr(server.getSockAddress().getAddress()); // server  
        // identifier  
        response.setFlags(request.getFlags());  
        response.setGiaddrRaw(request.getGiaddrRaw());  
        response.setChaddr(request.getChaddr());  
  
        response.setDHCPMessageType(DHCPACK);  
        response.setOptions(this.commonOptions);  
  
        response.setAddrPort(DHCPResponseFactory.getDefaultSocketAddress(  
                request, response.getOp()));  
  
        return response;  
    }  
  
    /** 
     * @see org.dhcp4java.DHCPServlet#doDecline(org.dhcp4java.DHCPPacket) 
     */  
    @Override  
    protected DHCPPacket doDecline(DHCPPacket request) {  
        logger.warning("DHCPDECLINE received:" + request.toString());  
        return null;  
    }  
  
    /** 
     * @see org.dhcp4java.DHCPServlet#doRelease(org.dhcp4java.DHCPPacket) 
     */  
    @Override  
    protected DHCPPacket doRelease(DHCPPacket request) {  
        logger.info("DHCPRELEASE received:" + request.toString());  
        return null;  
    }  
  
    /** 
     * Calculate address from packet and @MAC Address. 
     *  
     * <p> 
     * Also checks if this client is accepted (Vendor, User...) 
     *  
     * @param request 
     * @return address for client, or null if ignore 
     */  
    private InetAddress calcAddrFromMac(DHCPPacket request) {  
        // check @MAC address format  
        if ((request.getHtype() != HTYPE_ETHER) && (request.getHlen() != 6)) {  
            return null;  
        }  
  
        // look for map  
        InetAddress clientIp = this.macIpMap.get(request.getChaddrAsHex()  
                .toLowerCase());  
        if (clientIp == null) {  
            return null; // not found  
        }  
  
        return clientIp;  
    }  
  
    /** 
     * Server launcher. 
     *  
     * @param args 
     *            command line arguments - ignored 
     */  
    public static void main(String[] args) {  
        try {  
            DHCPCoreServer server = DHCPCoreServer.initServer(  
                    new DHCPStaticServlet(), null);  
            logger.setLevel(Level.ALL);  
            new Thread(server).start();  
        } catch (DHCPServerInitException e) {  
            logger.log(Level.SEVERE, "Server init", e);  
        }  
    }  
}  