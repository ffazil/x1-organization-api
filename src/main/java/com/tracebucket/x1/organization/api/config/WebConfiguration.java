package com.tracebucket.x1.organization.api.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Created by Vishwajit on 16-04-2015.
 */
@Configuration
@ComponentScan(basePackages = {"com.tracebucket.x1.organization.api.rest"})
public class WebConfiguration {
}