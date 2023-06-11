package org.tbl.microdaddy.util.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
@Slf4j
public class ServiceUtil {

    private final String port;
    private String serviceAddress = null;

    @Autowired
    public ServiceUtil(@Value("${server.port}") String port) {
        this.port = port;
    }

    // TODO/ Maybe use optionals? I hate null checks.
    public String getServiceAddress() {
        if (serviceAddress == null) {
            serviceAddress = findMyHostName() + "/" + findMyIpAddress() + ":" + port;
        }

        return serviceAddress;
    }

    private String findMyHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unable to determine hostname.";
        }
    }

    private String findMyIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "Unable to determine IP Address.";
        }
    }


}
