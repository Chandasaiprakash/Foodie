package com.foodie.discovery_service;

import com.netflix.discovery.EurekaClient;
import com.netflix.appinfo.InstanceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class EurekaServiceController {

    @Autowired
    private EurekaClient eurekaClient;

    @GetMapping("/eureka/services")
    public List<String> getEurekaServices() {
        return new ArrayList<>(eurekaClient.getApplications().getRegisteredApplications()
                .stream()
                .map(app -> app.getName().toLowerCase())
                .collect(Collectors.toList()));
    }

    @GetMapping("/eureka/services/{serviceName}")
    public InstanceInfo getServiceInstance(@PathVariable String serviceName) {
        return eurekaClient.getNextServerFromEureka(serviceName.toUpperCase(), false);
    }
}