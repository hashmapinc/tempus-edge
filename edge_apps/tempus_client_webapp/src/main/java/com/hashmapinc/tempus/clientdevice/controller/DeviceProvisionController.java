package com.hashmapinc.tempus.clientdevice.controller;

import com.hashmapinc.tempus.clientdevice.ApplicationConstants;
import com.hashmapinc.tempus.clientdevice.context.ApplicationContext;
import com.hashmapinc.tempus.clientdevice.edgeconfig.mqtt.MqttManager;
import com.hashmapinc.tempus.clientdevice.services.ApplicationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class DeviceProvisionController {

    ApplicationService applicationService = new ApplicationService();
   @GetMapping("/")
   public String index( Model model) {
       String deviceId=applicationService.getDeviceId();
       if(deviceId==null)
      return "index";
       else
       {
           model.addAttribute("name", deviceId);
           model.addAttribute("isConnected", ApplicationContext.getContext().getCommunicationManager().getStatus());
           return "hello";
       }

   }

    @PostMapping("/reconfigure")
    public String reconfigure( Model model) {
        String deviceId=applicationService.getDeviceId();
        if(deviceId==null)
            return "index";
        else
        {
            Map<String,String> context=applicationService.getContext();
            model.addAttribute("name", context.get(ApplicationConstants.DEVICE_ID));
            model.addAttribute("serverIP", context.get(ApplicationConstants.SERVER_IP));
            model.addAttribute("port", context.get(ApplicationConstants.SERVER_PORT));
            return "reconfigure";
        }

    }

   @PostMapping("/hello")
   public String sayHello(@RequestParam("name") String name,@RequestParam("serverIP") String serverIP,@RequestParam("password") String password,@RequestParam("port") String port, Model model) {
       applicationService.configureDevice(name,password,serverIP,port);
       model.addAttribute("name", name);
       model.addAttribute("isConnected", ApplicationContext.getContext().getCommunicationManager().getStatus());
      return "hello";
   }

    @GetMapping("/hello")
    public String getDeviceInfo(@RequestParam("name") String name,@RequestParam("serverIP") String serverIP,@RequestParam("password") String password,@RequestParam("port") String port, Model model) {
        String deviceId=applicationService.getDeviceId();
        if(deviceId==null)
            return "index";
        else
        {
            model.addAttribute("name", deviceId);
            model.addAttribute("isConnected", ApplicationContext.getContext().getCommunicationManager().getStatus());
            return "hello";
        }

    }


}
