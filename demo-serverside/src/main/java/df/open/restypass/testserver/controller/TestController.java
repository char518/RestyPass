package df.open.restypass.testserver.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by darrenfu on 17-7-29.
 */
@RestController
@RequestMapping("/resty")
public class TestController {
    @RequestMapping(value = "/get_nothing", method = RequestMethod.GET)
    public void nothing() {
//        System.out.println("############nothing");
    }

    @RequestMapping(value = "/get_status", method = RequestMethod.GET)
    public String getStatus() {
        return "Status is OK";
    }

}
